/*
package com.wasu.osgi.upgrade.network;

import com.wasu.osgi.upgrade.config.CommonConstant;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ProxySelector;
import java.net.SocketException;
import java.util.Enumeration;

*/
/**
 * @author glmx_
 *//*

public class WanInterfaceSelector {

    private static final Logger logger = Logger.getLogger(WanInterfaceSelector.class);

    public static NetworkInterface selectWanInterface(String targetUrl) {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                logger.info("网卡列表-----------"+ new JSONObject(ni) +"---------------");
                String interfaceAddresses = new JSONArray(ni.getInterfaceAddresses()).toString();
                if(interfaceAddresses.contains("2401:ce00:a400")) {
                    logger.info("-----------------选中的网卡："+new JSONObject(ni) +"---------------");
                    return ni;
                }
            }
        } catch (SocketException e) {
            logger.error("选中网卡异常，",e);
        }
        return null;
    }

    private static boolean isWanInterface(NetworkInterface ni) throws SocketException {
        return !ni.isLoopback() && ni.isUp() && !ni.isPointToPoint();
    }

    private static boolean matchesTargetUrl(NetworkInterface ni, String targetUrl) {
        String hostName = targetUrl.split("/")[2].replace("[","").replace("]","");
        logger.info("hostName:" + hostName);
        if (CommonConstant.TR069_HOSTS.contains(hostName)) {
            return new JSONArray(ni.getInterfaceAddresses()).toString().contains("2401:ce00:a400");
        }
        return false;
    }

    public static void selectNetAdepter(String urlStr){
        NetworkInterface wanInterface = WanInterfaceSelector.selectWanInterface(urlStr);
        if (wanInterface == null) {
            logger.info("No suitable WAN interface found.");
            return;
        }
        InetAddress localAddress = wanInterface.getInetAddresses().nextElement();
        ProxySelector.setDefault(new CustomProxySelector(localAddress));
    }
}
*/
