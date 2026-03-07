package com.zhushuai.zspicturebackend.manager.model;

import lombok.Data;

@Data
public class UploadContext {

    private String originalKey;
    private String webpKey;
    private String thumbnailKey;

    private Integer width;
    private Integer height;
    private Long size;

    private String mainColor;

    private String watermarkedWebpKey;
    private String watermarkedThumbnailKey;
}