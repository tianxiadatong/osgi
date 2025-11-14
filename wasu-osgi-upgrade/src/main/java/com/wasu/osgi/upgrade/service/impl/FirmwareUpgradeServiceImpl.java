package com.wasu.osgi.upgrade.service.impl;

import com.cw.smartgateway.accessservices.HardwareUpdate;
import com.wasu.osgi.upgrade.Activator;
import com.wasu.osgi.upgrade.config.CommonConstant;
import com.wasu.osgi.upgrade.dto.UpgradeFileDTO;
import com.wasu.osgi.upgrade.dto.UpgradeInfoDTO;
import com.wasu.osgi.upgrade.service.IFirmwareUpgradeService;
import com.wasu.osgi.upgrade.service.IMqttService;
import com.wasu.osgi.upgrade.util.FileUtil;
import com.wasu.osgi.upgrade.util.StringUtil;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.FrameworkWiring;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
                    current.put("version", "1.0.0");
                    currents.put(current);
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
            logger.info("收到升级检查请求，参数: " + params.toString());

            // 从 data 里取升级计划
            JSONArray dataUpgrades = null;
            if (params.has("data")) {
                JSONObject dataObj = params.getJSONObject("data");
                dataUpgrades = dataObj.optJSONArray("upgrades");
                // 判断 data.upgrades 是否为空
                if (dataUpgrades == null || dataUpgrades.length() == 0) {
                    logger.info("没有需要升级的组件，结束本次升级检查");
                    //发送失败消息
                    sendResult(params, "没有需要升级的组件，结束本次升级检查");
                    return false;
                }

                // 创建UpgradeInfoDTO对象
                UpgradeInfoDTO upgradeInfoDTO = new UpgradeInfoDTO();
                List<UpgradeFileDTO> fileList = new ArrayList<>();

                // 遍历upgrades数组，转换为UpgradeFileDTO列表
                for (int i = 0; i < dataUpgrades.length(); i++) {
                    JSONObject upgrade = dataUpgrades.getJSONObject(i);
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
                        logger.info("开始处理 " + model + " 类型固件升级");
                        return startFirmwareUpgrade(upgradeFileDTO);
                    }
                    // 如果是Java插件类型，则处理Java插件升级
                    else if (CommonConstant.UPGRADE_TYPE_JAVA_PLUGIN.equals(model)) {
                        logger.info("开始处理Java插件升级");
                        return handleJavaPluginUpgrade(upgradeFileDTO);
                    }
                }
            } else {
                logger.info("收到的参数中不包含upgrades字段或参数为空");
                //发送失败消息
                sendResult(params, "收到的参数中不包含upgrades字段或参数为空");
            }
        } catch (Exception e) {
            logger.error("检查固件升级异常", e);
            sendResult(9, "firmware", "unknown", 0, null);
        }
        return false;
    }

    private void sendResult(JSONObject params, String detail) {
        String component = "firmware";
        String version = "unknown";

        // 尝试从 params.upgrades 提取 component 和 version
        if (params.has("params")) {
            JSONObject paramsObj = params.getJSONObject("params");
            JSONArray upgrades = paramsObj.optJSONArray("upgrades");
            if (upgrades != null && upgrades.length() > 0) {
                JSONObject upgradeObj = upgrades.getJSONObject(0);
                component = upgradeObj.optString("component", "firmware");
                version = upgradeObj.optString("version", "unknown");
            }
        }
        // 调用 sendResult 并传入提取的值
        sendResult(9, component, version, 0, detail);
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
            BundleContext bundleContext = FrameworkUtil.getBundle(com.wasu.osgi.upgrade.Activator.class).getBundleContext();
            ServiceReference<HardwareUpdate> serviceReference = bundleContext.getServiceReference(HardwareUpdate.class);
            if (serviceReference == null) {
                logger.error("无法获取HardwareUpdate服务");
                sendResult(9, upgradeFileDTO.getModel(), upgradeFileDTO.getVersion(), 0, "无法获取HardwareUpdate服务");
                return false;
            }

            HardwareUpdate HardwareUpdate = bundleContext.getService(serviceReference);
            if (HardwareUpdate == null) {
                logger.error("HardwareUpdate实例为空");
                sendResult(9, upgradeFileDTO.getModel(), upgradeFileDTO.getVersion(), 0, "HardwareUpdate实例为空");
                return false;
            }

            String packageUrl = upgradeFileDTO.getUrl();
            String upgradeVersion = upgradeFileDTO.getVersion();

            logger.info("开始固件升级，类型：" + upgradeFileDTO.getModel() + "，URL：" + packageUrl + "，版本：" + upgradeVersion);

            // 调用固件升级接口
            int result = HardwareUpdate.startHardwareUpdate(
                    packageUrl,
                    upgradeFileDTO.getModel(),
                    upgradeVersion
            );

            if (result == 0) {
                logger.info("固件升级启动成功，开始轮询升级结果");
                // 启动定时任务查询升级结果
                scheduleUpgradeResultCheck(HardwareUpdate, upgradeFileDTO.getModel(), upgradeVersion);
                return true;
            } else {
                String errorMsg = "固件升级启动失败，返回码：" + result;
                logger.error(errorMsg);
                sendResult(9, upgradeFileDTO.getModel(), upgradeVersion, 0, errorMsg);
                return false;
            }
        } catch (Exception e) {
            String errorMsg = "启动固件升级异常: " + e.getMessage();
            logger.error(errorMsg, e);
            sendResult(9, upgradeFileDTO.getModel(), upgradeFileDTO.getVersion(), 0, errorMsg);
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
                sendResult(9, pluginModel, upgradeVersion, 0, "Java插件下载失败");
                return false;
            }

            // 解压zip包
            String extractPath = CommonConstant.BUNDLE_PATH + "temp_extract/";
            if (!extractZipFile(zipFile.getAbsolutePath(), extractPath)) {
                logger.error("Java插件解压失败");
                sendResult(9, pluginModel, upgradeVersion, 0, "Java插件解压失败");
                return false;
            }

            logger.info("Java插件解压成功：" + extractPath);

            // 将版本号写入到文件中，供后续读取使用
            writeVersionToFile(extractPath, upgradeVersion);

            // 复用处理规则的逻辑
            return processUpgradeRules(upgradeFileDTO);

        } catch (Exception e) {
            logger.error("处理Java插件升级异常", e);
            sendResult(9, pluginModel, upgradeVersion, 0, "处理Java插件升级异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 将版本号写入到文件中
     *
     * @param extractPath 解压路径
     * @param version     版本号
     */
    private void writeVersionToFile(String extractPath, String version) {
        try {
            File versionFile = new File(extractPath + "version.txt");
            try (java.io.FileWriter writer = new java.io.FileWriter(versionFile)) {
                writer.write(version);
            }
            logger.info("版本号已写入到文件: " + versionFile.getAbsolutePath());
        } catch (Exception e) {
            logger.error("写入版本号文件失败", e);
        }
    }

    /**
     * 检查并安装待处理的插件
     * 在升级com.wasu.osgi.bundle.update自身时，确保其他插件已正确安装
     * 此方法由Activator调用，在Bundle重启后检查并安装之前解压但未安装的插件
     */
    @Override
    public void checkAndInstallPendingPlugins() {
        try {
            String extractPath = CommonConstant.BUNDLE_PATH + "temp_extract/";

            // 读取版本号（如果存在）
            String version = readVersionFromFile(extractPath);
            UpgradeFileDTO upgradeFileDTO = new UpgradeFileDTO();
            upgradeFileDTO.setModel(CommonConstant.UPGRADE_TYPE_MAIN);
            upgradeFileDTO.setVersion(version);
            // 复用处理规则的逻辑
            processUpgradeRules(upgradeFileDTO);

        } catch (Exception e) {
            logger.error("检查并安装待处理插件时发生异常", e);
            sendResult(9, CommonConstant.UPGRADE_TYPE_MAIN, null, 0, "检查并安装待处理插件时发生异常: " + e.getMessage());
        }
    }

    /**
     * 从文件中读取版本号
     *
     * @param extractPath 解压路径
     * @return 版本号，如果读取失败或文件不存在则返回null
     */
    private String readVersionFromFile(String extractPath) {
        try {
            File versionFile = new File(extractPath + "version.txt");
            if (versionFile.exists()) {
                return new String(Files.readAllBytes(versionFile.toPath())).trim();
            }
        } catch (Exception e) {
            logger.error("读取版本号文件失败", e);
        }
        return null;
    }

    /**
     * 处理升级规则
     *
     * @param upgradeFileDTO 升级文件信息，如果为null表示仅安装插件
     * @return 是否处理成功
     */
    private boolean processUpgradeRules(UpgradeFileDTO upgradeFileDTO) {
        try {
            String extractPath = CommonConstant.BUNDLE_PATH + "temp_extract/";
            File rulesFile = new File(extractPath + "rules.txt");
            List<String> rulesList = new ArrayList<>();
            if (rulesFile.exists()) {
                // 读取rules.txt文件内容
                List<String> lines = Files.readAllLines(rulesFile.toPath());
                if (!lines.isEmpty()) {
                    // 规则文件只有一行，按逗号分割成规则集合
                    String[] rulesArray = lines.get(0).split(",");
                    for (String rule : rulesArray) {
                        // 去除每个规则前后的空格
                        rule = rule.trim();
                        if (!rule.isEmpty()) {
                            rulesList.add(rule);
                        }
                    }
                    logger.info("从rules.txt读取到规则: " + rulesList);

                } else {
                    logger.info("rules.txt文件内容为空，无升级规则");
                    sendResult(9, upgradeFileDTO.getModel(), upgradeFileDTO.getVersion(), 0, "rules.txt文件内容为空，无升级规则");

                    return false;
                }
            } else {

                logger.info("rules.txt文件不存在，无升级规则");
                sendResult(9, upgradeFileDTO.getModel(), upgradeFileDTO.getVersion(), 0, "rules.txt文件不存在，无升级规则");

                return false;
            }

            // 获取BundleContext
            BundleContext bundleContext = FrameworkUtil.getBundle(Activator.class).getBundleContext();
            Bundle[] bundles = bundleContext.getBundles();

            // 根据rules.txt中的规则进行处理
            boolean success = true;
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

                            sendResult(9, upgradeFileDTO.getModel(), upgradeFileDTO.getVersion(), 0, "未找到与规则匹配的jar文件: " + symbolicName);

                            success = false;
                            continue;
                        }

                        // 如果仅安装插件，则跳过版本检查和升级流程
                        if (upgradeFileDTO == null) {
                            logger.info("插件 " + symbolicName + " 已存在，跳过安装");
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

                            sendResult(9, upgradeFileDTO.getModel(), upgradeFileDTO.getVersion(), 0, "Java插件备份失败: " + symbolicName);

                            success = false;
                            continue;
                        }

                        try {

                            // 执行升级
                            logger.info("使用jar文件进行升级: " + jarFile.getAbsolutePath());
                            targetBundle.update(Files.newInputStream(Paths.get(jarFile.getAbsolutePath())));
                            logger.info("Java插件升级完成: " + symbolicName + ", 文件: " + jarFile.getName());
                            // 如果是升级组件，升级完结束任务，因为升级组件启动还要再跑一遍计划
                            if (symbolicName.equals(CommonConstant.UPGRADE_FILE_NAME)) {
                                break;
                            } else {
                                sendResult(1, upgradeFileDTO.getModel(), upgradeFileDTO.getVersion(), 100, "model:success,upgrade:success");
                                // 清理临时解压目录
                                cleanupTempExtractDirectory();
                            }
                            // 如果是model组件， wiring refreshed
                            if (symbolicName.equals(CommonConstant.MODEL_FILE_NAME)) {
                                refreshFramework();
                            }
                        } catch (Exception e) {
                            logger.error("升级Java插件文件时发生异常: " + symbolicName, e);
                            success = false;

                            sendResult(9, upgradeFileDTO.getModel(), upgradeFileDTO.getVersion(), 0, "升级Java插件文件时发生异常: " + symbolicName + ", 错误: " + e.getMessage());

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
                        if (!StringUtil.isEmpty(upgradeFileDTO.getVersion())) {
                            logger.info("准备安装新的Java插件: " + symbolicName + "，版本: " + upgradeFileDTO.getVersion());
                        } else {
                            logger.info("准备安装新的Java插件: " + symbolicName);
                        }

                        // 查找对应的jar文件
                        File jarFile = findJarFileBySymbolicName(symbolicName, extractPath);
                        if (jarFile == null) {
                            logger.error("未找到与规则匹配的jar文件: " + symbolicName);

                            sendResult(9, upgradeFileDTO.getModel(), upgradeFileDTO.getVersion(), 0, "未找到与规则匹配的jar文件: " + symbolicName);

                            success = false;
                            continue;
                        }

                        logger.info(symbolicName + "开始安装。");
                        //新bundle
                        Bundle bundle = bundleContext.installBundle("file:" + jarFile.getAbsolutePath());
                        bundle.start();
                        logger.info(symbolicName + " 插件安装成功");

                        sendResult(1, upgradeFileDTO.getModel(), upgradeFileDTO.getVersion(), 100, symbolicName + " upgrade success." + upgradeFileDTO.getVersion());

                    }
                } catch (Exception e) {
                    logger.error("处理Java插件时发生异常: " + symbolicName, e);
                    success = false;
                    break; // 一旦出错立即停止处理其他文件

                }
            }

            return success;
        } catch (Exception e) {
            logger.error("处理升级规则时发生异常", e);
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
                                sendResult(0, upgradeType, upgradeVersion, progress, null);
                                break;
                            case 1:
                                logger.info("固件升级成功...");
                                // 升级结束且成功
                                sendResult(1, upgradeType, upgradeVersion, 100, null);
                                scheduler.shutdown();
                                break;
                            case 9:
                                // 升级失败
                                logger.error("固件升级失败...");
                                sendResult(9, upgradeType, upgradeVersion, 0, null);
                                scheduler.shutdown();
                                break;
                            default:
                                logger.error("未知的升级状态码: " + upgradeStatus);
                                scheduler.shutdown();
                                break;
                        }
                    } else {
                        logger.warn("返回结果中不包含upgradeStatus字段");
                    }
                }
            } catch (Exception e) {
                logger.error("查询固件升级结果异常", e);
                sendResult(9, upgradeType, upgradeVersion, 0, "查询固件升级结果异常: " + e.getMessage());
                scheduler.shutdown();
            }
        }, 10, 30, TimeUnit.SECONDS); // 延迟10秒后执行，每30秒查询一次
    }

    private void sendResult(Integer status, String component, String version, Integer per, String detail) {
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
            params.put("detail", detail);

            // 通过MQTT发送升级结果
            sendMqttMessage(params, "per");

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
            //logger.info("准备通过MQTT发送消息，参数: " + params + "，属性: " + attribute);
            IMqttService mqttService = UpgradeServiceTracker.getService();
            int retryCount = 0;
            while (mqttService == null && retryCount < 10) {
                // logger.info("第 " + (retryCount + 1) + " 次尝试获取 IMqttService 服务失败，等待 10 秒后重试...");
                Thread.sleep(10000);
                mqttService = UpgradeServiceTracker.getService();
                retryCount++;
            }

            if (mqttService != null) {
                //logger.info("成功获取到 IMqttService 实例：" + mqttService);
                mqttService.upOTA(params, attribute);
                //logger.info("MQTT消息发送成功，参数: " + params + "，属性: " + attribute);
                return true;
            } else {
                logger.error("无法获取 IMqttService 服务");
            }

            return false;
        } catch (Exception e) {
            logger.error("通过MQTT发送升级结果失败，参数: " + params + "，属性: " + attribute, e);
            return false;
        }
    }

    /**
     * 在Activator启动时检查固件升级结果
     * 当固件升级导致程序重启后，主动获取升级结果并上报
     */
    @Override
    public void checkFirmwareUpgradeResultOnStartup() {
        try {
            // 获取硬件升级服务
            BundleContext bundleContext = FrameworkUtil.getBundle(Activator.class).getBundleContext();
            ServiceReference<HardwareUpdate> serviceReference = bundleContext.getServiceReference(HardwareUpdate.class);
            if (serviceReference == null) {
                logger.warn("无法获取HardwareUpdate服务");
                return;
            }

            HardwareUpdate hardwareUpdate = bundleContext.getService(serviceReference);
            if (hardwareUpdate == null) {
                logger.warn("HardwareUpdate实例为空");
                return;
            }

            String result = hardwareUpdate.getHardwareResult();
            if (!StringUtil.isEmpty(result)) {
                JSONObject resultJson = new JSONObject(result);
                logger.info("固件升级结果：" + resultJson.toString());

                // 检查升级状态
                if (resultJson.has("upgradeStatus")) {
                    int upgradeStatus = resultJson.getInt("upgradeStatus");
                    String component = resultJson.optString("component", "unknown");
                    String upgradeVersion = resultJson.optString("upgradeVersion", "unknown");
                    int progress = resultJson.optInt("per", 0);

                    switch (upgradeStatus) {
                        case 0:
                            sendResult(0, component, upgradeVersion, progress, null);
                            break;
                        case 1:
                            sendResult(1, component, upgradeVersion, 100, null);
                            break;
                        case 9:
                            sendResult(9, component, upgradeVersion, 0, null);
                            break;
                        default:
                            logger.error("目前无升级固件: " + upgradeStatus);
                            break;
                    }
                } else {
                    logger.warn("返回结果中不包含upgradeStatus字段");
                }
            }

        } catch (Exception e) {
            logger.error("检查固件升级结果时发生异常", e);
        }
    }

    public void refreshFramework() {
        try {
            /*BundleContext ctx = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
            FrameworkWiring fw = ctx.getBundle(0).adapt(FrameworkWiring.class);
            fw.refreshBundles(null);
            System.out.println("OSGi framework wiring refreshed successfully!");*/
            BundleContext ctx = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
            Bundle[] bundles = ctx.getBundles();

            List<Bundle> bundlesToRefresh = new ArrayList<>();
            for (Bundle b : bundles) {
                String bundleSymbolicName = b.getSymbolicName();
                if (CommonConstant.MODEL_FILE_NAME.equals(bundleSymbolicName)
                        || CommonConstant.UPGRADE_FILE_NAME.equals(bundleSymbolicName)) {
                    bundlesToRefresh.add(b);
                }
            }

            if (!bundlesToRefresh.isEmpty()) {
                FrameworkWiring fw = ctx.getBundle(0).adapt(FrameworkWiring.class);
                fw.refreshBundles(bundlesToRefresh);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清理临时解压目录
     */
    private void cleanupTempExtractDirectory() {
        try {
            String extractPath = CommonConstant.BUNDLE_PATH + "temp_extract/";
            File extractDir = new File(extractPath);
            if (extractDir.exists()) {
                deleteDirectory(extractDir);
                logger.info("临时解压目录已清理: " + extractPath);
            }
        } catch (Exception e) {
            logger.error("清理临时解压目录时发生异常", e);
        }
    }

    /**
     * 递归删除目录及其内容
     *
     * @param directory 要删除的目录
     * @return 是否删除成功
     */
    private boolean deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!deleteDirectory(file)) {
                        return false;
                    }
                }
            }
        }
        return directory.delete();
    }
}
