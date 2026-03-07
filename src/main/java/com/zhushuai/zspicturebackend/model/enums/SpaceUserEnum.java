package com.zhushuai.zspicturebackend.model.enums;

import lombok.Getter;

/**
 * @author zhushuai
 */

@Getter
public enum SpaceUserEnum {
    /**
     * 空间角色：viewer/editor/admin
     */
    VIEWER("viewer", 0),
    EDITOR("editor", 1),
    ADMIN("admin", 2);

    private final String role;
    private final int value;


    SpaceUserEnum(String role, int value) {
        this.role = role;
        this.value = value;
    }


    public static SpaceUserEnum getEnumByValue(int value) {
        for (SpaceUserEnum spaceUserEnum : values()) {
            if (spaceUserEnum.value == value) {
                return spaceUserEnum;
            }
        }
        return null;
    }
}
