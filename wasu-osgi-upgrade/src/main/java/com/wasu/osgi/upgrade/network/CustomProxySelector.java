/*
package com.wasu.osgi.upgrade.network;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.List;

*/
/**
 * @author glmx_
 *//*

public class CustomProxySelector extends ProxySelector {

    private final InetAddress localAddress;

    public CustomProxySelector(InetAddress localAddress) {
        this.localAddress = localAddress;
    }

    @Override
    public List<Proxy> select(URI uri) {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(localAddress, 0);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, inetSocketAddress);
        return Collections.singletonList(proxy);
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {

    }
}
*/
