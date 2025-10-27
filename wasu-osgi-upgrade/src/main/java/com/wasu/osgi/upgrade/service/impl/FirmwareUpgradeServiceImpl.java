package com.wasu.osgi.upgrade.service.impl;

import com.cw.smartgateway.accessservices.HardwareUpdate;
import com.wasu.osgi.upgrade.Activator;
import com.wasu.osgi.upgrade.config.CommonConstant;
import com.wasu.osgi.upgrade.dto.UpgradeFileDTO;
import com.wasu.osgi.upgrade.dto.UpgradeInfoDTO;
import com.wasu.osgi.upgrade.service.IFirmwareUpgradeService;
import com.wasu.osgi.upgrade.util.FileUtil;
import com.wasu.osgi.upgrade.util.StringUtil;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author: glmx_
 * @date: 2025/10/13
 * @description: 固件升级服务实现类
 */
public class FirmwareUpgradeServiceImpl implements IFirmwareUpgradeService {

    private static final Logger logger = Logger.getLogger(FirmwareUpgradeServiceImpl.class);

    @Override
    public boolean upOTA(JSONObject params) {
        try {
            // 构造符合MQTT要求的参数格式
            JSONObject mqttParams = new JSONObject();

            // 如果传入的参数包含upgrades字段，则直接使用
            if (params.has("upgrades")) {
                mqttParams.put("upgrades", params.getJSONArray("upgrades"));
            }

            // 构造currents字段，基于传入的upgrades数据
            JSONArray currents = new JSONArray();
            if (params.has("upgrades")) {
                JSONArray upgrades = params.getJSONArray("upgrades");
                for (int i = 0; i < upgrades.length(); i++) {
                    JSONObject upgrade = upgrades.getJSONObject(i);
                    JSONObject current = new JSONObject();
                    current.put("component", upgrade.getString("component"));
                    current.put("version", upgrade.getString("version"));
                    currents.put(current);
                }

                // 根据规范，当upgrades中包含main组件时，需额外在currents中添加component为wifi、version为1.1.1的条目
                boolean hasMainComponent = false;
                for (int i = 0; i < upgrades.length(); i++) {
                    JSONObject upgrade = upgrades.getJSONObject(i);
                    if ("main".equals(upgrade.getString("component"))) {
                        hasMainComponent = true;
                        break;
                    }
                }

                if (hasMainComponent) {
                    JSONObject wifiComponent = new JSONObject();
                    wifiComponent.put("component", "wifi");
                    wifiComponent.put("version", "1.1.1");
                    currents.put(wifiComponent);
                }
            }

            mqttParams.put("currents", currents);

            // 通过MQTT发送OTA请求
            if (sendMqttMessage(mqttParams, "get")) {
                return true;
            }

            logger.warn("无法通过MQTT发送OTA请求");
        } catch (Exception e) {
            logger.error("处理upOTA请求异常", e);
        }
        return false;
    }

