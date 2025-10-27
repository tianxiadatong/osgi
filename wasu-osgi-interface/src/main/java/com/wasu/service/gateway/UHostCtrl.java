package com.wasu.service.gateway;

/**
 * @author: glmx_
 * @date: 2024/12/31
 * @description: 终端管理接口
 */
public interface UHostCtrl {

    /**
     * 查看终端列表
     *
     * @return String类型, jsonObject格式
     */
    String getHostList();

    /**
     * 设置终端别名
     *
     * @param macAddress String类型，终端MAC地址，xx:xx:xx:xx:xx:xx格式
     * @param alias      String类型，终端别名
     * @return String类型, jsonObject格式
     */
    String setHostAlias(String macAddress, String alias);

    /**
     * 删除列表中的终端
     *
     * @param macAddress String类型，终端MAC地址，xx:xx:xx:xx:xx:xx格式
     * @return String类型, jsonObject格式
     */
    String removeHosts(String macAddress);

    /**
     * 获取指示灯状态
     *
     * @return int类型 控制指示灯开启/关闭，0：关闭； 1：开启
     */
    int getLedStatus();

    /**
     * 控制指示灯状态
     *
     * @param enable int类型，控制指示灯开启/关闭，0：关闭； 1：开启
     * @return boolean设置是否成功 true-设置成功 false-设置失败
     */
    boolean setLedStatus(int enable);

    /**
     * 获取WPS状态
     *
     * @return String类型 ,jsonObject格式
     */
    String getWpsStatus();

    /**
     * 控制WPS状态
     *
     * @param enable int类型，WPS功能开启/关闭，0：关闭； 1：开启
     * @param radio  int类型 WPS触发频段，0：所有的；1：2.4G；2：5G
     * @return boolean设置是否成功 true-设置成功 false-设置失败
     */
    boolean setWpsStatus(int enable, int radio);

    /**
     * 数据采集频率查询
     * 数据采集频率查询/设置，一般用于插件根据业务需要程度，以提供不同接口的数据回传周期，同时为确保对终端性能稳定性的可管可控，可针对时间周期进行调整，例如：5/10/30/60分钟
     *
     * @return Integer
     */
    Integer getDataLogSpace();

    /**
     * 数据采集频率设置
     * 数据采集频率查询/设置，一般用于插件根据业务需要程度，以提供不同接口的数据回传周期，同时为确保对终端性能稳定性的可管可控，可针对时间周期进行调整，例如：5/10/30/60分钟
     *
     * @param space Integer  5/10/30/60分钟
     * @return Integer
     */
    Integer setDataLogSpace(Integer space);
}
