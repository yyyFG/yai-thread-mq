package cn.y.yai.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum UserRoleEnum {

    USER("用户", "user"),
    ADMIN("管理员", "admin");

    private final String text;
    private final String value;

    // 静态 Map 缓存 value -> 枚举实例
    private static final Map<String, UserRoleEnum> VALUE_MAP = new HashMap<>();

    static {
        // 类加载时初始化缓存
        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
            VALUE_MAP.put(anEnum.value, anEnum);
        }
    }

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举（使用 Map 加速）
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
//        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
//            if (anEnum.value.equals(value)) {
//                return anEnum;
//            }
//        }
        return VALUE_MAP.get(value);
    }
}

