package com.zhushuai.zspicturebackend.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferManagerConfiguration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 腾讯云对象存储客户端
 * 可以实现自动获取secretId、secretKey
 */
@Configuration
@ConfigurationProperties(prefix = "cos.client")
@Data
public class CosClientConfig {

    /**
     * secretId
     */
    private String secretId;

    /**
     * secretKey
     */
    private String secretKey;

    /**
     * 区域
     */
    private String region;

    /**
     * 桶名
     */
    private String bucket;

    /**
     * 桶名
     */
    private String host;

    /**
     * CI 服务域名（用于数据万象 API）
     */
    private String ciHost;

    @Bean
    public COSClient cosClient() {

        // 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);

        // 设置bucket的区域
        ClientConfig clientConfig = new ClientConfig(new Region(region));

        // 生成cos客户端
        return new COSClient(cred, clientConfig);

    }

    @Bean(destroyMethod = "shutdownNow")
    public TransferManager transferManager(COSClient cosClient) {

        ExecutorService threadPool = Executors.newFixedThreadPool(16);

        TransferManager transferManager = new TransferManager(cosClient, threadPool);

        TransferManagerConfiguration configuration = new TransferManagerConfiguration();

        // 5MB 分片阈值
        configuration.setMultipartUploadThreshold(5 * 1024 * 1024);

        // 每片最小 5MB
        configuration.setMinimumUploadPartSize(5 * 1024 * 1024);

        transferManager.setConfiguration(configuration);

        return transferManager;
    }

    /**
     * 异步任务执行器（用于保存图片到本地等）
     */
    @Bean(name = "pictureAsyncExecutor")
    public ExecutorService asyncExecutor() {
        return Executors.newFixedThreadPool(10);
    }
}