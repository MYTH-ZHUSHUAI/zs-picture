package com.zhushuai.zspicturebackend.model.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileResultVO {

    /**
     * 文件在COS中的url
     */
    private String fileUrl;

    /**
     * 文件在cod中的文件名称
     */
    private String fileName;

    /**
     * 文件体积
     */
    private Long fileSize;


    /**
     * 文件格式
     */
    private String fileFormat;
}
