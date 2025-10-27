package com.wasu.osgi.hardware.hgu01.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wasu.osgi.common.config.ConfigProperties;
import com.wasu.osgi.common.util.ConfigUtil;
import com.wasu.osgi.hardware.hgu01.dto.HwinfoDTO;
import com.wasu.osgi.hardware.hgu01.dto.NetworkInterfaceInfoDTO;
import com.wasu.osgi.hardware.hgu01.service.IOSGIHardwareService;
import com.wasu.osgi.common.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * @author: glmx_
 * @date: 2024/8/21
 * @description:
 */
public class OSGIHardwareServiceImpl implements IOSGIHardwareService {

    private static final Logger logger = Logger.getLogger(OSGIHardwareServiceImpl.class);


    @Override
    public JSONObject getManufacturer() {
        return null;
    }

    @Override
    public JSONObject getOUI() {
        return null;
    }

    @Override
    public JSONObject getModel() {
        return null;
    }

    @Override
    public JSONObject getType() {
        return null;
    }

    @Override
    public JSONObject getSN() {
        return null;
    }

    @Override
    public JSONObject queryWiFiStats() {
        return null;
    }

    @Override
    public JSONObject getChipModel() {
        return null;
    }

    @Override
    public JSONObject getHardwareModel() {
        return null;
    }

    @Override
    public JSONObject getHardwareVersion() {
        return null;
    }

    @Override
    public JSONObject getLMTemp() {
        return null;
    }

    @Override
    public JSONObject getRAM() {
        return null;
    }

    @Override
    public JSONObject getROM() {
        return null;
    }

    @Override
    public JSONObject getMAC() {
        return null;
    }

    @Override
    public JSONObject getFirmwareModel() {
        return null;
    }

    @Override
    public JSONObject getFirmwareVersion() {
        return null;
    }

    @Override
    public JSONObject getPlatformVersion() {
        return null;
    }

    @Override
    public JSONObject isLastVersion() {
        return null;
    }

    @Override
    public JSONObject getDeviceUpTime() {
        return null;
    }

    @Override
    public JSONObject getCPUUsage() {
        return null;
    }

    @Override
    public JSONObject getMemUsage() {
        return null;
    }

    @Override
    public JSONObject getRomUsage() {
        return null;
    }

    @Override
    public JSONObject getCpuTemperature() {
        return null;
    }

    @Override
    public JSONObject getSystemSoftwareInfo() {
        return null;
    }

    @Override
    public JSONObject getOSGIInfo() {
        return null;
    }

    @Override
    public JSONObject updateProcess() {
        return null;
    }

    @Override
    public JSONObject setQos(JSONObject json) {
        return null;
    }

    @Override
    public JSONObject isSupportIPV6() {
        return null;
    }

    @Override
    public JSONObject isSupportVLan() {
        return null;
    }

    @Override
    public JSONObject getInternalStoragePath() {
        return null;
    }

    @Override
    public JSONObject getExternalStorageDirectory() {
        return null;
    }

    @Override
    public JSONObject getInternalFreeStorage() {
        return null;
    }

    @Override
    public JSONObject getExternalFreeStorage() {
        return null;
    }

    @Override
    public JSONObject reboot() {
        return null;
    }

    @Override
    public JSONObject factoryReset() {
        return null;
    }

    @Override
    public JSONObject getCAManufacturer() {
        return null;
    }

    @Override
    public JSONObject getCASN() {
        return null;
    }

    @Override
    public JSONObject getCAVersion() {
        return null;
    }

    @Override
    public JSONObject getCACard() {
        return null;
    }

    @Override
    public JSONObject getViewLevel() {
        return null;
    }

    @Override
    public JSONObject getStatus() {
        return null;
    }

    @Override
    public JSONObject getProductInfo() {
        return null;
    }

    @Override
    public JSONObject getCMDownstreamNumber() {
        return null;
    }

    @Override
    public JSONObject getCMDownstreamInfo() {
        return null;
    }

    @Override
    public JSONObject getCMInfo() {
        return null;
    }

    @Override
    public JSONObject setPingParameter(JSONObject json) {
        return null;
    }

    @Override
    public JSONObject getPingResult() {
        return null;
    }

    @Override
    public JSONObject setTracerouteParameter(JSONObject json) {
        return null;
    }

    @Override
    public JSONObject getTracerouteResult() {
        return null;
    }

    @Override
    public JSONObject getOutsideNetworkTraffic() {
        return null;
    }

    @Override
    public JSONObject startWiFiScan() {
        return null;
    }

    @Override
    public JSONObject getWiFiScanResult() {
        return null;
    }

    @Override
    public JSONObject startSpeedTest() {
        return null;
    }

    @Override
    public JSONObject startSpeedTestResult() {
        return null;
    }

    @Override
    public JSONObject startNetTest(JSONObject json) {
        return null;
    }

    @Override
    public JSONObject startNetTestResult() {
        return null;
    }

    @Override
    public JSONObject getWifiRadioEnable24G() {
        return null;
    }

