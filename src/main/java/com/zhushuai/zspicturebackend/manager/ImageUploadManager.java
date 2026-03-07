package com.zhushuai.zspicturebackend.manager;


import cn.hutool.core.io.file.FileNameUtil;
import com.zhushuai.zspicturebackend.exception.ErrorCode;
import com.zhushuai.zspicturebackend.exception.ThrowUtils;
import com.zhushuai.zspicturebackend.manager.template.UploadResult;
import com.zhushuai.zspicturebackend.manager.template.AbstractUploadTemplate;
import com.zhushuai.zspicturebackend.model.enums.ImageFormatEnum;
import com.zhushuai.zspicturebackend.model.vo.UploadPictureResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 图片上传管理器
 */
@Component
@Slf4j
public class ImageUploadManager extends AbstractUploadTemplate<UploadPictureResultVO> {

    @Override
    protected void validate(byte[] fileBytes, String originalName) {

        ThrowUtils.throwIf(!ImageFormatEnum.isValid(FileNameUtil.getSuffix(originalName)), ErrorCode.PARAMS_ERROR, "不支持的图片格式");
    }

    @Override
    protected UploadPictureResultVO buildResult(byte[] fileBytes, String originalName, UploadResult uploadResult) {
    
        // 生成完整的 URL
        String originalUrl = cosClientConfig.getHost() + "/" + uploadResult.getOriginalKey();
        String url = cosClientConfig.getHost() + "/" + uploadResult.getWebpKey();
        String thumbnailUrl = cosClientConfig.getHost() + "/" + uploadResult.getThumbnailKey();
        String watermarkedUrl = cosClientConfig.getHost() + "/" + uploadResult.getWatermarkedWebpKey();
        String watermarkedThumbnailUrl = cosClientConfig.getHost() + "/" + uploadResult.getWatermarkedThumbnailKey();
            
        return UploadPictureResultVO.builder()
                .url(url)
                .thumbnailUrl(thumbnailUrl)
                .watermarkedUrl(watermarkedUrl)
                .watermarkedThumbnailUrl(watermarkedThumbnailUrl)
                .mainColor(uploadResult.getMainColor())
                .name(originalName)
                .picSize(uploadResult.getSize())
                .picWidth(uploadResult.getWidth())
                .picHeight(uploadResult.getHeight())
                .picScale(uploadResult.getScale())
                .picFormat(FileNameUtil.getSuffix(originalName))
                .build();
    }
}
