package com.zhushuai.zspicturebackend.model.dto.picture;

import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
public class PictureUpdateReq implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;
}
