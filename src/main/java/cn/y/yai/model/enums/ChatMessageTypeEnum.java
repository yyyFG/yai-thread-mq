package cn.y.yai.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 聊天消息类型枚举
 */
@Getter
public enum ChatMessageTypeEnum {

    USER("用户", "user"),
    AI("AI", "ai"),
    ERROR("错误", "error");

    private final String text;
    private final String value;

    private static final Map<String, ChatMessageTypeEnum> VALUE_MAP = new HashMap<>();

    static {
        for (ChatMessageTypeEnum anEnum : ChatMessageTypeEnum.values()) {
            VALUE_MAP.put(anEnum.value, anEnum);
        }
    }

    ChatMessageTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static ChatMessageTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        return VALUE_MAP.get(value);
    }
}
