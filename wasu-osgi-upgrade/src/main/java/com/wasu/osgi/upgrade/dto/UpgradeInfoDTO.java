package com.wasu.osgi.upgrade.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author: glmx_
 * @date: 2024/8/26
 * @description:
 */
@Data
public class UpgradeInfoDTO implements Serializable {

    private Integer id;
    private String name;
    private String upgradeVersion;
    private Integer needPackage;
    private String startTime;
    private String endTime;
    private String upgradeStartTime;
    private String upgradeEndTime;
    private String rules;
    private String remark;
    private List<UpgradeFileDTO> fileList;
}
