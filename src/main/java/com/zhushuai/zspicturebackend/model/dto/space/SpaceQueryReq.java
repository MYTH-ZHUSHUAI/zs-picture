package com.zhushuai.zspicturebackend.model.dto.space;


import com.zhushuai.zspicturebackend.common.PageRequest;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class SpaceQueryReq extends PageRequest implements Serializable {
    /**
     * 创建用户 id
     */
    private Long userId;

}
