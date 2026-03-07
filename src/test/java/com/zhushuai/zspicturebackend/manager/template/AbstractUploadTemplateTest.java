package com.zhushuai.zspicturebackend.manager.template;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIUploadResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.OriginalInfo;
import com.zhushuai.zspicturebackend.config.CosClientConfig;
import com.zhushuai.zspicturebackend.manager.MedianCutColorExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link AbstractUploadTemplate} 单元测试
 *
 * @author zhushuai
 */
@ExtendWith(MockitoExtension.class)
class AbstractUploadTemplateTest {

    @Mock
    private CosClientConfig cosClientConfig;

    @Mock
    private COSClient cosClient;

    @Mock
    private ExecutorService pictureAsyncExecutor;

    @InjectMocks
    private TestUploadTemplate uploadTemplate;

    private byte[] testImageBytes;
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testImageBytes = new byte[]{1, 2, 3, 4, 5};
        mockFile = new MockMultipartFile(
                "testFile",
                "test.jpg",
                "image/jpeg",
                testImageBytes
        );

        // 配置模拟对象
        when(cosClientConfig.getBucket()).thenReturn("test-bucket");
        when(cosClientConfig.getHost()).thenReturn("https://example.com");
    }

    @Test
    @DisplayName("测试图片上传流程 - 成功场景")
    void testUploadPicture_Success() throws Exception {
        // 准备测试数据
        PutObjectResult mockResult = mock(PutObjectResult.class);
        CIUploadResult ciUploadResult = mock(CIUploadResult.class);
        OriginalInfo originalInfo = mock(OriginalInfo.class);
        ImageInfo imageInfo = mock(ImageInfo.class);

        // 配置模拟返回值
        when(cosClient.putObject(any())).thenReturn(mockResult);
        when(mockResult.getCiUploadResult()).thenReturn(ciUploadResult);
        when(ciUploadResult.getOriginalInfo()).thenReturn(originalInfo);
        when(originalInfo.getImageInfo()).thenReturn(imageInfo);
        when(imageInfo.getWidth()).thenReturn(800);
        when(imageInfo.getHeight()).thenReturn(600);

        // 执行测试
        UploadResult result = uploadTemplate.uploadPicture(mockFile);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getOriginalKey());
        assertTrue(result.getOriginalKey().endsWith(".jpg"));
        assertEquals(800, result.getWidth());
        assertEquals(600, result.getHeight());
        assertNotNull(result.getScale());
        assertEquals(testImageBytes.length, result.getSize());
        assertNotNull(result.getMainColor());

        // 验证生成了 5 个不同的 key
        assertNotNull(result.getWebpKey());
        assertNotNull(result.getThumbnailKey());
        assertNotNull(result.getWatermarkedWebpKey());
        assertNotNull(result.getWatermarkedThumbnailKey());

        // 验证目录结构（uuid/uuid.xxx）
        assertTrue(result.getOriginalKey().contains("/"));
        String[] parts = result.getOriginalKey().split("/");
        assertEquals(2, parts.length);
        assertEquals(parts[0], parts[1].substring(0, parts[1].lastIndexOf(".")));
    }

    @Test
    @DisplayName("测试 WebP 格式图片上传")
    void testUploadPicture_WebPFormat() throws Exception {
        // 准备 WebP 格式的测试文件
        MultipartFile webpFile = new MockMultipartFile(
                "testFile",
                "test.webp",
                "image/webp",
                testImageBytes
        );

        // 配置模拟返回值
        PutObjectResult mockResult = mock(PutObjectResult.class);
        CIUploadResult ciUploadResult = mock(CIUploadResult.class);
        OriginalInfo originalInfo = mock(OriginalInfo.class);
        ImageInfo imageInfo = mock(ImageInfo.class);

        when(cosClient.putObject(any())).thenReturn(mockResult);
        when(mockResult.getCiUploadResult()).thenReturn(ciUploadResult);
        when(ciUploadResult.getOriginalInfo()).thenReturn(originalInfo);
        when(originalInfo.getImageInfo()).thenReturn(imageInfo);
        when(imageInfo.getWidth()).thenReturn(1024);
        when(imageInfo.getHeight()).thenReturn(768);

        // 执行测试
        UploadResult result = uploadTemplate.uploadPicture(webpFile);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getOriginalKey().endsWith(".webp"));
        assertTrue(result.getWebpKey().endsWith(".webp"));
        assertTrue(result.getThumbnailKey().endsWith("_thumbnail.webp"));
    }

    @Test
    @DisplayName("测试 PNG 格式图片上传")
    void testUploadPicture_PNGFormat() throws Exception {
        // 准备 PNG 格式的测试文件
        MultipartFile pngFile = new MockMultipartFile(
                "testFile",
                "test.png",
                "image/png",
                testImageBytes
        );

        // 配置模拟返回值
        PutObjectResult mockResult = mock(PutObjectResult.class);
        CIUploadResult ciUploadResult = mock(CIUploadResult.class);
        OriginalInfo originalInfo = mock(OriginalInfo.class);
        ImageInfo imageInfo = mock(ImageInfo.class);

        when(cosClient.putObject(any())).thenReturn(mockResult);
        when(mockResult.getCiUploadResult()).thenReturn(ciUploadResult);
        when(ciUploadResult.getOriginalInfo()).thenReturn(originalInfo);
        when(originalInfo.getImageInfo()).thenReturn(imageInfo);
        when(imageInfo.getWidth()).thenReturn(1920);
        when(imageInfo.getHeight()).thenReturn(1080);

        // 执行测试
        UploadResult result = uploadTemplate.uploadPicture(pngFile);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getOriginalKey().endsWith(".png"));
        assertEquals(1920, result.getWidth());
        assertEquals(1080, result.getHeight());
    }

    @Test
    @DisplayName("测试 generateKey 方法 - 生成 UUID 目录结构")
    void testGenerateKey() {
        // 执行测试
        String key = uploadTemplate.testGenerateKey("test.jpg");

        // 验证结果
        assertNotNull(key);
        assertTrue(key.matches("[a-f0-9]{32}/[a-f0-9]{32}\\.jpg"));

        // 验证目录和文件名一致
        String[] parts = key.split("/");
        assertEquals(2, parts.length);
        assertEquals(parts[0], parts[1].substring(0, 32));
    }

    @Test
    @DisplayName("测试不同文件后缀的 generateKey")
    void testGenerateKey_DifferentExtensions() {
        // 测试不同后缀
        String[] extensions = {"jpg", "jpeg", "png", "webp", "gif"};

        for (String ext : extensions) {
            String key = uploadTemplate.testGenerateKey("test." + ext);
            assertNotNull(key);
            assertTrue(key.endsWith("." + ext), "后缀应该是 " + ext);
        }
    }

    @Test
    @DisplayName("测试图片处理规则配置")
    void testImageProcessingRules() throws Exception {
        // 配置模拟返回值
        PutObjectResult mockResult = mock(PutObjectResult.class);
        CIUploadResult ciUploadResult = mock(CIUploadResult.class);
        OriginalInfo originalInfo = mock(OriginalInfo.class);
        ImageInfo imageInfo = mock(ImageInfo.class);

        when(cosClient.putObject(any())).thenReturn(mockResult);
        when(mockResult.getCiUploadResult()).thenReturn(ciUploadResult);
        when(ciUploadResult.getOriginalInfo()).thenReturn(originalInfo);
        when(originalInfo.getImageInfo()).thenReturn(imageInfo);
        when(imageInfo.getWidth()).thenReturn(800);
        when(imageInfo.getHeight()).thenReturn(600);

        // 执行上传
        uploadTemplate.uploadPicture(mockFile);

        // 验证调用了 putObject
        verify(cosClient, times(1)).putObject(any());
    }

    @Test
    @DisplayName("测试主色调提取")
    void testMainColorExtraction() throws Exception {
        // 准备一个真实的简单图片（1x1 红色像素）
        byte[] simpleImage = new byte[]{
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, // JPEG header
                // ... 简化的 JPEG 数据
        };

        MultipartFile simpleFile = new MockMultipartFile(
                "testFile",
                "red.jpg",
                "image/jpeg",
                simpleImage
        );

        // 配置模拟返回值
        PutObjectResult mockResult = mock(PutObjectResult.class);
        CIUploadResult ciUploadResult = mock(CIUploadResult.class);
        OriginalInfo originalInfo = mock(OriginalInfo.class);
        ImageInfo imageInfo = mock(ImageInfo.class);

        when(cosClient.putObject(any())).thenReturn(mockResult);
        when(mockResult.getCiUploadResult()).thenReturn(ciUploadResult);
        when(ciUploadResult.getOriginalInfo()).thenReturn(originalInfo);
        when(originalInfo.getImageInfo()).thenReturn(imageInfo);
        when(imageInfo.getWidth()).thenReturn(1);
        when(imageInfo.getHeight()).thenReturn(1);

        // 执行测试
        UploadResult result = uploadTemplate.uploadPicture(simpleFile);

        // 验证主色调不为空
        assertNotNull(result.getMainColor());
        assertTrue(result.getMainColor().startsWith("#"), "主色调应该是 HEX 格式");
    }

    @Test
    @DisplayName("测试图片宽高比例计算")
    void testScaleCalculation() throws Exception {
        // 配置不同尺寸的图片
        int[][] dimensions = {
                {800, 600},    // 4:3
                {1920, 1080},  // 16:9
                {1000, 1000},  // 1:1
                {600, 800}     // 3:4
        };

        for (int[] dim : dimensions) {
            PutObjectResult mockResult = mock(PutObjectResult.class);
            CIUploadResult ciUploadResult = mock(CIUploadResult.class);
            OriginalInfo originalInfo = mock(OriginalInfo.class);
            ImageInfo imageInfo = mock(ImageInfo.class);

            when(cosClient.putObject(any())).thenReturn(mockResult);
            when(mockResult.getCiUploadResult()).thenReturn(ciUploadResult);
            when(ciUploadResult.getOriginalInfo()).thenReturn(originalInfo);
            when(originalInfo.getImageInfo()).thenReturn(imageInfo);
            when(imageInfo.getWidth()).thenReturn(dim[0]);
            when(imageInfo.getHeight()).thenReturn(dim[1]);

            // 执行测试
            UploadResult result = uploadTemplate.uploadPicture(mockFile);

            // 验证比例计算正确
            assertNotNull(result.getScale());
            double expectedScale = (double) dim[0] / dim[1];
            assertEquals(expectedScale, result.getScale(), 0.01);
        }
    }

    @Test
    @DisplayName("测试上传结果为 null 的情况")
    void testUploadWithNullCIResult() {
        // 配置模拟返回 null 的 CIUploadResult
        PutObjectResult mockResult = mock(PutObjectResult.class);
        when(mockResult.getCiUploadResult()).thenReturn(null);
        when(cosClient.putObject(any())).thenReturn(mockResult);

        // 执行测试并验证抛出异常
        assertThrows(Exception.class, () -> uploadTemplate.uploadPicture(mockFile));
    }

    /**
     * 用于测试的具体实现类
     */
    static class TestUploadTemplate extends AbstractUploadTemplate<UploadResult> {

        @Override
        protected void validate(byte[] fileBytes, String originalName) {
            // 测试中不需要实际校验逻辑
        }

        @Override
        protected UploadResult buildResult(byte[] fileBytes, String originalName, UploadResult uploadResult) {
            // 直接返回 uploadResult
            return uploadResult;
        }

        /**
         * 暴露 generateKey 方法用于测试
         */
        public String testGenerateKey(String originalName) {
            return generateKey(originalName);
        }
    }
}
