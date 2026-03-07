package com.zhushuai.zspicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhushuai.zspicturebackend.exception.BusinessException;
import com.zhushuai.zspicturebackend.exception.ErrorCode;
import com.zhushuai.zspicturebackend.exception.ThrowUtils;
import com.zhushuai.zspicturebackend.manager.ImageUploadManager;
import com.zhushuai.zspicturebackend.model.dto.picture.*;
import com.zhushuai.zspicturebackend.model.entity.Picture;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.model.vo.PictureVO;
import com.zhushuai.zspicturebackend.model.vo.UploadPictureResultVO;
import com.zhushuai.zspicturebackend.service.PictureService;
import com.zhushuai.zspicturebackend.mapper.PictureMapper;
import com.zhushuai.zspicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import cn.hutool.crypto.digest.DigestUtil;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhushuai
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2026-03-01 08:36:06
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    @Resource
    private UserService userService;

    @Resource
    private ImageUploadManager imageUploadManger;


    /**
     * 上传图片到公共空间中
     *
     * @param pictureUploadReq 图片请求
     * @param user             用户
     * @return
     */
    @Override
    public PictureVO uploadPicture(MultipartFile file,
                                   PictureUploadReq pictureUploadReq,
                                   User user) {

        ThrowUtils.throwIf(file == null || file.isEmpty() || pictureUploadReq == null,
                ErrorCode.PARAMS_ERROR,
                "请求参数错误");

        try {
            // ===== 1️⃣ 计算 MD5 =====
            String md5 = DigestUtil.md5Hex(file.getBytes());

            // ===== 2️⃣ 查询是否已存在 =====
            Picture existPicture = this.lambdaQuery().eq(Picture::getMd5, md5).one();

            if (existPicture != null) {
                // 秒传成功，直接返回
                return PictureVO.objToVo(existPicture, userService.getUserVO(user));
            }

            // ===== 3️⃣ 不存在才真正上传 =====
            UploadPictureResultVO uploadResult = imageUploadManger.uploadPicture(file);

            // ===== 4️⃣ 构造 Picture =====
            Picture picture = new Picture();
            BeanUtils.copyProperties(uploadResult, picture);

            picture.setMd5(md5);
            picture.setName(pictureUploadReq.getPictureName());
            picture.setIntroduction(pictureUploadReq.getPictureIntroduction());
            picture.setCategory(pictureUploadReq.getPictureCategory());
            picture.setTags(JSONUtil.toJsonStr(pictureUploadReq.getPictureTags()));
            picture.setUserId(user.getId());

            boolean saved = this.save(picture);
            ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "图片上传失败");

            log.info("图片上传到公共空间中");
            return PictureVO.objToVo(this.getById(picture.getId()), userService.getUserVO(user));

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片上传失败");
        }
    }

    /**
     * 用户更新图片信息
     *
     * @param pictureUpdateReq 请求封装类
     * @param user             用户
     * @return
     */
    @Override
    public PictureVO updatePicture(PictureUpdateReq pictureUpdateReq, User user) {
        ThrowUtils.throwIf(pictureUpdateReq == null, ErrorCode.PARAMS_ERROR, "请求参数错误");


        // 创建更新实体类
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateReq, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateReq.getTags()));

        // 更新操作
        boolean updated = this.updateById(picture);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "图片更新失败");

        // 将图片重新查出来
        picture = this.getById(picture.getId());

        return PictureVO.objToVo(picture, userService.getUserVO(user));
    }

    /**
     * 图片搜索
     * TODO 图片搜索
     *
     * @param pictureQueryReq
     * @param user
     * @return
     */
    @Override
    public PictureVO queryPicture(PictureQueryReq pictureQueryReq, User user) {


//        if (userService.isAdmin(user)) {
//            return PictureVO.objToVo(this.getById(id));
//        }


        return null;
    }


    /**
     * 图片搜索QueryWrapper
     *
     * @param pictureQueryReq
     * @return
     */
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryReq pictureQueryReq) {
        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();

        if (pictureQueryReq == null) {
            return pictureQueryWrapper;
        }

        Long id = pictureQueryReq.getId();
        String name = pictureQueryReq.getName();
        String introduction = pictureQueryReq.getIntroduction();
        String category = pictureQueryReq.getCategory();
        List<String> tags = pictureQueryReq.getTags();
        String picFormat = pictureQueryReq.getPicFormat();
        Long userId = pictureQueryReq.getUserId();
        Date createTime = pictureQueryReq.getCreateTime();
        String sortField = pictureQueryReq.getSortField();
        String sortOrder = pictureQueryReq.getSortOrder();
        String searchText = pictureQueryReq.getSearchText();


        pictureQueryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        pictureQueryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);

        // 在两个字段中查
        pictureQueryWrapper.and(
                StrUtil.isNotBlank(searchText),
                wrapper -> wrapper.like("name", searchText)
                        .or()
                        .like("introduction", searchText)
        );

        pictureQueryWrapper.eq(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        pictureQueryWrapper.ge(createTime != null, "createTime", createTime);

        // 查tag
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                pictureQueryWrapper.like("tags", "\"" + tag + "\"");
            }
        }

        return pictureQueryWrapper;
    }

    /**
     * 获取图片列表
     * 管理员会获得所有图片
     *
     * @param pictureListReq 图片列表请求封装类
     * @return
     */
    @Override
    public Page<PictureVO> getPictureVOPage(PictureListReq pictureListReq) {

        // 获得用户id
        Long userId = pictureListReq.getUserId();

        User user = userService.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        // 构造分页参数
        Page<Picture> page = new Page<>(pictureListReq.getCurrentPage(), pictureListReq.getPageSize());

        // 构造查询条件
        LambdaQueryWrapper<Picture> wrapper = new LambdaQueryWrapper<>();

        // 默认只查未删除
        wrapper.eq(Picture::getIsDelete, 0);

        // 权限控制
        if (!userService.isAdmin(user)) {
            // 非管理员 → 只能查自己的
            wrapper.eq(Picture::getUserId, userId);
        }
        // 管理员 → 什么都不加，默认查全部

        // 排序（推荐加上）
        wrapper.orderByDesc(Picture::getCreateTime);

        // 执行分页查询
        Page<Picture> picturePage = this.page(page, wrapper);

        // 转换成 VO
        Page<PictureVO> pictureVOPage = new Page<>(
                picturePage.getCurrent(),
                picturePage.getSize(),
                picturePage.getTotal()
        );

        List<PictureVO> pictureVOList = picturePage.getRecords()
                .stream()
                .map((picture -> PictureVO.objToVo(picture, userService.getUserVO(user))))
                .collect(Collectors.toList());

        pictureVOPage.setRecords(pictureVOList);

        return pictureVOPage;
    }


    /**
     * 通过url上传图片
     *
     * @param pictureUrlUploadReq 上传请求
     * @param user                用户
     * @return
     */
    @Override
    public PictureVO uploadPictureByUrl(PictureUrlUploadReq pictureUrlUploadReq, User user) {

        // 文件的url
        String url = pictureUrlUploadReq.getUrl();
        String pictureName = pictureUrlUploadReq.getPictureName();
        String pictureIntroduction = pictureUrlUploadReq.getPictureIntroduction();
        String pictureCategory = pictureUrlUploadReq.getPictureCategory();
        List<String> pictureTags = pictureUrlUploadReq.getPictureTags();

        return null;
    }

    /**
     * 通过id查询图片
     *
     * @param pictureQueryReq
     * @return
     */
    @Override
    public PictureVO queryPictureById(PictureQueryReq pictureQueryReq) {
        return null;
    }


    /**
     * 根据颜色查询图片
     * @param color
     * @return
     */
    @Override
    public Page<PictureVO> getPictureListByColor(String color) {
        return null;
    }
}






