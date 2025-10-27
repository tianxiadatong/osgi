package com.wasu.osgi.model.hgu01.config;

/**
 * @author: glmx_
 * @date: 2024/8/21
 * @description:
 */
public class ConfigProperties {
    public static final String productKey = "60009521";
    public static final String productSecret = "ps60cb026a8e065d42";
    public static String deviceId;
    public static String deviceSecret;
    public static final Integer maxRetries = 15;
    public static final Integer intervalBase = 30;
    public static final Integer intervalRange = 50;
    public static final Integer baseRetry = 5;

    public static String mqttUrl;
    public static String mqttClientId;
    public static String mqttUser;
    public static String mqttPassword;
    public static Integer mqttKeepalive;
    public static String mqttRetry;

    //测试
    public static final String wtsUrl = "https://iot-test.wasumedia.cn";
    //现网
//    public static final String wtsUrl = "https://hub.iot.wasumedia.cn";
}
