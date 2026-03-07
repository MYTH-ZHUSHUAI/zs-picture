package com.zhushuai.zspicturebackend.manager.uploader;

import cn.hutool.core.io.file.FileNameUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.zhushuai.zspicturebackend.manager.model.UploadContext;
import com.zhushuai.zspicturebackend.config.CosClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * COS 上传器 - 负责将图片上传到 COS 并进行图片处理
 */
@Component
@Slf4j
public class CosUploader {

    @Resource
    private COSClient cosClient;

    @Resource
    private CosClientConfig cosClientConfig;

    /**
     * 上传图片到 COS，并生成多种格式的图片
     * 
     * @param bytes 图片字节数组
     * @param key OSS 中的路径（格式：uuid/uuid.jpg）
     * @param contentType 文件类型
     * @return UploadContext 包含所有生成的图片 key
     */
    public UploadContext upload(byte[] bytes, String key, String contentType) {

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        metadata.setContentType(contentType);

        PutObjectRequest request = new PutObjectRequest(
                cosClientConfig.getBucket(),
                key,
                new ByteArrayInputStream(bytes),
                metadata
        );

        // ===== 配置腾讯数据万象图片处理 =====
        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(1);  // 返回原图信息

        List<PicOperations.Rule> rules = new ArrayList<>();

        // 从 key 中提取文件名（去掉后缀）
        // key 格式：uuid/uuid.suffix，例如：123/123.jpg
        String fileNameWithDir = key.substring(0, key.lastIndexOf('.'));

        // 1. WebP 格式原图
        String webpKey = fileNameWithDir + ".webp";
        PicOperations.Rule webpRule = new PicOperations.Rule();
        webpRule.setRule("imageMogr2/format/webp");
        webpRule.setBucket(cosClientConfig.getBucket());
        webpRule.setFileId(webpKey);
        rules.add(webpRule);

        // 2. 缩略图（WebP，宽度 300px）
        String thumbnailKey = fileNameWithDir + "_thumbnail.webp";
        PicOperations.Rule thumbnailRule = new PicOperations.Rule();
        thumbnailRule.setRule("imageMogr2/thumbnail/300x/format/webp");
        thumbnailRule.setBucket(cosClientConfig.getBucket());
        thumbnailRule.setFileId(thumbnailKey);
        rules.add(thumbnailRule);

        // 3. 带水印的原图（WebP）
        String watermarkedWebpKey = fileNameWithDir + "_watermarked.webp";
        PicOperations.Rule watermarkedRule = new PicOperations.Rule();
        watermarkedRule.setRule("imageWatermark/1,limit/0,dx/10,dy/10,image/aHR0cHM6Ly9leGFtcGxlLmNvbS93YXRlcm1hcmsucG5n/r/50");
        watermarkedRule.setBucket(cosClientConfig.getBucket());
        watermarkedRule.setFileId(watermarkedWebpKey);
        rules.add(watermarkedRule);

        // 4. 带水印的缩略图（WebP）
        String watermarkedThumbnailKey = fileNameWithDir + "_watermarked_thumbnail.webp";
        PicOperations.Rule watermarkedThumbnailRule = new PicOperations.Rule();
        watermarkedThumbnailRule.setRule("imageWatermark/1,limit/0,dx/10,dy/10,image/aHR0cHM6Ly9leGFtcGxlLmNvbS93YXRlcm1hcmsucG5n/r/50|imageMogr2/thumbnail/300x/format/webp");
        watermarkedThumbnailRule.setBucket(cosClientConfig.getBucket());
        watermarkedThumbnailRule.setFileId(watermarkedThumbnailKey);
        rules.add(watermarkedThumbnailRule);

        // 应用所有规则
        picOperations.setRules(rules);
        request.setPicOperations(picOperations);

        // 执行上传
        PutObjectResult result = cosClient.putObject(request);

        // 构建返回结果
        UploadContext context = new UploadContext();
        context.setOriginalKey(key);
        context.setWebpKey(webpKey);
        context.setThumbnailKey(thumbnailKey);
        context.setWatermarkedWebpKey(watermarkedWebpKey);
        context.setWatermarkedThumbnailKey(watermarkedThumbnailKey);
        context.setSize((long) bytes.length);

        // 从响应中获取图片宽高
        if (result.getCiUploadResult() != null 
                && result.getCiUploadResult().getOriginalInfo() != null
                && result.getCiUploadResult().getOriginalInfo().getImageInfo() != null) {
            
            context.setWidth(result.getCiUploadResult()
                    .getOriginalInfo()
                    .getImageInfo()
                    .getWidth());
            
            context.setHeight(result.getCiUploadResult()
                    .getOriginalInfo()
                    .getImageInfo()
                    .getHeight());
        }

        log.info("图片上传成功，key={}", key);

        return context;
    }
}
