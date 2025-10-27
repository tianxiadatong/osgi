package com.wasu.osgi.model.hgu01.util;

import com.wasu.osgi.model.hgu01.config.CommonConstant;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * @author: glmx_
 * @date: 2024/8/21
 * @description:
 */
public class ConfigUtil {

    private static final Logger logger = Logger.getLogger(ConfigUtil.class);

    public static void store(Properties properties, String fileName) {
        if (properties == null) {
            return;
        }
        try (FileOutputStream fileOut = new FileOutputStream(CommonConstant.DATA_PATH + fileName)) {
            properties.store(fileOut, "store configuration.");
        } catch (Exception e) {
            logger.error("保存配置文件异常，", e);
        }
    }

    public static <T> void storeWithClazz(T data, String fileName) {
        if (data == null) {
            return;
        }
        try (FileOutputStream fileOut = new FileOutputStream(CommonConstant.DATA_PATH + fileName)) {
            Properties properties = buildProperties(data);
            properties.store(fileOut, "store configuration.");
        } catch (Exception e) {
            logger.error("保存配置文件异常，", e);
        }
    }

    public static <T> T loadForClazz(Class<T> clazz, String fileName) {
        Properties properties = new Properties();
        try (FileInputStream fileIn = new FileInputStream(CommonConstant.DATA_PATH + fileName)) {
            properties.load(fileIn);
            T obj = clazz.getDeclaredConstructor().newInstance();
            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                Field field = clazz.getDeclaredField(key);
                field.setAccessible(true);
                if (field.getType().equals(String.class)) {
                    field.set(obj, value);
                } else if (field.getType().equals(Integer.class)) {
                    field.set(obj, Integer.parseInt(value));
                }
            }
            return obj;
        } catch (Exception e) {
            logger.error("配置文件转对象异常，" + e.getMessage());
            return null;
        }
    }

    public static Properties load(String fileName) {
        Properties properties = new Properties();
        try (FileInputStream fileIn = new FileInputStream(CommonConstant.DATA_PATH + fileName)) {
            properties.load(fileIn);
            return properties;
        } catch (Exception e) {
            logger.error("加载配置文件异常，" + e.getMessage());
            return null;
        }
    }

    private static <T> Properties buildProperties(T config) throws IllegalAccessException {
        Properties properties = new Properties();
        Class<?> configClass = config.getClass();
        Field[] fields = configClass.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(config);
            if (value != null) {
                String fieldName = field.getName();
                String fieldValue = value.toString();
                properties.setProperty(fieldName, fieldValue);
            }
        }
        return properties;
    }
}
