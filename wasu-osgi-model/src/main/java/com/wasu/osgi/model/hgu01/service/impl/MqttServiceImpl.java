package com.wasu.osgi.model.hgu01.service.impl;

import com.wasu.osgi.model.hgu01.connect.MqttConnect;
import com.wasu.osgi.upgrade.service.IMqttService;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * @ClassName MqttServiceImpl
 * @Description
 * @Author fangshengqun
 * @Date 2025/10/27 13:58
 **/
public class MqttServiceImpl implements IMqttService {

    @Override
    public void upOTA(Object params, String attribute) {
        try {
            MqttConnect.upOTA(params, attribute);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
}
