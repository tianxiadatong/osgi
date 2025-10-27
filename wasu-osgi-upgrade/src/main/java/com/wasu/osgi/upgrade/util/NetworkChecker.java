package com.wasu.osgi.upgrade.util;

import com.wasu.osgi.upgrade.service.NetworkService;
import lombok.Getter;

import java.util.concurrent.CompletableFuture;

/**
 * @author glmx_
 */
public class NetworkChecker implements Runnable {

    private final NetworkService networkService;
    @Getter
    private final CompletableFuture<Void> future;

    public NetworkChecker(NetworkService networkService) {
        this.networkService = networkService;
        this.future = new CompletableFuture<>();
    }

    @Override
    public void run() {
        if (networkService.isNetworkConnected()) {
            future.complete(null);
        }
    }
}
