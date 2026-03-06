package com.zhushuai.zspicturebackend.model.enums;


import lombok.AllArgsConstructor;

import lombok.Getter;

/**
 * 用户空间参数枚举
 */
@Getter
@AllArgsConstructor
public enum SpaceParametersEnum {

    
    COMMON(),
    NORMAL(0, "普通版", 1024 * 1024 * 1024 * 10L, 1000L),
    PROFESSIONAL(1, "专业版", 1024 * 1024 * 1024 * 50L, 5000L),
    FLAGSHIP(2, "旗舰版", 1024 * 1024 * 1024 * 100L, 10000L);

    private final Integer level;
    private final String name;
    private final Long maxSize;
    private final Long maxCount;


    public static SpaceParametersEnum getEnumByLevel(Integer level) {
        for (SpaceParametersEnum value : values()) {
            if (value.level.equals(level)) {
                return value;
            }
        }
        return null;
    }
}
