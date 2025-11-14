package com.wasu.osgi.model.hgu01.connect;

import com.wasu.osgi.model.hgu01.config.ConfigProperties;
import com.wasu.osgi.model.hgu01.handler.HandlerRegistry;
import com.wasu.osgi.model.hgu01.handler.IHandler;
import com.wasu.osgi.model.hgu01.service.impl.DataReportingService;
import com.wasu.osgi.model.hgu01.util.CertificateValidator;
import com.wasu.osgi.model.hgu01.util.EncryptUtil;
import com.wasu.osgi.model.hgu01.util.StringUtil;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author glmx_
 */
public class MqttConnect implements MqttCallbackExtended {

    private static final Logger logger = Logger.getLogger(MqttConnect.class);

    private static MqttClient mqttClient;
    private static MqttConnectOptions connOpts;
    private static String[] retryIntervals = null;
    private static String downTopic, upReplyTopic, productDownTopic, downReplyTopic, upTopic;
    private static final AtomicBoolean IS_CONNECTED = new AtomicBoolean(false);
    private final DataReportingService dataReportingService;

    public MqttConnect(BundleContext ctx) {
        this.dataReportingService = new DataReportingService();

        try {
            if (StringUtil.isEmpty(ConfigProperties.mqttUser)) {
                HttpRequest.request(ctx);
                return;
            }
            downTopic = "sys/" + ConfigProperties.productKey + "/" + ConfigProperties.deviceId + "/thing2/down";
            upReplyTopic = "sys/" + ConfigProperties.productKey + "/" + ConfigProperties.deviceId + "/thing2/up_reply";
//            productDownTopic = "sys/" + ConfigProperties.productKey + "/thing2/down";
            downReplyTopic = "sys/" + ConfigProperties.productKey + "/" + ConfigProperties.deviceId + "/thing2/down_reply";
            upTopic = "sys/" + ConfigProperties.productKey + "/" + ConfigProperties.deviceId + "/thing2/up";
            retryIntervals = ConfigProperties.mqttRetry.split(",");
            String mqttUrl = ConfigProperties.mqttUrl.replace("\\", "");
            connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setSocketFactory(CertificateValidator.sslCheck());
            connOpts.setUserName(ConfigProperties.mqttUser);
            connOpts.setPassword(ConfigProperties.mqttPassword.toCharArray());
            connOpts.setKeepAliveInterval(ConfigProperties.mqttKeepalive);
            connOpts.setAutomaticReconnect(true);
            mqttClient = new MqttClient(mqttUrl, ConfigProperties.mqttClientId, null);
            mqttClient.setCallback(this);
            boolean connected = connectToBroker();
            if (!connected) {
                scheduleReconnect();
            }
        } catch (Exception e) {
            logger.error("连接mqtt异常，", e);
        }
    }

    private static synchronized boolean connectToBroker() {
        try {
            logger.info("mqtt发起连接，状态：" + mqttClient.isConnected());
            if (!mqttClient.isConnected()) {
                mqttClient.connect(connOpts);
                logger.info("连接mqtt成功!");
                IS_CONNECTED.set(true);
                return true;
            }
            IS_CONNECTED.set(true);
            return true;
        } catch (Exception e) {
            logger.error("mqtt连接异常，重连间隔：" + Arrays.toString(retryIntervals) + "，异常信息：", e);
            return false;
        }
    }

