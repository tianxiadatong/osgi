package com.wasu.service.gateway;

/**
 * @author: glmx_
 * @date: 2024/12/31
 * @description: DVB接口（DVB网关）
 */
public interface UGatewayDvb {

    /**
     * 获取指定频点的信号质量
     *
     * @param frequency：int类型，频点，单位Hz
     * @return String类型, jsonObject格式
     */

    String getSignalQuality(int frequency);

    /**
     * 搜索指定频点的节目
     *
     * @param frequency：int类型，频点，单位Hz
     * @param qam                     String类型，QAM设置，包括”QAM16”、”QAM64”、”QAM256”、”QAM512”
     * @return String类型, jsonObject格式
     */
    String searchFrequency(int frequency, String qam);
}
