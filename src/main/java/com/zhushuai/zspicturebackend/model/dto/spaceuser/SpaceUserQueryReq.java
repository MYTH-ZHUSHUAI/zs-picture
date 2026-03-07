package com.zhushuai.zspicturebackend.model.dto.spaceuser;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class SpaceUserQueryReq {

    /**
     * 空间 id
     */
    private Long spaceId;
}
