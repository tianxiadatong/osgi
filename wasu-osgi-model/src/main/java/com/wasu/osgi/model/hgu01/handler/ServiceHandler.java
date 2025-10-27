package com.wasu.osgi.model.hgu01.handler;

import com.wasu.osgi.model.hgu01.annotation.MethodHandler;
import com.wasu.osgi.model.hgu01.connect.HttpRequest;
import com.wasu.osgi.model.hgu01.service.IHardwareService;
import com.wasu.osgi.model.hgu01.service.impl.HardwareServiceImpl;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author: glmx_
 * @date: 2024/8/27
 * @description:
 */
@MethodHandler("thing.service.+.post")
public class ServiceHandler implements IHandler {

    private static final Logger logger = Logger.getLogger(ServiceHandler.class);

    @Override
    public Object handle(String method, JSONObject params) {
        try {
            String[] split = method.split("\\.");
            if (split.length < 4) {
                return null;
            }
            String attribute = split[2];
            return invokeMethod(attribute, params);
        } catch (Exception e) {
            logger.error("服务调用失败，", e);
            return null;
        }
    }

    private Object invokeMethod(String attribute, JSONObject params) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        IHardwareService hardwareService = new HardwareServiceImpl();
        IHardwareService hardwareService = HttpRequest.getHardwareService();
        Method[] methods = IHardwareService.class.getDeclaredMethods();
        Optional<Method> first = Arrays.stream(methods).filter(s -> s.getName().equals(attribute)).findFirst();
        if (!first.isPresent()) {
            throw new NoSuchMethodException("Method " + attribute + " not found in " + IHardwareService.class.getName());
        }
        Method method = first.get();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 0) {
            return first.get().invoke(hardwareService, params);
        } else {
            return first.get().invoke(hardwareService);
        }
    }
}
