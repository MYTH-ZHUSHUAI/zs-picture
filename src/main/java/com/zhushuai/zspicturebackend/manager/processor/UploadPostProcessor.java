package com.zhushuai.zspicturebackend.manager.processor;

import com.zhushuai.zspicturebackend.manager.model.UploadContext;

public interface UploadPostProcessor {

    void process(UploadContext context, byte[] imageBytes);
}