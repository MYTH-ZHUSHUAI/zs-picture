package com.zhushuai.zspicturebackend.manager;

import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.transfer.TransferManager;
import com.zhushuai.zspicturebackend.config.CosClientConfig;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
public abstract class AbstractUploadTemplate<T> {

    @Resource
    protected CosClientConfig cosClientConfig;

    @Resource
    protected TransferManager transferManager;

    public final T upload(byte[] fileBytes, String originalName, String contentType) throws Exception {

        // 1️⃣ 校验
        validate(fileBytes, originalName);

        // 2️⃣ 生成 key
        String key = generateKey(originalName);

        // 3️⃣ 上传
        uploadToCos(fileBytes, key, contentType);

        // 4️⃣ 返回结果
        return buildResult(fileBytes, originalName, key);
    }

    protected abstract void validate(byte[] fileBytes, String originalName);

    protected abstract T buildResult(byte[] fileBytes, String originalName, String key)
            throws Exception;

    protected String generateKey(String originalName) {

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        String suffix = this.getSuffix(originalName);

        String filename = UUID.randomUUID().toString().replace("-", "");

        return "uploads/" + datePath + "/" + filename + suffix;
    }

    private void uploadToCos(byte[] bytes, String key, String contentType) throws Exception {

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


    protected String getSuffix(String originalName) {
        return originalName.substring(originalName.lastIndexOf(".")).replace(".", "");
    }
}