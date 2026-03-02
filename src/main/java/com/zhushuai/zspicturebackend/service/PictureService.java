package com.zhushuai.zspicturebackend.service;

import com.zhushuai.zspicturebackend.model.dto.picture.PictureUploadReq;
import com.zhushuai.zspicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author zhushuai
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2026-03-01 08:36:06
 */
public interface PictureService extends IService<Picture> {


    /**
     * 上传图片
     *
     * @param pictureUploadReq 图片上传请求
     * @param user             本次请求的用户
     * @return PictureVO
     */
    PictureVO uploadPicture(MultipartFile file,
                            PictureUploadReq pictureUploadReq,
                            User user) throws IOException, InterruptedException;

}
