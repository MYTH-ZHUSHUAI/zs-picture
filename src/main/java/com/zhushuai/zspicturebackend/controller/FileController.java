package com.zhushuai.zspicturebackend.controller;


import com.zhushuai.zspicturebackend.common.BaseResponse;
import com.zhushuai.zspicturebackend.common.ResultUtils;
import com.zhushuai.zspicturebackend.exception.ErrorCode;
import com.zhushuai.zspicturebackend.exception.ThrowUtils;
import com.zhushuai.zspicturebackend.manager.FileManager;
import com.zhushuai.zspicturebackend.model.vo.UploadFileResultVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;


@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileManager fileManager;

    /**
     * 上传文件
     *
     * @param file 文件
     * @return
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    public BaseResponse<UploadFileResultVO> uploadFile(@RequestParam("file") MultipartFile file) {
        ThrowUtils.throwIf(file.isEmpty(), ErrorCode.PARAMS_ERROR, "上传文件为空");

        UploadFileResultVO uploadFileResultVO = fileManager.uploadFile(file);
        return ResultUtils.success(uploadFileResultVO);
    }
}
