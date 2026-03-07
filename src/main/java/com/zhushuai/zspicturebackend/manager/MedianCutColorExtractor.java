package com.zhushuai.zspicturebackend.manager;

import com.zhushuai.zspicturebackend.exception.ErrorCode;
import com.zhushuai.zspicturebackend.exception.ThrowUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author zhushuai
 */
public class MedianCutColorExtractor {

    private static final int SAMPLE_SIZE = 100;

    /**
     * 提取图片主色调（RGB 数组）
     * 
     * @param imageBytes 图片字节数组
     * @return RGB 数组 [r, g, b]
     */
    public static int[] extractDominantColor(byte[] imageBytes) {

        try {

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));

            BufferedImage small = resize(image);

            List<int[]> pixels = collectPixels(small);

            List<ColorBox> boxes = new ArrayList<>();
            boxes.add(new ColorBox(pixels));

            while (boxes.size() < 4) {

                ColorBox box = boxes.stream()
                        .max(Comparator.comparingInt(ColorBox::getVolume))
                        .orElse(null);

                if (box == null) break;

                boxes.remove(box);

                boxes.addAll(box.split());
            }

            ColorBox largest = boxes.stream()
                    .max(Comparator.comparingInt(ColorBox::size))
                    .orElse(null);


            ThrowUtils.throwIf(largest == null, ErrorCode.SYSTEM_ERROR, "获取图片主色调失败");
            return largest.getAverageColor();

        } catch (Exception e) {

            throw new RuntimeException("提取图片主色调失败", e);
        }
    }

    private static BufferedImage resize(BufferedImage image) {

        BufferedImage small = new BufferedImage(SAMPLE_SIZE, SAMPLE_SIZE, BufferedImage.TYPE_INT_RGB);

        small.getGraphics().drawImage(image, 0, 0, SAMPLE_SIZE, SAMPLE_SIZE, null);

        return small;
    }

    private static List<int[]> collectPixels(BufferedImage image) {

        List<int[]> pixels = new ArrayList<>();

        for (int y = 0; y < image.getHeight(); y++) {

            for (int x = 0; x < image.getWidth(); x++) {

                int rgb = image.getRGB(x, y);

                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                if (r > 240 && g > 240 && b > 240) continue;

                pixels.add(new int[]{r, g, b});
            }
        }

        return pixels;
    }

    static class ColorBox {

        List<int[]> pixels;

        ColorBox(List<int[]> pixels) {
            this.pixels = pixels;
        }

        int size() {
            return pixels.size();
        }

        int getVolume() {

            int rMin = 255, rMax = 0;
            int gMin = 255, gMax = 0;
            int bMin = 255, bMax = 0;

            for (int[] p : pixels) {

                rMin = Math.min(rMin, p[0]);
                rMax = Math.max(rMax, p[0]);

                gMin = Math.min(gMin, p[1]);
                gMax = Math.max(gMax, p[1]);

                bMin = Math.min(bMin, p[2]);
                bMax = Math.max(bMax, p[2]);
            }

            return (rMax - rMin) + (gMax - gMin) + (bMax - bMin);
        }

        List<ColorBox> split() {

            pixels.sort(Comparator.comparingInt(a -> a[0]));

            int mid = pixels.size() / 2;

            List<int[]> left = pixels.subList(0, mid);
            List<int[]> right = pixels.subList(mid, pixels.size());

            List<ColorBox> list = new ArrayList<>();

            list.add(new ColorBox(new ArrayList<>(left)));
            list.add(new ColorBox(new ArrayList<>(right)));

            return list;
        }

        int[] getAverageColor() {

            long r = 0, g = 0, b = 0;

            for (int[] p : pixels) {

                r += p[0];
                g += p[1];
                b += p[2];
            }

            int size = pixels.size();

            return new int[]{
                    (int) (r / size),
                    (int) (g / size),
                    (int) (b / size)
            };
        }
    }
}
