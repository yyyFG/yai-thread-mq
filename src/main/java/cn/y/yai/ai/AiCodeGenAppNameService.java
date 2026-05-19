package cn.y.yai.ai;

import dev.langchain4j.service.SystemMessage;

/**
 * AI 代码生成名称智能服务
 */
public interface AiCodeGenAppNameService {

    /**
     * 根据用户需求生成应用名称
     * @param userPrompt
     * @return
     */
    @SystemMessage(fromResource = "prompt/app-name-system-prompt.txt")
    String appNameGen(String userPrompt);
}
