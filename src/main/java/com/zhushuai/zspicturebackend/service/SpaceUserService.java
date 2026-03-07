package com.zhushuai.zspicturebackend.service;

import com.zhushuai.zspicturebackend.model.dto.spaceuser.SpaceUserAddReq;
import com.zhushuai.zspicturebackend.model.dto.spaceuser.SpaceUserDeleteReq;
import com.zhushuai.zspicturebackend.model.dto.spaceuser.SpaceUserEditReq;
import com.zhushuai.zspicturebackend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhushuai.zspicturebackend.model.vo.SpaceUserVO;
import com.zhushuai.zspicturebackend.model.vo.UserVO;

import java.util.List;

/**
 * @author zhushuai
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service
 * @createDate 2026-03-07 19:54:53
 */
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 为空间添加用户
     *
     * @param spaceUserAddReq
     * @return
     */
    SpaceUserVO addSpaceUser(SpaceUserAddReq spaceUserAddReq);


    /**
     * 获取空间用户列表
     *
     * @param spaceId
     * @return
     */
    List<UserVO> getSpaceUserList(Long spaceId);


    /**
     * 删除空间用户
     *
     */
    Boolean deleteSpaceUser(SpaceUserDeleteReq spaceUserDeleteReq);


    /**
     * 为空间添加用户
     *
     * @param spaceUserAddReq
     * @return
     */
    SpaceUserVO editSpaceUser(SpaceUserEditReq spaceUserEditReq);

}
