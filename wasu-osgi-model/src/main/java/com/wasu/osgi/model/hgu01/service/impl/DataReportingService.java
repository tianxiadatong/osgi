package com.wasu.osgi.model.hgu01.service.impl;

import com.wasu.osgi.model.hgu01.connect.HttpRequest;
import com.wasu.osgi.model.hgu01.connect.MqttConnect;
import com.wasu.osgi.model.hgu01.message.MessageHandler;
import com.wasu.osgi.model.hgu01.service.IHardwareService;
import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * 数据上报服务
 *
 * @author Vicky
 * @date 2025年04月24日 16:00
 */

public class DataReportingService {
    private static final Logger logger = Logger.getLogger(MessageHandler.class);

    private final IHardwareService hardwareService;

    public DataReportingService() {
//        hardwareService = new HardwareServiceImpl();
        hardwareService = HttpRequest.getHardwareService();
    }

    /**
     * 事件上报：infoNotification
     *
     * @author Vicky
     * @date 2025/4/24 16:50
     */
    public void infoNotification() {
        logger.info("开始首次上电infoNotification上报");
        try {
            JSONObject params = new JSONObject();
            JSONObject deviceInfo = hardwareService.getDeviceInfo();
            int maxClient = hardwareService.getMaxAllowedUsers();
            if (deviceInfo != null) {
                params.put("cpuType", deviceInfo.getString("MainChipClass"));
                params.put("flash", deviceInfo.getInt("FlashSize"));
                params.put("wireless", "WIFI5");  //无线协议
                params.put("maxClient", maxClient);   //最大允许接入用户数
                params.put("supportIPV6", 1);  //是否支持ipv6，0不支持，1支持
                params.put("supportVlan", 1);  //是否支持vlan，0不支持，1支持
                MqttConnect.upEvent(params, "infoNotification");
            }
        } catch (Exception e) {
            logger.error("上报事件 infoNotification 失败: " + e.getMessage());
        }
    }
}
