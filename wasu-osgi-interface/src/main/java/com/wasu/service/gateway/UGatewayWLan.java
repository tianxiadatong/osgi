package com.wasu.service.gateway;

/**
 * @author: glmx_
 * @date: 2024/12/31
 * @description: WLAN配置服务
 */
public interface UGatewayWLan {

    /**
     * 获取wifi2.4G状态
     *
     * @return boolean类型
     */
    boolean getWifiRadioEnable24G();

    /**
     * 设置wifi2.4G状态
     *
     * @return boolean类型是否可用 false-关闭 true-打开
     */
    boolean setWifiRadioEnable24G(boolean enable);

    /**
     * 获取wifi5G状态
     *
     * @return boolean类型
     */
    boolean getWifiRadioEnable5G();

    /**
     * 设置wifi5G状态
     *
     * @return boolean类型是否可用 false-关闭 true-打开
     */
    boolean setWifiRadioEnable5G(boolean enable);

    /**
     * 获取wifi版本信息
     *
     * @return WIFI5/WIFI6/WIFI7
     */
    String getWifiVersion();

    /**
     * 获取wifi设置参数
     *
     * @return String类型, jsonObject格式
     */
    String getWifiSetting();

    /**
     * 设置wifi设置参数
     *
     * @return boolean设置是否成功 true-设置成功 false-设置失败。
     */
    boolean setWifiSetting(String type, String ssid, int enable, String newAuth, String password, String wepKey, String newChannel, String newBandWidth, String newBeconType, int transmitPower);

    /**
     * 开启或关闭wifi接入白名单
     */
    boolean enableWifiWhiteList(String ssid, boolean enable);

    /**
     * 增加wifi接入白名单
     */
    boolean addWifiWhiteList(String ssid, String mac);

    /**
     * 删除wifi接入白名单
     */
    boolean removeWifiWhiteList(String ssid, String mac);

    /**
     * 获取wifi接入白名单
     */
    String getWifiWhiteList(String ssid);

    /**
     * 获取访客wifi5G状态
     */
    boolean getWifiRadioEnableGuest5G();

    /**
     * 设置访客wifi5G状态
     */
    boolean setWifiRadioEnableGuest5G(boolean enable);

    /**
     * 获取网关定时休眠配置
     */
    String getSleepStatus();

    /**
     * 设置网关定时休眠配置
     */
    boolean setSleepStatus(int enable, String controlCycle, String startTime, String endTime);

    /**
     * 获取网关设备wifi频段信息
     */
    int getBandStatus();

    /**
     * WIFI状态同步设置
     */
    int setWifiAys(boolean val);

    /**
     * 获取STA漫游状态
     */
    int getSTAStatus();

    /**
     * 设置STA漫游状态
     */
    int setSTAStatus(String channel, boolean open);

    /**
     * 信道利用率
     */
    int getChannelUsed(String channel);

    /**
     * 底噪
     */
    int getDizao(String channel);

    /**
     * 获取Mesh角色
     */
    int getMeshActor();

}
