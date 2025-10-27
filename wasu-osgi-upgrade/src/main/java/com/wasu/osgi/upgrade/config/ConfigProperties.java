package com.wasu.osgi.upgrade.config;

/**
 * @author: glmx_
 * @date: 2024/8/21
 * @description:
 */
public class ConfigProperties {
    public static final String productKey = "60009521";
    public static final String productSecret = "your_product_secret"; // 需要替换为实际的产品密钥
    public static final String wtsUrl = "https://your-wts-url"; // 需要替换为实际的WTS URL

    //测试
//    public static final String otaUrl = "https://hgu-apidownload-test.iot.wasumedia.cn";
    //测试
    public static final String otaUrl = "https://[2401:ce00:4c00:1000::186]";
    //现网
//    public static final String otaUrl = "https://hgu-api.iot.wasumedia.cn";
    public static String deviceId;

    // MQTT配置
    public static String mqttUrl;
    public static String mqttUser;
    public static String mqttPassword;
    public static String mqttClientId;
    public static int mqttKeepalive = 60;
}
