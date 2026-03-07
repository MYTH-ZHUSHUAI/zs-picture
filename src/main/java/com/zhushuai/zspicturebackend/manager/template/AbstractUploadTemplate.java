package com.zhushuai.zspicturebackend.manager.template;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.lang.UUID;
import com.zhushuai.zspicturebackend.manager.model.UploadContext;
import com.zhushuai.zspicturebackend.manager.processor.ImageProcessor;
import com.zhushuai.zspicturebackend.manager.processor.UploadPostProcessor;
import com.zhushuai.zspicturebackend.manager.uploader.CosUploader;
import com.zhushuai.zspicturebackend.config.CosClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhushuai
 */
@Slf4j
public abstract class AbstractUploadTemplate<T> {

    @Resource
    private CosUploader cosUploader;

    @Resource
    private ImageProcessor imageProcessor;

    @Resource
    private List<UploadPostProcessor> postProcessors;

    @Resource
    protected CosClientConfig cosClientConfig;

    public final T uploadPicture(MultipartFile file) throws Exception {

        byte[] bytes = file.getBytes();
        String originalName = file.getOriginalFilename();
        String contentType = file.getContentType();

        // 1 校验
        validate(bytes, originalName);

        // 2 生成 key
        String key = generateKey(originalName);

        // 3 上传
        UploadContext context = cosUploader.upload(bytes, key, contentType);

        // 4 图片信息处理
        imageProcessor.process(context);

        // 5 后处理扩展（备份、提取主色调等）
        for (UploadPostProcessor processor : postProcessors) {
            processor.process(context, bytes);
        }

        // 6 构建结果
        return buildResult(bytes, originalName, context);
    }

    protected abstract void validate(byte[] bytes, String originalName);

    protected abstract T buildResult(byte[] bytes, String originalName, UploadContext context);

    protected String generateKey(String originalName) {

        String suffix = FileNameUtil.getSuffix(originalName);

        String uuid = UUID.randomUUID().toString().replace("-", "");

        return uuid + "/" + uuid + "." + suffix;
    }
}
