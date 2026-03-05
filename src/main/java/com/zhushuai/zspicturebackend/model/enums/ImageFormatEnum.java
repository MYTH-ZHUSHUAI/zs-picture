package com.zhushuai.zspicturebackend.model.enums;

import lombok.Getter;

import java.util.Locale;

@Getter
public enum ImageFormatEnum {

    JPG("jpg"),
    JPEG("jpeg"),
    PNG("png"),
    WEBP("webp");

    private final String value;

    ImageFormatEnum(String value) {
        this.value = value;
    }

    public static boolean isValid(String format) {
        if (format == null) {
            return false;
        }
        String lowerFormat = format.toLowerCase(Locale.ROOT).replace(".", "");
        for (ImageFormatEnum e : values()) {
            if (e.value.equals(lowerFormat)) {
                return true;
            }
        }
        return false;
    }
}