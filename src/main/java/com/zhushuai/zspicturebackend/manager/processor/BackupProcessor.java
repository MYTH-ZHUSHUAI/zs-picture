package com.zhushuai.zspicturebackend.manager.processor;

import com.zhushuai.zspicturebackend.manager.model.UploadContext;
import com.zhushuai.zspicturebackend.config.PictureConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;

@Component
@Slf4j
public class BackupProcessor implements UploadPostProcessor {

    @Resource
    private PictureConfig pictureConfig;

    @Resource
    private ExecutorService pictureAsyncExecutor;

    @Override
    public void process(UploadContext context, byte[] imageBytes) {

        pictureAsyncExecutor.submit(() -> {

            try {

                String baseDir = pictureConfig.getBackupDir();

                String fileName = context.getOriginalKey()
                        .substring(context.getOriginalKey().lastIndexOf("/") + 1);

                File file = new File(baseDir, fileName);

                // 创建父目录
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(imageBytes);
                    fos.flush();
                }

                log.info("图片已备份到：{}", file.getAbsolutePath());

            } catch (Exception e) {

                log.error("图片备份失败", e);
            }

        });
    }
}
