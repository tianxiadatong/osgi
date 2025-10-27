package com.wasu.osgi.model.hgu01.timetask;

import com.wasu.osgi.model.hgu01.connect.HttpRequest;
import com.wasu.osgi.model.hgu01.service.IHardwareService;
import com.wasu.osgi.model.hgu01.service.impl.HardwareServiceImpl;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * @author Vicky
 * @date 2025年06月11日 15:22
 */
public class BlacklistRemoveTask extends AbstractScheduled {
    private final IHardwareService hardwareService;
    private static final Logger logger = Logger.getLogger(HardwareServiceImpl.class);

    public BlacklistRemoveTask() {
        hardwareService = HttpRequest.getHardwareService();
    }

    @Override
    public void run() {
        JSONObject param = getParam();
        logger.info("[BlacklistRemoveTask] 执行定时任务，参数: " + param);

        if (param != null && param.has("mac") && param.has("weekDay")) {
            int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            int targetWeekDay = param.getInt("weekDay");
            if (today == targetWeekDay) {
                logger.info("[BlacklistRemoveTask] 今天是目标执行日，移除黑名单: " + param.getString("mac"));
                hardwareService.remoteBlackDevice(param);
            } else {
                logger.info("[BlacklistRemoveTask] 非目标执行日，跳过任务。today=" + today + ", target=" + targetWeekDay);
            }
        } else {
            logger.warn("[BlacklistRemoveTask] 参数为空或不包含 mac，跳过执行");
        }
    }
}
