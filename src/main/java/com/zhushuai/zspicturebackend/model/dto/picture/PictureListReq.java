package com.zhushuai.zspicturebackend.model.dto.picture;

import com.zhushuai.zspicturebackend.common.PageRequest;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class PictureListReq extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private Long userId;

}
