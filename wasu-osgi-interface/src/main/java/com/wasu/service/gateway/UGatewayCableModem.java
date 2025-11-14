package com.wasu.service.gateway;

/**
 * @author: glmx_
 * @date: 2024/12/31
 * @description: 网关CableModem信息接口(DVB网关)
 */
public interface UGatewayCableModem {

    /**
     * 下行通道个数（DVB网关）
     * @return int类型
     */
    int getCMDownstreamNumber();

    /**
     * 下行通道信息（DVB网关）
     * @return String类型, jsonObject格式
     */
    String getCMDownstreamInfo();

    /**
     * CM信息
     * @return String类型, jsonObject格式
     */
    int getCMInfo();
}
