package com.zhushuai.zspicturebackend.manager.template;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 上传结果
 */
@Data
@AllArgsConstructor
public class UploadResult {
    private String originalKey;      // 原图 key
    private String webpKey;          // WebP 格式原图 key
    private String thumbnailKey;     // 缩略图 key
    private Integer width;
    private Integer height;
    private Long size;
    private String mainColor;        // 主色调
    private String watermarkedWebpKey;        // 带水印原图 key
    private String watermarkedThumbnailKey;   // 带水印缩略图 key
    private Double scale;            // 图片宽高比
}
