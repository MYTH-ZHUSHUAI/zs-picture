package com.zhushuai.zspicturebackend.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhushuai.zspicturebackend.exception.BusinessException;
import com.zhushuai.zspicturebackend.model.entity.Picture;
import com.zhushuai.zspicturebackend.model.vo.PictureVO;
import com.zhushuai.zspicturebackend.service.PictureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link PictureService#getPictureListByColor(String)} 集成测试
 *
 * @author zhushuai
 */
@SpringBootTest
class PictureServiceColorSearchTest {

    @Autowired
    private PictureService pictureService;

    private String testColorHex;

    @BeforeEach
    void setUp() {
        // 准备测试颜色（橙红色）
        testColorHex = "#FF5733";
    }

    @Test
    @DisplayName("根据颜色搜索图片 - 基本功能测试")
    void testGetPictureListByColor_Basic() {
        // 执行查询
        Page<PictureVO> result = pictureService.getPictureListByColor(testColorHex);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCurrent());
        assertEquals(10, result.getSize());
        
        List<PictureVO> records = result.getRecords();
        assertNotNull(records);
        
        // 如果数据库中有数据，验证排序
        if (!records.isEmpty()) {
            assertTrue(records.size() <= 10, "最多返回 10 张图片");
            
            // 验证返回的图片都有主色调
            for (PictureVO picture : records) {
                assertNotNull(picture.getMainColor(), "返回的图片应该有主色调");
                assertTrue(picture.getMainColor().startsWith("#"), "主色调应该是 HEX 格式");
            }
        }
    }

    @Test
    @DisplayName("根据颜色搜索图片 - 空颜色测试")
    void testGetPictureListByColor_EmptyColor() {
        // 测试空字符串
        assertThrows(BusinessException.class, () -> {
            pictureService.getPictureListByColor("");
        });

        // 测试 null
        assertThrows(BusinessException.class, () -> {
            pictureService.getPictureListByColor(null);
        });

        // 测试空白字符
        assertThrows(BusinessException.class, () -> {
            pictureService.getPictureListByColor("   ");
        });
    }

    @Test
    @DisplayName("根据颜色搜索图片 - 无效颜色格式测试")
    void testGetPictureListByColor_InvalidColorFormat() {
        // 测试没有#前缀
        assertThrows(BusinessException.class, () -> {
            pictureService.getPictureListByColor("FF5733");
        });

        // 测试长度不对
        assertThrows(BusinessException.class, () -> {
            pictureService.getPictureListByColor("#FFF");
        });

        // 测试包含无效字符
        assertThrows(BusinessException.class, () -> {
            pictureService.getPictureListByColor("#GGGGGG");
        });
    }

    @Test
    @DisplayName("根据颜色搜索图片 - 不同颜色测试")
    void testGetPictureListByColor_DifferentColors() {
        // 测试不同的有效颜色
        String[] validColors = {
            "#FF0000", // 红色
            "#00FF00", // 绿色
            "#0000FF", // 蓝色
            "#FFFFFF", // 白色
            "#000000", // 黑色
            "#FF5733"  // 橙红色
        };

        for (String color : validColors) {
            Page<PictureVO> result = pictureService.getPictureListByColor(color);
            assertNotNull(result, "颜色 " + color + " 的查询结果不应为 null");
            assertEquals(1, result.getCurrent());
            assertEquals(10, result.getSize());
        }
    }

    @Test
    @DisplayName("RGB 距离计算测试")
    void testRgbDistanceCalculation() {
        // 相同颜色距离为 0
        double sameColorDistance = calculateDistance("#FF5733", "#FF5733");
        assertEquals(0, sameColorDistance, 0.01);

        // 红色和绿色的距离应该较大
        double redGreenDistance = calculateDistance("#FF0000", "#00FF00");
        assertTrue(redGreenDistance > 0);

        // 相近颜色距离应该较小
        double similarColorDistance = calculateDistance("#FF5733", "#FF5834");
        double differentColorDistance = calculateDistance("#FF5733", "#000000");
        assertTrue(similarColorDistance < differentColorDistance, 
                "相近颜色的距离应该小于差异大的颜色");
    }

    @Test
    @DisplayName("颜色相似度排序测试")
    void testColorSimilaritySorting() {
        // 目标颜色
        String targetColor = "#FF5733";
        
        // 执行查询
        Page<PictureVO> result = pictureService.getPictureListByColor(targetColor);
        
        // 如果有多个结果，验证排序
        if (result.getRecords().size() > 1) {
            List<PictureVO> pictures = result.getRecords();
            
            // 计算每张图片与目标颜色的距离
            double previousDistance = -1;
            for (PictureVO picture : pictures) {
                double currentDistance = calculateDistance(targetColor, picture.getMainColor());
                
                // 距离应该递增（相似度递减）
                if (previousDistance >= 0) {
                    assertTrue(currentDistance >= previousDistance - 0.01, 
                            "图片应该按颜色距离升序排列");
                }
                previousDistance = currentDistance;
            }
        }
    }

    /**
     * 辅助方法：计算两个颜色的 RGB 距离
     */
    private double calculateDistance(String hex1, String hex2) {
        try {
            int r1 = Integer.parseInt(hex1.substring(1, 3), 16);
            int g1 = Integer.parseInt(hex1.substring(3, 5), 16);
            int b1 = Integer.parseInt(hex1.substring(5, 7), 16);

            int r2 = Integer.parseInt(hex2.substring(1, 3), 16);
            int g2 = Integer.parseInt(hex2.substring(3, 5), 16);
            int b2 = Integer.parseInt(hex2.substring(5, 7), 16);

            int dr = r1 - r2;
            int dg = g1 - g2;
            int db = b1 - b2;

            return dr * dr + dg * dg + db * db;
        } catch (Exception e) {
            return Double.MAX_VALUE;
        }
    }
}
