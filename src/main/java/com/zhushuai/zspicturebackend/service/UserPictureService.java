package com.zhushuai.zspicturebackend.service;

import com.zhushuai.zspicturebackend.model.dto.picture.PictureUploadToUserSpaceReq;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.model.entity.UserPicture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhushuai.zspicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author zhushuai
 * @description 针对表【user_picture(图片)】的数据库操作Service
 * @createDate 2026-03-06 13:10:55
 */
public interface UserPictureService extends IService<UserPicture> {

    /**
     * 上传图片到用户私有空间
     *
     * @param pictureUploadToUserSpaceReq
     * @param user
     * @return
     */
    PictureVO uploadPictureToUserSpace(MultipartFile file,
                                       PictureUploadToUserSpaceReq pictureUploadToUserSpaceReq,
                                       User user);

}
