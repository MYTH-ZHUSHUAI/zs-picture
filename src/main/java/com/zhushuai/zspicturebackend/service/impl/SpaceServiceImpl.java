package com.zhushuai.zspicturebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhushuai.zspicturebackend.constant.SpaceConstant;
import com.zhushuai.zspicturebackend.exception.ErrorCode;
import com.zhushuai.zspicturebackend.exception.ThrowUtils;
import com.zhushuai.zspicturebackend.model.dto.space.SpaceAddReq;
import com.zhushuai.zspicturebackend.model.dto.space.SpaceEditReq;
import com.zhushuai.zspicturebackend.model.entity.Space;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.model.enums.SpaceParametersEnum;
import com.zhushuai.zspicturebackend.model.vo.SpaceVO;
import com.zhushuai.zspicturebackend.service.SpaceService;
import com.zhushuai.zspicturebackend.mapper.SpaceMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

/**
 * @author zhushuai
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2026-03-05 17:37:20
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private SpaceMapper spaceMapper;

    @Override
    @Transactional
    public SpaceVO spaceAdd(SpaceAddReq spaceAddReq, User user) {

        // TODO 并发问题
        ThrowUtils.throwIf(spaceAddReq == null, ErrorCode.PARAMS_ERROR);

        // 得到用户的空间统计信息
        Map<String, Object> stats = spaceMapper.getUserSpaceStats(user.getId());
        long totalCount = ((Number) stats.get("totalCount")).longValue();
        ThrowUtils.throwIf(totalCount >= SpaceConstant.MAX_SPACE_COUNT, ErrorCode.PARAMS_ERROR, "空间个数不能超过10个");
        long totalSize = ((Number) stats.get("totalSize")).longValue();
        long totalPictureCount = ((Number) stats.get("totalPictureCount")).longValue();


        // 得到用户空间类型枚举类
        SpaceParametersEnum spaceParametersEnum = SpaceParametersEnum.getEnumByLevel(spaceAddReq.getSpaceLevel());
        ThrowUtils.throwIf(spaceParametersEnum == null, ErrorCode.PARAMS_ERROR);

        // 进行空间不足判断
        ThrowUtils.throwIf(totalSize > spaceParametersEnum.getMaxSize() ||
                        totalPictureCount > spaceParametersEnum.getMaxCount(),
                ErrorCode.PARAMS_ERROR,
                "空间已满");


        // 下面进行空间创建
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddReq, space);

        BeanUtils.copyProperties(spaceParametersEnum, space);

        space.setUserId(user.getId());

        boolean saved = this.save(space);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "空间添加失败");


        return SpaceVO.objToVO(this.getById(space.getId()));

    }

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
}




