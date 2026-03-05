package com.zhushuai.zspicturebackend.manager;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.zhushuai.zspicturebackend.exception.BusinessException;
import com.zhushuai.zspicturebackend.exception.ErrorCode;
import com.zhushuai.zspicturebackend.exception.ThrowUtils;
import com.zhushuai.zspicturebackend.model.enums.ImageFormatEnum;
import com.zhushuai.zspicturebackend.model.vo.UploadPictureResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
@Slf4j
public class ImageUploadTemplate extends AbstractUploadTemplate<UploadPictureResultVO> {

    @Override
    protected void validate(byte[] fileBytes, String originalName) {

        String suffix = super.getSuffix(originalName);

        ThrowUtils.throwIf(!ImageFormatEnum.isValid(suffix), ErrorCode.PARAMS_ERROR, "不支持的图片格式");
    }

    @Override
    protected UploadPictureResultVO buildResult(byte[] fileBytes, String originalName, String key) {

        BufferedImage image = null;
        try {
            image = ImageIO.read(new ByteArrayInputStream(fileBytes));
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片解析失败");
        }

        int width = image.getWidth();
        int height = image.getHeight();

        double scale = (double) width / height;

        String url = cosClientConfig.getHost() + "/" + key;

        return UploadPictureResultVO.builder()
                .url(url)
                .name(originalName)
                .picSize((long) fileBytes.length)
                .picWidth(width)
                .picHeight(height)
                .picScale(scale)
                .picFormat(super.getSuffix(originalName))
                .key(key)
                .build();
    }

//    public UploadPictureResultVO uploadFromUrl(String imageUrl) throws Exception {
//
//        HttpResponse response = HttpRequest.get(imageUrl)
//                .timeout(10000)
//                .execute();
//
//        ThrowUtils.throwIf(!response.isOk(), ErrorCode.PARAMS_ERROR, "图片下载失败");
//
//        byte[] bytes = response.bodyBytes();
//
//        String contentType = response.contentType();
//
//        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
//
//        return upload(bytes, fileName, contentType);
//    }


}


