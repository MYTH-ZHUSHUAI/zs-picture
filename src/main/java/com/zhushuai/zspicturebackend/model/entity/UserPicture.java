package com.zhushuai.zspicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

import lombok.Data;

/**
 * 图片
 *
 * @TableName user_picture
 */
@TableName(value = "user_picture")
@Data
public class UserPicture {
    /**
     * 用户图片关联 id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long userPictureId;

    /**
     * 图片 url
     */
    private String url;

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
     * 标签（JSON 数组）
     */
    private String tags;

    /**
     * 图片体积
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
     * 图片宽高比例
     */
    private Double picScale;

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
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 是否公开
     */
    private Integer isOpen;

    /**
     * 缩略图 url
     */
    private String thumbnailUrl;

    /**
     * 图片 MD5 值
     */
    private String md5;

    /**
     * 空间 id（为 0 表示公共空间）
     */
    private Long spaceId;

    /**
     * 图片主色调（RGB 十六进制格式，如 #FF5733）
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

    /**
     * 原图 URL（原始格式）
     */
    private String originalUrl;
}