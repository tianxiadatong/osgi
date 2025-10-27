package com.wasu.osgi.model.hgu01.message;

import com.chinamobile.smartgateway.mangement.AbsSetMsgConfig;
import com.wasu.osgi.model.hgu01.connect.HttpRequest;
import com.wasu.osgi.model.hgu01.connect.MqttConnect;
import com.wasu.osgi.model.hgu01.service.IHardwareService;
import com.wasu.osgi.model.hgu01.service.impl.HardwareServiceImpl;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;

/**
 * @author: glmx_
 * @date: 2024/9/13
 * @description:
 */
public class MessageHandler extends AbsSetMsgConfig {

    private static final Logger logger = Logger.getLogger(MessageHandler.class);
    private static final String ONLINE_EVENTS = "WLAN_DEV_ONLINE,LAN_DEV_ONLINE";
    private static final String OFFLINE_EVENTS = "WLAN_DEV_OFFLINE,LAN_DEV_OFFLINE";
    private static final String CONNECT_FAILED = "WLAN_DEV_REFUSED,LAN_DEV_REFUSED";
    private static final String NETWORK_CONNECTED = "NOTIFY_DEST_ADDRESS_DETECTED";
    private static final String NETWORK_DISCONNECTED = "NOTIFY_DEST_ADDRESS_DISAPPEAR";
    private final IHardwareService hardwareService;

    public MessageHandler(BundleContext context) {
        super(context);
        hardwareService = HttpRequest.getHardwareService();
    }

    @Override
    public String SetMsgProcess(String msgContent) {
        logger.info("收到消息通知：" + msgContent);
        try {
            JSONObject jsonObject = new JSONObject(msgContent);
            String event = jsonObject.getString("Event");
            String radio = null;
            if (jsonObject.has("Interface")) {
                radio = jsonObject.getString("Interface");
            }
            if (ONLINE_EVENTS.contains(event)) {
                String macAddr = jsonObject.getString("MacAddr");
                JSONObject data = new JSONObject();
                data.put("mac", macAddr);
                JSONArray lanHostInfoByMAC = hardwareService.getLANHostInfoByMAC(new String[]{macAddr});
                if (lanHostInfoByMAC != null && lanHostInfoByMAC.length() > 0) {
                    JSONObject subDevice = lanHostInfoByMAC.getJSONObject(0);
                    data.put("name", subDevice.getString("DhcpName"));
                    data.put("deviceName", subDevice.getString("DeviceName"));
                    data.put("ipv4", subDevice.getString("IP"));
                    data.put("linkType", radio == null ? event.split("_")[0] : radio);
                }
                reportData("staOnlineNotification", data);
            }
            if (OFFLINE_EVENTS.contains(event)) {
                String macAddr = jsonObject.getString("MacAddr");
                JSONObject data = new JSONObject();
                data.put("mac", macAddr);
                reportData("staOfflineNotification", data);
            }
            if (CONNECT_FAILED.equals(event)) {
                String macAddr = jsonObject.getString("MacAddr");
            }
        } catch (Exception e) {
            logger.error("处理推送消息异常，", e);
        }
        return "success";
    }

    private void reportData(String attribute, JSONObject data) throws MqttException {
        MqttConnect.upEvent(data, attribute);
    }
}
