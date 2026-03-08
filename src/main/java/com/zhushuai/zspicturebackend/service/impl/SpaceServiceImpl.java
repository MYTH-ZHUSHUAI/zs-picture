package com.zhushuai.zspicturebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhushuai.zspicturebackend.constant.SpaceConstant;
import com.zhushuai.zspicturebackend.exception.ErrorCode;
import com.zhushuai.zspicturebackend.exception.ThrowUtils;
import com.zhushuai.zspicturebackend.model.dto.space.SpaceAddReq;
import com.zhushuai.zspicturebackend.model.dto.space.SpaceEditReq;
import com.zhushuai.zspicturebackend.model.dto.space.SpaceQueryReq;
import com.zhushuai.zspicturebackend.model.dto.spaceuser.SpaceUserAddReq;
import com.zhushuai.zspicturebackend.model.entity.Space;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.model.enums.SpaceParametersEnum;
import com.zhushuai.zspicturebackend.model.enums.SpaceUserTypeEnum;
import com.zhushuai.zspicturebackend.model.vo.SpaceUserVO;
import com.zhushuai.zspicturebackend.model.vo.SpaceVO;
import com.zhushuai.zspicturebackend.service.SpaceService;
import com.zhushuai.zspicturebackend.mapper.SpaceMapper;
import com.zhushuai.zspicturebackend.service.SpaceUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhushuai
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2026-03-05 17:37:20
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {


    @Resource
    private SpaceUserService spaceUserService;


    /**
     * 创建空间
     *
     * @param spaceAddReq
     * @param user
     * @return
     */
    @Override
    @Transactional
    public SpaceVO spaceAdd(SpaceAddReq spaceAddReq, User user) {

        ThrowUtils.throwIf(spaceAddReq == null, ErrorCode.PARAMS_ERROR);


        // 配置锁，锁住当前用户，空间个数不得大于10个
        String lock = String.valueOf(user.getId()).intern();
        synchronized (lock) {
            // 得到用户的空间个数
            Long count = this.lambdaQuery().eq(Space::getUserId, user.getId()).count();
            ThrowUtils.throwIf(count > SpaceConstant.MAX_SPACE_COUNT,
                    ErrorCode.OPERATION_ERROR,
                    "用户空间不得大于 10 个");

            // 创建空间
            // 得到用户空间类型枚举类
            SpaceParametersEnum spaceParametersEnum = SpaceParametersEnum.getEnumByLevel(spaceAddReq.getSpaceLevel());
            ThrowUtils.throwIf(spaceParametersEnum == null, ErrorCode.PARAMS_ERROR);

            Space space = new Space();
            BeanUtils.copyProperties(spaceAddReq, space);
            BeanUtils.copyProperties(spaceParametersEnum, space);


            space.setSpaceType(spaceAddReq.getSpaceType());
            space.setUserId(user.getId());
            boolean saved = this.save(space);
            ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "空间添加失败");


            // 添加空间用户
            SpaceUserAddReq spaceUserAddReq = new SpaceUserAddReq();
            spaceUserAddReq.setSpaceId(space.getId());
            spaceUserAddReq.setUserId(user.getId());
            spaceUserAddReq.setSpaceRole(SpaceUserTypeEnum.ADMIN.getValue());
            SpaceUserVO spaceUserVO = spaceUserService.addSpaceUser(spaceUserAddReq);


            return SpaceVO.objToVO(this.getById(space.getId()));
        }
    }


    /**
     * 编辑空间
     *
     * @param spaceEditReq
     * @param user
     * @return
     */
    @Override
    public SpaceVO spaceEdit(SpaceEditReq spaceEditReq, User user) {


        Long id = spaceEditReq.getId();
        String spaceName = spaceEditReq.getSpaceName();
        String spaceDescription = spaceEditReq.getSpaceDescription();

        Space spaceById = this.getById(id);

        spaceById.setSpaceName(spaceName);
        spaceById.setSpaceDescription(spaceDescription);


        boolean updated = this.updateById(spaceById);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "空间更新失败");


        return SpaceVO.objToVO(this.getById(id));
    }


    /**
     * 分页查询SpaceVO
     *
     * @param spaceQueryReq
     * @param user
     * @return
     */
    @Override
    public Page<SpaceVO> getSpaceVOList(SpaceQueryReq spaceQueryReq, User user) {

        // 得到space分页数据
        Page<Space> spacePage = this.getSpaceList(spaceQueryReq, user);

        // 得到spaceVO分页数据
        List<SpaceVO> spaceVORecords = spacePage.getRecords()
                .stream()
                .map(SpaceVO::objToVO)
                .collect(Collectors.toList());

        // 新建一个空的spaceVOPage
        Page<SpaceVO> spaceVOPage = new Page<>(
                spacePage.getCurrent(),
                spacePage.getSize(),
                spacePage.getTotal()
        );

        spaceVOPage.setRecords(spaceVORecords);
        return spaceVOPage;
    }


    /**
     * 分页查询Space
     *
     * @param spaceQueryReq
     * @param user
     * @return
     */
    @Override
    public Page<Space> getSpaceList(SpaceQueryReq spaceQueryReq, User user) {

        // 获取字段
        long currentPage = spaceQueryReq.getCurrentPage();
        long pageSize = spaceQueryReq.getPageSize();

        // 创建查询条件
        Page<Space> page = new Page<>(currentPage, pageSize);

        QueryWrapper<Space> spaceQueryWrapper = new QueryWrapper<>();
        spaceQueryWrapper.eq("userId", spaceQueryReq.getUserId());
        spaceQueryWrapper.orderByDesc("createTime");

        return this.page(page, spaceQueryWrapper);
    }


    /**
     * 删除空间
     *
     * @param spaceId
     * @return
     */
    @Override
    @Transactional
    public int deleteSpace(Long spaceId) {

        ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR);

        int deleted = baseMapper.deleteById(spaceId);

        if (deleted > 0) {
            int deleteSpaceUserBySpaceId = spaceUserService.deleteSpaceUserBySpaceId(spaceId);
        }
        ThrowUtils.throwIf(deleted <= 0, ErrorCode.OPERATION_ERROR, "未找到该空间");

        return deleted;
    }
}