    @Override
    public boolean checkFirmwareUpgrade(JSONObject params) {
        try {
            // 修改参数处理逻辑，因为现在传入的params已经是data对象
            if (params != null && params.has("upgrades")) {
                JSONArray upgradesArray = params.getJSONArray("upgrades");

                // 创建UpgradeInfoDTO对象
                UpgradeInfoDTO upgradeInfoDTO = new UpgradeInfoDTO();
                List<UpgradeFileDTO> fileList = new ArrayList<>();

                // 遍历upgrades数组，转换为UpgradeFileDTO列表
                for (int i = 0; i < upgradesArray.length(); i++) {
                    JSONObject upgrade = upgradesArray.getJSONObject(i);
                    UpgradeFileDTO upgradeFileDTO = new UpgradeFileDTO();

                    if (upgrade.has("component")) {
                        upgradeFileDTO.setModel(upgrade.getString("component"));
                    }

                    if (upgrade.has("version")) {
                        upgradeFileDTO.setVersion(upgrade.getString("version"));
                    }

                    if (upgrade.has("file")) {
                        upgradeFileDTO.setUrl(upgrade.getString("file"));
                    }

                    fileList.add(upgradeFileDTO);
                }

                upgradeInfoDTO.setFileList(fileList);

                // 处理固件升级（main或C插件）
                for (UpgradeFileDTO upgradeFileDTO : fileList) {
                    String model = upgradeFileDTO.getModel();

                    // 如果是main或C插件类型，则调用固件升级服务
                    if (CommonConstant.UPGRADE_TYPE_MAIN.equals(model) || CommonConstant.UPGRADE_TYPE_C_PLUGIN.equals(model)) {
                        return startFirmwareUpgrade(upgradeFileDTO);
                    }
                    // 如果是Java插件类型，则处理Java插件升级
                    else if (CommonConstant.UPGRADE_TYPE_JAVA_PLUGIN.equals(model)) {
                        return handleJavaPluginUpgrade(upgradeFileDTO);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("检查固件升级异常", e);
            sendResult(9, "firmware", "unknown", 0);
        }
        return false;
    }

    /**
     * 启动固件升级
     *
     * @param upgradeFileDTO 升级文件信息
     * @return 是否成功启动升级
     */
    private boolean startFirmwareUpgrade(UpgradeFileDTO upgradeFileDTO) {
        try {
            // 获取硬件升级服务
            BundleContext bundleContext = FrameworkUtil.getBundle(Activator.class).getBundleContext();
            ServiceReference<HardwareUpdate> serviceReference = bundleContext.getServiceReference(HardwareUpdate.class);
            if (serviceReference == null) {
                logger.error("无法获取HardwareUpdate服务");
                sendResult(9, upgradeFileDTO.getModel(), null, 0);
                return false;
            }

            HardwareUpdate HardwareUpdate = bundleContext.getService(serviceReference);
            if (HardwareUpdate == null) {
                logger.error("HardwareUpdate实例为空");
                sendResult(9, upgradeFileDTO.getModel(), null, 0);
                return false;
            }

            String packageUrl = upgradeFileDTO.getUrl();
            String upgradeVersion = upgradeFileDTO.getVersion();
            String currentVersion = "unknown"; // 可根据需要获取当前版本

            logger.info("开始固件升级，类型：" + upgradeFileDTO.getModel() + "，URL：" + packageUrl + "，版本：" + upgradeVersion);

            // 调用固件升级接口
            int result = HardwareUpdate.startHardwareUpdate(
                    packageUrl,
                    upgradeFileDTO.getModel(),
                    upgradeVersion,
                    currentVersion
            );

            if (result == 0) {
                logger.info("固件升级启动成功，开始轮询升级结果");
                // 启动定时任务查询升级结果
                scheduleUpgradeResultCheck(HardwareUpdate, upgradeFileDTO.getModel(), upgradeVersion);
                return true;
            } else {
                logger.error("固件升级启动失败，返回码：" + result);
                sendResult(9, upgradeFileDTO.getModel(), upgradeVersion, 0);
                return false;
            }
        } catch (Exception e) {
            logger.error("启动固件升级异常", e);
            sendResult(9, upgradeFileDTO.getModel(), upgradeFileDTO.getVersion(), 0);
            return false;
        }
    }

    /**
     * 处理Java插件升级
     *
     * @param upgradeFileDTO 升级文件信息
     * @return 是否成功启动升级
     */
    private boolean handleJavaPluginUpgrade(UpgradeFileDTO upgradeFileDTO) {
        String packageUrl = upgradeFileDTO.getUrl();
        String upgradeVersion = upgradeFileDTO.getVersion();
        String pluginModel = upgradeFileDTO.getModel();

        try {
            logger.info("开始Java插件升级，URL：" + packageUrl + "，版本：" + upgradeVersion);
            // 下载zip包
            File zipFile = FileUtil.downloadFile(packageUrl, CommonConstant.BUNDLE_PATH);
            if (zipFile == null) {
                logger.error("Java插件下载失败");
                sendResult(9, pluginModel, upgradeVersion, 0);
                return false;
            }

            // 解压zip包
            String extractPath = CommonConstant.BUNDLE_PATH + "temp_extract/";
            if (!extractZipFile(zipFile.getAbsolutePath(), extractPath)) {
                logger.error("Java插件解压失败");
                sendResult(9, pluginModel, upgradeVersion, 0);
                return false;
            }

            logger.info("Java插件解压成功：" + extractPath);

            // 从ZIP中读取rules.txt文件
            File rulesFile = new File(extractPath + "rules.txt");
            List<String> rulesList;
            if (rulesFile.exists()) {
                // 读取rules.txt文件内容
                rulesList = Files.readAllLines(rulesFile.toPath());
                logger.info("从rules.txt读取到规则: " + rulesList);
            } else {
                // 查找解压目录中的jar文件
                File extractDir = new File(extractPath);
                File[] files = extractDir.listFiles((dir, name) -> name.endsWith(".jar"));
                if (files == null || files.length == 0) {
                    logger.error("解压目录中未找到jar文件: " + extractPath);
                    sendResult(9, pluginModel, upgradeVersion, 0);
                    return false;
                }

                // 从jar文件名推断symbolic name作为规则
                rulesList = new ArrayList<>();
                for (File file : files) {
                    String fileName = file.getName();
                    int lastDashIndex = fileName.lastIndexOf("-");
                    if (lastDashIndex > 0) {
                        rulesList.add(fileName.substring(0, lastDashIndex));
                    } else {
                        // 如果没有版本号，去掉.jar后缀
                        rulesList.add(fileName.substring(0, fileName.length() - 4));
                    }
                }
                logger.info("从jar文件名推断出规则: " + rulesList);
            }

            // 确保UPGRADE_FILE_NAME在最后执行（如果存在的话）
            if (rulesList.remove(CommonConstant.UPGRADE_FILE_NAME)) { // 先移除（如果存在）
                rulesList.add(CommonConstant.UPGRADE_FILE_NAME);     // 再添加到末尾
            }

            // 获取BundleContext
            BundleContext bundleContext = FrameworkUtil.getBundle(Activator.class).getBundleContext();
            Bundle[] bundles = bundleContext.getBundles();

            // 根据rules.txt中的规则进行升级
            boolean success = true;
            Map<String, File> backupFiles = new HashMap<>();
            for (String symbolicName : rulesList) {
                try {
                    logger.info("处理规则: " + symbolicName);

                    // 查找匹配的bundle
                    Bundle targetBundle = null;
                    Optional<Bundle> first = Arrays.stream(bundles).filter(s -> symbolicName.equals(s.getSymbolicName())).findFirst();
                    //更新
                    if (first.isPresent()) {
                        targetBundle = first.get();

                        // 查找对应的jar文件
                        File jarFile = findJarFileBySymbolicName(symbolicName, extractPath);
                        if (jarFile == null) {
                            logger.error("未找到与规则匹配的jar文件: " + symbolicName);
                            sendResult(9, pluginModel, upgradeVersion, 0);
                            success = false;
                            continue;
                        }

                        // 从jar文件名中提取版本号进行比较
                        String jarVersion = extractVersionFromJarName(jarFile.getName());
                        String currentVersion = targetBundle.getVersion().toString();
                        if (!StringUtil.isEmpty(jarVersion) && jarVersion.compareTo(currentVersion) <= 0) {
                            logger.info("Java插件 " + symbolicName + " 当前版本(" + currentVersion + ")已大于或等于目标版本(" + jarVersion + ")，无需升级");
                            continue;
                        }

                        // 在更新前备份当前运行的JAR文件
                        File backupFile = FileUtil.getBackupFile(symbolicName);
                        boolean backResult = FileUtil.backupFile(backupFile, symbolicName);
                        if (!backResult) {
                            logger.error("Java插件备份失败: " + symbolicName);
                            sendResult(9, pluginModel, upgradeVersion, 0);
                            success = false;
                            continue;
                        }

                        try {
                            // 执行升级
                            logger.info("使用jar文件进行升级: " + jarFile.getAbsolutePath());
                            targetBundle.update(Files.newInputStream(Paths.get(jarFile.getAbsolutePath())));
                            logger.info("Java插件升级完成: " + symbolicName + ", 文件: " + jarFile.getName());
                            sendResult(1, pluginModel, upgradeVersion, 100);

                            // 如果是升级组件，升级完结束任务，因为升级组件启动还要再跑一遍计划
                            if (symbolicName.equals(CommonConstant.UPGRADE_FILE_NAME)) {
                                break;
                            }
                        } catch (Exception e) {
                            logger.error("升级Java插件文件时发生异常: " + symbolicName, e);
                            success = false;
                            sendResult(9, pluginModel, upgradeVersion, 0);

                            // 出现异常时直接回退
                            try {
                                if (backupFile.getAbsolutePath().contains(CommonConstant.DEFAULT_PATH)) {
                                    if (targetBundle.getState() != Bundle.ACTIVE) {
                                        targetBundle.update(Files.newInputStream(Paths.get(backupFile.getAbsolutePath())));
                                        targetBundle.start();
                                    }
                                } else {
                                    String backFilePath = FileUtil.getBackFilePath(symbolicName);
                                    if (!StringUtil.isEmpty(backFilePath)) {
                                        Files.copy(Paths.get(backFilePath), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                        if (targetBundle.getState() != Bundle.ACTIVE) {
                                            targetBundle.update(Files.newInputStream(Paths.get(backupFile.getAbsolutePath())));
                                            targetBundle.start();
                                        }
                                    }
                                }
                                logger.info("Java插件回退完成: " + symbolicName);
                            } catch (Exception rollbackException) {
                                logger.error("Java插件回退失败: " + symbolicName, rollbackException);
                            }

                            break; // 一旦出错立即停止处理其他文件
                        }
                    } else {
                        // 检查是否提供了版本信息
                        if (!StringUtil.isEmpty(upgradeVersion)) {
                            logger.info("准备安装新的Java插件: " + symbolicName + "，版本: " + upgradeVersion);
                        } else {
                            logger.info("准备安装新的Java插件: " + symbolicName);
                        }

                        // 查找对应的jar文件
                        File jarFile = findJarFileBySymbolicName(symbolicName, extractPath);
                        if (jarFile == null) {
                            logger.error("未找到与规则匹配的jar文件: " + symbolicName);
                            sendResult(9, pluginModel, upgradeVersion, 0);
                            success = false;
                            continue;
                        }

                        // 从jar文件名中提取版本号
                        String jarVersion = extractVersionFromJarName(jarFile.getName());
                        if (!StringUtil.isEmpty(jarVersion)) {
                            logger.info("准备安装新的Java插件: " + symbolicName + "，版本: " + jarVersion);
                        } else {
                            logger.info("准备安装新的Java插件: " + symbolicName);
                        }

                        logger.info(symbolicName + "开始安装。");
                        //新bundle
                        sendResult(1, pluginModel, null, 10);
                        Bundle bundle = bundleContext.installBundle("file:" + jarFile.getAbsolutePath());
                        bundle.start();
                        logger.info(symbolicName + " 插件安装成功");
                        sendResult(1, pluginModel, upgradeVersion, 100);

                    }
                } catch (Exception e) {
                    logger.error("升级Java插件文件时发生异常: " + symbolicName, e);
                    success = false;
                    // 尝试回退已升级的bundle
                    rollbackBundles(backupFiles);
                    break; // 一旦出错立即停止处理其他文件
                }
            }

            return success;
        } catch (Exception e) {
            logger.error("处理Java插件升级异常", e);
            sendResult(9, pluginModel, upgradeVersion, 0);
            return false;
        }
    }

    /**
     * 从jar文件名中提取版本号
     *
     * @param jarFileName jar文件名
     * @return 版本号
     */
    private String extractVersionFromJarName(String jarFileName) {
        if (jarFileName.endsWith(".jar")) {
            // 去掉.jar后缀
            String nameWithoutExtension = jarFileName.substring(0, jarFileName.length() - 4);
            // 提取版本号（最后一个"-"之后的部分）
            int lastDashIndex = nameWithoutExtension.lastIndexOf("-");
            if (lastDashIndex > 0 && lastDashIndex < nameWithoutExtension.length() - 1) {
                return nameWithoutExtension.substring(lastDashIndex + 1);
            }
        }
        return null;
    }

    /**
     * 根据symbolicName查找对应的jar文件
     *
     * @param symbolicName Bundle的symbolic name
     * @param extractPath  解压路径
     * @return 对应的jar文件，未找到返回null
     */
    private File findJarFileBySymbolicName(String symbolicName, String extractPath) {
        File extractDir = new File(extractPath);
        File[] files = extractDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (files == null || files.length == 0) {
            return null;
        }

        // 精确匹配
        for (File file : files) {
            String fileName = file.getName();
            // 去掉.jar后缀
            String nameWithoutExtension = fileName.substring(0, fileName.length() - 4);
            // 去掉版本号（最后一个"-"之后的部分）
            int lastDashIndex = nameWithoutExtension.lastIndexOf("-");
            String baseName = lastDashIndex > 0 ? nameWithoutExtension.substring(0, lastDashIndex) : nameWithoutExtension;

            if (symbolicName.equals(baseName)) {
                return file;
            }
        }

        return null;
    }

    /**
     * 回退多个Bundle到备份版本
     *
     * @param backupFiles 备份文件映射
     */
    private void rollbackBundles(Map<String, File> backupFiles) {
        try {
            BundleContext bundleContext = FrameworkUtil.getBundle(Activator.class).getBundleContext();
            Bundle[] bundles = bundleContext.getBundles();

            for (Map.Entry<String, File> entry : backupFiles.entrySet()) {
                String symbolicName = entry.getKey();
                File backupFile = entry.getValue();

                // 查找匹配的bundle
                Bundle targetBundle = null;
                for (Bundle bundle : bundles) {
                    if (symbolicName.equals(bundle.getSymbolicName())) {
                        targetBundle = bundle;
                        break;
                    }
                }

                if (targetBundle != null && backupFile != null) {
                    try {
                        String bundleFileName = new File(targetBundle.getLocation().substring(5)).getName();
                        if (backupFile.getAbsolutePath().contains(CommonConstant.DEFAULT_PATH)) {
                            if (targetBundle.getState() != Bundle.ACTIVE) {
                                targetBundle.update(Files.newInputStream(Paths.get(backupFile.getAbsolutePath())));
                                targetBundle.start();
                            }
                        } else {
                            String backFilePath = FileUtil.getBackFilePath(bundleFileName);
                            if (!StringUtil.isEmpty(backFilePath)) {
                                Files.copy(Paths.get(backFilePath), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                if (targetBundle.getState() != Bundle.ACTIVE) {
                                    targetBundle.update(Files.newInputStream(Paths.get(backupFile.getAbsolutePath())));
                                    targetBundle.start();
                                }
                            }
                        }
                        logger.info("Java插件回退完成: " + symbolicName);
                    } catch (Exception e) {
                        logger.error("Java插件回退失败: " + symbolicName, e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Java插件批量回退操作异常", e);
        }
    }

    /**
     * 解压 zip 文件（自动跳过顶层文件夹）
     *
     * @param zipFilePath zip 文件路径
     * @param extractPath 解压目标路径
     * @return 是否解压成功
     */
    private boolean extractZipFile(String zipFilePath, String extractPath) {
        try {
            File destDir = new File(extractPath);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            // ---- 第一步：判断是否存在统一根文件夹 ----
            String rootFolderName = null;
            Set<String> topDirs = new HashSet<>();
            try (ZipInputStream checkStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
                ZipEntry entry = checkStream.getNextEntry();
                while (entry != null) {
                    String name = entry.getName();
                    if (name.contains("/")) {
                        topDirs.add(name.substring(0, name.indexOf('/') + 1));
                    } else {
                        // 有文件直接在根目录，说明没有统一的顶层目录
                        topDirs.clear();
                        break;
                    }
                    entry = checkStream.getNextEntry();
                }
            }
            if (topDirs.size() == 1) {
                rootFolderName = topDirs.iterator().next();
            }

            // ---- 第二步：正式解压 ----
            byte[] buffer = new byte[1024];
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
                ZipEntry zipEntry = zis.getNextEntry();
                while (zipEntry != null) {
                    String entryName = zipEntry.getName();

                    // 如果有统一根目录，就去掉那一层路径
                    if (rootFolderName != null && entryName.startsWith(rootFolderName)) {
                        entryName = entryName.substring(rootFolderName.length());
                    }

                    if (entryName.isEmpty()) {
                        zipEntry = zis.getNextEntry();
                        continue;
                    }

                    File destFile = new File(destDir, entryName);
                    String destDirPath = destDir.getCanonicalPath();
                    String destFilePath = destFile.getCanonicalPath();

                    // 安全检查：防止路径穿越攻击
                    if (!destFilePath.startsWith(destDirPath + File.separator)) {
                        throw new IOException("条目在目标目录之外: " + entryName);
                    }

                    if (zipEntry.isDirectory()) {
                        destFile.mkdirs();
                    } else {
                        File parent = destFile.getParentFile();
                        if (!parent.isDirectory()) {
                            parent.mkdirs();
                        }

                        try (FileOutputStream fos = new FileOutputStream(destFile)) {
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                    }

                    zipEntry = zis.getNextEntry();
                }
            }

            return true;
        } catch (IOException e) {
            logger.error("解压 zip 文件异常", e);
            return false;
        }
    }

    /**
     * 定时查询升级结果并上报
     *
     * @param HardwareUpdate 硬件升级服务
     * @param upgradeType    升级类型
     * @param upgradeVersion 升级版本
     */
    private void scheduleUpgradeResultCheck(HardwareUpdate HardwareUpdate, String upgradeType, String upgradeVersion) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                // getHardwareResult返回值说明：
                // JSON_Data:String
                // upgradeStatus：0-正在升级中 1-升级结束且成功 9-升级失败
                // component：升级组件
                // upgradeVersion:升级版本号
                // per:升级进度
                // upgradeResult:升级结果
                String result = HardwareUpdate.getHardwareResult();
                if (!StringUtil.isEmpty(result)) {
                    JSONObject resultJson = new JSONObject(result);
                    logger.info("固件升级结果：" + resultJson.toString());

                    // 检查升级状态
                    if (resultJson.has("upgradeStatus")) {
                        int upgradeStatus = resultJson.getInt("upgradeStatus");
                        switch (upgradeStatus) {
                            case 0:
                                // 正在升级中，继续轮询
                                logger.info("固件升级进行中...");
                                // 获取进度信息
                                int progress = 0;
                                if (resultJson.has("per")) {
                                    progress = resultJson.getInt("per");
                                }
                                sendResult(0, upgradeType, upgradeVersion, progress);
                                break;
                            case 1:
                                logger.info("固件升级成功...");
                                // 升级结束且成功
                                sendResult(1, upgradeType, upgradeVersion, 100);
                                scheduler.shutdown();
                                break;
                            case 9:
                                // 升级失败
                                StringBuilder errorMsg = new StringBuilder("固件升级失败");
                                if (resultJson.has("upgradeResult")) {
                                    errorMsg.append(": ").append(resultJson.getString("upgradeResult"));
                                } else {
                                    errorMsg.append("，未知错误");
                                }
                                logger.error(errorMsg.toString());
                                sendResult(9, upgradeType, upgradeVersion, 0);
                                scheduler.shutdown();
                                break;
                            default:
                                logger.error("未知的升级状态码: " + upgradeStatus);
                                break;
                        }
                    } else {
                        logger.warn("返回结果中不包含upgradeStatus字段");
                    }
                }
            } catch (Exception e) {
                logger.error("查询固件升级结果异常", e);
                sendResult(9, upgradeType, upgradeVersion, 0);
                scheduler.shutdown();
            }
        }, 10, 30, TimeUnit.SECONDS); // 延迟10秒后执行，每30秒查询一次
    }

    private UpgradeInfoDTO convert(JSONObject wtsResponse) {
        UpgradeInfoDTO upgradeInfoDTO = new UpgradeInfoDTO();
        if (wtsResponse.has("rules")) {
            upgradeInfoDTO.setRules(wtsResponse.getString("rules"));
        }
        if (wtsResponse.has("startTime")) {
            upgradeInfoDTO.setStartTime(wtsResponse.getString("startTime"));
        }
        if (wtsResponse.has("endTime")) {
            upgradeInfoDTO.setEndTime(wtsResponse.getString("endTime"));
        }
        if (wtsResponse.has("upgradeStartTime")) {
            upgradeInfoDTO.setUpgradeStartTime(wtsResponse.getString("upgradeStartTime"));
        }
        if (wtsResponse.has("upgradeEndTime")) {
            upgradeInfoDTO.setUpgradeEndTime(wtsResponse.getString("upgradeEndTime"));
        }

        if (wtsResponse.has("fileList")) {
            JSONArray fileList = wtsResponse.getJSONArray("fileList");
            if (fileList != null) {
                List<UpgradeFileDTO> fileDTOS = new ArrayList<>();
                for (int i = 0; i < fileList.length(); i++) {
                    JSONObject file = fileList.getJSONObject(i);
                    UpgradeFileDTO upgradeFileDTO = new UpgradeFileDTO();
                    if (file.has("model")) {
                        upgradeFileDTO.setModel(file.getString("model"));
                    }
                    if (file.has("version")) {
                        upgradeFileDTO.setVersion(file.getString("version"));
                    }
                    if (file.has("md5")) {
                        upgradeFileDTO.setMd5(file.getString("md5"));
                    }
                    if (file.has("url")) {
                        upgradeFileDTO.setUrl(file.getString("url"));
                    }
                    fileDTOS.add(upgradeFileDTO);
                }
                upgradeInfoDTO.setFileList(fileDTOS);
            }
        }
        return upgradeInfoDTO;
    }

    private void sendResult(Integer status, String component, String version, Integer per) {
        try {
            // 构造MQTT消息参数，符合OTA升级状态上报格式
            JSONObject params = new JSONObject();
            params.put("status", status != null ? status : 0);
            params.put("component", component != null ? component : "main");
            if (version != null) {
                params.put("version", version);
            } else {
                params.put("version", "unknown");
            }
            params.put("per", per != null ? per : 0);

            // 通过MQTT发送升级结果
            if (sendMqttMessage(params, "per")) {
                return;
            }
        } catch (Exception e) {
            logger.error("发送升级结果异常", e);
        }
    }

    /**
     * 通过MQTT发送消息的通用方法
     *
     * @param params    消息参数
     * @param attribute 消息属性
     * @return 是否发送成功
     */
    private boolean sendMqttMessage(Object params, String attribute) {
        try {
            // 使用upgrade模块自己的MQTT客户端
            com.wasu.osgi.upgrade.util.MqttClient mqttClient = com.wasu.osgi.upgrade.util.MqttClient.getInstance();
            mqttClient.upOTA(params, attribute);
            return true;
        } catch (Exception e) {
            logger.error("通过MQTT发送升级结果失败", e);
            return false;
        }
    }

    @Override
    public boolean checkFirmwareUpgrade1(JSONObject params) {
        try {
            UpgradeFileDTO upgradeFileDTO = new UpgradeFileDTO();
            upgradeFileDTO.setUrl("https://example.com/firmware.zip");
            upgradeFileDTO.setVersion("1.1.0");
            upgradeFileDTO.setModel(CommonConstant.UPGRADE_TYPE_JAVA_PLUGIN);
            return handleJavaPluginUpgrade1(upgradeFileDTO);
        } catch (Exception e) {
            logger.error("检查固件升级异常", e);
            sendResult(9, CommonConstant.UPGRADE_TYPE_JAVA_PLUGIN, "1.0.0", 0);
        }
        return false;
    }

    /**
     * 处理Java插件升级
     *
     * @param upgradeFileDTO 升级文件信息
     * @return 是否成功启动升级
     */
    private boolean handleJavaPluginUpgrade1(UpgradeFileDTO upgradeFileDTO) {
        String packageUrl = upgradeFileDTO.getUrl();
        String upgradeVersion = upgradeFileDTO.getVersion();
        String pluginModel = upgradeFileDTO.getModel();

        try {
            logger.info("1开始Java插件升级，URL：" + packageUrl + "，版本：" + upgradeVersion);
            // 下载zip包
            /*File zipFile = FileUtil.downloadFile(packageUrl, CommonConstant.BUNDLE_PATH);
            if (zipFile == null) {
                logger.error("Java插件下载失败");
                sendResult(9, pluginModel, upgradeVersion, 0);
                return false;
            }*/

            // 解压zip包
            String extractPath = CommonConstant.BUNDLE_PATH + "temp_extract/";
            if (!extractZipFile(CommonConstant.BUNDLE_PATH + "tftp.zip", extractPath)) {
                logger.error("1Java插件解压失败");
                sendResult(9, pluginModel, upgradeVersion, 0);
                return false;
            }

            logger.info("1Java插件解压成功：" + extractPath);

            // 从ZIP中读取rules.txt文件
            File rulesFile = new File(extractPath + "rules.txt");
            List<String> rulesList;
            if (rulesFile.exists()) {
                // 读取rules.txt文件内容
                rulesList = Files.readAllLines(rulesFile.toPath());
                logger.info("从rules.txt读取到规则: " + rulesList);
            } else {
                // 查找解压目录中的jar文件
                File extractDir = new File(extractPath);
                File[] files = extractDir.listFiles((dir, name) -> name.endsWith(".jar"));
                if (files == null || files.length == 0) {
                    logger.error("解压目录中未找到jar文件: " + extractPath);
                    sendResult(9, pluginModel, upgradeVersion, 0);
                    return false;
                }

                // 从jar文件名推断symbolic name作为规则
                rulesList = new ArrayList<>();
                for (File file : files) {
                    String fileName = file.getName();
                    int lastDashIndex = fileName.lastIndexOf("-");
                    if (lastDashIndex > 0) {
                        rulesList.add(fileName.substring(0, lastDashIndex));
                    } else {
                        // 如果没有版本号，去掉.jar后缀
                        rulesList.add(fileName.substring(0, fileName.length() - 4));
                    }
                }
                logger.info("从jar文件名推断出规则: " + rulesList);
            }

            // 确保UPGRADE_FILE_NAME在最后执行（如果存在的话）
            if (rulesList.remove(CommonConstant.UPGRADE_FILE_NAME)) { // 先移除（如果存在）
                rulesList.add(CommonConstant.UPGRADE_FILE_NAME);     // 再添加到末尾
            }

            // 获取BundleContext
            BundleContext bundleContext = FrameworkUtil.getBundle(Activator.class).getBundleContext();
            Bundle[] bundles = bundleContext.getBundles();

            // 根据rules.txt中的规则进行升级
            boolean success = true;
            Map<String, File> backupFiles = new HashMap<>();
            for (String symbolicName : rulesList) {
                try {
                    logger.info("处理规则: " + symbolicName);

                    // 查找匹配的bundle
                    Bundle targetBundle = null;
                    Optional<Bundle> first = Arrays.stream(bundles).filter(s -> symbolicName.equals(s.getSymbolicName())).findFirst();
                    //更新
                    if (first.isPresent()) {
                        targetBundle = first.get();

                        // 查找对应的jar文件
                        File jarFile = findJarFileBySymbolicName(symbolicName, extractPath);
                        if (jarFile == null) {
                            logger.error("未找到与规则匹配的jar文件: " + symbolicName);
                            sendResult(9, pluginModel, upgradeVersion, 0);
                            success = false;
                            continue;
                        }

                        // 从jar文件名中提取版本号进行比较
                        String jarVersion = extractVersionFromJarName(jarFile.getName());
                        String currentVersion = targetBundle.getVersion().toString();
                        if (!StringUtil.isEmpty(jarVersion) && jarVersion.compareTo(currentVersion) <= 0) {
                            logger.info("Java插件 " + symbolicName + " 当前版本(" + currentVersion + ")已大于或等于目标版本(" + jarVersion + ")，无需升级");
                            continue;
                        }

                        // 在更新前备份当前运行的JAR文件
                        File backupFile = FileUtil.getBackupFile(symbolicName);
                        boolean backResult = FileUtil.backupFile(backupFile, symbolicName);
                        if (!backResult) {
                            logger.error("Java插件备份失败: " + symbolicName);
                            sendResult(9, pluginModel, upgradeVersion, 0);
                            success = false;
                            continue;
                        }

                        try {
                            // 执行升级
                            logger.info("使用jar文件进行升级: " + jarFile.getAbsolutePath());
                            targetBundle.update(Files.newInputStream(Paths.get(jarFile.getAbsolutePath())));
                            logger.info("Java插件升级完成: " + symbolicName + ", 文件: " + jarFile.getName());
                            sendResult(1, pluginModel, upgradeVersion, 100);

                            // 如果是升级组件，升级完结束任务，因为升级组件启动还要再跑一遍计划
                            if (symbolicName.equals(CommonConstant.UPGRADE_FILE_NAME)) {
                                break;
                            }
                        } catch (Exception e) {
                            logger.error("升级Java插件文件时发生异常: " + symbolicName, e);
                            success = false;
                            sendResult(9, pluginModel, upgradeVersion, 0);

                            // 出现异常时直接回退
                            try {
                                if (backupFile.getAbsolutePath().contains(CommonConstant.DEFAULT_PATH)) {
                                    if (targetBundle.getState() != Bundle.ACTIVE) {
                                        targetBundle.update(Files.newInputStream(Paths.get(backupFile.getAbsolutePath())));
                                        targetBundle.start();
                                    }
                                } else {
                                    String backFilePath = FileUtil.getBackFilePath(symbolicName);
                                    if (!StringUtil.isEmpty(backFilePath)) {
                                        Files.copy(Paths.get(backFilePath), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                        if (targetBundle.getState() != Bundle.ACTIVE) {
                                            targetBundle.update(Files.newInputStream(Paths.get(backupFile.getAbsolutePath())));
                                            targetBundle.start();
                                        }
                                    }
                                }
                                logger.info("Java插件回退完成: " + symbolicName);
                            } catch (Exception rollbackException) {
                                logger.error("Java插件回退失败: " + symbolicName, rollbackException);
                            }

                            break; // 一旦出错立即停止处理其他文件
                        }
                    } else {
                        // 检查是否提供了版本信息
                        if (!StringUtil.isEmpty(upgradeVersion)) {
                            logger.info("准备安装新的Java插件: " + symbolicName + "，版本: " + upgradeVersion);
                        } else {
                            logger.info("准备安装新的Java插件: " + symbolicName);
                        }

                        // 查找对应的jar文件
                        File jarFile = findJarFileBySymbolicName(symbolicName, extractPath);
                        if (jarFile == null) {
                            logger.error("未找到与规则匹配的jar文件: " + symbolicName);
                            sendResult(9, pluginModel, upgradeVersion, 0);
                            success = false;
                            continue;
                        }

                        // 从jar文件名中提取版本号
                        String jarVersion = extractVersionFromJarName(jarFile.getName());
                        if (!StringUtil.isEmpty(jarVersion)) {
                            logger.info("准备安装新的Java插件: " + symbolicName + "，版本: " + jarVersion);
                        } else {
                            logger.info("准备安装新的Java插件: " + symbolicName);
                        }

                        logger.info(symbolicName + "开始安装。");
                        //新bundle
                        sendResult(1, pluginModel, null, 10);
                        Bundle bundle = bundleContext.installBundle("file:" + jarFile.getAbsolutePath());
                        bundle.start();
                        logger.info(symbolicName + " 插件安装成功");
                        sendResult(1, pluginModel, upgradeVersion, 100);

                    }
                } catch (Exception e) {
                    logger.error("升级Java插件文件时发生异常: " + symbolicName, e);
                    success = false;
                    // 尝试回退已升级的bundle
                    rollbackBundles(backupFiles);
                    break; // 一旦出错立即停止处理其他文件
                }
            }

            return success;
        } catch (Exception e) {
            logger.error("处理Java插件升级异常", e);
            sendResult(9, pluginModel, upgradeVersion, 0);
            return false;
        }
    }

}
