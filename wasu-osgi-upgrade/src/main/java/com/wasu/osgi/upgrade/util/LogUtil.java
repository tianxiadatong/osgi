package com.wasu.osgi.upgrade.util;

import org.apache.log4j.PropertyConfigurator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * @author: glmx_
 * @date: 2024/8/1
 * @description:
 */
public class LogUtil {

    public static void initLog(BundleContext ctx) {
        InputStream inputStream = null;
        try {
            Bundle bundle = ctx.getBundle();
            URL resource = bundle.getResource("/log4j.properties");
            if (resource != null) {
                inputStream = resource.openStream();
                Properties properties = new Properties();
                properties.load(inputStream);
                PropertyConfigurator.configure(properties);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
