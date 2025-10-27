package com.wasu.osgi.model.hgu01.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Vicky
 * @date 2025年06月11日 11:06
 */
@Data
public class TimeLimitPolicy {
    // 策略基础信息
    private final int policyId;
    private final String mac;
    private final String days; // 格式："1010101"（周一至周日，1=启用）；
    private final long beginTime; // 开始时间（秒，从00:00计算）
    private final long endTime;   // 结束时间（秒，从00:00计算）

    // 运行时状态
    private boolean enabled;
    private final List<String> subTaskIds = new CopyOnWriteArrayList<>(); // 子任务ID集合

    //================= 构造方法 =================//

    public TimeLimitPolicy(int policyId, String mac, String days,
                           long beginTime, long endTime, boolean enabled) {
        this.policyId = policyId;
        this.mac = mac;
        this.days = days;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.enabled = enabled;
    }

    /**
     * 从字符串解析策略对象
     */
    public static TimeLimitPolicy fromStorageString(int policyId, String storageString) {
        // 格式: MAC|DAYS|BEGIN_TIME|END_TIME|ENABLED
        String[] parts = storageString.split("\\|");
        if (parts.length >= 5) {
            TimeLimitPolicy policy = new TimeLimitPolicy(
                    policyId,
                    parts[0], // mac
                    parts[1], // days
                    Long.parseLong(parts[2]), // beginTime
                    Long.parseLong(parts[3]), // endTime
                    Boolean.parseBoolean(parts[4]) // enabled
            );
            return policy;
        }
        return null;
    }

    /**
     * 获取策略对应的所有执行日
     *
     * @return 返回执行日的索引列表（0=周一，6=周日）
     */
    public List<Integer> getActiveDays() {
        List<Integer> activeDays = new ArrayList<>();
        for (int i = 0; i < days.length(); i++) {
            if (days.charAt(i) == '1') {
                activeDays.add(i);
            }
        }
        return activeDays;
    }

    //================= 状态管理方法 =================//

    public synchronized void enable() {
        this.enabled = true;
    }

    public synchronized void disable() {
        this.enabled = false;
    }

    public synchronized void addSubTaskId(String taskId) {
        if (!subTaskIds.contains(taskId)) {
            subTaskIds.add(taskId);
        }
    }

    public synchronized void removeSubTaskId(String taskId) {
        subTaskIds.remove(taskId);
    }

    @Override
    public String toString() {
        return String.join("|",
                mac,
                days,
                String.valueOf(beginTime),
                String.valueOf(endTime),
                String.valueOf(enabled)
        );
    }
}

