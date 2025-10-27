package com.wasu.osgi.model.hgu01.util;

public enum FuncNameEnum {
    SCHEDULED_RESTART("scheduledRestart"),
    GAME_ASSISTANT("gameAssistant");

    private final String code;

    FuncNameEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * 根据 code 查找对应的枚举值（不区分大小写）
     *
     * @param code 要查找的 code
     * @return 匹配的枚举值，找不到返回 null
     */
    public static FuncNameEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (FuncNameEnum value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return code;
    }
}
