package com.zhushuai.zspicturebackend.model.dto.picture;


import lombok.Data;

@Data
public class PictureUploadToUserSpaceReq {

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
     * 标签
     */
    private String pictureTags;

    /**
     * 用户空间id
     */
    private Long spaceId;

    /**
     * 是否公开
     */
    private int isOpen;

}
