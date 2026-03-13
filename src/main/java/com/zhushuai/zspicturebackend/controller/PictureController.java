package com.zhushuai.zspicturebackend.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhushuai.zspicturebackend.common.BaseResponse;
import com.zhushuai.zspicturebackend.common.ResultUtils;
import com.zhushuai.zspicturebackend.model.dto.picture.PictureListReq;
import com.zhushuai.zspicturebackend.model.dto.picture.PictureUpdateReq;
import com.zhushuai.zspicturebackend.model.dto.picture.PictureUploadReq;
import com.zhushuai.zspicturebackend.model.dto.picture.PictureUploadToUserSpaceReq;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.model.vo.PictureVO;
import com.zhushuai.zspicturebackend.service.PictureService;
import com.zhushuai.zspicturebackend.service.UserPictureService;
import com.zhushuai.zspicturebackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {


    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    @Resource
    private UserPictureService userPictureService;

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
    @Operation(summary = "上传到公共空间", description = "将图片上传到公共图库，支持秒传功能")
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile file,
            @RequestParam String pictureName,
            @RequestParam(required = false) String pictureIntroduction,
            @RequestParam(required = false) String pictureCategory,
            @RequestParam(required = false) String pictureTags,
            HttpServletRequest request) {

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
    @Operation(summary = "公共空间图片更新请求")
    public BaseResponse<PictureVO> uodatePicture(@RequestBody PictureUpdateReq pictureUpdateReq,
                                                 HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);

        PictureVO pictureVO = pictureService.updatePicture(pictureUpdateReq, loginUser);

        return ResultUtils.success(pictureVO);
    }


    /**
     * 用户获取图片列表
     */
    @PostMapping("/list")
    @Operation(summary = "用户获取图片列表")
    public BaseResponse<Page<PictureVO>> listPicture(@RequestBody PictureListReq pictureListReq) {

        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(pictureListReq);

        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 用户上传图片到个人空间
     *
     * @param file                图片文件
     * @param pictureName         图片名称
     * @param pictureIntroduction 简介
     * @param pictureCategory     分类
     * @param pictureTags         标签（JSON 数组字符串）
     * @param spaceId             空间 ID
     * @param isOpen              是否公开（0-不公开，1-公开）
     * @param request             请求
     * @return 上传后的图片信息
     */
    @PostMapping(value = "/upload/space", consumes = "multipart/form-data")
    @Operation(summary = "上传到用户空间", description = "将图片上传到用户的私有空间，支持公开/私密设置")
    public BaseResponse<PictureVO> uploadPictureToUserSpace(
            @RequestPart("file") MultipartFile file,
            @RequestParam String pictureName,
            @RequestParam(required = false) String pictureIntroduction,
            @RequestParam(required = false) String pictureCategory,
            @RequestParam(required = false) String pictureTags,
            @RequestParam Long spaceId,
            @RequestParam(defaultValue = "0") int isOpen,
            HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);

        PictureUploadToUserSpaceReq req = new PictureUploadToUserSpaceReq();
        req.setPictureName(pictureName);
        req.setPictureIntroduction(pictureIntroduction);
        req.setPictureCategory(pictureCategory);
        req.setPictureTags(pictureTags);
        req.setSpaceId(spaceId);
        req.setIsOpen(isOpen);

        PictureVO pictureVO = userPictureService.uploadPictureToUserSpace(file, req, loginUser);

        return ResultUtils.success(pictureVO);
    }

    @PostMapping(value = "/list/color")
    @Operation(summary = "根据颜色查找图片", description = "根据主色调筛选图片列表")
    public BaseResponse<Page<PictureVO>> getPictureListByColor(@RequestParam String color,
                                                                  HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);

        Page<PictureVO> pictureListByColor = pictureService.getPictureListByColor(color);

        return ResultUtils.success(pictureListByColor);
    }
}
