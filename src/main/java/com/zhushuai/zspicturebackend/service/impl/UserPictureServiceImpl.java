package com.zhushuai.zspicturebackend.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhushuai.zspicturebackend.exception.BusinessException;
import com.zhushuai.zspicturebackend.exception.ErrorCode;
import com.zhushuai.zspicturebackend.exception.ThrowUtils;
import com.zhushuai.zspicturebackend.manager.ImageUploadManager;
import com.zhushuai.zspicturebackend.model.dto.picture.PictureUploadToUserSpaceReq;
import com.zhushuai.zspicturebackend.model.entity.Picture;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.model.entity.UserPicture;
import com.zhushuai.zspicturebackend.model.enums.SpaceParametersEnum;
import com.zhushuai.zspicturebackend.model.vo.PictureVO;
import com.zhushuai.zspicturebackend.model.vo.UploadPictureResultVO;
import com.zhushuai.zspicturebackend.service.PictureService;
import com.zhushuai.zspicturebackend.service.UserPictureService;
import com.zhushuai.zspicturebackend.mapper.UserPictureMapper;
import com.zhushuai.zspicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhushuai
 * @description 针对表【user_picture(图片)】的数据库操作 Service 实现
 * @createDate 2026-03-06 13:10:55
 */
@Service
@Slf4j
public class UserPictureServiceImpl extends ServiceImpl<UserPictureMapper, UserPicture>
        implements UserPictureService {

    @Resource
    private UserService userService;

    @Resource
    private ImageUploadManager imageUploadManger;

    @Resource
    private PictureService pictureService;

    /**
     * 上传图片到用户空间
     * 核心逻辑：
     * 1. 计算文件 MD5，实现秒传
     * 2. 如果公开（isOpen=1）：先上传到公共 Picture 表
     * 3. 无论是否公开，都要添加到 UserPicture 表（关联空间）
     *
     * @param file 文件
     * @param req 请求参数
     * @param user 当前用户
     * @return 图片信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PictureVO uploadPictureToUserSpace(MultipartFile file,
                                              PictureUploadToUserSpaceReq req,
                                              User user) {
        
        // ===== 1️⃣ 参数校验 =====
        ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        ThrowUtils.throwIf(req == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(req.getPictureName()), ErrorCode.PARAMS_ERROR, "图片名称不能为空");
        ThrowUtils.throwIf(req.getSpaceId() == null, ErrorCode.PARAMS_ERROR, "空间 ID 不能为空");
        
        int isOpen = req.getIsOpen();
        ThrowUtils.throwIf(isOpen != 0 && isOpen != 1, ErrorCode.PARAMS_ERROR, "isOpen 只能为 0 或 1");

        try {
            // ===== 2️⃣ 计算 MD5，实现秒传 =====
            byte[] fileBytes = file.getBytes();
            String md5 = DigestUtil.md5Hex(fileBytes);
            
            // ===== 3️⃣ 处理公开图片（上传到公共库）=====
            Picture picture = null;
            if (isOpen == 1) {
                // 检查公共库是否已存在
                Picture existPublicPicture = pictureService.lambdaQuery()
                        .eq(Picture::getMd5, md5)
                        .one();
                
                if (existPublicPicture != null) {
                    // 秒传成功，直接使用已有记录
                    picture = existPublicPicture;
                    log.info("图片 MD5={} 已存在于公共库，使用秒传", md5);
                } else {
                    // 真正上传到 OSS
                    UploadPictureResultVO uploadResult = uploadFileToOSS(file, fileBytes);
                    
                    // 构建 Picture 对象
                    picture = buildPicture(uploadResult, req, user, md5);
                    
                    // 保存到公共库
                    boolean saved = pictureService.save(picture);
                    ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "保存图片到公共库失败");
                    
                    log.info("图片 MD5={} 已上传到公共库，ID={}", md5, picture.getId());
                }
            } else {
                // ===== 4️⃣ 处理私有图片（仅上传到 OSS）=====
                UploadPictureResultVO uploadResult = uploadFileToOSS(file, fileBytes);
                picture = buildPicture(uploadResult, req, user, md5);
            }

            // ===== 5️⃣ 添加到用户空间（UserPicture 表）=====
            UserPicture userPicture = buildUserPicture(picture, req, user, md5);
            boolean savedUserPicture = this.save(userPicture);
            ThrowUtils.throwIf(!savedUserPicture, ErrorCode.OPERATION_ERROR, "保存图片到用户空间失败");
            
            log.info("图片 MD5={} 已添加到用户空间，spaceId={}, isOpen={}", 
                     md5, req.getSpaceId(), isOpen);

            // ===== 6️⃣ 返回结果 =====
            return PictureVO.objToVo(picture, userService.getUserVO(user));

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("上传到用户空间失败，userId={}, spaceId={}, error={}", 
                     user.getId(), req.getSpaceId(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传文件到 OSS
     */
    private UploadPictureResultVO uploadFileToOSS(MultipartFile file, byte[] fileBytes) {
        try {
            return imageUploadManger.uploadPicture(fileBytes, file.getOriginalFilename(), file.getContentType());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传到 OSS 失败：" + e.getMessage());
        }
    }

    /**
     * 构建 Picture 对象
     */
    private Picture buildPicture(UploadPictureResultVO uploadResult, 
                                  PictureUploadToUserSpaceReq req, 
                                  User user, 
                                  String md5) {
        Picture picture = new Picture();
        BeanUtils.copyProperties(uploadResult, picture);
        picture.setMd5(md5);
        picture.setName(req.getPictureName());
        picture.setIntroduction(req.getPictureIntroduction());
        picture.setCategory(req.getPictureCategory());
        
        // 处理 tags：如果是 JSON 字符串则解析，否则直接设置
        String tagsStr = req.getPictureTags();
        if (StrUtil.isNotBlank(tagsStr)) {
            if (tagsStr.startsWith("[")) {
                // 已经是 JSON 字符串
                picture.setTags(tagsStr);
            } else {
                // 单个标签，转成数组
                picture.setTags(JSONUtil.toJsonStr(new String[]{tagsStr}));
            }
        }
        
        picture.setUserId(user.getId());
        return picture;
    }

    /**
     * 构建 UserPicture 对象
     */
    private UserPicture buildUserPicture(Picture picture, 
                                          PictureUploadToUserSpaceReq req, 
                                          User user, 
                                          String md5) {
        UserPicture userPicture = new UserPicture();
        BeanUtils.copyProperties(picture, userPicture);
        userPicture.setMd5(md5);
        userPicture.setSpaceId(req.getSpaceId());
        userPicture.setIsOpen(req.getIsOpen());
        userPicture.setUserId(user.getId());
        return userPicture;
    }
}




