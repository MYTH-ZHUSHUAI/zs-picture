package com.zhushuai.zspicturebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhushuai.zspicturebackend.model.dto.space.SpaceAddReq;
import com.zhushuai.zspicturebackend.model.dto.space.SpaceEditReq;
import com.zhushuai.zspicturebackend.model.dto.space.SpaceQueryReq;
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


    /**
     * 分页查询空间包装类（普通用户）
     *
     */
    Page<SpaceVO> getSpaceVOList(SpaceQueryReq spaceQueryReq, User user);


    /**
     * 分页查询空间类（管理员）
     *
     */
    Page<Space> getSpaceList(SpaceQueryReq spaceQueryReq, User user);


    /**
     * 删除空间
     * @param spaceId
     * @return
     */
    int deleteSpace(Long spaceId);

}
