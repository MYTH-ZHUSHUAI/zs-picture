package com.zhushuai.zspicturebackend.manager;


import cn.hutool.core.io.file.FileNameUtil;
import com.zhushuai.zspicturebackend.exception.ErrorCode;
import com.zhushuai.zspicturebackend.exception.ThrowUtils;
import com.zhushuai.zspicturebackend.manager.model.UploadContext;
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

        ThrowUtils.throwIf(!ImageFormatEnum.isValid(FileNameUtil.getSuffix(originalName)),
                ErrorCode.PARAMS_ERROR,
                "不支持的图片格式");
    }

    @Override
    protected UploadPictureResultVO buildResult(byte[] fileBytes, String originalName, UploadContext context) {

        // 生成完整的 URL
        String originalUrl = cosClientConfig.getHost() + "/" + context.getOriginalKey();
        String url = cosClientConfig.getHost() + "/" + context.getWebpKey();
        String thumbnailUrl = cosClientConfig.getHost() + "/" + context.getThumbnailKey();
        String watermarkedUrl = cosClientConfig.getHost() + "/" + context.getWatermarkedWebpKey();
        String watermarkedThumbnailUrl = cosClientConfig.getHost() + "/" + context.getWatermarkedThumbnailKey();

        // 计算图片比例
        Double picScale = null;
        if (context.getWidth() != null && context.getHeight() != null && context.getHeight() > 0) {
            picScale = (double) context.getWidth() / context.getHeight();
        }

        return UploadPictureResultVO.builder()
                .originalUrl(originalUrl)
                .url(url)
                .thumbnailUrl(thumbnailUrl)
                .watermarkedUrl(watermarkedUrl)
                .watermarkedThumbnailUrl(watermarkedThumbnailUrl)
                .mainColor(context.getMainColor())
                .name(originalName)
                .picSize(context.getSize())
                .picWidth(context.getWidth())
                .picHeight(context.getHeight())
                .picScale(picScale)
                .picFormat(FileNameUtil.getSuffix(originalName))
                .build();
    }
}
