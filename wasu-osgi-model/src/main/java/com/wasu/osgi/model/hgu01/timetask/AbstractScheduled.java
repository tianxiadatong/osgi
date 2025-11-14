package com.wasu.osgi.model.hgu01.timetask;

import com.wasu.osgi.model.hgu01.service.impl.HardwareServiceImpl;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author glmx_
 */
public abstract class AbstractScheduled implements Runnable {
    private static final Logger logger = Logger.getLogger(HardwareServiceImpl.class);

    private static final ScheduledExecutorService SHARED_EXECUTOR = Executors.newScheduledThreadPool(4);
    // 用于取消任务时记录句柄
    private ScheduledFuture<?> future;
    private boolean isRunning = false;
    @Setter
    @Getter
    private JSONObject param;
    public void startFetchingData(long initialDelay, long period, TimeUnit unit) {
        logger.info(getClass().getSimpleName() + " 定时任务启动：delay=" + initialDelay + "s, period=" + period + "s");
        if (future == null || future.isCancelled()) {
            future = SHARED_EXECUTOR.scheduleAtFixedRate(this, initialDelay, period, unit);
        } else {
            logger.warn(getClass().getSimpleName() + " 已经在运行，忽略重复启动");
        }
    }

    public void stopFetchingData() {
        if (future != null && !future.isCancelled()) {
            future.cancel(true);
            logger.info(getClass().getSimpleName() + " 定时任务已取消");
        }
    }

    @Override
    public abstract void run();
}
