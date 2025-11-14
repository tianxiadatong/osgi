package com.wasu.osgi.upgrade.service;

import org.json.JSONObject;

/**
 * @author: glmx_
 * @date: 2025/10/13
 * @description: 固件升级服务接口
 */
public interface IFirmwareUpgradeService {

    /**
     * 接口内容：设备请求升级
     * 请求topic：sys/${productKey}/${deviceId}/thing2/up
     * 请求类型：终端->消息通信服务
     */
    boolean upOTA(JSONObject params);


    /**
     * 1. 识别固件类型升级（main/C插件/Java插件）
     * 2. 调用硬件升级服务执行固件和C插件升级
     * 3. 下载、解压并升级Java插件
     * 4. 轮询升级结果并上报给OTA服务器
     */
    boolean checkFirmwareUpgrade(JSONObject params);

    void checkAndInstallPendingPlugins();

    void checkFirmwareUpgradeResultOnStartup();

}
