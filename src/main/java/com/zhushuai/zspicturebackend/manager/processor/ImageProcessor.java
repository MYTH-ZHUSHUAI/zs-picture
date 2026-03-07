package com.zhushuai.zspicturebackend.manager.processor;

import com.zhushuai.zspicturebackend.manager.model.UploadContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 图片信息处理器 - 从 COS 返回结果中提取图片信息
 */
@Slf4j
@Component
public class ImageProcessor {

    /**
     * 处理图片信息（已在 CosUploader 中完成）
     * 
     * @param context 上传上下文
     */
    public void process(UploadContext context) {
        // 宽高信息已经在 CosUploader 中从 COS 响应中获取
        log.debug("图片信息处理完成，width={}, height={}", context.getWidth(), context.getHeight());
    }
}
