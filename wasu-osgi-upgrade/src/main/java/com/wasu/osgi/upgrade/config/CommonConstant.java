package com.wasu.osgi.upgrade.config;

/**
 * @author: glmx_
 * @date: 2024/8/20
 * @description:
 */
public class CommonConstant {
    public static final String BUNDLE_PATH = "/usr/data/o_dl/";
    public static final String BUNDLE_BACK_PATH = "/usr/data/o_ins/";
    public static final String DEFAULT_PATH = "/usr/local/osgi/local/osgi/felix/bundle/";

    public static final String FILE_MD5_SALT = "$wasu.wts$";

    public static final String CLIENT_ID = "wasu.client.hub.phulw76a4l05nqav";
    public static final String CLIENT_SECRET = "72ulozmf6qtvubn4fc6t43ln5lh9p0h4";

    public static final String NETWORK_CHECK = "hgu-api.iot.wasumedia.cn";

    public static final String UPGRADE_FILE_NAME = "com.wasu.osgi.bundle.update";

    // 升级类型常量
    public static final String UPGRADE_TYPE_MAIN = "main";
    public static final String UPGRADE_TYPE_C_PLUGIN = "c_plugin";
    public static final String UPGRADE_TYPE_JAVA_PLUGIN = "java_plugin";
}
