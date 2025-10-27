package com.wasu.service.gateway;

/**
 * @author: glmx_
 * @date: 2024/12/31
 * @description: 下挂设备限速类
 */
public interface UGatewayLanHostSpeedLimitService {

    /**
     * 设置下挂设备限速
     *
     * @param hostMac   下挂设备MAC，格式XX:XX: XX: XX: XX: XX
     * @param upSpeed   上行速率，单位Kbps。0表示不限速。
     * @param downSpeed 下行速率，单位Kbps。0表示不限速。
     * @return int类型， 0 成功，-1 失败
     */
    String setLANHostSpeedLimit(String hostMac, int upSpeed, int downSpeed);
}
