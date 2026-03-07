package com.zhushuai.zspicturebackend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UploadPictureResultVO {

    /**
     * 图片 url
     */
    private String url;

    /**
     * 缩略图 url
     */
    private String thumbnailUrl;

    /**
     * 文件key
     */
    private String key;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 文件体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 图片主色调
     */
    private String mainColor;

    /**
     * 带水印的原图 URL
     */
    private String watermarkedUrl;

    /**
     * 带水印的缩略图 URL
     */
    private String watermarkedThumbnailUrl;
}