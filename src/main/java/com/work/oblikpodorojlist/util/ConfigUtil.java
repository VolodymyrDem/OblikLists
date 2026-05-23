package com.work.oblikpodorojlist.util;

import java.io.*;
import java.util.Properties;

public class ConfigUtil {
    private static final String CONFIG_FILE = "config.properties";

    public static String loadIpAddress() {
        Properties properties = new Properties();
        File file = new File(CONFIG_FILE);

        if (!file.exists()) {
            saveIpAddress("localhost");
        }

        try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
            return properties.getProperty("ip_address", "localhost");
        } catch (IOException e) {
            e.printStackTrace();
            return "localhost";
        }
    }

    public static String loadBackupPath() {
        Properties properties = new Properties();
        File file = new File(CONFIG_FILE);

        if (!file.exists()) {
            saveBackupPath("C:\\backups\\mysql");
        }

        try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
            return properties.getProperty("backupAddress", "C:\\backups\\mysql");
        } catch (IOException e) {
            e.printStackTrace();
            return "localhost";
        }
    }

    public static void saveIpAddress(String ipAddress) {
        Properties properties = new Properties();
        properties.setProperty("ip_address", ipAddress);

        try (FileOutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, "Configuration File");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveBackupPath(String address) {
        Properties properties = new Properties();
        properties.setProperty("backupAddress", address);

        try (FileOutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, "Configuration File");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
