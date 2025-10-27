package com.wasu.osgi.model.hgu01.util;

import com.wasu.osgi.model.hgu01.config.Certificates;
import com.wasu.osgi.model.hgu01.connect.MqttConnect;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import javax.net.SocketFactory;
import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author glmx_
 */
public class CertificateValidator {

    public static SocketFactory sslNotCheck() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[1];
        TrustManager tm = new MyTrustManager();
        trustAllCerts[0] = tm;
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        return sc.getSocketFactory();
    }

    public static SSLSocketFactory sslCheck() throws Exception {
        // 解析证书字符串
        List<X509Certificate> certificates = new ArrayList<>();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        for (String certStr : Certificates.SSL_CERTIFICATES) {
            certificates.add((X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certStr.getBytes())));
        }

        // 创建 KeyStore 并添加证书
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null); // 初始化一个空的 KeyStore
        int index = 1;
        for (X509Certificate cert : certificates) {
            keyStore.setCertificateEntry("ca" + index++, cert);
        }

        // 创建 TrustManagerFactory 并初始化
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        // 从 TrustManager 数组中筛选出 X509TrustManager
        List<X509TrustManager> x509TrustManagers = new ArrayList<>();
        for (TrustManager tm : tmf.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                x509TrustManagers.add((X509TrustManager) tm);
            }
        }

        // 创建 SSLContext 并设置信任管理器
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{new MultiTrustManager(x509TrustManagers.toArray(new X509TrustManager[0]))}, null);

        return sslContext.getSocketFactory();
    }

    static class MultiTrustManager implements X509TrustManager {
        private final X509TrustManager[] trustManagers;

        public MultiTrustManager(X509TrustManager[] trustManagers) {
            this.trustManagers = trustManagers;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            for (X509TrustManager tm : trustManagers) {
                try {
                    tm.checkClientTrusted(chain, authType);
                    return;
                } catch (CertificateException ignored) {
                    // Ignore and try next trust manager
                }
            }
            throw new CertificateException("None of the trust managers could validate the certificate.");
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            for (X509TrustManager tm : trustManagers) {
                try {
                    tm.checkServerTrusted(chain, authType);
                    return;
                } catch (CertificateException ignored) {
                    // Ignore and try next trust manager
                }
            }
            throw new CertificateException("None of the trust managers could validate the certificate.");
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            List<X509Certificate> issuers = new ArrayList<>();
            for (X509TrustManager tm : trustManagers) {
                X509Certificate[] tmIssuers = tm.getAcceptedIssuers();
                Collections.addAll(issuers, tmIssuers);
            }
            return issuers.toArray(new X509Certificate[0]);
        }
    }

    static class MyTrustManager implements TrustManager, X509TrustManager {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }
    }
}
