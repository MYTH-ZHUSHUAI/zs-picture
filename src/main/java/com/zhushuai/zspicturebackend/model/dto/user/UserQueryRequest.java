package com.zhushuai.zspicturebackend.model.dto.user;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhushuai.zspicturebackend.common.PageRequest;
import lombok.Data;

/**
 * 用户查询请求
 *
 * @TableName user
 */
@TableName(value = "user")
@Data
public class UserQueryRequest extends PageRequest {
    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;


    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

}