package com.wasu.osgi.upgrade.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class UpdateRuntime {
    private static final List<ExecutorService> EXECUTORS = new ArrayList<>();

    public static void register(ExecutorService executor) {
        EXECUTORS.add(executor);
    }

    public static void shutdownAll() {
        for (ExecutorService ex : EXECUTORS) {
            try { ex.shutdownNow(); } catch (Exception ignored) {}
        }
        EXECUTORS.clear();
    }
}
