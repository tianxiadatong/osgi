package com.wasu.osgi.model.hgu01.timetask;

import com.wasu.osgi.model.hgu01.service.NetworkService;
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
