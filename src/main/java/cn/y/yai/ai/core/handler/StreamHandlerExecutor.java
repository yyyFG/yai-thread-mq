package cn.y.yai.ai.core.handler;


import cn.y.yai.ai.model.enums.CodeGenTypeEnum;
import cn.y.yai.model.entity.User;
import cn.y.yai.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 流处理执行器
 * 根据代码生成类型创建合适的流处理器
 * 1. 传统的 Flux<String>流（HTML、MULTI_FILE）-> SimpleTextStreamHandler
 * 2. TokenStream 格式的复杂流（Vue_Project）-> JsonMessageStreamHandler
 */
@Slf4j
@Component
public class StreamHandlerExecutor {

    @Resource
    private JsonMessageStreamHandler jsonMessageStreamHandler;

    /**
     * 创建流处理器并处理聊天历史记录
     * @param originFlux
     * @param chatHistoryService
     * @param appId
     * @param loginUser
     * @param codeGenType
     * @return
     */
    public Flux<String> doExecute(Flux<String> originFlux,
                                  ChatHistoryService chatHistoryService,
                                  long appId, User loginUser, CodeGenTypeEnum codeGenType) {
        return switch (codeGenType) {
            case VUE_PROJECT -> //使用注入的组件实例
                jsonMessageStreamHandler.handle(originFlux, chatHistoryService, appId, loginUser);
            case HTML, MULTI_FILE -> // 简单文本处理器不需要依赖注入
                new SimpleTextStreamHandler().handle(originFlux, chatHistoryService, appId, loginUser);
        };
    }
}
