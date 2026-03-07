package com.zhushuai.zspicturebackend.model.dto.spaceuser;


import lombok.Data;

@Data
public class SpaceUserDeleteReq {

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 用户 id
     */
    private Long userId;

}
