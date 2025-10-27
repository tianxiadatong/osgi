package com.wasu.osgi.upgrade.util;

import com.wasu.osgi.upgrade.config.CommonConstant;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author: glmx_
 * @date: 2024/8/26
 * @description:
 */
public class FileUtil {

    private static final Logger logger = Logger.getLogger(FileUtil.class);

    public static File getDefaultBundleFile(String fileName) {
        File directory = new File(CommonConstant.DEFAULT_PATH);
        File[] files = directory.listFiles();
        if (files != null) {
            Optional<File> first = Arrays.stream(files).filter(s ->
                    s.getAbsolutePath().contains(fileName)).findFirst();
            if (first.isPresent()) {
                return first.get();
            }
        }
        return null;
    }

    public static void restoreFile(File targetFile) throws IOException {
        Path backupPath = new File(CommonConstant.BUNDLE_BACK_PATH + targetFile.getName()).toPath();
        Path targetPath = targetFile.toPath();
        Files.copy(backupPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Copied backup file from " + backupPath + " to " + targetPath);
    }

    public static File downloadFile(String downloadUrl, String saveDir) {
        try {
            logger.info("download file: " + downloadUrl);
            File directory = new File(saveDir);
            if (!directory.exists()) {
                Files.createDirectories(directory.toPath());
            }
            URL url = new URL(downloadUrl);
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
            String filePath = saveDir + File.separator + fileName;

            try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                 FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }
            logger.info("File downloaded to: " + filePath);
            return new File(filePath);
        } catch (Exception e) {
            logger.error("下载文件异常，", e);
        }
        return null;
    }

    public static boolean backupFile(File currentFile, String fileName) {
        if (currentFile == null || !currentFile.exists()) {
            return true;
        }
        //删除备份目录的历史文件
        deleteBackupFile(fileName);
        String targetPath = CommonConstant.BUNDLE_BACK_PATH;
        try {
            File backupDirectory = new File(targetPath);
            if (!backupDirectory.exists()) {
                Files.createDirectories(Paths.get(targetPath));
            }
            File backupFile = new File(backupDirectory, currentFile.getName());
            logger.info(currentFile.getAbsoluteFile() + " file backed up to: " + backupFile.getAbsolutePath());
            Files.copy(currentFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            if (currentFile.getAbsolutePath().contains(CommonConstant.BUNDLE_PATH)) {
                currentFile.delete();
            }
            return true;
        } catch (Exception e) {
            logger.error("备份文件异常，", e);
            return false;
        }
    }

    private static void deleteBackupFile(String fileName) {
        File directory = new File(CommonConstant.BUNDLE_BACK_PATH);
        File[] files = directory.listFiles();
        if (files != null && files.length > 0) {
            List<File> collect = Arrays.stream(files).filter(s ->
                    s.getAbsolutePath().contains(fileName)).collect(Collectors.toList());
            collect.forEach(File::delete);
        }
    }

    public static File getBackupFile(String fileName) {
        String filePath = CommonConstant.BUNDLE_PATH;
        File directory = new File(filePath);
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            filePath = CommonConstant.DEFAULT_PATH;
            directory = new File(filePath);
            files = directory.listFiles();
        }
        if (files != null && files.length > 0) {
            Optional<File> first = Arrays.stream(files).filter(s ->
                    s.getAbsolutePath().contains(fileName)).findFirst();
            if (first.isPresent()) {
                String fileFullPath = first.map(File::getAbsolutePath).orElse(null);
                logger.info("获取到的文件路径：" + fileFullPath);
                return first.get();
            }
        }
        return null;
    }

    public static String getBackFilePath(String fileName) {
        String filePath = CommonConstant.BUNDLE_BACK_PATH;
        File directory = new File(filePath);
        File[] files = directory.listFiles();
        if (files != null && files.length > 0) {
            Optional<File> first = Arrays.stream(files).filter(s ->
                    s.getAbsolutePath().contains(fileName)).findFirst();
            if (first.isPresent()) {
                return first.map(File::getAbsolutePath).orElse(null);
            }
        }
        return null;
    }

    public static boolean checkFileMd5(String filePath, String expectedMd5, String salt) {
        if (StringUtil.isEmpty(filePath) || StringUtil.isEmpty(expectedMd5)) {
            return false;
        }
        try {
            String calculatedMd5 = getFileMd5(filePath, salt);
            logger.info("Calculated MD5 with salt: " + calculatedMd5);
            return expectedMd5.equals(calculatedMd5);
        } catch (Exception e) {
            logger.error("文件md5校验异常，", e);
        }
        return false;
    }

    public static String getFileMd5(String filePath, String salt) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        md.update(bytes);
        if (!StringUtil.isEmpty(salt)) {
            md.update(salt.getBytes(StandardCharsets.UTF_8));
        }
        byte[] digest = md.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        String hashText = bigInt.toString(16);
        while (hashText.length() < 32) {
            hashText = "0" + hashText;
        }
        return hashText;
    }
}
