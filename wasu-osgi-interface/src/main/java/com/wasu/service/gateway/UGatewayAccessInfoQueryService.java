package com.wasu.service.gateway;

/**
 * @author: glmx_
 * @date: 2024/12/31
 * @description: 接入信息查询类
 */
public interface UGatewayAccessInfoQueryService {

    /**
     * 查询PON口的物理状态
     *
     * @return String类型 , jsonObject格式
     */
    String getPONIfPhyStatus();

    /**
     * 查询WAN口上下行速率
     *
     * @param wanIndex WAN连接的序号
     * @return String类型, jsonObject格式
     */
    String getWanIfBandwidth(int wanIndex);

    /**
     * 查询WAN口上下行流量
     *
     * @return String类型, jsonObject格式
     */
    String getWANInfo();

    /**
     * 查询WAN口状态
     *
     * @param wanIndex WAN连接的序号
     * @return String类型, jsonObject格式
     */
    String getWANIfStatus(int wanIndex);

    /**
     * 上下联协商速率
     *
     * @return String类型, jsonObject格式
     */
    String getRelaionSpeed();

    /**
     * 业务宽带账号
     *
     * @return String类型
     */
    String getAccount();

    /**
     * 业务宽带订购速率
     *
     * @return Integer类型
     */
    Integer getBookSpeed();

    /**
     * 业务接入方式
     *
     * @return String类型
     */
    String getLineType();

    /**
     * 设备在线时长
     *
     * @return long类型
     */
    long deviceOnlineTime();

    /**
     * 获取设备上线时间
     *
     * @return long类型
     */
    long getDeviceConnectTime();

    /**
     * 设备离线时间
     *
     * @return long类型
     */
    long getDeviceOffTime();

    /**
     * 查询最大接入数
     *
     * @return int类型
     */
    int getDeviceMaxSub();

    /**
     * 设置最大接入数
     *
     * @param max: 最大值
     * @return int类型
     */
    Integer setDeviceMaxSub(Integer max);
}
