package com.wasu.osgi.model.hgu01.connect;

import com.wasu.osgi.model.hgu01.config.CommonConstant;
import com.wasu.osgi.model.hgu01.config.ConfigProperties;
import com.wasu.osgi.model.hgu01.config.DataProperties;
import com.wasu.osgi.model.hgu01.service.IHardwareService;
import com.wasu.osgi.model.hgu01.service.impl.HardwareServiceImpl;
import com.wasu.osgi.model.hgu01.util.ConfigUtil;
import com.wasu.osgi.model.hgu01.util.EncryptUtil;
import com.wasu.osgi.model.hgu01.util.HttpUtil;
import com.wasu.osgi.model.hgu01.util.StringUtil;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: glmx_
 * @date: 2024/8/1
 * @description:
 */
public class HttpRequest {

    private static final Logger logger = Logger.getLogger(HttpRequest.class);

    private static final Random RAND = new Random();
    private static IHardwareService hardwareService;
    private static boolean isOnline = false;
    private static BundleContext bundleContext;

    public static void setHardwareService(IHardwareService service) {
        hardwareService = service;
    }

    public static IHardwareService getHardwareService() {
        return hardwareService;
    }

    public static void request(BundleContext ctx) {
        try {
            bundleContext = ctx;
            if (isOnline && !StringUtil.isEmpty(ConfigProperties.mqttUser)) {
                new MqttConnect(ctx);
                return;
            }
            JSONObject deviceConfig = hardwareService.getDeviceConfig();
            if (deviceConfig.has("deviceId")) {
                ConfigProperties.deviceId = deviceConfig.getString("deviceId");
                ConfigProperties.deviceSecret = deviceConfig.getString("deviceSecret");
            }
            String secretKey = EncryptUtil.getMD5(ConfigProperties.productSecret).substring(0, 16);
            byte[] ivKey = EncryptUtil.getIvKey(ConfigProperties.productKey, null);
            DataProperties dataProperties = ConfigUtil.loadForClazz(DataProperties.class, CommonConstant.DATA_FILE_NAME);
            if (dataProperties == null || StringUtil.isEmpty(dataProperties.getWlinkToken())) {
                boolean registerResult = httpRetry(secretKey, ivKey, "register");
                if (registerResult) {
                    httpRetry(secretKey, ivKey, "online");
                }
            } else {
                httpRetry(secretKey, ivKey, "online");
            }
        } catch (Exception e) {
            logger.error("连接梧桐树平台异常，", e);
        }
    }

