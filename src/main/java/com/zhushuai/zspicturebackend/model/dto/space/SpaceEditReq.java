package com.zhushuai.zspicturebackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;


/**
 * 编辑空间请求，根据id操作
 *
 */
@Data
public class SpaceEditReq implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间描述
     */
    private String spaceDescription;

    /**
     * 空间类型：0-私有 1-团队
     */
    private Integer spaceType;

}


