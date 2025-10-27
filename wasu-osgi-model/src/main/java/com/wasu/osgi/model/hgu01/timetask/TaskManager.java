package com.wasu.osgi.model.hgu01.timetask;

import com.wasu.osgi.model.hgu01.service.impl.HardwareServiceImpl;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author glmx_
 */
public class TaskManager {
    private static final Logger logger = Logger.getLogger(HardwareServiceImpl.class);
    private final Map<String, ServiceTracker<AbstractScheduled, AbstractScheduled>> taskTrackers = new ConcurrentHashMap<>();
    private final Map<String, AbstractScheduled> activeTasks = new ConcurrentHashMap<>();
    private BundleContext bundleContext;

    public void init(BundleContext context) {
        this.bundleContext = context;
    }

    public void addTask(String taskId,
                        Class<? extends AbstractScheduled> taskClass,
                        JSONObject param,
                        long initialDelay,
                        long period,
                        TimeUnit unit) {
        try {
            logger.info("进入到addTask");
            AbstractScheduled task = taskClass.getDeclaredConstructor().newInstance();
            logger.info("getService：" + task);
            task.setParam(param);
            task.startFetchingData(initialDelay, period, unit);
            activeTasks.put(taskId, task);
        } catch (Exception e) {
            logger.error("添加任务失败: " + taskId, e);
        }
    }

    public boolean containsTask(String taskId) {
        return taskTrackers.containsKey(taskId);
    }

    public void removeTask(String taskId) {
        AbstractScheduled task = activeTasks.remove(taskId);
        if (task != null) {
            task.stopFetchingData();
        }
    }

    private ServiceTracker<AbstractScheduled, AbstractScheduled> createTracker(Class<? extends AbstractScheduled> taskClass) {
        return new ServiceTracker<>(bundleContext, taskClass.getName(), null);
    }

    public void shutdown() {
        for (AbstractScheduled task : activeTasks.values()) {
            task.stopFetchingData();
        }
        activeTasks.clear();
    }

    public void dumpTasks() {
        logger.info("当前活跃任务数：" + activeTasks.size());
        activeTasks.forEach((id, task) -> logger.info("活跃任务：" + id));
    }
}
