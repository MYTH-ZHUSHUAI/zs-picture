package com.zhushuai.zspicturebackend.model.dto.picture;

import com.zhushuai.zspicturebackend.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class PictureQueryReq extends PageRequest implements Serializable {

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

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 搜索文本
     */
    private String searchText;

}
