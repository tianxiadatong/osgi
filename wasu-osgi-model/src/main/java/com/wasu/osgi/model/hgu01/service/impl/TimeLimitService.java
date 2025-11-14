package com.wasu.osgi.model.hgu01.service.impl;

import com.wasu.osgi.model.hgu01.config.CommonConstant;
import com.wasu.osgi.model.hgu01.domain.TimeLimitPolicy;
import com.wasu.osgi.model.hgu01.service.IHardwareService;
import com.wasu.osgi.model.hgu01.timetask.AbstractScheduled;
import com.wasu.osgi.model.hgu01.timetask.BlacklistAddTask;
import com.wasu.osgi.model.hgu01.timetask.BlacklistRemoveTask;
import com.wasu.osgi.model.hgu01.timetask.TaskManager;
import com.wasu.osgi.model.hgu01.util.ConfigUtil;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.ServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * @author Vicky
 * @date 2025年06月11日 10:42
 */
public class TimeLimitService {
    private static final Logger logger = Logger.getLogger(HardwareServiceImpl.class);
    //    private static final String TIME_LIMIT_KEY_PREFIX = "TIME_LIMIT_";
    private final TaskManager taskManager;
    private final IHardwareService hardwareService;
    private final ConcurrentHashMap<Integer, TimeLimitPolicy> policyMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Integer> subTaskToPolicyMap = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);
    private final Set<Integer> usedIds = ConcurrentHashMap.newKeySet();
    private final Path policyFilePath;
    private final Path policyBackUpFilePath;
    private final Path policyTempFilePath;

    public TimeLimitService(TaskManager taskManager, IHardwareService hardwareService) {
        this.taskManager = taskManager;
        this.hardwareService = hardwareService;
        Path configPath = Paths.get(CommonConstant.DATA_PATH);
        this.policyFilePath = configPath.resolve(CommonConstant.TIME_LIMIT_FILE_NAME);
        this.policyBackUpFilePath = configPath.resolve(CommonConstant.TIME_LIMIT_FILE_NAME + ".bak");
        this.policyTempFilePath = configPath.resolve(CommonConstant.TIME_LIMIT_FILE_NAME + ".tmp");
    }

    /**
     * 插件启动时加载所有策略（原TimeLimitLoader功能）
     */
    public synchronized void loadAllPolicies() {
        logger.info("开始loadAllPolicies");
        // 1. 检查文件存在性
        if (!Files.exists(policyFilePath)) {
            logger.info("无限时策略配置文件，跳过加载");
            return;
        }

        //读取文件，并加载限时任务配置
        try (InputStream in = Files.newInputStream(policyFilePath)) {
            Properties props = new Properties();
            props.load(in);

            //存储格式：1 = MAC|DAYS|BEGIN_TIME|END_TIME|ENABLE
            props.forEach((key, value) -> {
                int policyId = Integer.parseInt(key.toString());
                TimeLimitPolicy policy = TimeLimitPolicy.fromStorageString(policyId, value.toString());

                if (policy != null) {
                    policyMap.put(policyId, policy);
                    if (policy.isEnabled()) {
                        registerSubTasks(policy); // 直接复用现有方法
                    }
                }
            });

            logger.info("已加载{" + policyMap.size() + "}条策略");
        } catch (IOException e) {
            logger.error("策略加载失败", e);
        }
    }

    public boolean createTimeLimit(JSONObject params) {
        logger.info("开始createTimeLimit");
        logger.info(params.toString());
        if (!validateParams(params)) {
            return false;
        }

        String mac = params.getString("mac");
        String days = params.getString("days");
        long beginTime = params.getLong("beginTime");
        long endTime = params.getLong("endTime");
        logger.info("mac" + mac);
        logger.info("days" + days);
        logger.info("beginTime" + beginTime);
        logger.info("endTime" + endTime);

        // 2. 检查是否已存在相同策略（相同MAC+days+时间段）
        if (hasDuplicatePolicy(mac, days, beginTime, endTime)) {
            logger.warn("Duplicate policy found for MAC: { " + mac + " }");
            return true; // 存在重复策略视为成功
        }

        logger.info("不存在重复策略");
        logger.info("开始创建策略实体");
        // 3. 创建策略实体
        TimeLimitPolicy policy = new TimeLimitPolicy(
                generateUniquePolicyId(),
                mac,
                days,
                beginTime,
                endTime,
                true // 默认启用
        );

        logger.info("开始拆分子任务");
        // 4. 拆分子任务并注册
        if (!registerSubTasks(policy)) {
            return false;
        }

        logger.info("拆分子任务并注册成功");
        // 5. 保存策略
        logger.info("开始保存策略");
        synchronized (this) {
            policyMap.put(policy.getPolicyId(), policy);
            logger.info("policyMap：" + policyMap);
            savePolicies();
        }

        logger.info("Created time limit policy: { " + policy + " }");
        return true;
    }

    /**
     * 查询设备限时策略列表
     * @param macFilter 设备MAC（空字符串时返回所有策略）
     * @return 策略列表JSONArray
     */
    public JSONArray queryTimeLimits(String macFilter) {
        JSONArray result = new JSONArray();

        logger.info("policyMap:" + policyMap);
        // 遍历策略Map（线程安全遍历）
        policyMap.forEach((policyId, policy) -> {
            // 过滤MAC（如果指定了macFilter）
            if (macFilter.isEmpty() || policy.getMac().equalsIgnoreCase(macFilter)) {
                JSONObject item = new JSONObject();
                item.put("id", policyId);  // int
                item.put("beginTime", policy.getBeginTime());
                item.put("endTime", policy.getEndTime());
                item.put("days", policy.getDays());
                item.put("enable", policy.isEnabled() ? 1 : 0);
                result.put(item);
            }
        });

        return result;
    }

    public boolean setTimeLimitStatus(JSONObject params) {
        try {
            // 1. 参数校验
            if (!validateSetStatusParams(params)) {
                return false;
            }

            String mac = params.getString("mac");
            int policyId = params.getInt("id");
            boolean enable = params.getInt("enable") == 1;

            // 2. 获取策略（双重校验）
            TimeLimitPolicy policy = policyMap.get(policyId);
            if (policy == null || !policy.getMac().equals(mac)) {
                logger.warn("策略不存在或MAC不匹配: policyId={ " + policyId + " }, mac={ " + mac + " }");
                return false;
            }

            // 3. 状态变更（同步块保证原子性）
            synchronized (this) {
                if (enable) {
                    return enablePolicy(policy);
                } else {
                    return disablePolicy(policy);
                }
            }

        } catch (Exception e) {
            logger.error("设置限时状态失败", e);
            return false;
        }
    }

    public boolean removeOneTimeLimit(JSONObject params) {
        // 1. 参数校验
        if (!validateDeleteParams(params)) {
            return false;
        }

        String mac = params.getString("mac");
        int policyId = params.getInt("id");

        // 2. 获取并验证策略归属
        TimeLimitPolicy policy = policyMap.get(policyId);
        if (policy == null || !policy.getMac().equalsIgnoreCase(mac)) {
            logger.warn("策略不存在或MAC不匹配: policyId={" + policyId + "}, mac={" + mac + "}");
            return false;
        }

        // 3. 状态检查，生效中的先禁用
        if (policy.isEnabled()) {
            JSONObject param = new JSONObject();
            param.put("id", policyId);
            param.put("enable", 0);
            param.put("mac", mac);
            this.setTimeLimitStatus(param);
        }

        // 4. 执行删除
        synchronized (this) {
            try {
                // 4.1 取消所有关联定时任务
                cancelPolicyTasks(policy);

                // 4.2 清理内存记录
                policyMap.remove(policyId);
                usedIds.remove(policyId);

                // 4.3 持久化（savePolicies已包含文件清理）
                savePolicies();

                logger.info("已删除策略: policyId={" + policyId + "}, mac={" + mac + "}");
                return true;

            } catch (Exception e) {
                logger.error("删除过程异常", e);
                policyMap.putIfAbsent(policyId, policy); // 内存回滚
                return false;
            }
        }
    }


    /**
     * 注册子任务
     */
    private boolean registerSubTasks(TimeLimitPolicy policy) {
        logger.info("注册子任务开始");
        try {
            List<Integer> activeDays = policy.getActiveDays();
            logger.info("activeDays：" + activeDays);
            for (int dayIndex : activeDays) {
                String weekDayName = getWeekDayName(dayIndex);           // "MON" 之类
                int calendarDay = toCalendarDayOfWeek(dayIndex);         // 1~7

                logger.info("注册加入黑名单任务：" + weekDayName);
                // 注册加入黑名单任务
                String addTaskId = registerTask(
                        policy,
                        weekDayName,
                        calendarDay,
                        policy.getBeginTime(),
                        BlacklistAddTask.class
                );
                logger.info("注册加入黑名单任务：" + addTaskId);
                policy.addSubTaskId(addTaskId);

                // 注册移除黑名单任务
                logger.info("注册移除黑名单任务");
                String removeTaskId = registerTask(
                        policy,
                        weekDayName,
                        calendarDay,
                        policy.getEndTime(),
                        BlacklistRemoveTask.class
                );
                policy.addSubTaskId(removeTaskId);
            }
            return true;
        } catch (Exception e) {
            logger.error("Failed to register subtasks", e);
            // 清理已注册的任务
            policy.getSubTaskIds().forEach(taskManager::removeTask);
            return false;
        }
    }

    /**
     * 单个子任务注册
     */
    private String registerTask(TimeLimitPolicy policy, String weekDayName,
                                int calendarDayOfWeek, long triggerTime,
                                Class<?> taskClass) {
        logger.info("开始注册单个子任务：" + policy);
        String taskType = (taskClass == BlacklistAddTask.class) ? "ADD" : "REMOVE";

        String taskId = String.format("%s_%s_%s_%d",
                taskType,
                policy.getMac().replace(":", ""),
                weekDayName,
                triggerTime
        );

        logger.info("taskId：" + taskId);
        // 计算首次执行延迟
        long initialDelay = calculateInitialDelay(weekDayName, triggerTime);
        logger.info("initialDelay：" + initialDelay);

        JSONObject param = new JSONObject()
                .put("mac", policy.getMac())
                .put("weekDay", calendarDayOfWeek); // 用于 run() 判断; 1~7，和 Calendar 一致

        logger.info("[addTask] 任务参数 param=" + param);
        // 注册到TaskManager（每日执行）
        taskManager.addTask(
                taskId,
                (Class<? extends AbstractScheduled>) taskClass,
                param,
                initialDelay,
                24 * 60 * 60, // 24小时周期
                TimeUnit.SECONDS
        );
        logger.info("taskManager是否有该任务：" + taskManager.containsTask(taskId));
        logger.info("成功注册到taskManager");

        // 维护任务映射关系
        subTaskToPolicyMap.put(taskId, policy.getPolicyId());
        logger.info("subTaskToPolicyMap:" + subTaskToPolicyMap);
        return taskId;
    }

    /**
     * 启用策略（含子任务注册）
     */
    private boolean enablePolicy(TimeLimitPolicy policy) {
        if (!policy.isEnabled()) {
            policy.enable();
            recreateSubTasks(policy);
            savePolicies();
            logger.info("已启用策略: { " + policy.getPolicyId() + " }");
        }
        return true;
    }

    /**
     * 禁用策略（含子任务清理）
     */
    private boolean disablePolicy(TimeLimitPolicy policy) {
        if (policy.isEnabled()) {
            policy.disable();
            removeSubTasks(policy);
            savePolicies();

            // 仅当没有其他生效策略时才移除黑名单
            if (!hasOtherActivePolicy(policy.getMac(), policy.getPolicyId())) {
                if (isMacInBlacklist(policy.getMac())) {
                    hardwareService.remoteBlackDevice(new JSONObject().put("mac", policy.getMac()));
                }
            } else {
                logger.info("MAC { " + policy.getMac() + " } 仍有其他生效策略，跳过移除黑名单");
            }
            logger.info("已禁用策略: { " + policy.getPolicyId() + " }");
        }
        return true;
    }

    /**
     * 重建子任务（用于启用策略）
     */
    private void recreateSubTasks(TimeLimitPolicy policy) {
        policy.getSubTaskIds().forEach(taskId -> {
            if (!taskManager.containsTask(taskId)) {
                // 解析任务类型和时间
                String[] parts = taskId.split("_");
                long triggerTime = Long.parseLong(parts[3]);
                Class<?> taskClass = "ADD".equals(parts[0]) ?
                        BlacklistAddTask.class : BlacklistRemoveTask.class;

                // 重新注册任务
                taskManager.addTask(
                        taskId,
                        (Class<? extends AbstractScheduled>) taskClass,
                        new JSONObject().put("mac", policy.getMac()),
                        calculateInitialDelay(parts[2], triggerTime),
                        24 * 60 * 60,
                        TimeUnit.SECONDS
                );
            }
        });
    }

    /**
     * 移除子任务（用于禁用策略）
     */
    private void removeSubTasks(TimeLimitPolicy policy) {
        policy.getSubTaskIds().forEach(taskId -> {
            taskManager.removeTask(taskId);
            subTaskToPolicyMap.remove(taskId);
        });
    }

    /**
     * 计算初始延迟（秒）
     */
    private long calculateInitialDelay(String weekDay, long triggerTime) {
        DayOfWeek targetDay = DayOfWeek.valueOf(weekDay.toUpperCase());
        LocalDateTime now = LocalDateTime.now();

        // 拆解 triggerTime 为时分秒
        int hour = (int) (triggerTime / 3600);
        int minute = (int) ((triggerTime % 3600) / 60);
        int second = (int) (triggerTime % 60);

        // 计算本周该 weekday 的日期
        LocalDateTime target = now.with(TemporalAdjusters.nextOrSame(targetDay))
                .withHour(hour).withMinute(minute).withSecond(second).withNano(0);

        if (target.isBefore(now)) {
            target = target.plusWeeks(1); // 时间已过，推到下周
        }

        return Duration.between(now, target).getSeconds();
    }

    private boolean validateParams(JSONObject params) {
        try {
            String mac = params.getString("mac");
            String days = params.getString("days");
            long beginTime = params.getLong("beginTime");
            long endTime = params.getLong("endTime");

            // MAC地址校验
            if (!mac.matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")) {
                logger.error("Invalid MAC format: {}" + mac);
                return false;
            }

            // Days校验（7位0/1）
            if (days == null || !days.matches("[01]{7}")) {
                logger.error("Invalid days format: {}" + days);
                return false;
            }

            // 时间范围校验
            if (beginTime < 0 || beginTime >= 86400 ||
                    endTime <= beginTime || endTime > 86400) {
                logger.error("Invalid time range: { " + beginTime + " }-{ " + endTime + " }");
                return false;
            }

            return true;
        } catch (Exception e) {
            logger.error("Parameter validation failed", e);
            return false;
        }
    }

    /**
     * 参数校验 for setTimeLimitStatus
     */
    private boolean validateSetStatusParams(JSONObject params) {
        try {
            // 必填字段检查
            if (!params.has("mac") || !params.has("id") || !params.has("enable")) {
                logger.error("缺少必要参数");
                return false;
            }

            // MAC格式校验
            String mac = params.getString("mac");
            if (!mac.matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")) {
                logger.error("MAC地址格式错误: { “ + mac + “ }");
                return false;
            }

            // enable取值校验
            int enable = params.getInt("enable");
            if (enable != 0 && enable != 1) {
                logger.error("enable参数必须为0或1");
                return false;
            }

            return true;
        } catch (JSONException e) {
            logger.error("参数解析错误", e);
            return false;
        }
    }

    private boolean validateDeleteParams(JSONObject params) {
        try {
            // 必填字段检查
            if (!params.has("mac") || !params.has("id")) {
                logger.error("缺少必要参数");
                return false;
            }

            // MAC格式校验
            String mac = params.getString("mac");
            if (!mac.matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")) {
                logger.error("MAC地址格式错误: { " + mac + " }");
                return false;
            }

            // ID类型校验
            params.getInt("id"); // 仅检查是否能转为int
            return true;

        } catch (JSONException e) {
            logger.error("参数解析错误", e);
            return false;
        }
    }

    /**
     * 检查重复策略
     */
    private boolean hasDuplicatePolicy(String mac, String days,
                                       long beginTime, long endTime) {
        logger.info("policyMap:" + policyMap.values());
        return policyMap.values().stream()
                .filter(p -> p.getMac().equals(mac))
                .filter(p -> p.getDays().equals(days))
                .anyMatch(p -> p.getBeginTime() == beginTime &&
                        p.getEndTime() == endTime);
    }

    /**
     * 生成不重复的policyId
     */
    private int generateUniquePolicyId() {
        while (true) {
            int newId = idCounter.getAndIncrement();
            if (!usedIds.contains(newId)) {
                usedIds.add(newId);
                return newId;
            }
            // 如果计数器回绕（极端情况）
            if (newId == Integer.MAX_VALUE) {
                idCounter.set(1);
            }
        }
    }

    private String getWeekDayName(int dayIndex) {
        String[] names = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
        return names[dayIndex];
    }

