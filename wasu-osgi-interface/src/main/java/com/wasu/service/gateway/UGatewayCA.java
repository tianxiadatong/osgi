package com.wasu.service.gateway;

/**
 * @author: glmx_
 * @date: 2024/12/31
 * @description: 网关CA信息接口(DVB网关)
 */
public interface UGatewayCA {

    /**
     * CA厂商
     * @return String类型
     */
    String getCAManufacturer();

    /**
     * CA序列号
     * @return String类型
     */
    String getCASN();

    /**
     * CA版本
     * @return String类型
     */
    String getCAVersion();
    /**
     * 智能卡号
     * @return String类型
     */
    String getCACard();

    /**
     * 观看级别
     * @return String类型
     */
    String getViewLevel();

    /**
     * 智能卡状态
     * @return int类型，0-没有智能卡 1-智能卡错误 2-智能卡运行中
     */
    int getStatus();

    /**
     * CA产品信息
     * @return String类型, jsonObject格式
     */
    String getProductInfo();
}
