package com.zhushuai.zspicturebackend.model.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRoleEnum {

    USER("普通用户", "user"),
    ADMIN("管理员", "admin");

    private final String role;
    private final String value;

    /**
     * 静态缓存 Map
     * key: value
     * value: 枚举
     */
    private static final Map<String, UserRoleEnum> VALUE_ENUM_MAP = new HashMap<>();

    // 静态代码块初始化
    static {
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            VALUE_ENUM_MAP.put(userRoleEnum.value, userRoleEnum);
        }
    }

    UserRoleEnum(String role, String value) {
        this.role = role;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return VALUE_ENUM_MAP.get(value);
    }
}