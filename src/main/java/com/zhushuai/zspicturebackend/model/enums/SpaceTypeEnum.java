package com.zhushuai.zspicturebackend.model.enums;


import lombok.Getter;

@Getter
public enum SpaceTypeEnum {

    PRIVATE("私有空间", 0),
    PUBLIC("团队空间", 1);


    private final String typeName;
    private final int type;

    SpaceTypeEnum(String typeName, int type) {
        this.typeName = typeName;
        this.type = type;
    }


    /**
     * 根据type字段获取枚举类
     *
     * @param type
     * @return
     */
    public static SpaceTypeEnum getEnumByValue(int type) {
        for (SpaceTypeEnum value : values()) {
            if (value.type == type) {
                return value;
            }
        }
        return null;
    }
}
