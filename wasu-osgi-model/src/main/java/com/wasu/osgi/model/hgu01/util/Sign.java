package com.wasu.osgi.model.hgu01.util;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

/**
 * @author: glmx_
 * @date: 2024/9/3
 * @description:
 */
public class Sign {

    private static final Logger logger = Logger.getLogger(Sign.class);

    private static final Comparator<String> ORDER_COMPARATOR = String::compareTo;

    public static String generateSign(String json, String clientSecret) {
        JSONObject jsonObject = new JSONObject(json);
        System.out.println(jsonObject);

        JSONObject system = jsonObject.getJSONObject("system");
        String security = jointKeyValue(system);

        if (jsonObject.has("params") && jsonObject.getJSONObject("params") != null) {
            JSONObject params = jsonObject.getJSONObject("params");
            security = security + jointKeyValue(params);
        }
        security = security + clientSecret;
        logger.info("服务端原加密串: " + security);
        return getEncode(security);
    }

    private static String jointKeyValue(JSONObject jsonObject) {
        if (jsonObject == null || jsonObject.length() == 0) {
            return "";
        }
        String jsonString = jsonObject.toString();
        TreeMap<String, Object> stringObjectMap = jsonToMap(jsonString);
        TreeMap<String, Object> newMap = new TreeMap<>(ORDER_COMPARATOR);
        newMap.putAll(stringObjectMap);
        StringBuilder data2 = new StringBuilder();
        for (Object entry : newMap.keySet()) {
            String key = entry.toString();
            Object value = newMap.get(key);
            data2.append(key).append("=").append(value.toString()).append("||");
        }
        return data2.toString();
    }

    public static TreeMap<String, Object> jsonToMap(String jsonStr) {
        TreeMap<String, Object> treeMap = new TreeMap<>();
        JSONObject json = new JSONObject(jsonStr);
        for (Object key : json.keySet()) {
            Object value = json.get(key.toString());
            if (json.get(key.toString()) == null || value == null || value.toString().isEmpty() || "sign".equals(key)) {
                continue;
            }
            if (value instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) value;
                List<Object> arrayList = new ArrayList<>();
                if (jsonArray.length() == 0) {
                    continue;
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    Object object = jsonArray.get(i);
                    if (object instanceof JSONObject) {
                        object = jsonToMap(object.toString());
                    }
                    arrayList.add(object);
                }
                treeMap.put(key.toString(), arrayList);
            } else {
                boolean flag = isJSONValid(value.toString());
                if (flag) {
                    value = jsonToMap(value.toString());
                }
                treeMap.put(key.toString(), value);
            }
        }
        return treeMap;
    }

    public static boolean isJSONValid(String json) {
        try {
            new JSONObject(json);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }

    public static String getEncode(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bs = md.digest(input.getBytes());
            return toMD5String(bs);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String toMD5String(byte[] bs) {
        char[] chars = new char[bs.length];
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            int val = ((int) bs[i]) & 0xff;
            if (val < 16) {
                buffer.append("0");
            }
            buffer.append(Integer.toHexString(val));
        }
        return buffer.toString();
    }
}
