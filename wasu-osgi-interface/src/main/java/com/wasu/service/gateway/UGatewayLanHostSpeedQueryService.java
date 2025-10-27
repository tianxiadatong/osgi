package com.wasu.service.gateway;

/**
 * @author: glmx_
 * @date: 2024/12/31
 * @description: 下挂设备实时速率查询类
 */
public interface UGatewayLanHostSpeedQueryService {

    /**
     * 获取下挂设备实时速率
     *
     * @param hostMac 下挂设备MAC，格式XX:XX: XX: XX: XX: XX
     * @return String类型, jsonObject格式
     */
    String getLANHostSpeed(String hostMac);

    /**
     * 获取下挂设备品牌/类型/MAC/IP/接入方式/定时监测实时流量/
     *
     * @return String类型, jsonObject格式
     */
    String subDeviceInfo();

    /**
     * 下挂设备应用使用记录统计, 学习/影音/通讯/资讯/游戏/其他的使用时长统计
     *
     * @return String类型, jsonObject格式
     */
    String getDeviceUsedStat();
}
