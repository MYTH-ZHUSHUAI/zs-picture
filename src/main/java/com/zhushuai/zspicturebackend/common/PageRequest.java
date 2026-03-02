package com.zhushuai.zspicturebackend.common;

import lombok.Data;

@Data
public class PageRequest {

    /**
     * 当前页号
     */
    private long currentPage = 1;

    /**
     * 页面大小
     */
    private long pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认降序）
     */
    private String sortOrder = "descend";
}
