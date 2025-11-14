package com.wasu.osgi.model.hgu01.util;

import java.time.Duration;

/**
 * @author Vicky
 * @date 2025年06月06日 11:04
 */
public class CommonUtil {

    /**
     * 将秒为单位的time转换成时间 21：00 ; 9:00形式
     */
    public static String convertSecondsToHourMinute(Long totalSeconds) {
        if (totalSeconds == null) {
            return null;
        }

        Duration duration = Duration.ofSeconds(totalSeconds);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;  // 计算剩余分钟

        if (hours > 24 || (hours == 24 && minutes > 0)) {
            return "0:00";
        }

        return String.format("%d:%02d", hours, minutes);
    }

    /**
     * 将时间字符串（如 "1:00"）转换为秒数（如 3600）
     * @param timeStr 时间字符串，格式 "HH:mm"
     * @return 对应的秒数
     */
    public static int convertHourMinuteToSeconds(String timeStr) {
        String[] parts = timeStr.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("时间格式应为 HH:mm，实际是: " + timeStr);
        }

        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);

        return (hours * 3600) + (minutes * 60);
    }
}
