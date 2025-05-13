package orhestra.createVmTab.util;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
    private final Properties props = new Properties();
    private final File file;

    public ConfigManager(String path) {
        this.file = new File(path);
        load();
    }

    private void load() {
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            } catch (IOException e) {
                System.out.println("Не удалось загрузить конфиг: " + e.getMessage());
            }
        }
    }

    public String get(String key) {
        return props.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public void set(String key, String value) {
        props.setProperty(key, value);
    }

    public void save() {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            props.store(fos, "VM Orchestrator config");
        } catch (IOException e) {
            System.out.println("Не удалось сохранить конфиг: " + e.getMessage());
        }
    }
}



