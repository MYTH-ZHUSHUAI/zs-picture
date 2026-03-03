package com.zhushuai.zspicturebackend.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhushuai.zspicturebackend.common.BaseResponse;
import com.zhushuai.zspicturebackend.common.ResultUtils;
import com.zhushuai.zspicturebackend.model.dto.picture.PictureListReq;
import com.zhushuai.zspicturebackend.model.dto.picture.PictureUpdateReq;
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
import java.util.List;

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
     * @throws IOException
     * @throws InterruptedException
     */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile file,
            @RequestParam String pictureName,
            @RequestParam(required = false) String pictureIntroduction,
            @RequestParam(required = false) String pictureCategory,
            @RequestParam(required = false) List<String> pictureTags,
            HttpServletRequest request)
            throws IOException, InterruptedException {

        User loginUser = userService.getLoginUser(request);

        PictureUploadReq req = new PictureUploadReq();
        req.setPictureName(pictureName);
        req.setPictureIntroduction(pictureIntroduction);
        req.setPictureCategory(pictureCategory);
        req.setPictureTags(pictureTags);

        PictureVO pictureVO = pictureService.uploadPicture(file, req, loginUser);

        return ResultUtils.success(pictureVO);
    }

    /**
     * 用户根据id更新图片信息
     *
     * @param pictureUpdateReq
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<PictureVO> uodatePicture(@RequestBody PictureUpdateReq pictureUpdateReq,
                                                 HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);

        PictureVO pictureVO = pictureService.updatePicture(pictureUpdateReq, loginUser);

        return ResultUtils.success(pictureVO);
    }


    /**
     * 用户获取图片列表
     *
     */
    @PostMapping("/list")
    public BaseResponse<Page<PictureVO>> listPicture(@RequestBody PictureListReq pictureListReq) {

        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(pictureListReq);

        return ResultUtils.success(pictureVOPage);
    }


}
