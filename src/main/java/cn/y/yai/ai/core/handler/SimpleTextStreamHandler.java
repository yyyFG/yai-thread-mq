package cn.y.yai.ai.core.handler;

import cn.y.yai.exception.BusinessException;
import cn.y.yai.exception.ErrorCode;
import cn.y.yai.model.entity.User;
import cn.y.yai.model.enums.ChatMessageTypeEnum;
import cn.y.yai.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 *  简单文本流处理器
 *  处理 HTML 和 MULTI_FILE 类型的流式响应
 */
@Slf4j
public class SimpleTextStreamHandler {

    /**
     * 处理传统流（HTML, MULTI_FILE）
     * 直接收集完整的文本响应
     * @param originFlux
     * @param chatHistoryService
     * @param appId
     * @param loginUser
     * @return
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User loginUser) {
        StringBuilder aiResponseBuilder = new StringBuilder();
        return originFlux
                .map(chunk -> {
                    // 收集 AI 响应内容
                    aiResponseBuilder.append(chunk);
                    return chunk;
                })
                .doOnComplete(() -> {
                    // 流式响应完成后，添加 AI 消息到对话历史
                    String aiResponse = aiResponseBuilder.toString();
                    chatHistoryService.addChatMessage(appId, aiResponse, ChatMessageTypeEnum.AI.getValue(), loginUser.getId());
                })
                .doOnError(error -> {
                    // 如果 AI 回复失败，也要记录错误消息
                    String errorMessage = "AI回复失败" + error.getMessage();
                    chatHistoryService.addChatMessage(appId, errorMessage, ChatMessageTypeEnum.AI.getValue(), loginUser.getId());
                });
    }
}
