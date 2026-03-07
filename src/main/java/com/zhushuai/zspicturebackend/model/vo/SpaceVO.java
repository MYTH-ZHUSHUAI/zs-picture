package com.zhushuai.zspicturebackend.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.zhushuai.zspicturebackend.model.entity.Space;
import com.zhushuai.zspicturebackend.model.entity.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Data
public class SpaceVO {

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 描述
     */
    private String spaceDescription;


    /**
     * 当前空间下图片的总大小
     */
    private Long totalSize;

    /**
     * 当前空间下的图片数量
     */
    private Long totalCount;

    /**
     * 创建时间
     */
    private Date createTime;


    /**
     * 用户信息
     */
    private Long userId;

    /**
     * 空间类型：0-私有 1-团队
     */
    private Integer spaceType;


    public static SpaceVO objToVO(Space space) {
        if (space == null) {
            return null;
        }

        SpaceVO spaceVO = new SpaceVO();
        BeanUtils.copyProperties(space, spaceVO);

        return spaceVO;
    }
}
