package com.zhushuai.zspicturebackend.model.dto.space;


import lombok.Data;

import java.io.Serializable;


@Data
public class SpaceAddReq implements Serializable {

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间描述
     */
    private String spaceDescription;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

}
