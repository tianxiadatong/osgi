package com.wasu.service.gateway;

/**
 * @author: glmx_
 * @date: 2024/12/31
 * @description:
 */
public interface UGatewayHardwareInfo {

    /**
     * 主芯片型号
     *
     * @return String类型
     */
    String getChipModel();

    /**
     * 硬件型号
     *
     * @return String类型
     */
    String getHardwareModel();

    /**
     * 硬件版本
     *
     * @return String类型
     */
    String getHardwareVersion();

    /**
     * 光模块温度
     *
     * @return Integer类型
     */
    Integer getLMTemp();

    /**
     * RAM
     *
     * @return Integer类型
     */
    Integer getRAM();

    /**
     * ROM
     *
     * @return Integer类型
     */
    Integer getROM();

    /**
     * 设备MAC
     *
     * @return String类型
     */
    String getMAC();

}
