package com.wasu.osgi.upgrade.service.impl;

import com.wasu.osgi.upgrade.Activator;
import com.wasu.osgi.upgrade.config.CommonConstant;
import com.wasu.osgi.upgrade.config.ConfigProperties;
import com.wasu.osgi.upgrade.dto.UpgradeFileDTO;
import com.wasu.osgi.upgrade.service.IUpgradeService;
import com.wasu.osgi.upgrade.dto.UpgradeInfoDTO;
import com.wasu.osgi.upgrade.dto.WTSRequest;
import com.wasu.osgi.upgrade.util.EncryptUtil;
import com.wasu.osgi.upgrade.util.FileUtil;
import com.wasu.osgi.upgrade.util.HttpUtil;
import com.wasu.osgi.upgrade.util.StringUtil;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author: glmx_
 * @date: 2024/8/21
 * @description:
 */
public class UpgradeServiceImpl implements IUpgradeService {

    private static final Logger logger = Logger.getLogger(UpgradeServiceImpl.class);

    @Override
    public boolean checkUpgrade(String deviceId) {
        try {
            JSONObject params = new JSONObject();
            params.put("deviceId", deviceId);
            params.put("thirdCloudModel", ConfigProperties.productKey);
            WTSRequest<Object> request = new WTSRequest<>().create(params);
            HttpUtil.HttpResult httpResult = HttpUtil.postRequest(ConfigProperties.otaUrl + "/ota/plug/upgrade/check_upgrade",
                    new JSONObject(request).toString());
            if (httpResult.isSuccess()) {
                JSONObject wtsResponse = new JSONObject(httpResult.getBody());
                JSONObject system = wtsResponse.getJSONObject("system");
                if (system == null || system.getInt("code") != 0 || !wtsResponse.has("result")) {
                    return false;
                }
                JSONObject result = wtsResponse.getJSONObject("result");
                UpgradeInfoDTO upgradeInfoDTO = convert(result);
                List<UpgradeFileDTO> upgradeFileDTOS = upgradeInfoDTO.getFileList();
                BundleContext bundleContext = FrameworkUtil.getBundle(Activator.class).getBundleContext();
                Bundle[] bundles = bundleContext.getBundles();
                String rules = upgradeInfoDTO.getRules();
                String[] split = rules.split(",");
                StringBuilder resultMessage = new StringBuilder();
                //默认升级成功
                int upgradeResult = 31;
                for (String symbolicName : split) {
                    Optional<UpgradeFileDTO> upgradeDTOOptional = upgradeFileDTOS.stream().filter(s -> symbolicName.equals(s.getModel())).findFirst();
                    if (!upgradeDTOOptional.isPresent()) {
                        continue;
                    }
                    UpgradeFileDTO upgradeFileDTO = upgradeDTOOptional.get();
                    Optional<Bundle> first = Arrays.stream(bundles).filter(s -> symbolicName.equals(s.getSymbolicName())).findFirst();
                    //更新
                    if (first.isPresent()) {
                        Bundle bundle = first.get();
                        logger.info(symbolicName + "插件已存在:" + bundle.getLocation());
                        if (!StringUtil.isEmpty(upgradeFileDTO.getVersion())
                                && upgradeFileDTO.getVersion().compareTo(bundle.getVersion().toString()) > 0) {
                            logger.info(symbolicName + "开始升级。");
                            File backupFile = FileUtil.getBackupFile(symbolicName);
                            try {
                                sendResult(1, symbolicName + " start update.", null);
                                boolean backResult = FileUtil.backupFile(backupFile, symbolicName);
                                if (backResult) {
                                    File newFile = downloadFile(symbolicName, upgradeFileDTO.getUrl(), upgradeFileDTO.getMd5());
                                    if (newFile != null) {
                                        logger.info(symbolicName + "文件下载成功。");
                                        sendResult(21, symbolicName + " file download success.", upgradeFileDTO.getVersion());
                                        if (!newFile.getName().contains(symbolicName)) {
                                            logger.error(symbolicName + "升级失败，文件和包名不匹配。");
                                            sendResult(3, symbolicName + " upgrade fail:file name not match.", upgradeFileDTO.getVersion());
                                            resultMessage.append(symbolicName).append(" upgrade fail:file name not match.");
                                            upgradeResult = 32;
                                            continue;
                                        }
                                        bundle.update(Files.newInputStream(Paths.get(newFile.getAbsolutePath())));
                                        logger.info(symbolicName + " 升级成功");
                                        sendResult(3, symbolicName + " upgrade success.", upgradeFileDTO.getVersion());
                                        resultMessage.append(symbolicName).append(" upgrade success.");
                                        //如果是升级组件，升级完结束任务，因为升级组件启动还要再跑一遍计划
                                        if (symbolicName.equals(CommonConstant.UPGRADE_FILE_NAME)) {
                                            break;
                                        }
                                    } else {
                                        logger.error(symbolicName + "升级失败，下载文件失败。");
                                        sendResult(3, symbolicName + " upgrade fail:download file fail.", upgradeFileDTO.getVersion());
                                        resultMessage.append(symbolicName).append(" upgrade fail:download file fail.");
                                        upgradeResult = 32;
                                    }
                                } else {
                                    logger.error(symbolicName + "升级失败，备份文件失败。");
                                    sendResult(3, symbolicName + " upgrade fail:backup file fail.", upgradeFileDTO.getVersion());
                                    resultMessage.append(symbolicName).append(" upgrade fail:backup file fail.");
                                    upgradeResult = 32;
                                }
                            } catch (Exception e) {
                                logger.error(symbolicName + "升级异常，开始执行回退操作，异常信息：" + e.getMessage());
                                sendResult(3, symbolicName + " upgrade fail:" + e.getMessage(), upgradeFileDTO.getVersion());
                                resultMessage.append(symbolicName).append(" upgrade fail:").append(e.getMessage());
                                upgradeResult = 32;
                                //回退版本
                                if (backupFile != null) {
                                    if (backupFile.getAbsolutePath().contains(CommonConstant.DEFAULT_PATH)) {
                                        if (bundle.getState() != Bundle.ACTIVE) {
                                            bundle.update(Files.newInputStream(Paths.get(backupFile.getAbsolutePath())));
                                            bundle.start();
                                        }
                                    } else {
                                        String backFilePath = FileUtil.getBackFilePath(symbolicName);
                                        if (!StringUtil.isEmpty(backFilePath)) {
                                            Files.copy(Paths.get(backFilePath), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                            if (bundle.getState() != Bundle.ACTIVE) {
                                                bundle.update(Files.newInputStream(Paths.get(backupFile.getAbsolutePath())));
                                                bundle.start();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        logger.info(symbolicName + "开始安装。");
                        //新bundle
                        sendResult(1, symbolicName + " start install.", null);
                        File newFile = downloadFile(symbolicName, upgradeFileDTO.getUrl(), upgradeFileDTO.getMd5());
                        if (newFile != null) {
                            logger.info(symbolicName + "文件下载成功。");
                            sendResult(21, symbolicName + " file download success.", upgradeFileDTO.getVersion());
                            if (!newFile.getName().contains(symbolicName)) {
                                logger.error(symbolicName + "升级失败，文件和包名不匹配。");
                                sendResult(3, symbolicName + " upgrade fail:file name not match.", upgradeFileDTO.getVersion());
                                resultMessage.append(symbolicName).append(" upgrade fail:file name not match.");
                                upgradeResult = 32;
                                continue;
                            }
                            Bundle bundle = bundleContext.installBundle("file:" + CommonConstant.BUNDLE_PATH + newFile.getName());
                            bundle.start();
                            logger.info(symbolicName + " 插件安装成功");
                            sendResult(3, symbolicName + " upgrade success.", upgradeFileDTO.getVersion());
                            resultMessage.append(symbolicName).append(" upgrade success.");
                        } else {
                            logger.error(symbolicName + "文件下载失败。");
                            sendResult(3, symbolicName + " upgrade fail:download file fail.", upgradeFileDTO.getVersion());
                            resultMessage.append(symbolicName).append(" upgrade fail:download file fail.");
                            upgradeResult = 32;
                        }
                    }
                }
                if (!StringUtil.isEmpty(resultMessage.toString())) {
                    sendResult(upgradeResult, resultMessage.toString(), null);
                }
            }
        } catch (Exception e) {
            logger.error("升级bundle异常，", e);
            sendResult(32, "upgrade error:" + e.getMessage(), null);
        }
        return false;
    }

    private UpgradeInfoDTO convert(JSONObject wtsResponse) {
        UpgradeInfoDTO upgradeInfoDTO = new UpgradeInfoDTO();
        upgradeInfoDTO.setRules(wtsResponse.getString("rules"));
        upgradeInfoDTO.setStartTime(wtsResponse.getString("startTime"));
        upgradeInfoDTO.setEndTime(wtsResponse.getString("endTime"));
        upgradeInfoDTO.setUpgradeStartTime(wtsResponse.getString("upgradeStartTime"));
        upgradeInfoDTO.setUpgradeEndTime(wtsResponse.getString("upgradeEndTime"));
        JSONArray fileList = wtsResponse.getJSONArray("fileList");
        if (fileList != null) {
            List<UpgradeFileDTO> fileDTOS = new ArrayList<>();
            for (int i = 0; i < fileList.length(); i++) {
                JSONObject file = fileList.getJSONObject(i);
                UpgradeFileDTO upgradeFileDTO = new UpgradeFileDTO();
                upgradeFileDTO.setModel(file.getString("model"));
                upgradeFileDTO.setVersion(file.getString("version"));
                upgradeFileDTO.setMd5(file.getString("md5"));
                upgradeFileDTO.setUrl(file.getString("url"));
                fileDTOS.add(upgradeFileDTO);
            }
            upgradeInfoDTO.setFileList(fileDTOS);
        }
        return upgradeInfoDTO;
    }

    private File downloadFile(String symbolicName, String url, String md5) throws IOException, NoSuchAlgorithmException {
        File newFile = FileUtil.downloadFile(url, CommonConstant.BUNDLE_PATH);
        if (StringUtil.isEmpty(md5)) {
            return newFile;
        }
        if (newFile != null) {
            String fileMd5 = FileUtil.getFileMd5(newFile.getAbsolutePath(), null);
            fileMd5 += CommonConstant.FILE_MD5_SALT;
            String newMd5 = EncryptUtil.getMD5(fileMd5);
            if (md5.equals(newMd5)) {
                return newFile;
            }
            logger.info("下载文件md5值校验失败，原md5：" + md5 + ",校验结果：" + newMd5);
            sendResult(32, symbolicName + " upgrade fail,check file md5 fail.", null);
        }
        return null;
    }

    private void sendResult(Integer status, String message, String version) {
        JSONObject params = new JSONObject();
        params.put("thirdCloudModel", ConfigProperties.productKey);
        params.put("deviceId", ConfigProperties.deviceId);
        params.put("status", status);
        params.put("desc", message);
        WTSRequest<Object> request = new WTSRequest<>().create(params);
        HttpUtil.postRequest(ConfigProperties.otaUrl + "/ota/plug/upgrade/report_upgrade_status",
                new JSONObject(request).toString());
    }
}
