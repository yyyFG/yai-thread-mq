package cn.y.yai.ai.parser;

import cn.y.yai.ai.model.enums.CodeGenTypeEnum;
import cn.y.yai.exception.BusinessException;
import cn.y.yai.exception.ErrorCode;

/**
 * 代码解析执行器
 * 根据代码生成类型执行相应的解析逻辑
 */
public class CodeParserExecutor {

    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();

    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();


    /**
     * 执行代码解析
     * @param content
     * @param codeGenTypeEnum
     * @return
     */
    public static Object executeParser(String content, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML -> htmlCodeParser.parserCode(content);
            case MULTI_FILE -> multiFileCodeParser.parserCode(content);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型" + codeGenTypeEnum);
        };
    }
}
