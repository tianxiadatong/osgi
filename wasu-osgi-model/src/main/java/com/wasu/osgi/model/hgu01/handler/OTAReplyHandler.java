package com.wasu.osgi.model.hgu01.handler;

import com.wasu.osgi.model.hgu01.annotation.MethodHandler;
import com.wasu.osgi.upgrade.service.IFirmwareUpgradeService;
import org.apache.log4j.Logger;
import org.json.JSONObject;

@MethodHandler("thing.ota.get_reply")
public class OTAReplyHandler implements IHandler {

    private static final Logger logger = Logger.getLogger(OTAReplyHandler.class);


    @Override
    public Object handle(String method, JSONObject params) {
        try {
            logger.info("收到云端响应设备请求升级，开始执行升级。");

            IFirmwareUpgradeService upgradeService = UpgradeServiceTracker.getService();
            int retryCount = 0;
            while (upgradeService == null && retryCount < 10) {
                logger.info("第 " + (retryCount + 1) + " 次尝试获取 IFirmwareUpgradeService 服务失败，等待 10 秒后重试...");
                Thread.sleep(10000);
                upgradeService = UpgradeServiceTracker.getService();
                retryCount++;
            }

            if (upgradeService != null) {
                //logger.info("成功获取到 IFirmwareUpgradeService 实例：" + upgradeService);
                upgradeService.checkFirmwareUpgrade(params);
            } else {
                logger.error("无法获取 IFirmwareUpgradeService 服务，云端响应设备请求升级失败。");
            }

        } catch (Exception e) {
            logger.error("收到云端响应设备请求升级时发生异常：", e);
        }
        return null;
    }
}
