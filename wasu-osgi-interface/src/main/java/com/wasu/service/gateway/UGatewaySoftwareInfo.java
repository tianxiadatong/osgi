package com.wasu.service.gateway;

/**
 * @author: glmx_
 * @date: 2024/12/31
 * @description:
 */
public interface UGatewaySoftwareInfo {

    /**
     * 固件型号
     *
     * @return String类型
     */
    String getFirmwareModel();

    /**
     * 固件版本
     *
     * @return String类型
     */
    String getFirmwareVersion();

    /**
     * 平台版本
     *
     * @return String类型
     */
    String getPlatformVersion();

    /**
     * 是否最新版本
     *
     * @return boolean类型
     */
    boolean isLastVersion();

    /**
     * 获取启动时间
     *
     * @return int类型
     */
    int getDeviceUpTime();

    /**
     * CPU占用百分比
     *
     * @return int类型
     */
    int getCPUUsage();

    /**
     * RAM占用百分比
     *
     * @return int类型
     */
    int getMemUsage();

    /**
     * ROM占用百分比
     *
     * @return int类型
     */
    int getRomUsage();

    /**
     * Cpu温度，单位: 摄氏度
     *
     * @return int类型
     */
    int getCpuTemperature();

    /**
     * 系统软件信息
     *
     * @return String类型, jsonObject格式
     */
    String getSystemSoftwareInfo();

    /**
     * OSGI插件信息
     *
     * @return String类型, jsonObject格式
     */
    String getOSGIInfo();

    /**
     * 升级结果上报
     *
     * @return String类型
     */
    Integer updateProcess();

    /**
     * 设备Qos策略
     *
     * @return int类型，0表示成功，非0表示失败
     */
    Integer setQos(String vl);

    /**
     * 是否支持IPV6
     *
     * @return Integer类型, 1 支持 0不支持
     */
    Integer isSupportIPV6();

    /**
     * 是否支持VLAN绑定
     *
     * @return Integer类型, 1 支持 0不支持
     */
    Integer isSupportVLan();
}
