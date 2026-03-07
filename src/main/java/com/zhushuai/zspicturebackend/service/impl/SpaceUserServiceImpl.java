package com.zhushuai.zspicturebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhushuai.zspicturebackend.exception.ErrorCode;
import com.zhushuai.zspicturebackend.exception.ThrowUtils;
import com.zhushuai.zspicturebackend.model.dto.spaceuser.SpaceUserAddReq;
import com.zhushuai.zspicturebackend.model.dto.spaceuser.SpaceUserDeleteReq;
import com.zhushuai.zspicturebackend.model.dto.spaceuser.SpaceUserEditReq;
import com.zhushuai.zspicturebackend.model.entity.SpaceUser;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.model.enums.SpaceUserEnum;
import com.zhushuai.zspicturebackend.model.vo.SpaceUserVO;
import com.zhushuai.zspicturebackend.model.vo.UserVO;
import com.zhushuai.zspicturebackend.service.SpaceUserService;
import com.zhushuai.zspicturebackend.mapper.SpaceUserMapper;
import com.zhushuai.zspicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhushuai
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
 * @createDate 2026-03-07 19:54:53
 */
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
        implements SpaceUserService {

    @Resource
    private UserService userService;

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
        ThrowUtils.throwIf(spaceUserEnum == null, ErrorCode.PARAMS_ERROR, "请求参数错误");

        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserAddReq, spaceUser);
        spaceUser.setSpaceRole(spaceUserEnum.getRole());

        boolean saved = this.save(spaceUser);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "空间用户添加失败");

        SpaceUser spaceUserById = this.getById(spaceUser.getId());
        return SpaceUserVO.objToVO(spaceUserById);
    }

    /**
     * 获取空间用户列表
     *
     * @param spaceId
     * @return
     */
    @Override
    public List<UserVO> getSpaceUserList(Long spaceId) {
        ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR);

        LambdaQueryWrapper<SpaceUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SpaceUser::getSpaceId, spaceId);
        List<SpaceUser> spaceUsers = this.list(queryWrapper);

        List<Long> userIds = spaceUsers.stream()
                .map(SpaceUser::getUserId)
                .collect(Collectors.toList());

        return userService.getUserVOList(userService.listByIds(userIds));
    }


    /**
     * 删除空间用户
     *
     * @param spaceUserDeleteReq
     * @return
     */
    @Override
    public Boolean deleteSpaceUser(SpaceUserDeleteReq spaceUserDeleteReq) {
        ThrowUtils.throwIf(spaceUserDeleteReq == null, ErrorCode.PARAMS_ERROR);

        LambdaQueryWrapper<SpaceUser> wrapper = new LambdaQueryWrapper<SpaceUser>()
                .eq(SpaceUser::getSpaceId, spaceUserDeleteReq.getSpaceId())
                .eq(SpaceUser::getUserId, spaceUserDeleteReq.getUserId());


        boolean removed = this.remove(wrapper);

        ThrowUtils.throwIf(!removed, ErrorCode.OPERATION_ERROR, "空间用户删除失败");

        return true;
    }


    /**
     * 编辑空间用户
     *
     * @param spaceUserEditReq
     * @return
     */
    @Override
    public SpaceUserVO editSpaceUser(SpaceUserEditReq spaceUserEditReq) {

        Long spaceId = spaceUserEditReq.getSpaceId();
        Long userId = spaceUserEditReq.getUserId();
        int spaceRole = spaceUserEditReq.getSpaceRole();
        SpaceUserEnum spaceUserEnum = SpaceUserEnum.getEnumByValue(spaceRole);

        ThrowUtils.throwIf(spaceUserEnum == null, ErrorCode.PARAMS_ERROR, "请求参数错误");

        LambdaQueryWrapper<SpaceUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpaceUser::getSpaceId, spaceId)
                .eq(SpaceUser::getUserId, userId);

        SpaceUser spaceUser = this.getOne(wrapper);

        // 校验记录是否存在
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR, "空间用户不存在");

        // 更新角色
        spaceUser.setSpaceRole(spaceUserEnum.getRole());
        boolean updated = this.updateById(spaceUser);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新失败");

        return SpaceUserVO.objToVO(spaceUser);
    }


    /**
     * 根据空间 id 删除空间用户
     *
     * @param id 空间 ID
     * @return 是否删除成功
     */
    @Override
    public int deleteSpaceUserBySpaceId(Long id) {
        // 校验参数
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "空间 ID 无效");

        LambdaQueryWrapper<SpaceUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpaceUser::getSpaceId, id);

        // 删除并返回影响行数
        return baseMapper.delete(wrapper);
    }
}




