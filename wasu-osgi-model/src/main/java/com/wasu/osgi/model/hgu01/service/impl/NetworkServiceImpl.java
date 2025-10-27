package com.wasu.osgi.model.hgu01.service.impl;

import com.wasu.osgi.model.hgu01.config.CommonConstant;
import com.wasu.osgi.model.hgu01.service.NetworkService;
import org.apache.log4j.Logger;

import java.net.InetAddress;

/**
 * @author glmx_
 */
public class NetworkServiceImpl implements NetworkService {

    private static final Logger logger = Logger.getLogger(NetworkServiceImpl.class);

    @Override
    public boolean isNetworkConnected() {
        try {
            boolean reachable = InetAddress.getByName(CommonConstant.NETWORK_CHECK).isReachable(5000);
            logger.info(CommonConstant.NETWORK_CHECK + " network state:" + reachable);
            return reachable;
        } catch (Exception e) {
            logger.error(CommonConstant.NETWORK_CHECK + " network error:" + e.getMessage());
            return false;
        }
    }
}