package com.zhushuai.zspicturebackend.manager;

import cn.hutool.core.lang.UUID;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferManagerConfiguration;
import com.zhushuai.zspicturebackend.config.CosClientConfig;
import com.zhushuai.zspicturebackend.constant.FileConstant;
import com.zhushuai.zspicturebackend.exception.BusinessException;
import com.zhushuai.zspicturebackend.exception.ErrorCode;
import com.zhushuai.zspicturebackend.exception.ThrowUtils;
import com.zhushuai.zspicturebackend.model.enums.ImageFormatEnum;
import com.zhushuai.zspicturebackend.model.vo.UploadFileResultVO;
import com.zhushuai.zspicturebackend.model.vo.UploadPictureResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 文件上传服务
 */
@Service
@Slf4j
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    @Resource
    private TransferManager transferManager;


    public UploadFileResultVO uploadFile(MultipartFile file) {
        ThrowUtils.throwIf(file == null, ErrorCode.PARAMS_ERROR, "文件不能为空");

        try {
            // 获取文件后缀
            String originalName = file.getOriginalFilename();
            String suffix = "";

            if (originalName != null && originalName.contains(".")) {
                suffix = originalName.substring(originalName.lastIndexOf(".")).replace(".", "");
            }

            String bucket = cosClientConfig.getBucket();

            // 读取文件为字节数组（避免多次读取流）
            byte[] fileBytes = file.getBytes();


            // 日期路径
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

            // 生成唯一文件名
            String filename = UUID.randomUUID().toString().replace("-", "");

            String key = "file/" + datePath + "/" + filename + suffix;

            // ================= 文件上传 =================
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
            log.info("文件上传成功：{}", url);

            return new UploadFileResultVO(url,
                    originalName,
                    (long) fileBytes.length / FileConstant.ONE_MB,
                    suffix);

        } catch (IOException | InterruptedException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }
}
