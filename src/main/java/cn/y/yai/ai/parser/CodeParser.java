package cn.y.yai.ai.parser;


/**
 * 代码解析器策略接口
 */
public interface CodeParser<T> {

    /**
     * 解析代码内容
     * @param codeContent
     * @return
     */
    T parserCode(String codeContent);
}
