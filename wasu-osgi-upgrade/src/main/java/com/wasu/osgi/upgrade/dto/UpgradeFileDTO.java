package com.wasu.osgi.upgrade.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: glmx_
 * @date: 2024/9/2
 * @description:
 */
@Data
public class UpgradeFileDTO implements Serializable {
    private String name;
    private String model;
    private String version;
    private String url;
    private String md5;
    private Integer fileLength;
    private String contentType;
}
