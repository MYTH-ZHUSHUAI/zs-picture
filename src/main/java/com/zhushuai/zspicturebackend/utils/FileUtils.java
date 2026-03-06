package com.zhushuai.zspicturebackend.utils;

import com.zhushuai.zspicturebackend.exception.BusinessException;
import com.zhushuai.zspicturebackend.exception.ErrorCode;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class FileUtils {
    public static String getMD5(MultipartFile file) {

        try {
            return DigestUtils.md5DigestAsHex(file.getBytes());
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取文件md5失败");
        }
    }
}
