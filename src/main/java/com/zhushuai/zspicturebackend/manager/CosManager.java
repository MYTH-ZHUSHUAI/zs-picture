package com.zhushuai.zspicturebackend.manager;

import cn.hutool.core.lang.UUID;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferManagerConfiguration;
import com.qcloud.cos.transfer.Upload;
import com.zhushuai.zspicturebackend.config.CosClientConfig;
import com.zhushuai.zspicturebackend.exception.BusinessException;
import com.zhushuai.zspicturebackend.exception.ErrorCode;
import com.zhushuai.zspicturebackend.exception.ThrowUtils;
import com.zhushuai.zspicturebackend.model.enums.ImageFormatEnum;
import com.zhushuai.zspicturebackend.model.vo.UploadPictureResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    private TransferManager transferManager;

    /**
     * 初始化 TransferManager（单例）
     */
    @PostConstruct
    public void init() {

        ExecutorService threadPool = Executors.newFixedThreadPool(16);

        transferManager = new TransferManager(cosClient, threadPool);

        TransferManagerConfiguration configuration = new TransferManagerConfiguration();

        configuration.setMultipartUploadThreshold(5 * 1024 * 1024);
        configuration.setMinimumUploadPartSize(5 * 1024 * 1024);

        transferManager.setConfiguration(configuration);

        log.info("COS TransferManager 初始化完成");
    }

    /**
     * 销毁
     */
    @PreDestroy
    public void destroy() {
        if (transferManager != null) {
            transferManager.shutdownNow(false);
        }
    }

    /**
     * 图片上传（带缩略图）
     */
    public UploadPictureResultVO uploadImage(MultipartFile file) throws IOException, InterruptedException {


        // 4️⃣ 获取后缀
        String originalName = file.getOriginalFilename();
        String suffix = "";

        if (originalName != null && originalName.contains(".")) {
            suffix = originalName.substring(originalName.lastIndexOf("."));
        }

        ThrowUtils.throwIf(!ImageFormatEnum.isValid(suffix), ErrorCode.PARAMS_ERROR, "不支持的文件格式");

        String bucket = cosClientConfig.getBucket();

        // 1️⃣ 读取文件为字节数组（避免多次读取流）
        byte[] fileBytes = file.getBytes();

        // 2️⃣ 解析图片
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(fileBytes));
        ThrowUtils.throwIf(originalImage == null, ErrorCode.PARAMS_ERROR, "图片为空");

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        double scale = (double) width / height;

        // 3️⃣ 日期路径
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));


        String format = suffix.replace(".", "");

        // 5️⃣ 生成唯一文件名
        String filename = UUID.randomUUID().toString().replace("-", "");

        String key = "images/" + datePath + "/" + filename + suffix;

        // ================= 原图上传 =================


        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(fileBytes.length);
        metadata.setContentType(file.getContentType());

        // 构造请求
        PutObjectRequest putObjectRequest = new PutObjectRequest(
                bucket,
                key,
                new ByteArrayInputStream(fileBytes),
                metadata
        );

        transferManager.upload(putObjectRequest).waitForUploadResult();


        String url = cosClientConfig.getHost() + "/" + key;
        log.info("图片上传成功：{}", url);


        // ================= 生成缩略图 =================

//            String thumbnailKey = "images/" + datePath + "/" + filename + "_thumb" + suffix;
//
//            BufferedImage originalImage = ImageIO.read(file.getInputStream());
//
//            BufferedImage thumbnail = resizeImage(originalImage, 300);
//
//            ByteArrayOutputStream os = new ByteArrayOutputStream();
//            ImageIO.write(thumbnail, suffix.replace(".", ""), os);
//
//            byte[] thumbnailBytes = os.toByteArray();
//
//            ObjectMetadata thumbMetadata = new ObjectMetadata();
//            thumbMetadata.setContentLength(thumbnailBytes.length);
//            thumbMetadata.setContentType(file.getContentType());
//
//            PutObjectRequest thumbRequest = new PutObjectRequest(
//                    bucket,
//                    thumbnailKey,
//                    new ByteArrayInputStream(thumbnailBytes),
//                    thumbMetadata
//            );
//
//            transferManager.upload(thumbRequest).waitForUploadResult();
//
//            String thumbnailUrl = cosClientConfig.getHost() + "/" + thumbnailKey;

        return new UploadPictureResultVO(
                url,
                null,
                key,
                (long) fileBytes.length,
                width,
                height,
                scale,
                format
        );


    }

    /**
     * 等比例缩放
     */
    private BufferedImage resizeImage(BufferedImage original, int targetWidth) {

        int width = original.getWidth();
        int height = original.getHeight();

        double scale = (double) targetWidth / width;
        int targetHeight = (int) (height * scale);

        Image scaled = original.getScaledInstance(
                targetWidth,
                targetHeight,
                Image.SCALE_SMOOTH
        );

        BufferedImage resized = new BufferedImage(
                targetWidth,
                targetHeight,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();

        return resized;
    }


}