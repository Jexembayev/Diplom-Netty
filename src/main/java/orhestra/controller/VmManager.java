package orhestra.controller;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import orhestra.service.VmHttpService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class VmManager {
    private final VBox vmListBox;
    private final List<TextField> ipFields = new ArrayList<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public VmManager(VBox vmListBox) {
        this.vmListBox = vmListBox;
    }

    public void addVmIpField() {
        TextField ipField = new TextField();
        ipField.setPromptText("Введите IP");

        Button checkButton = new Button("Проверить");
        Label statusLabel = new Label();

        checkButton.setOnAction(e -> checkIp(ipField, statusLabel));

        HBox row = new HBox(10, ipField, checkButton, statusLabel);
        vmListBox.getChildren().add(row);
        ipFields.add(ipField);
    }

    private void checkIp(TextField ipField, Label statusLabel) {
        String ip = ipField.getText().trim();
        if (ip.isEmpty()) {
            statusLabel.setText("❗ Пусто");
            return;
        }
        executor.submit(() -> {
            String result = VmHttpService.pingVM(ip);
            Platform.runLater(() -> statusLabel.setText(result));
        });
    }

    public List<String> getValidIps() {
        return ipFields.stream()
                .map(TextField::getText)
                .map(String::trim)
                .filter(ip -> !ip.isEmpty())
                .collect(Collectors.toList());
    }

    public boolean hasIps() {
        return !getValidIps().isEmpty();
    }
}


