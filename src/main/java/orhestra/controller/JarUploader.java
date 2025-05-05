package orhestra.controller;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import orhestra.service.VmHttpService;

import java.io.File;
import java.util.Map;
import java.util.concurrent.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class JarUploader {
    private final Map<String, String> uploadedJarPaths = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public void uploadToAll(List<String> ips, File selectedJar, TextArea outputArea, Consumer<String> onSuccess) {
        if (selectedJar == null) {
            outputArea.appendText("❗ Сначала выберите JAR-файл.\n");
            return;
        }

        uploadedJarPaths.clear();

        for (String ip : ips) {
            executorService.submit(() -> {
                try {
                    String path = VmHttpService.uploadJarToVm(ip, selectedJar);
                    uploadedJarPaths.put(ip, path);

                    Platform.runLater(() -> {
                        outputArea.appendText("✅ Загружен JAR на " + ip + "\n");
                        onSuccess.accept(ip);
                    });

                } catch (Exception e) {
                    Platform.runLater(() ->
                            outputArea.appendText("❌ Не удалось загрузить JAR на " + ip + ": " + e.getMessage() + "\n"));
                }
            });
        }
    }

    public Map<String, String> getUploadedJarPaths() {
        return uploadedJarPaths;
    }
}


