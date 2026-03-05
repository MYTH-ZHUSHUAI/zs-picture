package com.zhushuai.zspicturebackend.manager;


import cn.hutool.core.io.file.FileNameUtil;
import com.zhushuai.zspicturebackend.exception.ErrorCode;
import com.zhushuai.zspicturebackend.exception.ThrowUtils;
import com.zhushuai.zspicturebackend.manager.template.AbstractUploadTemplate;
import com.zhushuai.zspicturebackend.model.enums.ImageFormatEnum;
import com.zhushuai.zspicturebackend.model.vo.UploadPictureResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ImageUploadManager extends AbstractUploadTemplate<UploadPictureResultVO> {

    @Override
    protected void validate(byte[] fileBytes, String originalName) {

        ThrowUtils.throwIf(!ImageFormatEnum.isValid(FileNameUtil.getSuffix(originalName)),
                ErrorCode.PARAMS_ERROR,
                "不支持的图片格式");
    }

    @Override
    protected UploadPictureResultVO buildResult(byte[] fileBytes, String originalName, UploadResult uploadResult) {

        String url = cosClientConfig.getHost() + "/" + uploadResult.getWebpKey();
        String thumbnailUrl = cosClientConfig.getHost() + "/" + uploadResult.getThumbnailKey();

        return UploadPictureResultVO.builder()
                .url(url)
                .thumbnailUrl(thumbnailUrl)
                .name(originalName)
                .picSize(uploadResult.getSize())
                .picWidth(uploadResult.getWidth())
                .picHeight(uploadResult.getHeight())
                .picFormat(FileNameUtil.getSuffix(originalName))
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


