package com.wasu.osgi.model.hgu01.handler;

import com.wasu.osgi.model.hgu01.annotation.MethodHandler;
import com.wasu.osgi.model.hgu01.util.ClassPathScanner;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author glmx_
 */
public class HandlerRegistry {

    private static final Logger logger = Logger.getLogger(HandlerRegistry.class);
    private static final Map<String, IHandler> HANDLERS = new HashMap<>();

    static {
        try {
            ClassPathScanner scanner = new ClassPathScanner();
            scanner.scanPackage("com.wasu.osgi.model.hgu01.handler");
            for (Class<?> clazz : scanner.getClasses()) {
                if (IHandler.class.isAssignableFrom(clazz)) {
                    MethodHandler annotation = clazz.getAnnotation(MethodHandler.class);
                    if (annotation != null) {
                        Constructor<?> constructor = clazz.getDeclaredConstructor();
                        IHandler handler = (IHandler) constructor.newInstance();
                        HANDLERS.put(annotation.value(), handler);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("加载mqtt处理类异常，", e);
        }
    }

    public static IHandler getHandler(String method) {
        for (Map.Entry<String, IHandler> entry : HANDLERS.entrySet()) {
            if (entry.getKey().equals(method)) {
                return entry.getValue();
            }
            Pattern pattern = Pattern.compile(entry.getKey());
            Matcher matcher = pattern.matcher(method);
            if (matcher.matches()) {
                return entry.getValue();
            }
        }
        return null;
    }
}
