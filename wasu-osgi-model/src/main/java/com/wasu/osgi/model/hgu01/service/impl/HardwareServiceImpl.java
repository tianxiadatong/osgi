package com.wasu.osgi.model.hgu01.service.impl;

import com.wasu.osgi.model.hgu01.Activator;
import com.wasu.osgi.model.hgu01.config.CommonConstant;
import com.wasu.osgi.model.hgu01.config.HguContext;
import com.wasu.osgi.model.hgu01.config.SkyWorthWifiEnum;
import com.wasu.osgi.model.hgu01.connect.MqttConnect;
import com.wasu.osgi.model.hgu01.service.IHardwareService;
import com.wasu.osgi.model.hgu01.util.CommonUtil;
import com.wasu.osgi.model.hgu01.util.ConfigUtil;
import com.wasu.osgi.model.hgu01.util.FuncNameEnum;
import com.wasu.osgi.model.hgu01.util.StringUtil;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: glmx_
 * @date: 2024/8/21
 * @description:
 */
public class HardwareServiceImpl extends InitReferenceService implements IHardwareService {
    private static final Logger logger = Logger.getLogger(HardwareServiceImpl.class);
    private TimeLimitService timeLimitService;

    public TimeLimitService getTimeLimitService() {
        if (timeLimitService == null) {
            timeLimitService = new TimeLimitService(HguContext.getSharedTaskManager(), this);
        }
        return timeLimitService;
    }

    @Override
    public String getManufacturer() {
        return null;
    }

    @Override
    public String getOUI() {
        return null;
    }

    @Override
    public String getModel() {
        return null;
    }

    @Override
    public JSONObject getType() {
        return null;
    }