    private synchronized void scheduleReconnect() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        try {
            logger.info("IS_CONNECTED:" + IS_CONNECTED.get() + ",mqttClient.isConnected():" + mqttClient.isConnected());
            if (IS_CONNECTED.get() && mqttClient.isConnected()) {
                return;
            }
            for (String interval : retryIntervals) {
                logger.info("重试间隔：" + interval);
                int value = Integer.parseInt(interval);
                ScheduledFuture<Boolean> schedule = scheduler.schedule(() -> {
                    logger.info("执行mqtt重连操作：" + interval);
                    boolean connected = connectToBroker();
                    if (connected) {
                        IS_CONNECTED.set(true);
                    }
                    return connected;
                }, value, TimeUnit.SECONDS);
                if (schedule.get()) {
                    break;
                }
            }
            if (!IS_CONNECTED.get()) {
                //全都不成功重新上线
                String secretKey = EncryptUtil.getMD5(ConfigProperties.productSecret).substring(0, 16);
                byte[] ivKey = EncryptUtil.getIvKey(ConfigProperties.productKey, null);
                HttpRequest.httpRetry(secretKey, ivKey, "online");
            }
        } catch (Exception e) {
            logger.error("mqtt重连任务异常，", e);
        } finally {
            if (!scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        try {
            subscribeTopic();
            logger.info("主题订阅成功!");
            dataReportingService.infoNotification(); // 首次上电  上报事件infoNotification
        } catch (Exception e) {
            logger.error("mqtt订阅主题异常，", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        logger.error("mqtt连接断开，原因：" + cause.toString());
        try {
            logger.info("休眠10秒后重连。");
            Thread.sleep(10000);
            IS_CONNECTED.set(false);
            logger.info("mqtt发起重连。");
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
            mqttClient.reconnect();
            logger.info("mqtt发起重连成功，休眠10秒获取连接结果。");
            Thread.sleep(10000);
            if (mqttClient.isConnected()) {
                logger.info("mqtt重连成功。");
                IS_CONNECTED.set(true);
            } else {
                logger.info("mqtt重连不成功，手动发起重连。");
                boolean connectedRes = connectToBroker();
                if (!connectedRes) {
                    scheduleReconnect();
                }
            }
        } catch (Exception e) {
            logger.error("mqtt重连异常，尝试重新发起连接，", e);
            boolean connectedRes = connectToBroker();
            if (!connectedRes) {
                scheduleReconnect();
            }
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        try {
            logger.info("接收到mqtt消息，topic：" + topic + ",message:" + mqttMessage);
            String secretKey = EncryptUtil.getMD5(ConfigProperties.deviceSecret).substring(0, 16);
            byte[] ivKey = EncryptUtil.getIvKey(ConfigProperties.productKey, ConfigProperties.deviceId);
            String decryptStr = EncryptUtil.decrypt(secretKey, new String(mqttMessage.getPayload()), ivKey);
            if (!StringUtil.isEmpty(decryptStr)) {
                logger.info("解密后数据：" + decryptStr);
                handlerMessage(topic, decryptStr, secretKey, ivKey);
            }
        } catch (Exception e) {
            logger.error("mqtt消息处理异常，", e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("Delivery complete");
    }

    private static void subscribeTopic() throws MqttException {
        String[] topics = new String[]{downTopic, upReplyTopic};
        mqttClient.subscribe(topics);
    }

    private void handlerMessage(String topic, String mqttMessage, String secretKey, byte[] ivKey) throws MqttException {
        if (downTopic.equals(topic)) {
            //调用服务
            JSONObject msg = new JSONObject(mqttMessage);
            String sourceId = msg.getString("id");
            String method = msg.getString("method");
            JSONObject params = null;
            if (msg.has("params")) {
                params = msg.getJSONObject("params");
            }
            IHandler handler = HandlerRegistry.getHandler(method);
            if (handler != null) {
                JSONObject replyObject = new JSONObject();
                replyObject.put("id", UUID.randomUUID().toString());
                replyObject.put("code", 200);
                replyObject.put("sourceId", sourceId);
                replyObject.put("message", "success");
                replyObject.put("method", method + "_reply");
                Object result = null;

                try {
                    result = handler.handle(method, params);
                    logger.info("handle result: " + result);
                } catch (Exception e) {
                    logger.error("服务调用异常：" + e);
                    replyObject.put("code", 500);
                    replyObject.put("message", e.getMessage());
                }

                if (!"thing.ota.notify".equals(method)) {
                    if (result instanceof Boolean) {
                        boolean handlerResult = (Boolean) result;
                        if (!handlerResult) {
                            replyObject.put("code", 500);
                            replyObject.put("message", "error");
                        }
                    } else if (result instanceof JSONArray) {
                        JSONObject data = new JSONObject();
                        data.put("list", result);
                        replyObject.put("data", data);
                    } else {
                        replyObject.put("data", result);
                    }
                    String jsonString = replyObject.toString();
                    String encrypt = EncryptUtil.encrypt(secretKey, jsonString, ivKey);
                    if (encrypt != null) {
                        MqttMessage message = new MqttMessage(encrypt.getBytes());
                        message.setQos(1);
                        mqttClient.publish(downReplyTopic, message);
                        logger.info("mqtt发送响应数据：" + jsonString);
                    }
                }
                return;
            }
            logger.error("找不到处理类，method：" + method);
        }
        if (upReplyTopic.equals(topic)) {
            // 处理设备请求升级的响应
            JSONObject msg = new JSONObject(mqttMessage);
            String method = msg.getString("method");
            if ("thing.ota.get_reply".equals(method)) {
                // 修改参数处理逻辑，适配实际参数结构
               /* JSONObject params = null;
                if (msg.has("data")) {
                    params = msg.getJSONObject("data");
                }*/
                IHandler handler = HandlerRegistry.getHandler(method);
                if (handler != null) {
                    try {
                        handler.handle(method, msg);
                    } catch (Exception e) {
                        logger.error("服务调用异常：" + e);
                    }
                } else {
                    logger.error("未找到设备请求升级响应处理类，method：" + method);
                }
            }
        }
//        if(productDownTopic.equals(topic)) {
//
//        }
    }

    public static void upProperties(JSONObject params) throws MqttException {
        if (mqttClient == null || !mqttClient.isConnected()) {
            boolean connected = connectToBroker();
            if (!connected) {
                return;
            }
        }
        JSONObject properties = new JSONObject();
        properties.put("id", UUID.randomUUID().toString());
        properties.put("version", "2.0.0");
        properties.put("method", "thing.property.post");
        properties.put("params", params);
        String jsonString = properties.toString();
        upData(jsonString);
    }

    public static void upEvent(Object params, String attribute) throws MqttException {
        if (mqttClient == null || !mqttClient.isConnected()) {
            boolean connected = connectToBroker();
            if (!connected) {
                return;
            }
        }
        JSONObject properties = new JSONObject();
        properties.put("id", UUID.randomUUID().toString());
        properties.put("version", "2.0.0");
        properties.put("method", "thing.event." + attribute + ".post");
        properties.put("params", params);
        String jsonString = properties.toString();
        upData(jsonString);
    }


    /**
     * 通用OTA消息发送方法
     * 7.3.3.2设备上报固件升级进度  7.3.3.3设备请求升级
     * 支持发送各种OTA相关消息，如进度上报、升级请求等
     * @param params 参数对象，根据attribute不同包含不同的字段
     * @param attribute 方法属性，如"per"表示进度上报，"get"表示请求升级
     * @throws MqttException MQTT异常
     */
    public static void upOTA(Object params, String attribute) throws MqttException {
        if (mqttClient == null || !mqttClient.isConnected()) {
            boolean connected = connectToBroker();
            if (!connected) {
                return;
            }
        }
        JSONObject properties = new JSONObject();
        properties.put("id", UUID.randomUUID().toString());
        properties.put("version", "2.0.0");
        properties.put("method", "thing.ota." + attribute);
        properties.put("params", params);
        String jsonString = properties.toString();
        upData(jsonString);
    }

    private static void upData(String jsonString) throws MqttException {
        String secretKey = EncryptUtil.getMD5(ConfigProperties.deviceSecret).substring(0, 16);
        byte[] ivKey = EncryptUtil.getIvKey(ConfigProperties.productKey, ConfigProperties.deviceId);
        String encrypt = EncryptUtil.encrypt(secretKey, jsonString, ivKey);
        if (!StringUtil.isEmpty(encrypt)) {
            MqttMessage message = new MqttMessage(encrypt.getBytes());
            message.setQos(1);
            logger.info("mqtt数据上报：" + jsonString);
            mqttClient.publish(upTopic, message);
        }
    }

    public static void disconnect() {
        IS_CONNECTED.set(false);
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
            } catch (MqttException e) {
                logger.error("mqtt断开连接异常：" + e.getMessage());
            }
        }
    }
}
