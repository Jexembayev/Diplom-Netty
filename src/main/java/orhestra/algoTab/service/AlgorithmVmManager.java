package orhestra.algoTab.service;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlgorithmVmManager {
    private final VBox vmListBox;
    private final ObservableList<String> vmIps = FXCollections.observableArrayList();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public AlgorithmVmManager(VBox vmListBox) {
        this.vmListBox = vmListBox;
    }

    public void setIps(List<String> ips) {
        vmIps.setAll(ips);
        updateVmListBox();
    }


    public ObservableList<String> getIps() {
        return vmIps;
    }

    public List<String> getValidIps() {
        return vmIps.stream().toList();
    }

    private void updateVmListBox() {
        Platform.runLater(() -> {
            vmListBox.getChildren().clear();
            for (String ip : vmIps) {
                TextField ipField = new TextField(ip);
                ipField.setEditable(false);

                Button checkButton = new Button("Проверить");
                Label statusLabel = new Label();

                checkButton.setOnAction(e -> checkIp(ipField, statusLabel));

                HBox row = new HBox(10, ipField, checkButton, statusLabel);
                vmListBox.getChildren().add(row);
            }
        });
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
}