    @Override
    public String getSN() {
        if (deviceSecretInfoQueryService != null) {
            return deviceSecretInfoQueryService.getDeviceSerialNumber();
        }
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
    public String getMAC() {
        if (deviceInfoQueryService != null) {
            return deviceInfoQueryService.getDeviceMAC();
        }
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
    public Integer getDeviceUpTime() {
        if (deviceInfoQueryService != null) {
            return deviceInfoQueryService.getDeviceUpTime();
        }
        return null;
    }

    @Override
    public Integer getCPUUsage() {
        if (deviceInfoQueryService != null) {
            return deviceInfoQueryService.getCPUOccupancyRate();
        }
        return null;
    }

    @Override
    public Integer getMemUsage() {
        if (deviceInfoQueryService != null) {
            return deviceInfoQueryService.getRAMOccupancyRate();
        }
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
    public void reboot() {
        deviceRsetService.reset();
    }

    @Override
    public JSONObject factoryReset() {
        return null;
    }

    @Override
    public boolean deviceTimeLimit(JSONObject params) {
        return timeLimitService.createTimeLimit(params);
    }

    @Override
    public boolean removeDeviceTimeLimit(JSONObject params) {
        return false;
    }

    @Override
    public JSONArray getSubTimeLimitList(JSONObject params) {
        if (params != null && params.has("mac")) {
            return timeLimitService.queryTimeLimits(params.getString("mac"));
        }
        return null;
    }

    @Override
    public boolean removeSubTimeLimitList(JSONObject params) {
        return timeLimitService.removeOneTimeLimit(params);
    }

    @Override
    public boolean setSubTimeLimitList(JSONObject params) {
        return timeLimitService.setTimeLimitStatus(params);
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
    public boolean speedTest(JSONObject params) {
        JSONObject wanIndex = getWanIndex("_INTERNET_R_VID_");
        if (wanIndex == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        long delay = 0;
        if (params.has("time")) {
            long time = params.getLong("time");
            String event = "speedTest";
            boolean checkRes = isTaskExist(event, String.valueOf(time), null);
            if (checkRes) {
                return false;
            }
            storeTaskInfo(event, String.valueOf(time), null);
            delay = time - now;
            if (delay < 0) {
                return false;
            }
        }
        JSONObject wanIfInfo = getWANIfInfo(wanIndex.getInt("Index"));
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            /**
             * 下载：http://c1.speedtest.hzcnc.com:12347/shmfile/2000
             * 上传：http://c1.speedtest.hzcnc.com:12347/upload
             */
            int index = wanIndex.getInt("Index");
            String extStr = new JSONObject().toString();
            logger.info("startHttpDownloadDiagnostics入参：wanIndex:" + index + ",url:" + params.getString("downloadUrl") + ",parameter:" + extStr);
            int downRes = httpDownloadDiagnosticsService.startHttpDownloadDiagnostics(index, params.getString("downloadUrl"), extStr);
            logger.info("startHttpDownloadDiagnostics结果：" + downRes);
            logger.info("startHttpUploadDiagnostics入参：wanIndex:" + index + ",url:" + params.getString("uploadUrl") + ",parameter:" + extStr);
            int upRes = httpUploadDiagnosticsService.startHttpUploadDiagnostics(index, params.getString("uploadUrl"), extStr);
            logger.info("startHttpUploadDiagnostics结果：" + upRes);
            AtomicInteger sendCount = new AtomicInteger();
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(() -> {
                sendCount.getAndIncrement();
                try {
                    JSONObject speedTestResult = getSpeedTestResult();
                    if (speedTestResult.has("upAvgRate") && speedTestResult.has("downAvgRate")) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("upAvgRate", speedTestResult.getInt("upAvgRate"));
                        jsonObject.put("downAvgRate", speedTestResult.getInt("downAvgRate"));
                        if (wanIfInfo != null) {
                            jsonObject.put("ipAddress", wanIfInfo.getString("ExternalIPAddress"));
                        }

                        String startTimeStr = speedTestResult.getString("startTime");
                        String endTimeStr = speedTestResult.getString("endTime");

                        // 直接提取时间部分（HH:mm:ss）
                        LocalTime startTime = LocalTime.parse(startTimeStr.substring(11), DateTimeFormatter.ofPattern("HH:mm:ss"));
                        LocalTime endTime = LocalTime.parse(endTimeStr.substring(11), DateTimeFormatter.ofPattern("HH:mm:ss"));

                        // 计算从 00:00:00 开始的秒数
                        long secondsStartTime = startTime.toSecondOfDay();
                        long secondsEndTime = endTime.toSecondOfDay();

                        jsonObject.put("startTime", secondsStartTime);
                        jsonObject.put("endTime", secondsEndTime);
                        jsonObject.put("dataLength", speedTestResult.getInt("dataLength"));
                        MqttConnect.upEvent(jsonObject, "speedTestReport");
                        executorService.shutdown();
                    }
                    if (sendCount.get() >= 5) {
                        executorService.shutdown();
                    }
                } catch (MqttException e) {
                    logger.error("上报测速结果异常：" + e.getMessage());
                }
            }, 30, 30, TimeUnit.SECONDS);
        }, delay, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public JSONObject getSpeedTestResult() {
        JSONObject jsonObject = new JSONObject();
        String httpDownloadDiagnosticsResult = httpDownloadDiagnosticsService.getHttpDownloadDiagnosticsResult();
        logger.info("测速下载结果：" + httpDownloadDiagnosticsResult);
        JSONObject downObj = new JSONObject(httpDownloadDiagnosticsResult);
        if ("Completed".equals(downObj.getString("Result"))) {
            int downloadRate = downObj.getInt("TestDownloadRate");
            jsonObject.put("downAvgRate", downloadRate / 1024);
            jsonObject.put("testTime", formatTime(downObj.getString("ROMTime")));
            jsonObject.put("startTime", formatTime(downObj.getString("BOMTime")));
            jsonObject.put("endTime", formatTime(downObj.getString("EOMTime")));
            jsonObject.put("dataLength", downObj.getLong("TestBytesReceived") / 1024);
        } else {
            return downObj;
        }
        String httpUploadDiagnosticsResult = httpUploadDiagnosticsService.getHttpUploadDiagnosticsResult();
        logger.info("测速上传结果：" + httpUploadDiagnosticsResult);
        JSONObject upObj = new JSONObject(httpUploadDiagnosticsResult);
        if ("Completed".equals(upObj.getString("Result"))) {
            int uploadRate = upObj.getInt("TestUploadRate");
            jsonObject.put("upAvgRate", uploadRate / 1024);
            jsonObject.put("testTime", formatTime(upObj.getString("ROMTime")));
            jsonObject.put("startTime", formatTime(upObj.getString("BOMTime")));
            jsonObject.put("endTime", formatTime(upObj.getString("EOMTime")));
            jsonObject.put("dataLength", upObj.getLong("TestBytesSent") / 1024);
        } else {
            return upObj;
        }
        return jsonObject;
    }

    private String formatTime(String time) {
        if (StringUtil.isEmpty(time)) {
            return "";
        }
        return time.split("\\.")[0];
    }

    @Override
    public boolean pingTest(JSONObject params) {
        long now = System.currentTimeMillis();
        long delay = 0;
        if (params.has("time")) {
            long time = params.getLong("time");
            String event = "pingTest";
            boolean checkRes = isTaskExist(event, String.valueOf(time), null);
            if (checkRes) {
                return false;
            }
            storeTaskInfo(event, String.valueOf(time), null);
            delay = time - now;
            if (delay < 0) {
                return false;
            }
        }
        JSONObject wanIndex = getWanIndex("_INTERNET_R_VID_");
        if (wanIndex == null) {
            return false;
        }
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            int index = wanIndex.getInt("Index");
            JSONObject parameter = new JSONObject(1);
            parameter.put("NumberOfRepetitions", params.getInt("tests"));
            logger.info("startIPPingDiagnostics 入参：wanIndex:" + index + ",url:" + params.getString("url") + ",parameter:" + parameter);
            int result = ipPingDiagnosticsService.startIPPingDiagnostics(index, params.getString("url"), parameter.toString());
            logger.info("startIPPingDiagnostics result:" + result);
            AtomicInteger sendCount = new AtomicInteger();
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(() -> {
                sendCount.getAndIncrement();
                JSONObject pingResult = getPingResult();
                if (pingResult.has("successCount")) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("targetUrl", params.getString("url"));
                        jsonObject.put("avgLatency", pingResult.getInt("avgLatency"));
                        jsonObject.put("maxLatency", pingResult.getInt("maxLatency"));
                        jsonObject.put("minLatency", pingResult.getInt("minLatency"));
                        jsonObject.put("successCount", pingResult.getInt("successCount"));
                        jsonObject.put("failureCount", pingResult.getInt("failureCount"));
                        MqttConnect.upEvent(jsonObject, "pingReport");
                        executorService.shutdown();
                    } catch (MqttException e) {
                        logger.error("上报ping结果异常：" + e.getMessage());
                    }
                }
                if (sendCount.get() >= 5) {
                    executorService.shutdown();
                }
            }, 30, 30, TimeUnit.SECONDS);

        }, delay, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public JSONObject getPingResult() {
        JSONObject resultOjb = new JSONObject();
        String ipPingDiagnosticsResult = ipPingDiagnosticsService.getIPPingDiagnosticsResult();
        JSONObject jsonObject = new JSONObject(ipPingDiagnosticsResult);
        int result = jsonObject.getInt("Result");
        if (result == 0) {
            resultOjb.put("successCount", jsonObject.getInt("SuccessCount"));
            resultOjb.put("failureCount", jsonObject.getInt("FailureCount"));
            resultOjb.put("avgLatency", jsonObject.getInt("AverageResponseTime"));
            resultOjb.put("minLatency", jsonObject.getInt("MinimumResponseTime"));
            resultOjb.put("maxLatency", jsonObject.getInt("MaximumResponseTime"));
        } else {
            resultOjb = jsonObject;
        }
        return resultOjb;
    }

    @Override
    public boolean tracerouteTest(JSONObject params) {
        JSONObject wanIndex = getWanIndex("_INTERNET_R_VID_");
        if (wanIndex == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        long delay = 0;
        if (params.has("time")) {
            long time = params.getLong("time");
            String event = "tracerouteTest";
            boolean checkRes = isTaskExist(event, String.valueOf(time), "");
            if (checkRes) {
                return false;
            }
            storeTaskInfo(event, String.valueOf(time), null);
            delay = time - now;
            if (delay < 0) {
                return false;
            }
        }
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            int index = wanIndex.getInt("Index");
            JSONObject parameter = new JSONObject(1);
            int tests = params.optInt("tests", 3);
            parameter.put("NumberOfTries", tests);
            logger.info("setTracerouteParameter 入参：wanIndex:" + index + ",url:" + params.getString("url") + ",parameter:" + params);
            int result = traceRouteDiagnosticsService.startTraceRouteDiagnostics(index, params.getString("url"), parameter.toString());
            logger.info("startTraceRouteDiagnostics result:" + result);
            AtomicInteger sendCount = new AtomicInteger();
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(() -> {
                sendCount.getAndIncrement();
                try {
                    JSONObject tracerouteResult = getTracerouteResult();
                    if (tracerouteResult.has("hopLatency")) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("tests", tests);
                        jsonObject.put("targetUrl", params.getString("url"));
                        jsonObject.put("hopLatency", tracerouteResult.getString("hopLatency"));
                        MqttConnect.upEvent(jsonObject, "tracerouteReport");
                        executorService.shutdown();
                    }
                } catch (MqttException e) {
                    logger.error("上报traceroute结果异常：" + e.getMessage());
                }
                if (sendCount.get() >= 5) {
                    executorService.shutdown();
                }
            }, 30, 30, TimeUnit.SECONDS);
        }, delay, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public JSONObject getTracerouteResult() {
        JSONObject resultOjb = new JSONObject();
        String traceRouteDiagnosticsResult = traceRouteDiagnosticsService.getTraceRouteDiagnosticsResult();
        JSONObject jsonObject = new JSONObject(traceRouteDiagnosticsResult);
        int result = jsonObject.getInt("Result");
        if (result == 0) {
            JSONArray routeHops = jsonObject.getJSONArray("RouteHops");
            if (routeHops != null && routeHops.length() > 0) {
                StringBuilder hopLatency = new StringBuilder();
                for (int i = 0; i < routeHops.length(); i++) {
                    JSONObject resObj = routeHops.getJSONObject(i);
                    String hopHostAddress = resObj.getString("HopHostAddress");
                    String hopRTTimes = resObj.getString("HopRTTimes");
                    hopLatency.append(hopHostAddress).append(",").append(hopRTTimes).append(";");
                }
                resultOjb.put("hopLatency", hopLatency.toString());
                return resultOjb;
            }
        }
        return jsonObject;
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
    public JSONObject hwinfo() {
        return null;
    }

    @Override
    public JSONObject getPonSN() {
        return null;
    }

    @Override
    public JSONArray networkInterface() {
        return null;
    }

    @Override
    public int getMaxAllowedUsers() {
        if (deviceInfoQueryService != null) {
            return deviceInfoQueryService.getMaxAllowedUsers();
        }
        return 0;
    }

    @Override
    public JSONObject getDeviceInfo() {
        if (deviceInfoQueryService != null) {
            JSONObject jsonObject = new JSONObject(deviceInfoQueryService.getDeviceInfo());
            int result = jsonObject.getInt("Result");
            if (result == 0) {
                return jsonObject;
            }
        }
        return null;
    }

    @Override
    public JSONObject getUplinkIfStats() {
        if (accessInfoQueryService != null) {
            JSONObject jsonObject = new JSONObject(accessInfoQueryService.getUplinkIfStats());
            int result = jsonObject.getInt("Result");
            if (result == 0) {
                return jsonObject;
            }
        }
        return null;
    }

    @Override
    public JSONArray getWANIfList() {
        if (accessInfoQueryService != null) {
            JSONObject jsonObject = new JSONObject(accessInfoQueryService.getWANIfList());
            int result = jsonObject.getInt("Result");
            if (result == 0) {
                return jsonObject.getJSONArray("List");
            }
        }
        return new JSONArray();
    }

    @Override
    public JSONObject getWanIfBandwidth(Integer index) {
        if (accessInfoQueryService != null) {
            JSONObject jsonObject = new JSONObject(accessInfoQueryService.getWanIfBandwidth(index));
            int result = jsonObject.getInt("Result");
            if (result == 0) {
                return jsonObject;
            }
        }
        return null;
    }

    @Override
    public JSONObject getWANIfInfo(Integer index) {
        if (accessInfoQueryService != null) {
            JSONObject jsonObject = new JSONObject(accessInfoQueryService.getWANIfInfo(index));
            int result = jsonObject.getInt("Result");
            if (result == 0) {
                return jsonObject;
            }
        }
        return null;
    }

    @Override
    public JSONObject getPONIfPhyStatus() {
        if (accessInfoQueryService != null) {
            JSONObject jsonObject = new JSONObject(accessInfoQueryService.getPONIfPhyStatus());
            int result = jsonObject.getInt("Result");
            if (result == 0) {
                JSONObject object = new JSONObject();
                object.put("transceiverTemperature", jsonObject.getInt("TransceiverTemperature") / 1000);
                object.put("txPower", Math.round(jsonObject.getInt("TXPower") / 1000.0) / 10.0);  // 保留一位小数
                object.put("rxPower", Math.round(jsonObject.getInt("RXPower") / 1000.0) / 10.0);  // 保留一位小数
                return object;
            }
        }
        return null;
    }

    @Override
    public boolean scheduledRestart(JSONObject params) {
        int result = -1;  // 0 - 成功， -1 - 失败
        if (deviceRsetService != null) {
            Long time = params.has("time") ? params.getLong("time") : null; //时间点，从00：00开始，单位秒
            String days = params.has("days") ? params.getString("days") : null; //时间周期，星期一-星期日 ; 格式：1000000 - 每周一
            Integer enable = params.has("enable") ? params.getInt("enable") : null; //定时开关，1开启，0关闭

            if (enable != null && enable == 0) {
                result = deviceRsetService.scheduledRestart(false, days, CommonUtil.convertSecondsToHourMinute(time));
            }

            if (time != null && days != null && enable != null) {
                result = deviceRsetService.scheduledRestart(enable == 1, days, CommonUtil.convertSecondsToHourMinute(time));
            }
        }
        return result == 0;
    }

    @Override
    public boolean setSwitchStatus(JSONObject params) {
        if (deviceRsetService != null) {
            String funcName = params.getString("funcName"); //功能名称枚举： scheduledRestart（定时重启）
            int enable = params.getInt("enable");  //开关，1开启，0关闭
            if (funcName != null & FuncNameEnum.SCHEDULED_RESTART.getCode().equalsIgnoreCase(funcName)) {
                int result = deviceRsetService.setScheduledRestartStatus(enable == 1);
                return result == 0;
            }
        }
        return false;
    }

    @Override
    public JSONArray getScheduledRestart() {
        if (deviceRsetService != null) {
            JSONArray resultList = new JSONArray();
            JSONObject jsonObject = new JSONObject(deviceRsetService.getScheduledRestart());
            logger.info("getScheduledRestart: " + jsonObject);
            int result = jsonObject.getInt("Result");
            if (result == 0) {
                JSONArray list = jsonObject.getJSONArray("List");
                for (int i = 0; i < list.length(); i++) {
                    JSONObject resultObj = new JSONObject();
                    JSONObject object = list.getJSONObject(i);
                    resultObj.put("id", object.getInt("Id"));
                    resultObj.put("enable", object.getString("EnableState").equals("true") ? 1 : 0);
                    resultObj.put("days", object.getString("Days"));

                    String timeStr = object.getString("Time");
                    if (timeStr != null && !timeStr.trim().isEmpty()) {
                        resultObj.put("time", CommonUtil.convertHourMinuteToSeconds(timeStr));
                    } else {
                        resultObj.put("time", 0);
                    }
//                    resultObj.put("time", CommonUtil.convertHourMinuteToSeconds(object.getString("Time")));
                    resultList.put(resultObj);
                }
                return resultList;
            }
        }
        return null;
    }

    @Override
    public boolean removeScheduledRestart(JSONObject params) {
        int result = -1; // 0 - 成功， -1 - 失败
        if (deviceRsetService != null && params.has("id")) {
            result = deviceRsetService.removeScheduledRestart(params.getInt("id"));
        }
        return result == 0;
    }

    @Override
    public boolean setLightStatus(JSONObject params) {
        if (deviceConfigService != null) {
            int enable = params.getInt("enable");
            //1开启，0关闭
            int result = deviceConfigService.setLedSwitch(enable == 1);
            return result == 0;
        }
        return false;
    }

    @Override
    public JSONObject getLightStatus() {
        if (deviceConfigService != null) {
            boolean result = deviceConfigService.getLedSwitch();
            JSONObject object = new JSONObject();
            object.put("enable", result ? 1 : 0);  // 1 - 开启 ； 0 - 关闭
            return object;
        }
        return null;
    }

    @Override
    public JSONArray getPortList() {
        JSONArray result = new JSONArray();
//        result.put(getWANIfStatus(0));   // 0-光纤口，不需要上报
        result.put(getLANEthernetInfo(1));
        result.put(getLANEthernetInfo(2));
        result.put(getLANEthernetInfo(3));
        result.put(getLANEthernetInfo(4));
        return result;
    }

    @Override
    public JSONObject getWANIfStatus(Integer wanIndex) {
        if (accessInfoQueryService != null) {
            JSONObject jsonObject = new JSONObject(accessInfoQueryService.getWANIfStatus(wanIndex));
            int result = jsonObject.getInt("Result");
            if (result == 0) {
                JSONObject object = new JSONObject();
                String connectionStatus = jsonObject.getString("ConnectionStatus");
                String uptime = jsonObject.getString("Uptime");

                LocalDateTime dateTime = LocalDateTime.parse(uptime);
                long timestamp = dateTime.atZone(ZoneId.systemDefault()).toEpochSecond();

                object.put("port", wanIndex);
                object.put("status", connectionStatus.equals("Connected ") ? 1 : 0);
                object.put("linkTime", timestamp);

                return object;
            }
        }
        return null;
    }

    @Override
    public JSONObject getLANEthernetInfo(Integer portIndex) {
        if (ethQueryService != null) {
            JSONObject jsonObject = new JSONObject(ethQueryService.getLANEthernetInfo(portIndex));
            int result = jsonObject.getInt("Result");
            if (result == 0) {
                JSONObject object = new JSONObject();
                object.put("port", portIndex);
                object.put("status", jsonObject.getString("Status").equals("Up") ? 1 : 0);
//                object.put("linkTime",-1);  //该字段无值，不上报
                return object;
            }
        }
        return null;
    }

    @Override
    public boolean channelInterference() {
        try {

            JSONArray wLANNeighbor24 = getWLANNeighbor("2.4G");
            JSONArray wLANNeighbor5 = getWLANNeighbor("5G");

            JSONArray list = new JSONArray();

            // 添加 2.4G 数据
            for (int i = 0; i < wLANNeighbor24.length(); i++) {
                list.put(wLANNeighbor24.get(i));
            }

            // 添加 5G 数据
            for (int i = 0; i < wLANNeighbor5.length(); i++) {
                list.put(wLANNeighbor5.get(i));
            }

            if (list.length() > 0) {
                logger.info("准备上报事件channelInterferenceNotification");
                MqttConnect.upEvent(list, "channelInterferenceNotification");
            }
        } catch (MqttException e) {
            logger.error("上报信道干扰结果异常：" + e.getMessage());
        }
        return true;
    }

    @Override
    public JSONArray getWLANNeighbor(String radioType) {
        if (wlanQueryService != null) {
            logger.info("开始调用getWLANNeighbor");
            JSONObject wLANNeighbor = new JSONObject(wlanQueryService.getWLANNeighbor(radioType, "TRUE"));
            int result = wLANNeighbor.getInt("Result");
            if (result == 0
                    && wLANNeighbor.getJSONArray("List") != null
                    && wLANNeighbor.getJSONArray("List").length() > 0) {
                JSONArray neighborList = wLANNeighbor.getJSONArray("List");
                logger.info("neighborList: " + neighborList);

                JSONArray topList = new JSONArray();
                int count = Math.min(20, neighborList.length()); // 最多取 20 条
                for (int i = 0; i < count; i++) {
                    JSONObject item = neighborList.optJSONObject(i);
                    if (item != null) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("ssid", item.optString("SSIDName", "").trim());
                        jsonObject.put("mac", item.optString("BSSID", "").trim());
                        jsonObject.put("rssi", item.optString("RSSI", "").trim());
                        jsonObject.put("channel", item.optString("Channel", "").trim());
                        jsonObject.put("radio", radioType);
                        topList.put(jsonObject);
                    }
                }
                return topList;
            }
        }
        return new JSONArray();
    }

    @Override
    public JSONArray getWLANHostInfo(String[] macs) {
        if (wlanQueryService != null) {
            JSONObject jsonObject = new JSONObject(wlanQueryService.getWLANHostInfo(macs));
            int result = jsonObject.getInt("Result");
            if (result == 0) {
                return jsonObject.getJSONArray("List");
            }
        }
        return new JSONArray();
    }

    @Override
    public JSONObject getLANHostSpeed(String mac) {
        if (lanHostSpeedQueryService != null) {
            JSONObject jsonObject = new JSONObject(lanHostSpeedQueryService.getLANHostSpeed(mac));
            int result = jsonObject.getInt("Result");
            if (result == 0) {
                return jsonObject;
            }
        }
        return null;
    }

    @Override
    public JSONArray getLANHostInfoByMAC(String[] macs) {
        if (lanHostsInfoQueryService != null) {
            JSONObject jsonObject = new JSONObject(lanHostsInfoQueryService.getLANHostInfoByMAC(macs));
            int result = jsonObject.getInt("Result");
            if (result == 0) {
                return jsonObject.getJSONArray("List");
            }
        }
        return new JSONArray();
    }

    @Override
    public JSONArray getBundleList() {
        BundleContext bundleContext = FrameworkUtil.getBundle(Activator.class).getBundleContext();
        Bundle[] bundles = bundleContext.getBundles();
        JSONArray jsonArray = new JSONArray();
        for (Bundle bundle : bundles) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("bundleId", bundle.getBundleId());
            jsonObject.put("symbolicName", bundle.getSymbolicName());
            jsonObject.put("version", bundle.getVersion());
            jsonObject.put("state", bundle.getState());
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    @Override
    public int setDeviceConfig(String productKey, String productSecret, String deviceId, String deviceSecret) {
        int pk = deviceConfigService.setProductKey(productKey);
        int ps = deviceConfigService.setProductSKey(productSecret);
        int did = deviceConfigService.setDeviceID(deviceId);
        int ds = deviceConfigService.setDeviceSKey(deviceSecret);
        return pk + ps + did + ds;
    }

    @Override
    public JSONObject getDeviceConfig() {
        JSONObject jsonObject = new JSONObject();
        String productKey = deviceInfoQueryService.getProductKey();
        if (!StringUtil.isEmpty(productKey) && !"-1".equals(productKey)) {
            jsonObject.put("productKey", productKey);
        }
        String productSecret = deviceInfoQueryService.getProductSKey();
        if (!StringUtil.isEmpty(productSecret) && !"-1".equals(productSecret)) {
            jsonObject.put("productSecret", productSecret);
        }
        String deviceId = deviceInfoQueryService.getDeviceID();
        if (!StringUtil.isEmpty(deviceId) && !"-1".equals(deviceId)) {
            jsonObject.put("deviceId", deviceId);
        }
        String deviceSecret = deviceInfoQueryService.getDeviceSKey();
        if (!StringUtil.isEmpty(deviceSecret) && !"-1".equals(deviceSecret)) {
            jsonObject.put("deviceSecret", deviceSecret);
        }
        return jsonObject;
    }

    @Override
    public void wifiSetting(JSONObject params) {
        int visible = params.getInt("visible");
        // 开启/关闭双频合一：  0：关闭双频合一，1：开启双频合一
        boolean isBoth = params.has("radioBoth") && params.getInt("radioBoth") == 1;
        // 确定需要配置的频段列表
        int[] indices;
        if (isBoth) {
            indices = new int[]{1, 5}; // 2.4G 和 5G
        } else {
            String radio = params.getString("radio");
            int index = "2.4G".equals(radio) ? 1 : 5;
            indices = new int[]{index};
        }
        // 开启/关闭双频合一：  0：关闭双频合一，1：开启双频合一
        if (params.has("radioBoth")) {
            int radioBoth = params.getInt("radioBoth");
            deviceConfigService.setDualBandFusion(radioBoth == 1);
        }
        // 对每个频段进行配置
        for (int index : indices) {
            String currentRadio = (index == 1) ? "2.4G" : "5G";
            int statusRes = wlanConfigService.setWLANSSIDSwitch(index, visible == 1);
            if (statusRes == 0) {
                String ssid = params.has("ssid") ? params.getString("ssid") : null;
                logger.info("设置ssid：" + ssid);

                String encrypt = params.has("encrypt") ? params.getString("encrypt") : null;
                logger.info("encrypt:" + encrypt);

                String pwd = params.has("pwd") ? params.getString("pwd") : null;
                logger.info("pwd:" + pwd);

                // 如果有缺，就从ssidInfo补齐
                if (ssid == null || encrypt == null || pwd == null) {
                    JSONObject ssidInfo = getSsidInfo(index);
                    if (ssidInfo != null) {
                        if (ssid == null) ssid = ssidInfo.getString("ssid");
                        if (encrypt == null) encrypt = ssidInfo.getString("encrypt");
                        if (pwd == null) pwd = ssidInfo.getString("pwd");
                    }
                }

                // 转换encrypt
                String customEncrypt = SkyWorthWifiEnum.getCustomValue(encrypt);
                logger.info("customEncrypt:" + customEncrypt);

                // 最终传递
                int securityRes = wlanConfigService.setWLANSSIDSecurity(index, ssid, customEncrypt, pwd);
                logger.info("setWLANSSIDSecurity : " + securityRes);

                if (params.has("wireType") && !isBoth) {
                    String wireType = params.getString("wireType");
                    String customWireType = SkyWorthWifiEnum.getCustomValue(wireType);
                    logger.info("customWireType : " + customWireType);
                    int standardRes = wlanConfigService.setWLANStandard(currentRadio, customWireType);
                    logger.info("setWLANStandard : " + standardRes);
                }

                if (params.has("channel") && !isBoth) {
                    String channel = params.getString("channel");
                    int channelRes = wlanConfigService.setWLANRadioChannel(currentRadio, "auto".equals(channel) ? 0 : Integer.parseInt(channel));
                    logger.info("setWLANRadioChannel :" + channelRes);
                }

                if (params.has("bandWidth") && !isBoth) {
                    String bandWidth = params.getString("bandWidth");
                    String customBandWidth = SkyWorthWifiEnum.getCustomValue(bandWidth);

                    int widthRes = wlanConfigService.setWLANRadioFrequencyWidth(currentRadio, customBandWidth);
                    logger.info("setWLANRadioFrequencyWidth : " + widthRes);
                }

                if (params.has("transmitPower") && !isBoth) {
                    int transmitPower = params.getInt("transmitPower");
                    int tp = transmitPower == 1 ? 40 : transmitPower == 2 ? 80 : 100;
                    int powerSetResult = wlanConfigService.setWLANRadioPower(currentRadio, tp);
                    logger.info("setWLANRadioPower : " + powerSetResult);
                }
            }
        }
    }

    @Override
    public JSONArray getWifiSetting() {
        JSONArray jsonArray = new JSONArray();
        JSONObject ssidInfo = getSsidInfo(1);
        logger.info("ssidInfo 1：" + ssidInfo);
        // 双频合一
        boolean isDualBandFusionEnabled = deviceConfigService.isDualBandFusionEnabled();
        if (ssidInfo != null) {
            // 0：关闭双频合一，1：开启双频合一
            ssidInfo.put("radioBoth", isDualBandFusionEnabled ? 1 : 0);
            jsonArray.put(ssidInfo);
        }
        JSONObject jsonObject = getSsidInfo(5);
        logger.info("ssidInfo 5：" + jsonObject);
        if (jsonObject != null) {
            // 0：关闭双频合一，1：开启双频合一
            jsonObject.put("radioBoth", isDualBandFusionEnabled ? 1 : 0);
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    @Override
    public JSONArray staList() {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject(lanHostsInfoQueryService.getLANHostInfoByClass("ONLINE"));
        int result = jsonObject.getInt("Result");
        if (result == 0) {
            JSONArray list = jsonObject.getJSONArray("List");
            if (list != null && list.length() > 0) {
                for (int i = 0; i < list.length(); i++) {
                    JSONObject data = new JSONObject();
                    JSONObject object = list.getJSONObject(i);
                    String mac = object.getString("MAC");
                    String connectInterface = object.getString("ConnectInterface");
                    String radio = connectInterface.contains("LAN") ? "LAN" : "SSID5".equals(connectInterface) ? "5g" : "2.4G";
                    if (!radio.equals("LAN")) {
                        JSONArray hostList = getWLANHostInfo(new String[]{mac});
                        if (hostList != null && hostList.length() > 0) {
                            JSONObject radioObj = hostList.getJSONObject(0);
                            data.put("rssi", radioObj.getString("RSSI"));
                            data.put("rxRate", radioObj.getString("RxRate"));
                            data.put("txRate", radioObj.getString("TxRate"));
                            data.put("mac", mac);
                        }
                    }
                    data.put("mac", mac);
                    data.put("radio", radio);
                    data.put("keepTime", object.getString("OnlineTime"));
                    data.put("name", object.getString("DhcpName"));
                    data.put("deviceName", object.getString("DeviceName"));
                    jsonArray.put(data);
                }
            }
        }
        return jsonArray;
    }

    @Override
    public boolean addStaticIpv4(JSONObject params) {
        String destIPAddress = params.getString("destIPAddress");
        String destSubnetMask = params.getString("destSubnetMask");
        JSONObject iptvInterface = getWanIndex("_IPTV_R_");
        JSONArray staticIpv4 = getStaticIpv4();
        for (int i = 0; i < staticIpv4.length(); i++) {
            JSONObject jsonObject = staticIpv4.getJSONObject(i);
            String oldIp = jsonObject.getString("destIPAddress");
            String oldMask = jsonObject.getString("destSubnetMask");
            if (oldIp.equals(destIPAddress) && oldMask.equals(destSubnetMask)) {
                return false;
            }
        }
        String gatewayIPAddress = "0.0.0.0";
        if (params.has("gatewayIPAddress")) {
            gatewayIPAddress = params.getString("gatewayIPAddress");
        }
        String wanInterface = iptvInterface.getString("Name");
        int result = staticLayer3ForwardingConfigService.addLayer3ForwardingStatic(destIPAddress, destSubnetMask, gatewayIPAddress, wanInterface);
        return result > 0;
    }

    @Override
    public boolean deleteStaticIpv4(JSONObject params) {
        int id = staticLayer3ForwardingConfigService.deleteLayer3ForwardingStatic(params.getInt("index"));
        return id == 0;
    }

    @Override
    public JSONArray getStaticIpv4() {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject(transferQueryService.getLayer3ForwardingStaticConfigInfo());
        int result = jsonObject.getInt("Result");
        if (result == 0) {
            JSONArray list = jsonObject.getJSONArray("List");
            for (int i = 0; i < list.length(); i++) {
                JSONObject data = new JSONObject();
                JSONObject object = list.getJSONObject(i);
                data.put("index", object.getString("Index"));
                data.put("destIPAddress", object.getString("DestIPAddress"));
                data.put("destSubnetMask", object.getString("DestSubnetMask"));
                data.put("gatewayIPAddress", object.getString("GatewayIPAddress"));
                data.put("wanInterface", object.getString("Interface"));
                data.put("ipVersion", "IPV4");
                jsonArray.put(data);
            }
        }
        return jsonArray;
    }

    @Override
    public boolean addStaticIpv6(JSONObject params) {
        String destIPPrefix = params.getString("destIPPrefix");
        String nextHop = params.getString("nextHop");
        String wanInterface = params.getString("wanInterface");
        int result = staticLayer3ForwardingConfigService.addIPv6Layer3ForwardingStatic(destIPPrefix, nextHop, wanInterface);
        return result > 0;
    }

    @Override
    public boolean deleteStaticIpv6(JSONObject params) {
        int id = staticLayer3ForwardingConfigService.deleteIPv6Layer3ForwardingStatic(params.getInt("index"));
        return id == 0;
    }

    @Override
    public JSONArray getStaticIpv6() {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject(transferQueryService.getIPv6Layer3ForwardingStaticConfigInfo());
        int result = jsonObject.getInt("Result");
        if (result == 0) {
            JSONArray list = jsonObject.getJSONArray("List");
            for (int i = 0; i < list.length(); i++) {
                JSONObject data = new JSONObject();
                JSONObject object = list.getJSONObject(i);
                data.put("index", object.getString("Index"));
                data.put("destIPPrefix", object.getString("DestIPPrefix"));
                data.put("nextHop", object.getString("NextHop"));
                data.put("wanInterface", object.getString("Interface"));
                data.put("ipVersion", "IPV6");
                jsonArray.put(data);
            }
        }
        return jsonArray;
    }

    @Override
    public boolean deviceSpeedLimit(JSONObject params) {
        String mac = params.getString("mac");
        int rxRate = params.getInt("rxRate");
        int txRate = params.getInt("txRate");
        return lanHostSpeedLimitService.setLANHostSpeedLimit(mac, txRate, rxRate) == 0;
    }

    @Override
    public JSONObject getStaLimitInfo(JSONObject params) {
        JSONObject resultObj = new JSONObject();
        JSONObject jsonObject = new JSONObject(lanHostsInfoQueryService.getLANHostSpeedLimitInfo(
                new String[]{params.getString("mac")}));
        int result = jsonObject.getInt("Result");
        if (result == 0) {
            JSONArray list = jsonObject.getJSONArray("List");
            if (list != null && list.length() > 0) {
                JSONObject dataObj = list.getJSONObject(0);
                resultObj.put("txRate", dataObj.getInt("UpSpeed"));
                resultObj.put("rxRate", dataObj.getInt("DownSpeed"));
            }
        }
        return resultObj;
    }

    @Override
    public JSONArray blackDeviceList() {
        return getBlackWhiteList(1, "BLOCK");
    }

    @Override
    public boolean addBlackDevice(JSONObject params) {
        logger.info("进入方法addBlackDevice： " + params.toString());
        int macFilterMode = lanHostsInfoQueryService.getMacFilterMode();
        if (macFilterMode != 1) {
            int mode = lanNetworkAccessConfigService.setMacFilterMode(1);
            if (mode == -1) {
                return false;
            }
        }
        String mac = params.getString("mac");
        return lanNetworkAccessConfigService.addLANHostToBlackList(new String[]{mac}) == 0;
    }

    @Override
    public boolean remoteBlackDevice(JSONObject params) {
        logger.info("进入方法remoteBlackDevice：" + params.toString());
        return lanNetworkAccessConfigService.deleteLANHostFromBlackList(new String[]{params.getString("mac")}) == 0;
    }

    @Override
    public JSONArray whiteDeviceList() {
        return getBlackWhiteList(2, "TRUST");
    }

    @Override
    public boolean addWhiteDevice(JSONObject params) {
        int macFilterMode = lanHostsInfoQueryService.getMacFilterMode();
        if (macFilterMode != 2) {
            int mode = lanNetworkAccessConfigService.setMacFilterMode(2);
            if (mode == -1) {
                return false;
            }
        }
        String mac = params.getString("mac");
        return lanNetworkAccessConfigService.addLANHostToTrustList(new String[]{mac}) == 0;
    }

    @Override
    public boolean remoteWhiteDevice(JSONObject params) {
        return lanNetworkAccessConfigService.deleteLANHostFromTrustList(new String[]{params.getString("mac")}) == 0;
    }

    @Override
    public JSONObject info() {
        JSONObject result = new JSONObject();
        result.put("sn", getSN());
        result.put("systemTime", System.currentTimeMillis());
        result.put("authType", 2);
        result.put("openTime", getDeviceUpTime());
        BundleContext bundleContext = FrameworkUtil.getBundle(Activator.class).getBundleContext();
        result.put("plugVersion", bundleContext.getBundle().getVersion().toString());
        JSONObject deviceInfo = getDeviceInfo();
        if (deviceInfo != null) {
            result.put("vendor", deviceInfo.getString("Vendor"));
            result.put("model", deviceInfo.getString("ProductCLass"));
            result.put("firmwareVersion", deviceInfo.getString("FirmwareVer"));
            result.put("hardwareVersion", deviceInfo.getString("HardwareVer"));
            result.put("ramSize", deviceInfo.getInt("RamSize"));
        }
        JSONObject wanIndex = getWanIndex("_INTERNET_R_VID_");
        if (wanIndex != null) {
            JSONObject wanIfInfo = getWANIfInfo(wanIndex.getInt("Index"));
            result.put("ipAddress", wanIfInfo.getString("ExternalIPAddress"));
        }
        return result;
    }

    @Override
    public JSONObject getWanIndex(String name) {
        JSONArray wanIfList = getWANIfList();
        if (wanIfList != null) {
            JSONObject wanIf = null;
            for (int i = 0; i < wanIfList.length(); i++) {
                JSONObject jsonObject = wanIfList.getJSONObject(i);
                if (jsonObject.getString("Name").contains(name)) {
                    wanIf = jsonObject;
                    break;
                }
            }
            return wanIf;
        }
        return null;
    }

//    @Override
//    public boolean setPilotLamp(JSONObject params){
//        //设置指示灯状态，无回显接口
//        int enable = params.getInt("enable");
//        int result = deviceConfigService.setLedSwitch(enable == 1);
//        return result == 0;
//    }

    private JSONArray getBlackWhiteList(int mode, String modeType) {
        JSONArray jsonArray = new JSONArray();
        int macFilterMode = lanHostsInfoQueryService.getMacFilterMode();
        if (macFilterMode == mode) {
            JSONObject jsonObject = new JSONObject(lanHostsInfoQueryService.getLANHostInfoByClass(modeType));
            int result = jsonObject.getInt("Result");
            if (result == 0) {
                JSONArray list = jsonObject.getJSONArray("List");
                for (int i = 0; i < list.length(); i++) {
                    JSONObject data = new JSONObject();
                    JSONObject object = list.getJSONObject(i);
                    data.put("mac", object.getString("MAC"));
                    jsonArray.put(data);
                }
            }
        }
        return jsonArray;
    }

    private JSONObject getSsidInfo(int ssidIndex) {
        JSONObject ssdInfo = new JSONObject(wlanQueryService.getWLANSSIDInfo(ssidIndex));
        int result = ssdInfo.getInt("Result");
        if (result == 0) {
            JSONObject wifiInfo = new JSONObject();
            String radio = ssidIndex == 1 ? "2.4G" : "5G";
            wifiInfo.put("radio", radio);
            wifiInfo.put("ssid", ssdInfo.getString("SSIDName"));

            String encrypt = ssdInfo.has("EncryptionMode") ? ssdInfo.getString("EncryptionMode") : null;
            String iotEncrypt = SkyWorthWifiEnum.getIotKey(encrypt);

            wifiInfo.put("encrypt", iotEncrypt);

            String wireType = ssdInfo.has("Standard") ? ssdInfo.getString("Standard") : null;
            String iotWireType = SkyWorthWifiEnum.getIotKey(wireType);

            wifiInfo.put("wireType", iotWireType);

            wifiInfo.put("visible", "TRUE".equals(ssdInfo.getString("Enable")) ? 1 : 0);
            String pwd = wlanSecretQueryService.getWLANSSIDPassword(ssidIndex);
            wifiInfo.put("pwd", pwd);
            JSONObject radioInfo = new JSONObject(wlanQueryService.getWLANRadioInfo(radio));
            int result1 = radioInfo.getInt("Result");
            if (result1 == 0) {
                String autoChannelEnable = radioInfo.getString("AutoChannelEnable");
                if ("TRUE".equals(autoChannelEnable)) {
                    wifiInfo.put("channel", "auto");
                } else {
                    wifiInfo.put("channel", radioInfo.getInt("Channel"));
                }

                String bandWidth = radioInfo.has("FrequencyWidth") ? radioInfo.getString("FrequencyWidth") : null;
                String iotBandWidth = SkyWorthWifiEnum.getIotKey(bandWidth);

                wifiInfo.put("bandWidth", iotBandWidth);
                String transmitPower = radioInfo.getString("TransmitPower");
                int powerInt = transmitPower == null ? 0 : Integer.parseInt(transmitPower);
                wifiInfo.put("transmitPower", powerInt <= 40 ? 1 : powerInt <= 80 ? 2 : 3);
            }
            return wifiInfo;
        }
        return null;
    }

    private void storeTaskInfo(String event, String time, String cycle) {
        if (isTaskExist(event, time, cycle)) {
            return;
        }
        //type,beginTime,endTime,period,mac
        //type,time,period;
        String taskTimes;
        if (!StringUtil.isEmpty(cycle)) {
            taskTimes = CommonConstant.PERIODIC_TASK + "," + time + "," + cycle + ";";
        } else {
            taskTimes = CommonConstant.ONETIME_TASK + "," + time + ";";
        }
        String oldTask = getTaskInfo(event);
        if (!StringUtil.isEmpty(oldTask)) {
            taskTimes = oldTask + taskTimes;
        }
        Properties properties = new Properties();
        properties.setProperty(event, taskTimes);
        ConfigUtil.store(properties, CommonConstant.TASK_FILE_NAME);
    }

    private String getTaskInfo(String event) {
        Properties load = ConfigUtil.load(CommonConstant.TASK_FILE_NAME);
        if (load != null) {
            return load.getProperty(event);
        }
        return null;
    }

    private boolean isTaskExist(String event, String time, String cycle) {
        String taskInfo = getTaskInfo(event);
        if (!StringUtil.isEmpty(taskInfo)) {
            String[] taskList = taskInfo.split(";");
            //超过20个定时任务则拒绝
            if (taskList.length >= CommonConstant.MAX_TASKS) {
                return true;
            }
            boolean result = false;
            for (String task : taskList) {
                String[] split = task.split(",");
                String taskType = split[0];
                String taskTime = split[1];
                if (CommonConstant.PERIODIC_TASK.equals(taskType)) {
                    String taskCycle = split[2];
                    if (taskTime.equals(time) && taskCycle.equals(cycle)) {
                        result = true;
                        break;
                    }
                } else {
                    if (taskTime.equals(time)) {
                        result = true;
                        break;
                    }
                }
            }
            return result;
        }
        return false;
    }

   /* @Override
    public boolean setPlugSwitchStatus1(JSONObject params) {
        if(plugConfigService != null) {
            int enable = params.getInt("enable");
            //1开启，0关闭
            int result = plugConfigService.setPlugSwitchStatus(enable == 1);
            return result == 0;
        }
        return false;
    }

    @Override
    public JSONObject getPlugSwitchStatus1() {
        if (plugConfigService != null) {
            boolean result = plugConfigService.getPlugSwitchStatus1();
            JSONObject object = new JSONObject();
            object.put("enable", result ? 1 : 0);  // 1 - 开启 ； 0 - 关闭
            return object;
        }
        return null;
    }
    @Override
    public boolean setPlugSwitchStatus(JSONObject params) {
        if (plugConfigService != null) {
            String funcName = params.getString("PlugName"); //功能名称枚举： gameAssistant（游帮帮）
            int enable = params.getInt("Status");  //开关，1关，0开
            if (funcName != null & FuncNameEnum.GAME_ASSISTANT.getCode().equalsIgnoreCase(funcName)) {
                int result =  plugConfigService.setPlugSwitchStatus(enable == 1);
                return result == 0;
            }
        }
        return false;
    }

    @Override
    public JSONArray getPlugSwitchStatus() {
        if (plugConfigService != null) {
            JSONArray resultList = new JSONArray();
            JSONObject jsonObject = new JSONObject(plugConfigService.getPlugSwitchStatus());
            logger.info("getScheduledRestart: " + jsonObject);
            int result = jsonObject.getInt("Result");
            if (result == 0) {
                JSONArray list = jsonObject.getJSONArray("List");
                for (int i = 0; i < list.length(); i++) {
                    JSONObject resultObj = new JSONObject();
                    JSONObject object = list.getJSONObject(i);
                    resultObj.put("id", object.getInt("Id"));
                    resultObj.put("enable", object.getString("EnableState").equals("true") ? 1 : 0);
                    resultObj.put("days", object.getString("Days"));

                    String timeStr = object.getString("Time");
                    if (timeStr != null && !timeStr.trim().isEmpty()) {
                        resultObj.put("time", CommonUtil.convertHourMinuteToSeconds(timeStr));
                    } else {
                        resultObj.put("time", 0);
                    }
//                    resultObj.put("time", CommonUtil.convertHourMinuteToSeconds(object.getString("Time")));
                    resultList.put(resultObj);
                }
                return resultList;
            }
        }
        return null;
    }*/

}
