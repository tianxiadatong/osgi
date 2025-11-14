package com.wasu.service.gateway;

/**
 * @author: glmx_
 * @date: 2024/12/31
 * @description:
 */
public interface UGatewayDeviceInfo {

    /**
     * 生产厂商
     * @return String类型
     */
    String getManufacturer();

    /**
     * OUI
     * @return String类型
     */
    String getOUI();

    /**
     * 设备型号
     * @return String类型
     */
    String getModel();

    /**
     * 设备类型
     * @return String类型
     */
    String getType();

    /**
     * 网关SN
     * @return String类型
     */
    String getSN();

    /**
     * 收发报文统计接口
     * @return String类型, jsonObject格式
     */
    String queryWiFiStats();
}
