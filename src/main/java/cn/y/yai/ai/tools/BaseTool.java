package cn.y.yai.ai.tools;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * 工具基类
 * 定义所有工具的通用接口
 */
public abstract class BaseTool {

    /**
     * 获取工具的英文名称（对应方法名）
     * @return
     */
    public abstract String getToolName();

    /**
     * 获取工具的显示名称（对应中文名称）
     * @return
     */
    public abstract String getDisplayName();

    /**
     * 生成工具请求的返回值（显示给用户）
     * @return
     */
    public String generateToolRequestResponse() {
        return String.format("\n\n[选择工具] %s\n\n", getDisplayName());
    }

    /**
     * 生成工具执行结果格式（保存到数据库）
     * @param arguments 工具执行参数
     * @return
     */
    public abstract String generateToolExecutedResult(JSONObject arguments);
}


