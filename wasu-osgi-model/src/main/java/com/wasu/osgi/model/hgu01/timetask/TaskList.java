package com.wasu.osgi.model.hgu01.timetask;

import com.wasu.osgi.model.hgu01.service.impl.TimeLimitService;
import org.osgi.framework.BundleContext;

import java.util.concurrent.TimeUnit;

/**
 * @author glmx_
 */
public class TaskList {

    private TaskManager taskManager;

    public TaskList(TaskManager taskManager) {
        this.taskManager = taskManager;
    }
    public void init(BundleContext context) {
//        taskManager = new TaskManager();
        taskManager.init(context);

        // 属性上报
        taskManager.addTask("propertiesReport", PropertiesReportTask.class, null,
                0, 10, TimeUnit.MINUTES);
        // 客户端数据上报
        taskManager.addTask("clientInfoReport", ClientInfoReportTask.class, null,
                1, 10, TimeUnit.MINUTES);
    }

    public void cleanup() {
        taskManager.shutdown();
        taskManager.removeTask("propertiesReport");
        taskManager.removeTask("clientInfoReport");
    }
}
