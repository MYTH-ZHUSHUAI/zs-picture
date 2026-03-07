package com.zhushuai.zspicturebackend.manager.processor;

import com.zhushuai.zspicturebackend.manager.MedianCutColorExtractor;
import com.zhushuai.zspicturebackend.manager.model.UploadContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 主色调提取处理器 - 使用中位切分算法提取图片主色调
 */
@Slf4j
@Component
public class ColorExtractorProcessor implements UploadPostProcessor {

    @Override
    public void process(UploadContext context, byte[] imageBytes) {

        try {

            int[] rgb = MedianCutColorExtractor.extractDominantColor(imageBytes);

            String hex = String.format("#%02X%02X%02X", rgb[0], rgb[1], rgb[2]);

            context.setMainColor(hex);

            log.debug("提取主色调成功：{}", hex);

        } catch (Exception e) {

            log.error("提取主色调失败", e);
        }
    }
}
