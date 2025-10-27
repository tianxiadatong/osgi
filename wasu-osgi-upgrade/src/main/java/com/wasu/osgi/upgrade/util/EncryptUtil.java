package com.wasu.osgi.upgrade.util;

import org.apache.log4j.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @description: 加解密工具类
 * @author: zhangjr
 * @createDate: 2022/3/7
 * @version: 1.0
 */
public class EncryptUtil {

    private static final Logger logger = Logger.getLogger(EncryptUtil.class);

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