//    /** 保存限时任务策略到本地文件 */
//    private void savePolicies() {
//        Properties properties = new Properties();
//        policyMap.forEach((id, policy) ->
//                properties.put(id, policy.toString()));
//        ConfigUtil.store(properties, CommonConstant.TIME_LIMIT_FILE_NAME);
//    }

    /**
     * 原子化保存策略到文件
     */
    private void savePolicies() {
        logger.info("开始原子化保存策略到文件");
        Path original = policyFilePath;
        Path backup = policyBackUpFilePath;
        Path temp = policyTempFilePath; //新建临时文件

        try {
            // 1. 准备数据快照（无锁操作）
            Properties properties = new Properties();
            policyMap.forEach((id, policy) ->
                    properties.put(String.valueOf(id), policy.toString())
            );

            // 2. 写入临时文件
            try (OutputStream out = Files.newOutputStream(temp,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {

                properties.store(out, "TimeLimitPolicy " + new Date());
            }

            // 3. 备份原文件（如果存在）
            if (Files.exists(original)) {
                Files.copy(original, backup, StandardCopyOption.REPLACE_EXISTING);
            }

            // 4. 原子替换（临时文件->正式文件）
            Files.move(temp, original,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);

            // 5. 清理备份
            Files.deleteIfExists(backup);

        } catch (Exception e) {
            logger.error("策略保存失败", e);
            try {
                // 恢复流程：备份->正式文件
                if (Files.exists(backup) && !Files.exists(original)) {
                    Files.move(backup, original, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException ex) {
                logger.fatal("恢复备份失败！", ex);
            }
        } finally {
            // 确保清理临时文件
            try {
                Files.deleteIfExists(temp);
            } catch (IOException e) {
                logger.warn("临时文件清理失败", e);
            }
        }
    }

    private boolean isMacInBlacklist(String mac) {
        // 1. 快速空值检查
        if (mac == null || mac.isEmpty()) {
            return false;
        }

        // 2. 获取黑名单
        JSONArray blacklist;
        try {
            blacklist = hardwareService.blackDeviceList();
            if (blacklist == null || blacklist.length() == 0) {
                return false;
            }
        } catch (Exception e) {
            logger.error("获取黑名单失败", e);
            return false;
        }

        try {
            return IntStream.range(0, blacklist.length())
                    .mapToObj(blacklist::getJSONObject)
                    .filter(Objects::nonNull)
                    .anyMatch(device -> {
                        try {
                            return mac.equalsIgnoreCase(device.optString("mac", ""));
                        } catch (Exception e) {
                            logger.warn("MAC比对异常", e);
                            return false;
                        }
                    });
        } catch (Exception e) {
            logger.error("黑名单遍历异常", e);
            return false;
        }
    }

    /**
     * 取消策略关联的所有任务
     */
    private void cancelPolicyTasks(TimeLimitPolicy policy) {
        policy.getSubTaskIds().forEach(taskId -> {
            // 从TaskManager取消任务
            taskManager.removeTask(taskId);

            // 清理映射关系
            subTaskToPolicyMap.remove(taskId);
        });
    }

    private int toCalendarDayOfWeek(int dayIndex) {
        int[] calendarDays = {
                Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
        };
        return calendarDays[dayIndex];
    }

    private boolean hasOtherActivePolicy(String mac, int excludePolicyId) {
        long nowSeconds = LocalTime.now().toSecondOfDay();
        int todayIndex = LocalDate.now().getDayOfWeek().getValue() - 1; // 转为 0~6  0-周一  6-周日，为了和getActiveDays同步

        return policyMap.values().stream()
                .filter(TimeLimitPolicy::isEnabled) // 只看启用策略
                .filter(p -> p.getPolicyId() != excludePolicyId) // 排除自己
                .filter(p -> p.getMac().equalsIgnoreCase(mac))
                .anyMatch(p -> {
                    // 检查今天是否是有效日
                    if (!p.getActiveDays().contains(todayIndex)) {
                        return false;
                    }
                    // 检查当前时间是否在 beginTime-endTime 区间
                    return nowSeconds >= p.getBeginTime() && nowSeconds <= p.getEndTime();
                });
    }

}
