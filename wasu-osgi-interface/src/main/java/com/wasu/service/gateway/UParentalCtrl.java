package com.wasu.service.gateway;

/**
 * @author: glmx_
 * @date: 2024/12/31
 * @description: 父母控制服务管理接口
 */
public interface UParentalCtrl {
    /**
     * 查看父母控制开关
     *
     * @return String类型, jsonObject格式
     */
    String getParentalCtrlParam();

    /**
     * 设置父母控制开关
     * 若要开始启用父母控制功能，必须首先打开此开关。
     *
     * @param enableParentalCtrl String类型，"on"或"off"
     * @return String类型, jsonObject格式
     */
    String setParentalCtrlParam(String enableParentalCtrl);

    /**
     * 获取直播频道列表(DVB网关)
     *
     * @return String类型, jsonObject格式
     */
    String getDvbChannelList();

    /**
     * 获取受限终端列表
     *
     * @return String类型, jsonObject格式
     */
    String getUnderCtrlHostList();

    /**
     * 增加受限终端
     *
     * @param macAddress String类型，xx:xx:xx:xx:xx:xx格式的终端MAC地址
     * @return String类型, jsonObject格式
     */
    String addUnderCtrlHost(String macAddress);

    /**
     * 删除受限终端
     *
     * @param macAddress String类型，xx:xx:xx:xx:xx:xx格式的终端MAC地址
     * @return String类型, jsonObject格式
     */
    String removeUnderCtrlHost(String macAddress);

    /**
     * 获取直播收视受控规则(DVB网关)
     *
     * @param macAddress String类型，xx:xx:xx:xx:xx:xx格式的终端MAC地址
     * @return String类型, jsonObject格式
     */
    String getUnderCtrlHostDvbRules(String macAddress);

    /**
     * 设置直播收视控制状态(DVB网关)
     * 设置直播收视控制状态针对一个具体的规则对象设置开启和关闭，因此，需先使用“增加直播收视控制状态”接口创建一个规则对象后才能调用此接口设置功能。
     *
     * @param macAddress      String类型，xx:xx:xx:xx:xx:xx格式的终端MAC地址,
     * @param objIndex        String类型，（时间，电视规则）对象索引号,
     * @param timeOrDvbStatus String类型，"timeStatus"或"dvbStatus",
     * @param status          String类型，"on"或"off"
     * @return String类型, jsonObject格式
     */
    String setUnderCtrlHostDvbStatus(String macAddress, String objIndex, String timeOrDvbStatus, String status);

    /**
     * 增加直播收视受控规则(DVB网关)
     *
     * @param objIndex   String类型，（时间，电视规则）对象索引号,如果要创建一个新对象，则需要传入"NULL"，这样服务层会创建一个新的对象索引，并通过该接口返回值返回。
     *                   虽然一个设备可以创建多个规则对象，但当前版本只有第一个规则对象是有效的。
     * @param timeRange  String类型，时间范围参数
     * @param dvbWatch   String类型，直播收视控制参数
     * @param macAddress String类型，主机MAC地址，xx:xx:xx:xx:xx:xx格式。
     * @return String类型, jsonObject格式
     */
    String addUnderCtrlHostDvbRules(String objIndex, String timeRange, String dvbWatch, String macAddress);

    /**
     * 删除直播频道受控规则(DVB网关)
     *
     * @param objIndex   String类型，（时间，电视规则）对象索引号,
     * @param timeRange  String类型，时间范围参数
     * @param dvbWatch   String类型，直播频点控制参数
     * @param macAddress String类型，主机MAC地址，xx:xx:xx:xx:xx:xx格式。
     * @return String类型, jsonObject格式
     */
    String removeUnderCtrlHostDvbRules(String objIndex, String timeRange, String dvbWatch, String macAddress);

    /**
     * 获取上网名单
     *
     * @param macAddress String类型，xx:xx:xx:xx:xx:xx格式的终端MAC地址
     * @return String类型, jsonObject格式
     */
    String getUnderCtrlHostInternetRules(String macAddress);

    /**
     * 设置上网名单状态
     *
     * @param macAddress String类型，xx:xx:xx:xx:xx:xx格式的终端MAC地址
     * @param status     String类型，"on"或"off"
     * @return String类型, jsonObject格式
     */
    String setUnderCtrlHostInternetStatus(String macAddress, String status);

    /**
     * 增加上网受控名单
     *
     * @param accessInternet String类型，时间范围参数
     * @param macAddress     String类型，主机MAC地址，xx:xx:xx:xx:xx:xx格式
     * @return String类型, jsonObject格式
     */
    String addUnderCtrlHostInternetRules(String accessInternet, String macAddress);

    /**
     * 删除上网受控名单
     *
     * @param accessInternet String类型，上网规则参数
     * @param macAddress     String类型，主机MAC地址，xx:xx:xx:xx:xx:xx格式。
     * @return String类型, jsonObject格式
     */
    String removeUnderCtrlHostInternetRules(String accessInternet, String macAddress);

    /**
     * 获取上网许可信息
     *
     * @param macAddress String类型，xx:xx:xx:xx:xx:xx格式的终端MAC地址
     * @return String类型, jsonObject格式
     */
    String getUnderCtrlHostInternetPermission(String macAddress);

    /**
     * 设置上网许可状态
     *
     * @param macAddress String类型，xx:xx:xx:xx:xx:xx格式的终端MAC地址
     * @param status     String类型，"on"或"off"
     * @return String类型, jsonObject格式
     */
    String setUnderCtrlHostInternetPermission(String macAddress, String status);

    /**
     * 增加上网许可时段
     *
     * @param timeRange  String类型，时间范围参数
     * @param macAddress String类型，主机MAC地址，xx:xx:xx:xx:xx:xx格式。
     * @return String类型, jsonObject格式
     */
    String addUnderCtrlHostInternetPermission(String timeRange, String macAddress);

    /**
     * 删除上网许可时段
     *
     * @param timeRange  String类型，时间范围参数
     * @param macAddress String类型，主机MAC地址，xx:xx:xx:xx:xx:xx格式。
     * @return String类型, jsonObject格式
     */
    String removeUnderCtrlHostInternetPermission(String timeRange, String macAddress);

    /**
     * 获取终端访问域名的历史记录
     *
     * @param macAddress String类型，xx:xx:xx:xx:xx:xx格式的终端MAC地址,* 表示获取所有终端的访问历史
     * @return String类型, jsonObject格式
     */
    String getWebSiteHistory(String macAddress);

    /**
     * 设置应用禁用规则
     *
     * @param type 1 应用类型 2 IP  3 域名
     * @return 1调用成功 0失败
     */
    int setAppDisableRole(Integer type);

    /**
     * 查询PON口接入方式
     *
     * @return String
     */
    String getPonLinkType();
}
