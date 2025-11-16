package com.wasu.osgi.model.hgu01;

import com.wasu.osgi.model.hgu01.config.HguContext;
import com.wasu.osgi.model.hgu01.connect.HttpRequest;
import com.wasu.osgi.model.hgu01.connect.MqttConnect;
import com.wasu.osgi.model.hgu01.handler.OTAHandler;
import com.wasu.osgi.model.hgu01.handler.UpgradeServiceTracker;
import com.wasu.osgi.model.hgu01.message.MessageHandler;
import com.wasu.osgi.model.hgu01.service.IHardwareService;
import com.wasu.osgi.model.hgu01.service.NetworkService;
import com.wasu.osgi.model.hgu01.service.impl.HardwareServiceImpl;
import com.wasu.osgi.model.hgu01.service.impl.MqttServiceImpl;
import com.wasu.osgi.model.hgu01.service.impl.NetworkServiceImpl;
import com.wasu.osgi.model.hgu01.timetask.NetworkChecker;
import com.wasu.osgi.model.hgu01.timetask.TaskList;
import com.wasu.osgi.model.hgu01.timetask.TaskManager;
import com.wasu.osgi.model.hgu01.util.LogUtil;
import com.wasu.osgi.upgrade.service.IMqttService;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ManagedService;
import org.osgi.util.tracker.ServiceTracker;

import java.util.concurrent.*;


/**
 * @author glmx_
 */
public class Activator implements BundleActivator {

    private static final Logger logger = Logger.getLogger(Activator.class);

    private TaskList taskList;
    private BundleContext bundleContext;
    private ServiceTracker<NetworkService, NetworkService> tracker;
    private ScheduledExecutorService executor;

    // 共享实例
    private final TaskManager sharedTaskManager = new TaskManager();

    @Override
    public void start(BundleContext context) {
        bundleContext = context;
        LogUtil.initLog(context);
        logger.info("===============wasu-osgi-model bundle start=========================");
        try {
            CompletableFuture.supplyAsync(() -> {
                networkRetry(context);
                return "success";
            });
        } catch (Exception e) {
            logger.error("wasu-osgi-model 启动异常,", e);
        }
    }

    @Override
    public void stop(BundleContext ctx) {
        if (taskList != null) {
            taskList.cleanup();
        }
        closeResource();
        MqttConnect.disconnect();
        // 关闭 UpgradeServiceTracker
        UpgradeServiceTracker.close();


    }

    private void networkRetry(BundleContext context) {
        try {
            context.registerService(NetworkService.class, new NetworkServiceImpl(), null);
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
                    logger.error("网络连接等待超时。");
                    return;
                }
                init();
                closeResource();
            }
        } catch (InterruptedException e) {
            logger.error("wasu-osgi-model 启动失败:" + e.getMessage());
        }
    }

    private void init() {
        logger.info("开始初始化");

        // 注册 sharedTaskManager 到上下文中，供 HardwareServiceImpl 动态使用
        HguContext.setSharedTaskManager(sharedTaskManager);

        // 初始化 hardwareService（不再需要先构造 TimeLimitService）
        IHardwareService hardwareService = new HardwareServiceImpl();

        // 注入 hardwareService 到 HttpRequest
        HttpRequest.setHardwareService(hardwareService);
        HttpRequest.request(bundleContext);

        //判断升级组件是否活着
        OTAHandler.startUpgradeBundle(bundleContext);
        //注册定时任务
//        bundleContext.registerService(PropertiesReportTask.class, new PropertiesReportTask(), null);
//        bundleContext.registerService(ClientInfoReportTask.class, new ClientInfoReportTask(), null);
//        bundleContext.registerService(BlacklistAddTask.class, new BlacklistAddTask(), null);
//        bundleContext.registerService(BlacklistRemoveTask.class, new BlacklistRemoveTask(), null);
        taskList = new TaskList(sharedTaskManager);
        taskList.init(bundleContext);

        // TimeLimitService 也从 HardwareServiceImpl 内部统一管理，直接调用 loadAllPolicies 即可
        if (hardwareService instanceof HardwareServiceImpl) {
            ((HardwareServiceImpl) hardwareService).getTimeLimitService().loadAllPolicies();
            logger.info("完成初始化：loadAllPolicies");
        }


        //事件监听
        bundleContext.registerService(ManagedService.class, new MessageHandler(bundleContext), null);
        // 注册MQTT服务
        bundleContext.registerService(IMqttService.class, new MqttServiceImpl(), null);
        logger.info("IMqttService接口注册成功。");
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
