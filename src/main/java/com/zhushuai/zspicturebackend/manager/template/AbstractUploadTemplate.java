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
import com.zhushuai.zspicturebackend.manager.MedianCutColorExtractor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Slf4j
public abstract class AbstractUploadTemplate<T> {

    @Resource
    protected CosClientConfig cosClientConfig;

    @Resource
    protected TransferManager transferManager;

    @Resource
    protected COSClient cosClient;

    @Resource
    private ExecutorService pictureAsyncExecutor;

    public final T uploadPicture(MultipartFile file) throws Exception {

        byte[] fileBytes = file.getBytes();
        String originalName = file.getOriginalFilename();
        String contentType = file.getContentType();

        // 1️⃣ 校验
        validate(fileBytes, originalName);

        // 2️⃣ 生成 key -- 模板方法
        String key = generateKey(originalName);

        // 3️⃣ 上传  -- 模板方法
        UploadResult uploadResult = uploadToCos(fileBytes, key, contentType);

        // 4️⃣ 返回结果  -- 抽象方法
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
     * @param fileBytes    文件字节数组
     * @param originalName 原始文件名
     * @param uploadResult 上传结果
     * @return T
     */
    protected abstract T buildResult(byte[] fileBytes, String originalName, UploadResult uploadResult)
            throws Exception;

    /**
     * 生成唯一 key（包含 UUID 目录）
     *
     * @param originalName
     * @return 返回 UUID 目录路径，例如：uuid/uuid.jpg
     */
    protected String generateKey(String originalName) {
        String suffix = FileNameUtil.getSuffix(originalName);
        String uuid = UUID.randomUUID().toString().replace("-", "");

        // 创建目录结构：uuid/uuid.suffix
        return String.format("%s/%s.%s", uuid, uuid, suffix);
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
     * 使用 COSClient 上传文件（带图片处理和主色调提取）
     *
     * @param bytes       文件字节数组
     * @param key         上传路径（格式：uuid/uuid.suffix）
     * @param contentType 文件类型
     */
    private UploadResult uploadToCos(byte[] bytes, String key, String contentType) {
        long size = bytes.length;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(size);
        metadata.setContentType(contentType);

        // 构建发送请求
        PutObjectRequest request = new PutObjectRequest(
                cosClientConfig.getBucket(),
                key,
                new ByteArrayInputStream(bytes),
                metadata
        );

        // ===== 图片处理配置 =====
        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(1);

        List<PicOperations.Rule> rules = new ArrayList<>();

        // 从 key 中提取 uuid 目录和文件名
        // key 格式：uuid/uuid.suffix，例如：1234567890/1234567890.jpg
        String basePath = FileNameUtil.getPrefix(key);

        // 1. WebP 格式原图
        String webpKey = basePath + ".webp";
        PicOperations.Rule webpRule = new PicOperations.Rule();
        webpRule.setRule("imageMogr2/format/webp");
        webpRule.setBucket(cosClientConfig.getBucket());
        webpRule.setFileId(webpKey);
        rules.add(webpRule);

        // 2. 缩略图（WebP）
        String thumbnailKey = basePath + "_thumbnail.webp";
        PicOperations.Rule thumbnailRule = new PicOperations.Rule();
        thumbnailRule.setRule("imageMogr2/thumbnail/300x/format/webp");
        thumbnailRule.setBucket(cosClientConfig.getBucket());
        thumbnailRule.setFileId(thumbnailKey);
        rules.add(thumbnailRule);

        // 3. 带水印的原图
        String watermarkedWebpKey = basePath + "_watermarked.webp";

        PicOperations.Rule watermarkedRule = new PicOperations.Rule();

        watermarkedRule.setRule("watermark/2/text/enN6c3pzenM=");

        watermarkedRule.setBucket(cosClientConfig.getBucket());
        watermarkedRule.setFileId(watermarkedWebpKey);

        rules.add(watermarkedRule);

        // 4. 带水印的缩略图
        String watermarkedThumbnailKey = basePath + "_watermarked_thumbnail.webp";

        PicOperations.Rule watermarkedThumbnailRule = new PicOperations.Rule();

        watermarkedThumbnailRule.setRule(
                "watermark/2/text/enN6c3pzenM="
                        + "|imageMogr2/thumbnail/300x/format/webp"
        );

        watermarkedThumbnailRule.setBucket(cosClientConfig.getBucket());
        watermarkedThumbnailRule.setFileId(watermarkedThumbnailKey);

        rules.add(watermarkedThumbnailRule);


        // 将处理规则全部添加进 request 中
        picOperations.setRules(rules);
        request.setPicOperations(picOperations);

        // 执行上传
        PutObjectResult result = cosClient.putObject(request);

        ThrowUtils.throwIf(result.getCiUploadResult() == null, ErrorCode.OPERATION_ERROR, "图片处理失败");

        // 获取宽高
        Integer width = null;
        Integer height = null;
        Double scale = null;

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

            // 计算比例
            if (width != null && height != null && height > 0) {
                scale = (double) width / height;
            }
        }

        // 提取主色调
        String mainColor = MedianCutColorExtractor.extractDominantColor(bytes);

        // 构建返回结果（参数顺序必须与 UploadResult 构造函数一致）
        return new UploadResult(
                key,           // originalKey: uuid/uuid.jpg
                basePath + "/" + webpKey,       // webpKey: uuid/uuid.webp
                basePath + "/" + thumbnailKey,  // thumbnailKey: uuid/uuid_thumbnail.webp
                width,         // width
                height,        // height
                size,          // size
                mainColor,     // mainColor
                basePath + "/" + watermarkedWebpKey,        // watermarkedWebpKey: uuid/uuid_watermarked.webp
                basePath + "/" + watermarkedThumbnailKey,   // watermarkedThumbnailKey: uuid/uuid_watermarked_thumbnail.webp
                scale          // scale
        );
    }
}
