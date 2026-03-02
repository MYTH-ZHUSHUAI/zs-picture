package com.zhushuai.zspicturebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhushuai.zspicturebackend.exception.ErrorCode;
import com.zhushuai.zspicturebackend.exception.ThrowUtils;
import com.zhushuai.zspicturebackend.manager.CosManager;
import com.zhushuai.zspicturebackend.model.dto.picture.PictureUploadReq;
import com.zhushuai.zspicturebackend.model.entity.Picture;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.model.vo.PictureVO;
import com.zhushuai.zspicturebackend.model.vo.UploadPictureResultVO;
import com.zhushuai.zspicturebackend.service.PictureService;
import com.zhushuai.zspicturebackend.mapper.PictureMapper;
import com.zhushuai.zspicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;

/**
 * @author zhushuai
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2026-03-01 08:36:06
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    @Resource
    private CosManager cosManager;

    @Resource
    private UserService userService;


    /**
     * 上传图片
     *
     * @param pictureUploadReq
     * @param user
     * @return
     */
    @Override
    public PictureVO uploadPicture(MultipartFile file,
                                   PictureUploadReq pictureUploadReq,
                                   User user) throws IOException, InterruptedException {
        ThrowUtils.throwIf(file == null, ErrorCode.PARAMS_ERROR, "请求参数错误");

        // 上传图片，获取图片信息
        UploadPictureResultVO uploadPictureResultVO = cosManager.uploadImage(file);

        String pictureName = pictureUploadReq.getPictureName();
        String pictureIntroduction = pictureUploadReq.getPictureIntroduction();
        String pictureCategory = pictureUploadReq.getPictureCategory();
        String pictureTags = pictureUploadReq.getPictureTags();


        // 构造picture对象
        Picture picture = new Picture();
        BeanUtils.copyProperties(uploadPictureResultVO, picture);
        picture.setName(pictureName);
        picture.setIntroduction(pictureIntroduction);
        picture.setCategory(pictureCategory);
        picture.setTags(pictureTags);
        picture.setUserId(user.getId());

        // 将图片保存到数据库中
        boolean saved = this.save(picture);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "图片上传失败");
        picture.setCreateTime(new Date());


        PictureVO pictureVO = PictureVO.objToVo(picture);
        pictureVO.setUser(userService.getUserVO(user));

        return pictureVO;
    }
}




