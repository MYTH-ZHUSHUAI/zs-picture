package com.zhushuai.zspicturebackend.model.dto.picture;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Data
public class PictureUploadReq {

    /**
     * 图片名称
     */
    private String pictureName;

    /**
     * 简介
     */
    private String pictureIntroduction;

    /**
     * 分类
     */
    private String pictureCategory;

    /**
     * 标签（JSON 数组）
     */
    private String pictureTags;
}
