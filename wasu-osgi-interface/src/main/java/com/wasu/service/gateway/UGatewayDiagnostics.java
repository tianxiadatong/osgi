package com.wasu.service.gateway;

/**
 * @author: glmx_
 * @date: 2024/12/31
 * @description: 网络诊断接口
 */
public interface UGatewayDiagnostics {

    /**
     * 开始Ping诊断
     *
     * @return int类型
     */
    int setPingParameter(String host, String wanIndex, int numberOfRepetitions, int timeout, int dataBlockSize);

    /**
     * 获取Ping诊断结果
     *
     * @return String类型, jsonObject格式
     */
    String getPingResult();

    /**
     * 开始traceroute诊断
     *
     * @return int类型
     */
    int setTracerouteParameter(String host, String wanIndex, int numberOfTries, int timeout, int dataBlockSize, int maxHopCount);

    /**
     * 获取traceroute诊断结果
     *
     * @return String类型, jsonObject格式
     */
    String getTracerouteResult();

    /**
     * 获取外网流量情况
     *
     * @return String类型，jsonObject格式
     */
    String getOutsideNetworkTraffic();

    /**
     * 开始wifi干扰诊断
     *
     * @return int类型
     */
    int startWiFiScan();

    /**
     * 获取wifi干扰诊断结果
     *
     * @return String类型, jsonObject格式
     */
    String getWiFiScanResult();

    /**
     * 开始测试诊断
     *
     * @return int类型; 1调用成功，0失败
     */
    int startSpeedTest();

    /**
     * 获取诊断结果
     *
     * @return String类型, jsonObject格式
     */
    String startSpeedTestResult();

    /**
     * 开始网络抖动诊断
     *
     * @return int类型; 1调用成功，0失败
     */
    Integer startNetTest(Integer delay);

    /**
     * 获取网络抖动诊断结果
     *
     * @return String类型, jsonObject格式
     */
    String startNetTestResult();
}
