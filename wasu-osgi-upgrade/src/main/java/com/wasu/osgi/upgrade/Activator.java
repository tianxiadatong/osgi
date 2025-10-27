package com.wasu.osgi.upgrade;

import com.cw.smartgateway.commservices.DeviceInfoQueryService;
import com.wasu.osgi.upgrade.config.CommonConstant;
import com.wasu.osgi.upgrade.config.ConfigProperties;
import com.wasu.osgi.upgrade.service.IUpgradeService;
import com.wasu.osgi.upgrade.service.IFirmwareUpgradeService; // 添加新服务接口导入
import com.wasu.osgi.upgrade.service.NetworkService;
import com.wasu.osgi.upgrade.service.impl.NetworkServiceImpl;
import com.wasu.osgi.upgrade.service.impl.UpgradeServiceImpl;
import com.wasu.osgi.upgrade.service.impl.FirmwareUpgradeServiceImpl; // 添加新服务实现类导入
import com.wasu.osgi.upgrade.util.LogUtil;
import com.wasu.osgi.upgrade.util.NetworkChecker;
import com.wasu.osgi.upgrade.util.StringUtil;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import java.util.concurrent.*;

/**
 * @author glmx_
 */
public class Activator implements BundleActivator {

    private static final Logger logger = Logger.getLogger(Activator.class);
    private ServiceTracker<NetworkService, NetworkService> tracker;
    private ScheduledExecutorService executor;

    @Override
    public void start(BundleContext context) {
        LogUtil.initLog(context);
        logger.info("==========com.wasu.osgi.bundle.update bundle start==========");
        try {
            context.registerService(IUpgradeService.class, new UpgradeServiceImpl(), null);
            // 注册新的固件升级服务
            context.registerService(IFirmwareUpgradeService.class, new FirmwareUpgradeServiceImpl(), null);
            logger.info("IUpgradeService 和 IFirmwareUpgradeService 接口注册成功。");
            CompletableFuture.supplyAsync(() -> {
                networkRetry(context);
                return "success";
            });
        } catch (Exception e) {
            logger.error("com.wasu.osgi.bundle.update启动异常，", e);
        }
    }

    @Override
    public void stop(BundleContext ctx) {
        closeResource();
    }

    private void networkRetry(BundleContext context) {
        try {
            NetworkServiceImpl networkService = new NetworkServiceImpl();
            context.registerService(NetworkService.class, networkService, null);
            tracker = new ServiceTracker<>(context, NetworkService.class, null);
            tracker.open();

            NetworkService trackedService = tracker.waitForService(Long.MAX_VALUE);
            if (trackedService != null) {
                NetworkChecker checker = new NetworkChecker(trackedService);
                executor = Executors.newSingleThreadScheduledExecutor();
                executor.scheduleWithFixedDelay(checker, 0, 30, TimeUnit.SECONDS);
                try {
                    checker.getFuture().get();
                } catch (InterruptedException | ExecutionException e) {
                    Thread.currentThread().interrupt();
                    logger.error("com.wasu.osgi.bundle.update 启动失败：网络超时，" + e.getMessage());
                    return;
                }
                init(context);
                closeResource();
            }
        } catch (InterruptedException e) {
            logger.error("com.wasu.osgi.bundle.update 启动失败：" + e.getMessage());
        }
    }

    private void init(BundleContext context) {
        ServiceReference<DeviceInfoQueryService> deviceInfoServiceReference = context.getServiceReference(DeviceInfoQueryService.class);
        if (deviceInfoServiceReference == null) {
            logger.error("com.wasu.osgi.bundle.update 启动无法获取到DeviceInfoQueryService接口，项目启动失败。");
            return;
        }
        DeviceInfoQueryService deviceInfoQueryService = context.getService(deviceInfoServiceReference);
        String deviceId = deviceInfoQueryService.getDeviceID();
        logger.info("获取到的设备id：" + deviceId);
        if (!StringUtil.isEmpty(deviceId) && !"-1".equals(deviceId)) {
            ConfigProperties.deviceId = deviceId;
            // 初始化MQTT配置
            com.wasu.osgi.upgrade.util.MqttInitializer.initializeMqtt(deviceId);
            // 初始化MQTT客户端
            com.wasu.osgi.upgrade.util.MqttClient.getInstance();

            IUpgradeService upgradeService = new UpgradeServiceImpl();
            upgradeService.checkUpgrade(deviceId);
            // 修改为:
            IFirmwareUpgradeService firmwareUpgradeService = new FirmwareUpgradeServiceImpl();
            JSONObject params = new JSONObject();
            JSONArray upgrades = new JSONArray();
            JSONObject mainComponent = new JSONObject();
            mainComponent.put("component", CommonConstant.UPGRADE_TYPE_MAIN);
            mainComponent.put("version", "1.1.0");
            upgrades.put(mainComponent);

            JSONObject cComponent = new JSONObject();
            cComponent.put("component", CommonConstant.UPGRADE_TYPE_C_PLUGIN);
            cComponent.put("version", "1.1.0");
            upgrades.put(cComponent);
            JSONObject javaComponent = new JSONObject();
            javaComponent.put("component", CommonConstant.UPGRADE_TYPE_JAVA_PLUGIN);
            javaComponent.put("version", "1.1.0");
            upgrades.put(javaComponent);

            params.put("upgrades", upgrades);
            firmwareUpgradeService.upOTA(params);
        }
    }

    private void closeResource() {
        if (tracker != null) {
            tracker.close();
        }
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
