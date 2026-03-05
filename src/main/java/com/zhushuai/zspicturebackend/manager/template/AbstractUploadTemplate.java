package com.zhushuai.zspicturebackend.manager.template;

import cn.hutool.core.io.file.FileNameUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.qcloud.cos.transfer.TransferManager;
import com.zhushuai.zspicturebackend.config.CosClientConfig;
import com.zhushuai.zspicturebackend.exception.ErrorCode;
import com.zhushuai.zspicturebackend.exception.ThrowUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class AbstractUploadTemplate<T> {

    @Resource
    protected CosClientConfig cosClientConfig;

    @Resource
    protected TransferManager transferManager;

    @Resource
    protected COSClient cosClient;

    @Data
    @AllArgsConstructor
    protected static class UploadResult {
        private String webpKey;
        private String thumbnailKey;
        private Integer width;
        private Integer height;
        private Long size;
    }


    public final T upload(byte[] fileBytes, String originalName, String contentType) throws Exception {

        // 1️⃣ 校验
        validate(fileBytes, originalName);

        // 2️⃣ 生成 key
        String key = generateKey(originalName);

        // 3️⃣ 上传
        UploadResult uploadResult = uploadToCos(fileBytes, key, contentType);

        // 4️⃣ 返回结果
        return buildResult(fileBytes, originalName, uploadResult);
    }


    /**
     * 校验文件是否合法
     *
     * @param fileBytes
     * @param originalName
     */
    protected abstract void validate(byte[] fileBytes, String originalName);


    /**
     * 构建结果
     *
     * @param fileBytes
     * @param originalName
     * @param key
     * @return
     */
    protected abstract T buildResult(byte[] fileBytes, String originalName, UploadResult uploadResult)
            throws Exception;

    protected String generateKey(String originalName) {

        String suffix = FileNameUtil.getSuffix(originalName);
        String uuidFilename = UUID.randomUUID().toString().replace("-", "");

//        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));

        return String.format("%s.%s", uuidFilename, suffix);
    }

    /**
     * 使用 TransferManager 上传文件
     *
     * @param bytes
     * @param key
     * @param contentType
     * @throws Exception
     */
    private void uploadToCosByTransferManager(byte[] bytes, String key, String contentType) throws Exception {

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        metadata.setContentType(contentType);

        PutObjectRequest request = new PutObjectRequest(
                cosClientConfig.getBucket(),
                key,
                new ByteArrayInputStream(bytes),
                metadata
        );

        transferManager.upload(request).waitForUploadResult();
    }

    /**
     * 使用 COSClient 上传文件
     *
     * @param bytes
     * @param key
     * @param contentType
     */
    private UploadResult uploadToCos(byte[] bytes, String key, String contentType) {

        long size = bytes.length;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(size);
        metadata.setContentType(contentType);

        PutObjectRequest request = new PutObjectRequest(
                cosClientConfig.getBucket(),
                key,
                new ByteArrayInputStream(bytes),
                metadata
        );

        // ===== 图片处理配置 =====
        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(1);  // 返回原图信息

        List<PicOperations.Rule> rules = new ArrayList<>();

        // 获取主文件名（带路径）
        String fileName = FileNameUtil.getPrefix(key);

        // webp 文件
        String webpKey = fileName + ".webp";

        PicOperations.Rule webpRule = new PicOperations.Rule();
        webpRule.setRule("imageMogr2/format/webp");
        webpRule.setBucket(cosClientConfig.getBucket());
        webpRule.setFileId(webpKey);

        rules.add(webpRule);

        // 缩略图
        String thumbnailKey = fileName + "_thumbnail.webp";

        PicOperations.Rule thumbnailRule = new PicOperations.Rule();
        thumbnailRule.setRule("imageMogr2/thumbnail/300x/format/webp");
        thumbnailRule.setBucket(cosClientConfig.getBucket());
        thumbnailRule.setFileId(thumbnailKey);

        rules.add(thumbnailRule);

        picOperations.setRules(rules);
        request.setPicOperations(picOperations);

        // 执行上传
        PutObjectResult result = cosClient.putObject(request);

        ThrowUtils.throwIf(result.getCiUploadResult() == null, ErrorCode.OPERATION_ERROR, "图片处理失败");

        // 获取宽高
        Integer width = null;
        Integer height = null;

        if (result.getCiUploadResult().getOriginalInfo() != null
                && result.getCiUploadResult().getOriginalInfo().getImageInfo() != null) {

            width = result.getCiUploadResult()
                    .getOriginalInfo()
                    .getImageInfo()
                    .getWidth();

            height = result.getCiUploadResult()
                    .getOriginalInfo()
                    .getImageInfo()
                    .getHeight();
        }

        // 删除原图（只保留 webp）
        cosClient.deleteObject(cosClientConfig.getBucket(), key);

        return new UploadResult(webpKey, thumbnailKey, width, height, size);
    }
}