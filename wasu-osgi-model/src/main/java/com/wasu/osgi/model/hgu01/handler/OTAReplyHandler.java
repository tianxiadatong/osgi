package com.wasu.osgi.model.hgu01.handler;

import com.wasu.osgi.model.hgu01.Activator;
import com.wasu.osgi.model.hgu01.annotation.MethodHandler;
import com.wasu.osgi.model.hgu01.config.CommonConstant;
import com.wasu.osgi.model.hgu01.config.ConfigProperties;
import com.wasu.osgi.upgrade.service.IFirmwareUpgradeService;
import com.wasu.osgi.upgrade.service.IUpgradeService;
import com.wasu.osgi.upgrade.util.FileUtil;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

/**
 * @Author fangshengqun
 * @Description
 * @Date 2025/10/20 16:20
 **/
@MethodHandler("thing.ota.get_reply")
public class OTAReplyHandler implements IHandler {

    private static final Logger logger = Logger.getLogger(OTAReplyHandler.class);

    @Override
    public Object handle(String method, JSONObject params) {
        try {
            logger.info("收到云端响应设备请求升级，开始执行升级。");
            BundleContext bundleContext = FrameworkUtil.getBundle(Activator.class).getBundleContext();

            ServiceReference<IFirmwareUpgradeService> iFirmwareUpgradeServiceServiceReference = bundleContext.getServiceReference(IFirmwareUpgradeService.class);
            logger.info("获取到的IFirmwareUpgradeService：" + iFirmwareUpgradeServiceServiceReference);
            if (iFirmwareUpgradeServiceServiceReference != null) {
                IFirmwareUpgradeService iFirmwareUpgradeService = bundleContext.getService(iFirmwareUpgradeServiceServiceReference);
                iFirmwareUpgradeService.checkFirmwareUpgrade(params);
            }
            return null;

        } catch (Exception e) {
            logger.info("收到云端响应设备请求升级,升级执行异常，", e);
        }
        return null;
    }

}
