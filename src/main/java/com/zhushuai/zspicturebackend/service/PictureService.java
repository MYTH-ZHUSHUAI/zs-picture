package com.zhushuai.zspicturebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhushuai.zspicturebackend.model.dto.picture.*;
import com.zhushuai.zspicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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
                            User user);


    /**
     * 修改图片信息
     *
     * @param pictureUpdateReq 图片修改请求
     * @param user             本次请求的用户
     * @return PictureVO
     */
    PictureVO updatePicture(PictureUpdateReq pictureUpdateReq, User user);


    /**
     * 根据条件查询图片
     *
     * @param pictureQueryReq
     * @return
     */
    PictureVO queryPicture(PictureQueryReq pictureQueryReq, User user);


    /**
     * 根据用户id获取其全部图片
     *
     * @param pictureListReq
     * @return
     */
    Page<PictureVO> getPictureVOPage(PictureListReq pictureListReq);


    /**
     * 根据url上传图片
     *
     * @param pictureUrlUploadReq 上传请求
     * @param user                用户
     * @return
     */
    PictureVO uploadPictureByUrl(PictureUrlUploadReq pictureUrlUploadReq, User user);
}
