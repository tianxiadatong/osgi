package com.wasu.service.gateway;

/**
 * @author: glmx_
 * @date: 2024/12/31
 * @description: 网关设置接口
 */
public interface UGatewaySetup {

    /**
     * 重启网关
     * @return int类型，0表示成功，非0表示失败
     */
    int reboot();

    /**
     * 重置网关
     * @return int类型，0表示成功，非0表示失败
     */
    int factoryReset();
}
