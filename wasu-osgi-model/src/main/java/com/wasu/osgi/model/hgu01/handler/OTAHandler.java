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
 * @author: glmx_
 * @date: 2024/8/27
 * @description:
 */
@MethodHandler("thing.ota.notify")
public class OTAHandler implements IHandler {

    private static final Logger logger = Logger.getLogger(OTAHandler.class);

    @Override
    public Object handle(String method, JSONObject params) {
        try {
            logger.info("收到升级通知，开始执行升级。");
            BundleContext bundleContext = FrameworkUtil.getBundle(Activator.class).getBundleContext();
            boolean started = startUpgradeBundle(bundleContext);
            if (started) {
                ServiceReference<IUpgradeService> serviceReference = bundleContext.getServiceReference(IUpgradeService.class);
                logger.info("获取到的IUpgradeService：" + serviceReference);
                if (serviceReference != null) {
                    IUpgradeService upgradeService = bundleContext.getService(serviceReference);
                    upgradeService.checkUpgrade(ConfigProperties.deviceId);
                }
                ServiceReference<IFirmwareUpgradeService> iFirmwareUpgradeServiceServiceReference = bundleContext.getServiceReference(IFirmwareUpgradeService.class);
                logger.info("获取到的IFirmwareUpgradeService：" + iFirmwareUpgradeServiceServiceReference);
                if (iFirmwareUpgradeServiceServiceReference != null) {
                    IFirmwareUpgradeService iFirmwareUpgradeService = bundleContext.getService(iFirmwareUpgradeServiceServiceReference);
                    iFirmwareUpgradeService.upOTA(params);
                }
                return null;
            }
        } catch (Exception e) {
            logger.info("升级通知执行异常，", e);
        }
        return null;
    }

    public static boolean startUpgradeBundle(BundleContext bundleContext) {
        try {
            Bundle[] bundles = bundleContext.getBundles();
            Optional<Bundle> first = Arrays.stream(bundles).filter(s -> "com.wasu.osgi.bundle.update".equals(s.getSymbolicName())).findFirst();
            if (!first.isPresent()) {
                try {
                    File backupFile = FileUtil.getBackupFile(CommonConstant.UPGRADE_FILE_NAME);
                    if (backupFile != null) {
                        Bundle bundle = bundleContext.installBundle("file:" + backupFile.getAbsolutePath());
                        bundle.start();
                        logger.info("com.wasu.osgi.bundle.update bundle started");
                        return true;
                    }
                    logger.error("com.wasu.osgi.bundle.update jar file not found");
                } catch (Exception e) {
                    logger.error(e);
                    File jarFile = FileUtil.getDefaultBundleFile(CommonConstant.UPGRADE_FILE_NAME);
                    if (jarFile != null) {
                        Bundle bundle = bundleContext.installBundle("file:" + jarFile.getAbsolutePath());
                        bundle.start();
                        logger.info("com.wasu.osgi.bundle.update back jar started");
                        return true;
                    }
                }
            } else {
                Bundle bundle = first.get();
                logger.info(bundle.getSymbolicName() + " bundle status: " + bundle.getState());
                if (bundle.getState() == Bundle.ACTIVE) {
                    return true;
                }
                if (bundle.getState() == Bundle.STOP_TRANSIENT) {
                    bundle.start();
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("安装com.wasu.osgi.bundle.update bundle异常，", e);
        }
        return false;
    }
}
