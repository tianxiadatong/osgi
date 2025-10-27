package com.wasu.osgi.model.hgu01.timetask;

import com.wasu.osgi.model.hgu01.connect.HttpRequest;
import com.wasu.osgi.model.hgu01.connect.MqttConnect;
import com.wasu.osgi.model.hgu01.service.IHardwareService;
import com.wasu.osgi.model.hgu01.service.impl.HardwareServiceImpl;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author glmx_
 */
public class ClientInfoReportTask extends AbstractScheduled {

    private static final Logger logger = Logger.getLogger(ClientInfoReportTask.class);

    private final IHardwareService hardwareService;

    public ClientInfoReportTask() {
        hardwareService = HttpRequest.getHardwareService();
    }

    @Override
    public void run() {
        reportData();
    }

    private void reportData() {
        try {
            JSONArray hostList = hardwareService.staList();
            if (hostList != null && hostList.length() > 0) {
                JSONArray list = new JSONArray();
                for (int i = 0; i < hostList.length(); i++) {
                    JSONObject jsonObject = hostList.getJSONObject(i);
                    String mac = jsonObject.getString("mac");
                    JSONObject lanHostSpeed = hardwareService.getLANHostSpeed(mac);
                    JSONObject data = new JSONObject();
                    data.put("mac", mac);
                    //获取下挂设备限速信息有则返回upSpeed downSpeed
                    if (lanHostSpeed != null) {
                        if (lanHostSpeed.has("upSpeed")) {
                            data.put("upSpeed", lanHostSpeed.getInt("upSpeed"));
                        }
                        if (lanHostSpeed.has("downSpeed")) {
                            data.put("downSpeed", lanHostSpeed.getInt("downSpeed"));
                        }
                    }
                    data.put("keepTime", jsonObject.getString("keepTime"));
                    if (jsonObject.has("rxRate")) {
                        data.put("rxRate", jsonObject.getString("rxRate"));
                    }
                    if (jsonObject.has("txRate")) {
                        data.put("txRate", jsonObject.getString("txRate"));
                    }
                    if (jsonObject.has("rssi")) {
                        data.put("rssi", jsonObject.getString("rssi"));
                    }
                    list.put(data);
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("list", list);
                MqttConnect.upEvent(jsonObject, "deviceDataNotification");
            }
        } catch (Exception e) {
            logger.error("属性定时上报异常，", e);
        }
    }
}
