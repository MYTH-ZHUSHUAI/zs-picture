package com.zhushuai.zspicturebackend.manager;

import com.zhushuai.zspicturebackend.exception.ErrorCode;
import com.zhushuai.zspicturebackend.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 主色调提取（Median Cut 算法，纯 Java 实现，支持所有平台）
 *
 * @author zhushuai
 */
@Slf4j
@Component
public class MedianCutColorExtractor {

    private static final int TARGET_COLORS = 5;
    private static final int SAMPLE_SIZE = 150;

    static {
        // 确保 ImageIO 插件已注册
        ImageIO.scanForPlugins();
    }

    /**
     * 提取主色调（HEX格式）
     */
    public static String extractDominantColor(byte[] imageBytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            ThrowUtils.throwIf(image == null, ErrorCode.PARAMS_ERROR, "无法读取图片，格式不支持");

            List<Color> pixels = samplePixels(image);
            ThrowUtils.throwIf(pixels.isEmpty(), ErrorCode.SYSTEM_ERROR, "图片像素为空");

            List<ColorBucket> buckets = medianCut(pixels, TARGET_COLORS);

            ColorBucket dominantBucket = buckets.stream()
                    .max(Comparator.comparingInt(b -> b.pixels.size()))
                    .orElse(null);

            ThrowUtils.throwIf(dominantBucket == null, ErrorCode.SYSTEM_ERROR, "获取图片主色调失败");

            Color avgColor = dominantBucket.getAverageColor();
            String hexColor = rgbToHex(avgColor);

            log.info("提取主色调成功: {}", hexColor);
            return hexColor;

        } catch (Exception e) {
            log.error("提取图片主色调失败", e);
            throw new RuntimeException("提取图片主色调失败: " + e.getMessage());
        }
    }

    /**
     * 提取多个主色调
     */
    public static List<ColorInfo> extractDominantColors(byte[] imageBytes, int colorCount) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            ThrowUtils.throwIf(image == null, ErrorCode.PARAMS_ERROR, "无法读取图片");

            List<Color> pixels = samplePixels(image);
            ThrowUtils.throwIf(pixels.isEmpty(), ErrorCode.SYSTEM_ERROR, "图片像素为空");

            List<ColorBucket> buckets = medianCut(pixels, colorCount);

            return buckets.stream()
                    .map(bucket -> new ColorInfo(
                            rgbToHex(bucket.getAverageColor()),
                            (double) bucket.pixels.size() / pixels.size()
                    ))
                    .sorted((a, b) -> Double.compare(b.percentage, a.percentage))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("提取图片主色调失败", e);
            throw new RuntimeException("提取图片主色调失败", e);
        }
    }

    /**
     * 采样图片像素
     */
    private static List<Color> samplePixels(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int xStep = Math.max(1, width / SAMPLE_SIZE);
        int yStep = Math.max(1, height / SAMPLE_SIZE);

        List<Color> pixels = new ArrayList<>();

        for (int y = 0; y < height; y += yStep) {
            for (int x = 0; x < width; x += xStep) {
                int rgb = image.getRGB(x, y);

                // 处理透明度
                int alpha = (rgb >> 24) & 0xff;
                if (alpha < 128) continue;

                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                // 跳过接近纯白背景
                if (r > 245 && g > 245 && b > 245) continue;

                pixels.add(new Color(r, g, b));
            }
        }

        return pixels;
    }

    /**
     * Median Cut 算法
     */
    private static List<ColorBucket> medianCut(List<Color> pixels, int targetCount) {
        PriorityQueue<ColorBucket> queue = new PriorityQueue<>(
                Comparator.comparingInt(b -> -b.getVolume())
        );

        queue.offer(new ColorBucket(pixels));

        while (queue.size() < targetCount && queue.size() < pixels.size()) {
            ColorBucket bucket = queue.poll();
            if (bucket == null || bucket.getVolume() == 0) break;

            List<ColorBucket> split = bucket.split();
            queue.addAll(split);
        }

        return new ArrayList<>(queue);
    }

    /**
     * RGB 转 HEX
     */
    private static String rgbToHex(Color color) {
        return String.format("#%02X%02X%02X", color.r, color.g, color.b);
    }

    /**
     * 颜色类
     */
    static class Color {
        int r, g, b;

        Color(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        int getComponent(int index) {
            return switch (index) {
                case 0 -> r;
                case 1 -> g;
                case 2 -> b;
                default -> throw new IllegalArgumentException("Invalid index: " + index);
            };
        }
    }

    /**
     * 颜色桶类
     */
    static class ColorBucket {
        List<Color> pixels;
        int rMin, rMax, gMin, gMax, bMin, bMax;

        ColorBucket(List<Color> pixels) {
            this.pixels = new ArrayList<>(pixels);
            computeBounds();
        }

        void computeBounds() {
            rMin = gMin = bMin = 255;
            rMax = gMax = bMax = 0;

            for (Color p : pixels) {
                rMin = Math.min(rMin, p.r);
                rMax = Math.max(rMax, p.r);
                gMin = Math.min(gMin, p.g);
                gMax = Math.max(gMax, p.g);
                bMin = Math.min(bMin, p.b);
                bMax = Math.max(bMax, p.b);
            }
        }

        int getLongestDimension() {
            int rRange = rMax - rMin;
            int gRange = gMax - gMin;
            int bRange = bMax - bMin;

            if (rRange >= gRange && rRange >= bRange) return 0;
            if (gRange >= rRange && gRange >= bRange) return 1;
            return 2;
        }

        int getVolume() {
            return (rMax - rMin) + (gMax - gMin) + (bMax - bMin);
        }

        List<ColorBucket> split() {
            int dim = getLongestDimension();
            final int sortDim = dim;

            pixels.sort(Comparator.comparingInt(c -> c.getComponent(sortDim)));

            int mid = pixels.size() / 2;

            // 确保分割点两侧都有数据
            while (mid > 0 && mid < pixels.size() - 1 &&
                   pixels.get(mid).getComponent(dim) == pixels.get(mid - 1).getComponent(dim)) {
                mid++;
            }

            List<Color> left = pixels.subList(0, mid);
            List<Color> right = pixels.subList(mid, pixels.size());

            if (left.isEmpty() || right.isEmpty()) {
                mid = pixels.size() / 2;
                left = pixels.subList(0, mid);
                right = pixels.subList(mid, pixels.size());
            }

            List<ColorBucket> result = new ArrayList<>();
            result.add(new ColorBucket(left));
            result.add(new ColorBucket(right));
            return result;
        }

        Color getAverageColor() {
            long rSum = 0, gSum = 0, bSum = 0;

            for (Color p : pixels) {
                rSum += p.r;
                gSum += p.g;
                bSum += p.b;
            }

            int count = pixels.size();
            return new Color(
                    (int) (rSum / count),
                    (int) (gSum / count),
                    (int) (bSum / count)
            );
        }
    }

    /**
     * 颜色信息类
     */
    public static class ColorInfo {
        public final String hex;
        public final double percentage;

        public ColorInfo(String hex, double percentage) {
            this.hex = hex;
            this.percentage = percentage;
        }

        @Override
        public String toString() {
            return String.format("%s (%.1f%%)", hex, percentage * 100);
        }
    }
}
