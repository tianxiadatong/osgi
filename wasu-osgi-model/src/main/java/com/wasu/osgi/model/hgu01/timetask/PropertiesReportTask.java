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
public class PropertiesReportTask extends AbstractScheduled {

    private static final Logger logger = Logger.getLogger(PropertiesReportTask.class);

    private final IHardwareService hardwareService;

    public PropertiesReportTask() {
        hardwareService = HttpRequest.getHardwareService();
    }

    @Override
    public void run() {
        reportProperties();
    }

    private void reportProperties() {
        try {
            JSONObject params = new JSONObject();
            params.put("cpuRate", hardwareService.getCPUUsage());
            params.put("ramRate", hardwareService.getMemUsage());
            params.put("runningTime", hardwareService.getDeviceUpTime());
            JSONObject ponIfPhyStatus = hardwareService.getPONIfPhyStatus();
            if (ponIfPhyStatus != null) {
                params.put("transceiverTemperature", ponIfPhyStatus.getDouble("transceiverTemperature"));
                params.put("txPower", ponIfPhyStatus.getDouble("txPower"));
                params.put("rxPower", ponIfPhyStatus.getDouble("rxPower"));
            }
            JSONObject uplinkIfStats = hardwareService.getUplinkIfStats();
            if (uplinkIfStats != null) {
                params.put("upFlow", uplinkIfStats.getLong("UsStats") / 1024);
                params.put("downFlow", uplinkIfStats.getLong("DsStats") / 1024);
                params.put("flow", (uplinkIfStats.getLong("UsStats") + uplinkIfStats.getLong("DsStats")) / 1024);
            }
            JSONArray staList = hardwareService.staList();
            params.put("staNumber", staList.length());
            JSONObject wanIndex = hardwareService.getWanIndex("_INTERNET_R_VID_");
            if (wanIndex != null) {
                JSONObject wanIfBandwidth = hardwareService.getWanIfBandwidth(wanIndex.getInt("Index"));
                if (wanIfBandwidth != null) {
                    params.put("txRate", wanIfBandwidth.getLong("DsBandwidth"));
                    params.put("rxRate", wanIfBandwidth.getLong("UsBandwidth"));
                }
                JSONObject wanIfInfo = hardwareService.getWANIfInfo(wanIndex.getInt("Index"));
                if (wanIfInfo != null) {
                    params.put("ipAddress", wanIfInfo.getString("ExternalIPAddress"));
                    params.put("ipv6Address", wanIfInfo.getString("IPv6IPAddress"));
                    params.put("connectionType", wanIfInfo.getString("ConnectionType"));
                }
            }
            if (params.length() > 0) {
                MqttConnect.upProperties(params);
            }
        } catch (Exception e) {
            logger.error("属性定时上报异常，", e);
        }
    }
}
