package com.wasu.osgi.model.hgu01.util;

import org.apache.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * @description: 加解密工具类
 * @author: zhangjr
 * @createDate: 2022/3/7
 * @version: 1.0
 */
public class EncryptUtil {

    private static final Logger logger = Logger.getLogger(EncryptUtil.class);

    public static final byte[] iv = "1111111111111111".getBytes();
    private static boolean enableEncrpt = true; //测试开关

    private static SecretKey generateKey(String secretKey) {
        return new SecretKeySpec(secretKey.getBytes(), "AES");
    }

    public static String encrypt(String secretKey, String data, byte[] iv) {
        if (!enableEncrpt) {
            return data;
        }
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, generateKey(secretKey), ivParameterSpec);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            logger.error("数据加密失败，data：" + data, e);
            return null;
        }
    }

    public static String decrypt(String secretKey, String base64Str, byte[] iv) {
        if (!enableEncrpt) {
            return base64Str;
        }
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            byte[] encrypted = Base64.getDecoder().decode(base64Str);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, generateKey(secretKey), ivParameterSpec);
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("数据解密失败：base64Str:" + base64Str);
            return null;
        }
    }

    public static byte[] getIvKey(String productKey, String deviceId) {
        if (StringUtil.isEmpty(productKey)) {
            return iv;
        }
        if (!StringUtil.isEmpty(deviceId)) {
            return getMD5(deviceId).substring(0, 16).getBytes();
        }
        return getMD5(productKey).substring(0, 16).getBytes();
    }

    public static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] messageDigest = md.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("md5加密异常，", e);
        }
        return "";
    }
}
