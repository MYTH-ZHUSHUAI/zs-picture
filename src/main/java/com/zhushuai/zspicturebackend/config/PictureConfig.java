package com.zhushuai.zspicturebackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author zhushuai
 */
@ConfigurationProperties(prefix = "picture")
@Data
@Component
public class PictureConfig {

    private String backupDir;
}