package com.wasu.osgi.upgrade.util;
import lombok.Data;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * @author: glmx_
 * @date: 2024/8/23
 * @description:
 */
public class HttpUtil {

    private static final Logger logger = Logger.getLogger(HttpUtil.class);

    private static final Integer CONNECT_TIMEOUT = 5000;
    private static final Integer READ_TIMEOUT = 10000;
    private static final String CONTENT_TYPE = "application/json;charset=UTF-8";

    public static HttpResult postRequest(String requestUrl, String postData) {
        HttpResult httpResult = new HttpResult();
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;
        try {
            logger.info("发起http请求，url：" + requestUrl + "，参数：" + postData);
            connection = getConnection(requestUrl, postData, "POST");
            int responseCode = connection.getResponseCode();
            httpResult.setStatus(responseCode);
            if (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE) {
                httpResult.success = true;
                bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = bufferedReader.readLine()) != null) {
                    content.append(inputLine);
                }
                httpResult.setBody(content.toString());
            }
            logger.info("http请求结果：" + new JSONObject(httpResult));
            return httpResult;
        } catch (Exception e) {
            logger.error("发送http请求异常，", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ignored) {
                }
            }
        }
        return httpResult;
    }

    public static HttpResult getRequest(String requestUrl, String postData) {
        HttpResult httpResult = new HttpResult();
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;
        try {
            logger.info("发起http请求GET，url：" + requestUrl + "，参数：" + postData);
            connection = getConnection(requestUrl, postData, "GET");
            int responseCode = connection.getResponseCode();
            httpResult.setStatus(responseCode);
            if (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE) {
                httpResult.success = true;
                bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = bufferedReader.readLine()) != null) {
                    content.append(inputLine);
                }
                httpResult.setBody(content.toString());
                return httpResult;
            }
            logger.info("http请求结果：" + new JSONObject(httpResult));
            return httpResult;
        } catch (Exception e) {
            logger.error("发送http请求异常，", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ignored) {
                }
            }
        }
        return httpResult;
    }

    private static HttpURLConnection getConnection(String requestUrl, String postData, String method) throws Exception {
        if (requestUrl.contains("2401:ce00:4c00:1000::186")) {
            CertificateValidator.sslNotCheck();
        } else {
            CertificateValidator.sslCheck();
        }
        URL url = new URL(requestUrl);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", CONTENT_TYPE);
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
            wr.writeBytes(new String(postData.getBytes(), StandardCharsets.UTF_8));
            wr.flush();
        }
        connection.connect();
        return connection;
    }

    private static String getIpUrl(String hostUrl) throws UnknownHostException, MalformedURLException {
        URL url = new URL(hostUrl);
        InetAddress ipv6Address = Inet6Address.getByName(url.getHost());
        String[] split = hostUrl.split(url.getHost());
        String requestUrl = split[0] + "[" + ipv6Address.getHostAddress() + "]";
        if (split.length > 1) {
            requestUrl += split[1];
        }
        return requestUrl;
    }

    @Data
    public static class HttpResult {
        private boolean success = false;
        private Integer status;
        private String body;
    }
}
