package com.zhushuai.zspicturebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhushuai.zspicturebackend.exception.ErrorCode;
import com.zhushuai.zspicturebackend.exception.ThrowUtils;
import com.zhushuai.zspicturebackend.model.dto.spaceuser.SpaceUserAddReq;
import com.zhushuai.zspicturebackend.model.entity.SpaceUser;
import com.zhushuai.zspicturebackend.model.enums.SpaceUserEnum;
import com.zhushuai.zspicturebackend.model.vo.SpaceUserVO;
import com.zhushuai.zspicturebackend.service.SpaceUserService;
import com.zhushuai.zspicturebackend.mapper.SpaceUserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
* @author zhushuai
* @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
* @createDate 2026-03-07 19:54:53
*/
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
    implements SpaceUserService{


    /**
     * 为空间增加用户
     *
     * @param spaceUserAddReq
     * @return
     */
    @Override
    public SpaceUserVO addSpaceUser(SpaceUserAddReq spaceUserAddReq) {

        int spaceRole = spaceUserAddReq.getSpaceRole();
        SpaceUserEnum spaceUserEnum = SpaceUserEnum.getEnumByValue(spaceRole);
        ThrowUtils.throwIf(spaceUserEnum == null, ErrorCode.PARAMS_ERROR,"请求参数错误");

        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserAddReq, spaceUser);
        spaceUser.setSpaceRole(spaceUserEnum.getRole());

        boolean saved = this.save(spaceUser);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "空间用户添加失败");

        SpaceUser spaceUserById = this.getById(spaceUser.getId());
        return SpaceUserVO.objToVO(spaceUserById);
    }
}




