package com.wasu.service.gateway;

/**
 * @author: glmx_
 * @date: 2024/12/31
 * @description:
 */
public interface UGatewayStorage {

    /**
     * 获取系统内部存储路径
     *
     * @return String类型
     */
    String getInternalStoragePath();

    /**
     * 获取系统外部存储路径
     *
     * @return String类型
     */
    String getExternalStorageDirectory();

    /**
     * 获取系统内部存储空间
     *
     * @return long 类型，单位字节
     */
    long getInternalFreeStorage();

    /**
     * 获取系统外部存储空间
     *
     * @return long 类型，单位字节
     */
    long getExternalFreeStorage();
}
