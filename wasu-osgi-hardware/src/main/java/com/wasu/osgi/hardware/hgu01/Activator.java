package com.wasu.osgi.hardware.hgu01;

import com.wasu.osgi.common.util.LogUtil;
import com.wasu.osgi.hardware.hgu01.service.IOSGIHardwareService;
import com.wasu.osgi.hardware.hgu01.service.impl.OSGIHardwareServiceImpl;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


/**
 * @author glmx_
 */
public class Activator implements BundleActivator{

    private static final Logger logger = Logger.getLogger(Activator.class);

    @Override
    public void start(BundleContext ctx) {
        LogUtil.initLog(ctx);
        logger.info("==========wasu-osgi-hardware bundle start==========");
        ctx.registerService(IOSGIHardwareService.class, new OSGIHardwareServiceImpl(), null);
        logger.info("wasu-osgi-hardware bundle register.");
    }

    @Override
    public void stop(BundleContext ctx) throws Exception {

    }
}
