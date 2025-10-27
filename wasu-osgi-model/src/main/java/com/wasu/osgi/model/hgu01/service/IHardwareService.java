package com.wasu.osgi.model.hgu01.service;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author: glmx_
 * @date: 2024/8/21
 * @description:
 */
public interface IHardwareService {

    /*====================WLAN配置服务接口====================*/

    /**
     * 获取生产厂商
     */
    String getManufacturer();

    String getOUI();

    String getModel();

    JSONObject getType();

    String getSN();

    JSONObject queryWiFiStats();

    JSONObject getChipModel();

    JSONObject getHardwareModel();

    JSONObject getHardwareVersion();

    JSONObject getLMTemp();

    JSONObject getRAM();

    JSONObject getROM();

    String getMAC();

    JSONObject getFirmwareModel();

    JSONObject getFirmwareVersion();

    JSONObject getPlatformVersion();

    JSONObject isLastVersion();

    Integer getDeviceUpTime();

    Integer getCPUUsage();

    Integer getMemUsage();

    JSONObject getRomUsage();

    JSONObject getCpuTemperature();

    JSONObject getSystemSoftwareInfo();

    JSONObject getOSGIInfo();

    JSONObject updateProcess();

    JSONObject setQos(JSONObject json);

    JSONObject isSupportIPV6();

    JSONObject isSupportVLan();

    JSONObject getInternalStoragePath();

    JSONObject getExternalStorageDirectory();

    JSONObject getInternalFreeStorage();

    JSONObject getExternalFreeStorage();

    /*====================网关设置接口====================*/

    void reboot();

    JSONObject factoryReset();

    /**
     * 7.3.1.2.4.8 设备限时设置
     */
    boolean deviceTimeLimit(JSONObject params);

    /**
     * 7.3.1.2.4.9 删除设备限时
     */
    boolean removeDeviceTimeLimit(JSONObject params);

    /**
     * 7.3.1.2.4.32 获取客户端限时设置列表
     */
    JSONArray getSubTimeLimitList(JSONObject params);

    /**
     * 7.3.1.2.4.33 删除客户端单个限时设置
     */
    boolean removeSubTimeLimitList(JSONObject params);

    /**
     * 7.3.1.2.4.34 设置客户端单个限时开关状态
     */
    boolean setSubTimeLimitList(JSONObject params);

    /*====================网关CA信息接口(DVB网关)接口====================*/

    JSONObject getCAManufacturer();

    JSONObject getCASN();

    JSONObject getCAVersion();

    JSONObject getCACard();

    JSONObject getViewLevel();

    JSONObject getStatus();

    JSONObject getProductInfo();

    /*====================网关CableModem信息接口(DVB网关)接口====================*/

    JSONObject getCMDownstreamNumber();

    JSONObject getCMDownstreamInfo();

    JSONObject getCMInfo();

    /*====================网络诊断接口====================*/

    boolean pingTest(JSONObject json);

    JSONObject getPingResult();

    boolean tracerouteTest(JSONObject json);

    JSONObject getTracerouteResult();

    JSONObject getOutsideNetworkTraffic();

    JSONObject startWiFiScan();

    JSONObject getWiFiScanResult();

    boolean speedTest(JSONObject params);

    JSONObject getSpeedTestResult();

    JSONObject startNetTest(JSONObject json);

    JSONObject startNetTestResult();

    /*====================WLAN配置服务接口====================*/

    JSONObject getWifiRadioEnable24G();

    JSONObject setWifiRadioEnable24G(JSONObject json);

    JSONObject getWifiRadioEnable5G();

    JSONObject setWifiRadioEnable5G(JSONObject json);

    JSONObject getWifiVersion();

    JSONArray getWifiSetting();

    JSONObject setWifiSetting(JSONObject json);

    JSONObject enableWifiWhiteList(JSONObject json);

    JSONObject addWifiWhiteList(JSONObject json);

    JSONObject removeWifiWhiteList(JSONObject json);

    JSONObject getWifiWhiteList(JSONObject json);

    JSONObject getWifiRadioEnableGuest5G();

    JSONObject setWifiRadioEnableGuest5G(JSONObject json);

    JSONObject getSleepStatus();

    JSONObject setSleepStatus(JSONObject json);

    JSONObject getBandStatus();

    JSONObject setWifiAys(JSONObject json);

    JSONObject getSTAStatus();

    JSONObject setSTAStatus(JSONObject json);

    JSONObject getChannelUserd(JSONObject json);

    JSONObject getDizao(JSONObject json);

    JSONObject getMeshActor();

    JSONObject hwinfo();

    JSONObject getPonSN();

    JSONArray networkInterface();

    int getMaxAllowedUsers();

    JSONObject getDeviceInfo();

    JSONObject getUplinkIfStats();

    JSONArray getWANIfList();

    JSONObject getWanIfBandwidth(Integer index);

    JSONObject getWANIfInfo(Integer index);

    JSONObject getPONIfPhyStatus();

    /**
     * 7.3.1.2.4.25 定时重启
     */
    boolean scheduledRestart(JSONObject params);

    /**
     * 7.3.1.2.4.28设置开关状态
     * 入参：funcName 枚举
     * scheduledRestart（定时重启)
     */
    boolean setSwitchStatus(JSONObject params);

    /**
     * 7.3.1.2.4.37 获取定时重启任务列表
     */
    JSONArray getScheduledRestart();

    /**
     * 7.3.1.2.4.38 删除定时重启任务
     */
    boolean removeScheduledRestart(JSONObject params);

    /**
     * 7.3.1.2.4.46 设置指示灯状态
     */
    boolean setLightStatus(JSONObject params);

    /**
     * 7.3.1.2.4.47 获取指示灯状态
     */
    JSONObject getLightStatus();

    /**
     * 7.3.1.2.4.51 信道干扰
     */
    boolean channelInterference();

    /**
     * 获取WLAN频段邻居信息
     */
    JSONArray getWLANNeighbor(String radioType);

    /**
     * 7.3.1.2.4.53 获取端口列表 同步上报
     */
    JSONArray getPortList();

    /**
     * 查询WAN口状态
     */
    JSONObject getWANIfStatus(Integer wanIndex);

    /**
     * 获取以太网端口信息 LAN端口状态
     */
    JSONObject getLANEthernetInfo(Integer portIndex);

    JSONArray getWLANHostInfo(String[] macs);

    JSONObject getLANHostSpeed(String mac);

    JSONArray getLANHostInfoByMAC(String[] macs);

    JSONArray getBundleList();

    int setDeviceConfig(String productKey, String productSecret, String deviceId, String deviceSecret);

    JSONObject getDeviceConfig();

    void wifiSetting(JSONObject params);

    JSONArray staList();

    boolean addStaticIpv4(JSONObject params);

    boolean deleteStaticIpv4(JSONObject params);

    JSONArray getStaticIpv4();

    boolean addStaticIpv6(JSONObject params);

    boolean deleteStaticIpv6(JSONObject params);

    JSONArray getStaticIpv6();

    boolean deviceSpeedLimit(JSONObject params);

    JSONObject getStaLimitInfo(JSONObject params);

    JSONArray blackDeviceList();

    boolean addBlackDevice(JSONObject params);

    boolean remoteBlackDevice(JSONObject params);

    JSONArray whiteDeviceList();

    boolean addWhiteDevice(JSONObject params);

    boolean remoteWhiteDevice(JSONObject params);

    JSONObject info();

    JSONObject getWanIndex(String name);

//    boolean setPilotLamp(JSONObject params);

    //  boolean setPlugSwitchStatus(JSONObject params);

    //  JSONArray getPlugSwitchStatus();
}