    @Override
    public JSONObject setWifiRadioEnable24G(JSONObject json) {
        return null;
    }

    @Override
    public JSONObject getWifiRadioEnable5G() {
        return null;
    }

    @Override
    public JSONObject setWifiRadioEnable5G(JSONObject json) {
        return null;
    }

    @Override
    public JSONObject getWifiVersion() {
        return null;
    }

    @Override
    public JSONObject getWifiSetting() {
        return null;
    }

    @Override
    public JSONObject setWifiSetting(JSONObject json) {
        return null;
    }

    @Override
    public JSONObject enableWifiWhiteList(JSONObject json) {
        return null;
    }

    @Override
    public JSONObject addWifiWhiteList(JSONObject json) {
        return null;
    }

    @Override
    public JSONObject removeWifiWhiteList(JSONObject json) {
        return null;
    }

    @Override
    public JSONObject getWifiWhiteList(JSONObject json) {
        return null;
    }

    @Override
    public JSONObject getWifiRadioEnableGuest5G() {
        return null;
    }

    @Override
    public JSONObject setWifiRadioEnableGuest5G(JSONObject json) {
        return null;
    }

    @Override
    public JSONObject getSleepStatus() {
        return null;
    }

    @Override
    public JSONObject setSleepStatus(JSONObject json) {
        return null;
    }

    @Override
    public JSONObject getBandStatus() {
        return null;
    }

    @Override
    public JSONObject setWifiAys(JSONObject json) {
        return null;
    }

    @Override
    public JSONObject getSTAStatus() {
        return null;
    }

    @Override
    public JSONObject setSTAStatus(JSONObject json) {
        return null;
    }

    @Override
    public JSONObject getChannelUserd(JSONObject json) {
        return null;
    }

    @Override
    public JSONObject getDizao(JSONObject json) {
        return null;
    }

    @Override
    public JSONObject getMeshActor() {
        return null;
    }

    @Override
    public HwinfoDTO hwinfo() {
        HwinfoDTO dto = null;
        ConfigProperties config = ConfigUtil.getConfig();
        if (config == null) {
            logger.error("hwinfo接口，加载配置文件失败，程序终止。");
            return dto;
        }
        String url = config.getWtsUrl() + "/hwinfo";
        HttpUtil.HttpResult httpResult = HttpUtil.getRequest(url, "");
        if (httpResult.isSuccess()) {
            JSONObject result = JSONObject.parseObject(httpResult.getBody());
            //{"chipmodel":"ECNT7528","hwmodel":"GDHG-JL5100","hwver":"V6.0.0.0"}
            dto = JSONObject.parseObject(result.toJSONString(), HwinfoDTO.class);
        }
        return dto;
    }

    @Override
    public JSONObject getPonSN() {
        JSONObject jsonObject = null;
        ConfigProperties config = ConfigUtil.getConfig();
        if (config == null) {
            logger.error("getPonSN接口，加载配置文件失败，程序终止。");
            return jsonObject;
        }
        String url = config.getWtsUrl() + "/GetPonSN";
        HttpUtil.HttpResult httpResult = HttpUtil.getRequest(url, "");
        if (httpResult.isSuccess()) {
            JSONObject result = JSONObject.parseObject(httpResult.getBody());
            //{"function": "GetPonSN","errorCode": "0","errorMsg": "OK","PonSN": "UMTC44BB9BAB"}
            if (null != result && "0".equals(result.getString("errorCode"))) {
                String ponSN = result.getString("PonSN");
                jsonObject = new JSONObject();
                jsonObject.put("ponSN",ponSN);
            }
        }
        return jsonObject;
    }

    @Override
    public List<NetworkInterfaceInfoDTO> networkInterface() {
        List<NetworkInterfaceInfoDTO> infoList = null;
        ConfigProperties config = ConfigUtil.getConfig();
        if (config == null) {
            logger.error("networkInterface接口，加载配置文件失败，程序终止。");
            return infoList;
        }
        String url = config.getWtsUrl() + "/NetworkInterface";
        HttpUtil.HttpResult httpResult = HttpUtil.getRequest(url, "");
        if (httpResult.isSuccess()) {
            JSONObject result = JSONObject.parseObject(httpResult.getBody());
            //{"errorCode":"0","errorMsg":"OK","info":[{"ifType":"WAN","displayname":"WAN.eRouter","ifName":"ppp1","pid":"18","ipType":"IPv4","status":"down","mac":"D4:38:44:BB:9B:AC","mtu":"1492"},{"ifType":"WAN","displayname":"WAN.eSTB","ifName":"nas0_0","pid":"90","ipType":"IPv4","status":"up","mac":"D4:38:44:BB:9B:AB","mtu":"1452"}]}
            if (null != result && "0".equals(result.getString("errorCode"))) {
                JSONArray infos = result.getJSONArray("info");
                if (infos != null && !infos.isEmpty()) {
                    infoList = JSON.parseArray(JSON.toJSONString(infos), NetworkInterfaceInfoDTO.class);
                }
            }
        }
        return infoList;
    }
}