    public synchronized static boolean httpRetry(String secretKey, byte[] ivKey, String requestType) {
        int retryCount = 0;
        AtomicBoolean requestSuccessful = new AtomicBoolean(false);
        ScheduledExecutorService scheduler = null;
        try {
            while (retryCount <= ConfigProperties.maxRetries) {
                if (retryCount < ConfigProperties.baseRetry) {
                    if ("register".equals(requestType)) {
                        requestSuccessful.set(register(secretKey, ivKey));
                    } else {
                        requestSuccessful.set(online(secretKey, ivKey));
                    }
                    if (requestSuccessful.get()) {
                        break;
                    }
                    retryCount++;
                } else {
                    retryCount++;
                    int interval = calculateInterval(retryCount);
                    logger.info(requestType + "请求失败，正在重试... (重试次数: " + retryCount + ", 间隔时间: " + interval + "秒)");
                    scheduler = Executors.newSingleThreadScheduledExecutor();
                    ScheduledFuture<Boolean> schedule = scheduler.schedule(() -> {
                        if ("register".equals(requestType)) {
                            boolean register = register(secretKey, ivKey);
                            if (register) {
                                logger.info(requestType + "请求成功！");
                            }
                            requestSuccessful.set(register);
                            return register;
                        } else {
                            boolean online = online(secretKey, ivKey);
                            if (online) {
                                logger.info(requestType + "请求成功！");
                            }
                            requestSuccessful.set(online);
                            return online;
                        }
                    }, interval, TimeUnit.SECONDS);
                    if (schedule.get()) {
                        logger.info(requestType + "请求成功！");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("获取http请求结果异常，", e);
        } finally {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        }
        return requestSuccessful.get();
    }

    private static int calculateInterval(int retryCount) {
        if (retryCount > ConfigProperties.maxRetries) {
            retryCount = ConfigProperties.maxRetries;
        }
        int baseInterval = ConfigProperties.intervalBase * (int) Math.pow(2, retryCount - 1);
        int randomOffset = RAND.nextInt(ConfigProperties.intervalRange + 1);
        return baseInterval + randomOffset;
    }

    private static boolean register(String secretKey, byte[] ivKey) {
        String registerStr = EncryptUtil.encrypt(secretKey, buildRegisterParam().toString(), ivKey);
        String registerUrl = ConfigProperties.wtsUrl + "/v1/wasu-hub/hardware/device/register/" + ConfigProperties.productKey;
        HttpUtil.HttpResult httpResult = HttpUtil.postRequest(registerUrl, registerStr);
        if (!httpResult.isSuccess()) {
            logger.error("请求梧桐树注册接口失败，status：" + httpResult.getStatus());
            return false;
        }
        String registerBody = httpResult.getBody();
        logger.info("注册结果：" + registerBody);
        String registerDecrypt = EncryptUtil.decrypt(secretKey, registerBody, ivKey);
        if (StringUtil.notEmpty(registerDecrypt)) {
            JSONObject registerResultObj = new JSONObject(registerDecrypt);
            JSONObject registerSystem = registerResultObj.getJSONObject("system");
            if (registerSystem.getInt("code") == 0) {
                JSONObject result = registerResultObj.getJSONObject("result");
                if (StringUtil.isEmpty(ConfigProperties.deviceId)) {
                    String deviceId = result.getString("deviceId");
                    String deviceSecret = result.getString("deviceSecret");
                    ConfigProperties.deviceId = deviceId;
                    ConfigProperties.deviceSecret = deviceSecret;
                    hardwareService.setDeviceConfig(ConfigProperties.productKey, ConfigProperties.productSecret, deviceId, deviceSecret);
                }
                DataProperties dataProperties = new DataProperties();
                dataProperties.setWlinkToken(result.getString("wlinkToken"));
                ConfigUtil.storeWithClazz(dataProperties, CommonConstant.DATA_FILE_NAME);
                return true;
            }
        }
        return false;
    }

    private static boolean online(String secretKey, byte[] ivKey) {
        logger.info("开始上线");
        String onlineStr = EncryptUtil.encrypt(secretKey, buildOnlineParam().toString(), ivKey);
        String onlineUrl = ConfigProperties.wtsUrl + "/v1/wasu-hub/hardware/device/online/" + ConfigProperties.deviceId;
        HttpUtil.HttpResult httpResult = HttpUtil.postRequest(onlineUrl, onlineStr);
        if (!httpResult.isSuccess()) {
            logger.error("请求梧桐树上线接口失败，status：" + httpResult.getStatus());
            return false;
        }
        String onlineBody = httpResult.getBody();
        String onlineDecrypt = EncryptUtil.decrypt(secretKey, onlineBody, ivKey);
        if (StringUtil.notEmpty(onlineDecrypt)) {
            JSONObject onlineResultObj = new JSONObject(onlineDecrypt);
            JSONObject onlineSystem = onlineResultObj.getJSONObject("system");
            if (onlineSystem.getInt("code") == 0) {
                JSONObject result = onlineResultObj.getJSONObject("result");
                ConfigProperties.mqttUrl = result.getString("mqttUrl");
                ConfigProperties.mqttClientId = result.getString("mqttClientId");
                ConfigProperties.mqttUser = result.getString("mqttUser");
                ConfigProperties.mqttPassword = result.getString("mqttPassword");
                ConfigProperties.mqttKeepalive = result.getInt("mqttKeepalive");
                logger.info("mqttUrl: " + ConfigProperties.mqttUrl);
                logger.info("mqttClientId: " + ConfigProperties.mqttClientId);
                logger.info("mqttUser: " + ConfigProperties.mqttUser);
                logger.info("mqttPassword: " + ConfigProperties.mqttPassword);
                JSONArray mqttRetry = result.getJSONArray("mqttRetry");
                if (mqttRetry != null && mqttRetry.length() > 0) {
                    StringBuilder retryStr = new StringBuilder();
                    for (int i = 0; i < mqttRetry.length(); i++) {
                        retryStr.append(mqttRetry.getInt(i)).append(",");
                    }
                    ConfigProperties.mqttRetry = retryStr.substring(0, retryStr.length() - 1);
                }
                logger.info("online接口请求成功，发起mqtt连接。");
                isOnline = true;
                new MqttConnect(bundleContext);
                return true;
            }
        }
        return false;
    }

    private static JSONObject buildRegisterParam() {
        JSONObject deviceInfo = hardwareService.getDeviceInfo();
        JSONObject bodyMap = new JSONObject();
        JSONObject system = new JSONObject();
        system.put("requestId", UUID.randomUUID().toString());
        system.put("ts", System.currentTimeMillis());
        bodyMap.put("system", system);
        JSONObject params = new JSONObject();
        params.put("deviceMac", hardwareService.getMAC());
        params.put("productKey", ConfigProperties.productKey);
        params.put("timestamp", System.currentTimeMillis());
        JSONObject deviceExtInfo = new JSONObject();
        String deviceId = ConfigProperties.deviceId;
        deviceExtInfo.put("deviceId", deviceId == null ? "" : deviceId);
        deviceExtInfo.put("sn", hardwareService.getSN());
        if (deviceInfo != null) {
            deviceExtInfo.put("deviceVendor", deviceInfo.getString("Vendor"));
            deviceExtInfo.put("deviceModel", deviceInfo.getString("ProductCLass"));
            deviceExtInfo.put("firmwareVersion", deviceInfo.getString("FirmwareVer"));
        }
        params.put("deviceExtInfo", deviceExtInfo);
        bodyMap.put("params", params);
        return bodyMap;
    }

    private static JSONObject buildOnlineParam() {
        JSONObject deviceInfo = hardwareService.getDeviceInfo();
        JSONObject bodyMap = new JSONObject();
        JSONObject system = new JSONObject();
        system.put("requestId", UUID.randomUUID().toString());
        system.put("ts", System.currentTimeMillis());
        bodyMap.put("system", system);
        JSONObject params = new JSONObject();
        params.put("deviceId", ConfigProperties.deviceId);
        params.put("productKey", ConfigProperties.productKey);
        params.put("timestamp", System.currentTimeMillis());
        if (deviceInfo != null) {
            params.put("firmwareVersion", deviceInfo.getString("FirmwareVer"));
        }
        JSONObject exData = new JSONObject();
        exData.put("mqttType", 2);
        exData.put("support5G", 1);
        exData.put("apUplinkType", "Fiber");
        params.put("exData", exData);
        bodyMap.put("params", params);
        return bodyMap;
    }
}
