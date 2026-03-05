package com.zhushuai.zspicturebackend.service;

import com.zhushuai.zspicturebackend.model.dto.space.SpaceAddReq;
import com.zhushuai.zspicturebackend.model.dto.space.SpaceEditReq;
import com.zhushuai.zspicturebackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.model.vo.SpaceVO;

/**
 * @author zhushuai
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2026-03-05 17:37:20
 */
public interface SpaceService extends IService<Space> {


    /**
     * 创建空间
     *
     * @param spaceAddReq
     * @return
     */
    SpaceVO spaceAdd(SpaceAddReq spaceAddReq, User user);


    /**
     * 编辑空间
     *
     * @param spaceEditReq
     * @param user
     * @return
     */
    SpaceVO spaceEdit(SpaceEditReq spaceEditReq, User user);

}
