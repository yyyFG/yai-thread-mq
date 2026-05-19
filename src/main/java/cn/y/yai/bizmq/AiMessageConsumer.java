package cn.y.yai.bizmq;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.y.yai.ai.core.AiCodeGeneratorFacade;
import cn.y.yai.ai.core.handler.StreamHandlerExecutor;
import cn.y.yai.ai.model.enums.CodeGenTypeEnum;
import cn.y.yai.constant.AppConstant;
import cn.y.yai.exception.BusinessException;
import cn.y.yai.exception.ErrorCode;
import cn.y.yai.model.entity.App;
import cn.y.yai.model.entity.User;
import cn.y.yai.model.enums.ChatMessageTypeEnum;
import cn.y.yai.service.ChatHistoryService;
import cn.y.yai.service.UserService;
import cn.y.yai.service.impl.AppServiceImpl;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// 使用@Component注解标记该类为一个组件，让Spring框架能够扫描并将其纳入管理
@Component
// 使用@Slf4j注解生成日志记录器
@Slf4j
public class AiMessageConsumer {

    private final Map<Long, Sinks.Many<String>> appGenSinkMap = new ConcurrentHashMap<>();

    // 正在生成中的 appId 集合：用于保证同一个 appId 的生成任务只会启动一次（并发幂等控制）
    private final Set<Long> runningGenAppIdSet = ConcurrentHashMap.newKeySet();
    @Resource
    private AppServiceImpl appService;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;


    /**
     * 接收消息的方法
     *
     * @param message      接收到的消息内容，是一个字符串类型
     * @param channel      消息所在的通道，可以通过该通道与 RabbitMQ 进行交互，例如手动确认消息、拒绝消息等
     * @param deliveryTag  消息的投递标签，用于唯一标识一条消息
     */
    @SneakyThrows
    @RabbitListener(queues = {AiMqConstant.AI_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message = {}", message);
        if (StrUtil.isBlank(message)) {
            //
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
        }
        Long appId = null;
        String prompt = null;
        Long userId = null;
        try {
            if (message.trim().startsWith("{")) {
                JSONObject json = JSONUtil.parseObj(message);
                appId = json.getLong("appId");
                prompt = json.getStr("prompt");
                userId = json.getLong("userId");
            } else {
                appId = Long.parseLong(message);
            }
        } catch (Exception e) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息格式错误");
        }

        if (appId == null || appId <= 0) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "appId 无效");
        }

        App app = appService.getById(appId);
        if (app == null) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用为空");
        }

        if (userId == null) {
            userId = app.getUserId();
        }
        User loginUser = userService.getById(userId);

        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == null) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }

        Sinks.Many<String> sink = appGenSinkMap.computeIfAbsent(appId,
                id -> Sinks.many().multicast().directBestEffort());

        try {
            App runningApp = new App();
            runningApp.setId(appId);
            runningApp.setStatus(AppConstant.RUNNING_STATUS);
            runningApp.setExecMessage("执行中");
            boolean updated = appService.updateById(runningApp);
            if (!updated) {
                channel.basicNack(deliveryTag, false, false);
                handleAppUpdateError(appId, "更新应用状态执行失败");
                sink.tryEmitError(new BusinessException(ErrorCode.SYSTEM_ERROR, "更新应用状态执行失败"));
                return;
            }

            if (StrUtil.isBlank(prompt)) {
                prompt = message;
            }

            Flux<String> originFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(prompt, codeGenTypeEnum, appId);
            Flux<String> processedFlux = streamHandlerExecutor.doExecute(originFlux, chatHistoryService, appId, loginUser, codeGenTypeEnum);

            processedFlux
                    .doOnNext(chunk -> sink.tryEmitNext(chunk))
                    .doOnComplete(() -> {
                        App succeedApp = new App();
                        succeedApp.setId(app.getId());
                        succeedApp.setStatus(AppConstant.SUCCEED_STATUS);
                        succeedApp.setExecMessage("执行成功");
                        appService.updateById(succeedApp);
                        sink.tryEmitComplete();
                    })
                    .doOnError(e -> {
                        handleAppUpdateError(app.getId(), e.getMessage());
                        chatHistoryService.addChatMessage(app.getId(), "AI回复失败: " + e.getMessage(),
                                ChatMessageTypeEnum.ERROR.getValue(), loginUser.getId());
                        sink.tryEmitError(e);
                    })
                    .blockLast();

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        } finally {
            runningGenAppIdSet.remove(appId);
            appGenSinkMap.remove(appId);
        }
    }

    private void handleAppUpdateError(long appId, String execMessage) {
        App updateAppResult = new App();
        updateAppResult.setId(appId);
        updateAppResult.setStatus(AppConstant.FAILED_STATUS);
        updateAppResult.setExecMessage(execMessage);
        boolean result = appService.updateById(updateAppResult);
        if (!result) {
            log.error("更新应用状态失败" + appId + ", " + execMessage);
        }
    }

}

