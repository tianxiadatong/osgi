package com.wasu.osgi.model.hgu01.timetask;

import com.wasu.osgi.model.hgu01.connect.HttpRequest;
import com.wasu.osgi.model.hgu01.service.IHardwareService;
import com.wasu.osgi.model.hgu01.service.impl.HardwareServiceImpl;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * @author Vicky
 * @date 2025年06月11日 14:54
 */
public class BlacklistAddTask extends AbstractScheduled {
    private final IHardwareService hardwareService;
    private static final Logger logger = Logger.getLogger(HardwareServiceImpl.class);

    public BlacklistAddTask() {
        hardwareService = HttpRequest.getHardwareService();
    }

    @Override
    public void run() {
        JSONObject param = getParam();
        logger.info("[BlacklistAddTask] 执行定时任务，参数: " + param);
        if (param != null && param.has("mac") && param.has("weekDay")) {
            // 判断是否是目标星期几
            int targetWeekDay = param.getInt("weekDay"); // 1~7 周日 = 1，周一 = 2...
            int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK); // 周日 = 1，周一 = 2...
            logger.info("targetWeekDay: " + targetWeekDay + ", today: " + today);
            if (today == targetWeekDay) {
                logger.info("[BlacklistAddTask] 今天是目标执行日，加入黑名单：" + param.getString("mac"));
                hardwareService.addBlackDevice(param);
            } else {
                logger.info("[BlacklistAddTask] 非目标执行日，跳过黑名单任务。today=" + today + ", target=" + targetWeekDay);
            }
        } else {
            logger.warn("[BlacklistAddTask] 参数为空或不包含 mac，跳过执行");
        }
    }
}
