package com.wasu.osgi.model.hgu01.config;

import com.wasu.osgi.model.hgu01.timetask.TaskManager;

/**
 * @author Vicky
 * @date 2025年08月04日 16:12
 */
public class HguContext {
    private static TaskManager sharedTaskManager;

    public static void setSharedTaskManager(TaskManager manager) {
        sharedTaskManager = manager;
    }

    public static TaskManager getSharedTaskManager() {
        return sharedTaskManager;
    }
}
