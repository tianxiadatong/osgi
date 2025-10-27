package com.wasu.osgi.hardware.hgu01.service;

import com.alibaba.fastjson.JSONObject;
import com.wasu.osgi.hardware.hgu01.dto.HwinfoDTO;
import com.wasu.osgi.hardware.hgu01.dto.NetworkInterfaceInfoDTO;

import java.util.List;

/**
 * @author: glmx_
 * @date: 2024/8/21
 * @description:
 */
public interface IOSGIHardwareService {

    /*====================WLAN配置服务接口====================*/

    /**
     * 获取生产厂商
     */
    JSONObject getManufacturer();

    JSONObject getOUI();

    JSONObject getModel();

    JSONObject getType();

    JSONObject getSN();

    JSONObject queryWiFiStats();

    JSONObject getChipModel();

    JSONObject getHardwareModel();

    JSONObject getHardwareVersion();

    JSONObject getLMTemp();

    JSONObject getRAM();

    JSONObject getROM();

    JSONObject getMAC();

    JSONObject getFirmwareModel();

    JSONObject getFirmwareVersion();

    JSONObject getPlatformVersion();

    JSONObject isLastVersion();

    JSONObject getDeviceUpTime();

    JSONObject getCPUUsage();

    JSONObject getMemUsage();

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

    JSONObject reboot();

    JSONObject factoryReset();

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

    JSONObject setPingParameter(JSONObject json);

    JSONObject getPingResult();

    JSONObject setTracerouteParameter(JSONObject json);

    JSONObject getTracerouteResult();

    JSONObject getOutsideNetworkTraffic();

    JSONObject startWiFiScan();

    JSONObject getWiFiScanResult();

    JSONObject startSpeedTest();

    JSONObject startSpeedTestResult();

    JSONObject startNetTest(JSONObject json);

    JSONObject startNetTestResult();

    /*====================WLAN配置服务接口====================*/

    JSONObject getWifiRadioEnable24G();

    JSONObject setWifiRadioEnable24G(JSONObject json);

    JSONObject getWifiRadioEnable5G();

    JSONObject setWifiRadioEnable5G(JSONObject json);

    JSONObject getWifiVersion();

    JSONObject getWifiSetting();

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

    HwinfoDTO hwinfo();

    JSONObject getPonSN();

    List<NetworkInterfaceInfoDTO> networkInterface();
}
