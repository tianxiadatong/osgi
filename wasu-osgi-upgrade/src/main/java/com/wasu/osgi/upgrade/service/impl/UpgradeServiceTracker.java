package com.wasu.osgi.upgrade.service.impl;

import com.wasu.osgi.upgrade.Activator;
import com.wasu.osgi.upgrade.service.IMqttService;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * 全局追踪 IMqttService 的工具类。
 * 自动处理服务的注册、注销和修改事件。
 * 所有需要调用 IMqttService 的地方都通过此类获取实例。
 *
 * @Author fangshengqun
 * @Date 2025/11/04
 */
public class UpgradeServiceTracker {

    private static final Logger logger = Logger.getLogger(UpgradeServiceTracker.class);
    private static ServiceTracker<IMqttService, IMqttService> tracker;

    private static synchronized void initTracker() {
        if (tracker != null) return;

        try {
            BundleContext context = FrameworkUtil.getBundle(Activator.class).getBundleContext();

            tracker = new ServiceTracker<>(context, IMqttService.class,
                    new ServiceTrackerCustomizer<IMqttService, IMqttService>() {

                        @Override
                        public IMqttService addingService(ServiceReference<IMqttService> reference) {
                            IMqttService service = context.getService(reference);
                            logger.info("检测到 IMqttService 已注册: " + service.getClass().getName());
                            return service;
                        }

                        @Override
                        public void removedService(ServiceReference<IMqttService> reference,
                                                   IMqttService service) {
                            logger.warn("IMqttService 已注销: " + service.getClass().getName());
                            context.ungetService(reference);
                        }

                        @Override
                        public void modifiedService(ServiceReference<IMqttService> reference,
                                                    IMqttService service) {
                            logger.info("IMqttService 已修改: " + service.getClass().getName());
                        }
                    });
            tracker.open();
            logger.info("UpgradeServiceTracker 初始化完成，开始追踪 IMqttService。");

        } catch (Exception e) {
            logger.error("UpgradeServiceTracker 初始化失败：", e);
        }
    }

    /**
     * 获取当前可用的 IMqttService。
     * 如果服务暂时不可用，将返回 null。
     */
    public static IMqttService getService() {
        if (tracker == null) {
            initTracker();
        }
        return tracker.getService();
    }

    public static synchronized void close() {
        if (tracker != null) {
            tracker.close();
            tracker = null;
        }
    }
}
