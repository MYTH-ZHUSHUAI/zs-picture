package com.zhushuai.zspicturebackend.model.dto.spaceuser;


import lombok.Data;

@Data
public class SpaceUserAddReq {

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private int spaceRole;
}
