package com.zhushuai.zspicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.zhushuai.zspicturebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class ZsPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZsPictureBackendApplication.class, args);
    }

}
