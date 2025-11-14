package com.wasu.service.gateway;

/**
 * @author: glmx_
 * @date: 2024/12/31
 * @description: LAN侧主机服务
 */
public interface UGatewayLan {
    /**
     * 获取LAN侧主机列表
     * @return 返回值:String类型,jsonObject格式
     */
    String getHostList();

    /**
     * 获取LAN设备接入方式
     *
     * @return Integer;	1中继、2 D-HCP、3-PPPOE拨号（宽带账号）
     */
    Integer getLanLinkType();

    /**
     * 获取黑名单列表
     *
     * @return String类型, jsonObject格式
     */
    String getHostBlackList();

    /**
     * 设置黑名单
     *
     * @param mac     String类型主机mac
     * @param isBlack boolean类型是否加入黑名单 true-加入 false-移除
     * @return boolean设置是否成功 true-设置成功 false-设置失败
     */
    Boolean setHostBlack(String mac, boolean isBlack);
}
