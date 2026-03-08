package com.zhushuai.zspicturebackend.model.enums;

import lombok.Getter;

/**
 * @author zhushuai
 */

@Getter
public enum SpaceUserTypeEnum {
    /**
     * 空间角色：viewer/editor/admin
     */
    VIEWER("viewer", 0),
    EDITOR("editor", 1),
    ADMIN("admin", 2);

    private final String role;
    private final int value;


    SpaceUserTypeEnum(String role, int value) {
        this.role = role;
        this.value = value;
    }


    public static SpaceUserTypeEnum getEnumByValue(int value) {
        for (SpaceUserTypeEnum spaceUserTypeEnum : values()) {
            if (spaceUserTypeEnum.value == value) {
                return spaceUserTypeEnum;
            }
        }
        return null;
    }

    public static SpaceUserTypeEnum getEnumByRole(String role) {
        for (SpaceUserTypeEnum spaceUserTypeEnum : values()) {
            if (spaceUserTypeEnum.role.equals(role)) {
                return spaceUserTypeEnum;
            }
        }
        return null;
    }
}
