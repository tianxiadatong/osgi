package com.wasu.osgi.model.hgu01.service.impl;

import com.cw.smartgateway.accessservices.*;
import com.cw.smartgateway.commservices.DeviceConfigService;
import com.cw.smartgateway.commservices.DeviceInfoQueryService;
import com.cw.smartgateway.commservices.DeviceRsetService;
import com.cw.smartgateway.commservices.DeviceSecretInfoQueryService;
import com.cw.smartgateway.lanservices.*;
import com.cw.smartgateway.transferservices.StaticLayer3ForwardingConfigService;
import com.cw.smartgateway.transferservices.TransferQueryService;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * @author: glmx_
 * @date: 2024/11/8
 * @description:
 */
public class InitReferenceService {

    private static final Logger logger = Logger.getLogger(HardwareServiceImpl.class);

    protected static DeviceInfoQueryService deviceInfoQueryService;
    protected static AccessInfoQueryService accessInfoQueryService;
    protected static DeviceSecretInfoQueryService deviceSecretInfoQueryService;
    protected static WlanQueryService wlanQueryService;
    protected static LanHostSpeedQueryService lanHostSpeedQueryService;
    protected static LANHostsInfoQueryService lanHostsInfoQueryService;
    protected static DeviceConfigService deviceConfigService;
    protected static HttpDownloadDiagnosticsService httpDownloadDiagnosticsService;
    protected static HttpUploadDiagnosticsService httpUploadDiagnosticsService;
    protected static WlanConfigService wlanConfigService;
    protected static DeviceRsetService deviceRsetService;
    protected static IPPingDiagnosticsService ipPingDiagnosticsService;
    protected static TraceRouteDiagnosticsService traceRouteDiagnosticsService;
    protected static TransferQueryService transferQueryService;
    protected static StaticLayer3ForwardingConfigService staticLayer3ForwardingConfigService;
    protected static LanHostSpeedLimitService lanHostSpeedLimitService;
    protected static LanNetworkAccessConfigService lanNetworkAccessConfigService;
    protected static WlanSecretQueryService wlanSecretQueryService;
    protected static EthQueryService ethQueryService;
    //protected static PlugConfigService plugConfigService;

