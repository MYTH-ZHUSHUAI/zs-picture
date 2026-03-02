package com.zhushuai.zspicturebackend.controller;


import com.zhushuai.zspicturebackend.common.BaseResponse;
import com.zhushuai.zspicturebackend.common.ResultUtils;
import com.zhushuai.zspicturebackend.model.dto.picture.PictureUploadReq;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.model.vo.PictureVO;
import com.zhushuai.zspicturebackend.service.PictureService;
import com.zhushuai.zspicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/file")
public class PictureController {


    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;


    /**
     * 上传图片
     *
     * @param file
     * @param pictureName
     * @param pictureIntroduction
     * @param pictureCategory
     * @param pictureTags
     * @param request
     * @return
     */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public BaseResponse<PictureVO> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam() String pictureName,
            @RequestParam(required = false) String pictureIntroduction,
            @RequestParam(required = false) String pictureCategory,
            @RequestParam(required = false) String pictureTags,
            HttpServletRequest request) throws IOException, InterruptedException {

        User loginUser = userService.getLoginUser(request);

        PictureUploadReq req = new PictureUploadReq();
        req.setPictureName(pictureName);
        req.setPictureIntroduction(pictureIntroduction);
        req.setPictureCategory(pictureCategory);
        req.setPictureTags(pictureTags);

        PictureVO pictureVO = pictureService.uploadPicture(file, req, loginUser);

        return ResultUtils.success(pictureVO);
    }


}