    static {
        try {
            logger.info("初始化外部依赖。。。");
            BundleContext bundleContext = FrameworkUtil.getBundle(com.wasu.osgi.model.hgu01.Activator.class).getBundleContext();
            ServiceReference<WlanSecretQueryService> wlanSecretQueryServiceReference = bundleContext.getServiceReference(WlanSecretQueryService.class);
            if (wlanSecretQueryServiceReference != null) {
                wlanSecretQueryService = bundleContext.getService(wlanSecretQueryServiceReference);
            }
            ServiceReference<LanNetworkAccessConfigService> lanNetworkAccessConfigServiceReference = bundleContext.getServiceReference(LanNetworkAccessConfigService.class);
            if (lanNetworkAccessConfigServiceReference != null) {
                lanNetworkAccessConfigService = bundleContext.getService(lanNetworkAccessConfigServiceReference);
            }
            ServiceReference<LanHostSpeedLimitService> lanHostSpeedLimitServiceReference = bundleContext.getServiceReference(LanHostSpeedLimitService.class);
            if (lanHostSpeedLimitServiceReference != null) {
                lanHostSpeedLimitService = bundleContext.getService(lanHostSpeedLimitServiceReference);
            }
            ServiceReference<StaticLayer3ForwardingConfigService> forwardingConfigServiceReference = bundleContext.getServiceReference(StaticLayer3ForwardingConfigService.class);
            if (forwardingConfigServiceReference != null) {
                staticLayer3ForwardingConfigService = bundleContext.getService(forwardingConfigServiceReference);
            }
            ServiceReference<TransferQueryService> transferQueryServiceReference = bundleContext.getServiceReference(TransferQueryService.class);
            if (transferQueryServiceReference != null) {
                transferQueryService = bundleContext.getService(transferQueryServiceReference);
            }
            ServiceReference<TraceRouteDiagnosticsService> traceRouteDiagnosticsServiceReference = bundleContext.getServiceReference(TraceRouteDiagnosticsService.class);
            if (traceRouteDiagnosticsServiceReference != null) {
                traceRouteDiagnosticsService = bundleContext.getService(traceRouteDiagnosticsServiceReference);
            }
            ServiceReference<IPPingDiagnosticsService> ipPingDiagnosticsServiceReference = bundleContext.getServiceReference(IPPingDiagnosticsService.class);
            if (ipPingDiagnosticsServiceReference != null) {
                ipPingDiagnosticsService = bundleContext.getService(ipPingDiagnosticsServiceReference);
            }
            ServiceReference<DeviceInfoQueryService> deviceInfoServiceReference = bundleContext.getServiceReference(DeviceInfoQueryService.class);
            if (deviceInfoServiceReference != null) {
                deviceInfoQueryService = bundleContext.getService(deviceInfoServiceReference);
            }
            ServiceReference<AccessInfoQueryService> accessInfoQueryServiceReference = bundleContext.getServiceReference(AccessInfoQueryService.class);
            if (accessInfoQueryServiceReference != null) {
                accessInfoQueryService = bundleContext.getService(accessInfoQueryServiceReference);
            }
            ServiceReference<WlanQueryService> wlanQueryServiceReference = bundleContext.getServiceReference(WlanQueryService.class);
            if (wlanQueryServiceReference != null) {
                wlanQueryService = bundleContext.getService(wlanQueryServiceReference);
            }
            ServiceReference<LanHostSpeedQueryService> lanHostSpeedQueryServiceReference = bundleContext.getServiceReference(LanHostSpeedQueryService.class);
            if (lanHostSpeedQueryServiceReference != null) {
                lanHostSpeedQueryService = bundleContext.getService(lanHostSpeedQueryServiceReference);
            }
            ServiceReference<LANHostsInfoQueryService> lanHostsInfoQueryServiceReference = bundleContext.getServiceReference(LANHostsInfoQueryService.class);
            if (lanHostsInfoQueryServiceReference != null) {
                lanHostsInfoQueryService = bundleContext.getService(lanHostsInfoQueryServiceReference);
            }
            ServiceReference<DeviceSecretInfoQueryService> deviceSecretInfoQueryServiceReference = bundleContext.getServiceReference(DeviceSecretInfoQueryService.class);
            if (deviceSecretInfoQueryServiceReference != null) {
                deviceSecretInfoQueryService = bundleContext.getService(deviceSecretInfoQueryServiceReference);
            }
            ServiceReference<DeviceConfigService> deviceConfigServiceReference = bundleContext.getServiceReference(DeviceConfigService.class);
            if (deviceConfigServiceReference != null) {
                deviceConfigService = bundleContext.getService(deviceConfigServiceReference);
            }
            ServiceReference<HttpUploadDiagnosticsService> uploadDiagnosticsServiceReference = bundleContext.getServiceReference(HttpUploadDiagnosticsService.class);
            if (uploadDiagnosticsServiceReference != null) {
                httpUploadDiagnosticsService = bundleContext.getService(uploadDiagnosticsServiceReference);
            }
            ServiceReference<HttpDownloadDiagnosticsService> downloadDiagnosticsServiceReference = bundleContext.getServiceReference(HttpDownloadDiagnosticsService.class);
            if (downloadDiagnosticsServiceReference != null) {
                httpDownloadDiagnosticsService = bundleContext.getService(downloadDiagnosticsServiceReference);
            }
            ServiceReference<WlanConfigService> wlanConfigServiceReference = bundleContext.getServiceReference(WlanConfigService.class);
            if (wlanConfigServiceReference != null) {
                wlanConfigService = bundleContext.getService(wlanConfigServiceReference);
            }
            ServiceReference<DeviceRsetService> deviceRsetServiceReference = bundleContext.getServiceReference(DeviceRsetService.class);
            if (deviceRsetServiceReference != null) {
                deviceRsetService = bundleContext.getService(deviceRsetServiceReference);
            }
            ServiceReference<EthQueryService> ethQueryServiceReference = bundleContext.getServiceReference(EthQueryService.class);
            if (ethQueryServiceReference != null) {
                ethQueryService = bundleContext.getService(ethQueryServiceReference);
            }
            /*ServiceReference<PlugConfigService> plugConfigServiceReference = bundleContext.getServiceReference(PlugConfigService.class);
            if(plugConfigServiceReference != null){
                plugConfigService = bundleContext.getService(plugConfigServiceReference);
            }*/
            logger.info("初始化外部依赖成功。");
        } catch (Exception e) {
            logger.error("初始化外部依赖异常，", e);
        }
    }
}
