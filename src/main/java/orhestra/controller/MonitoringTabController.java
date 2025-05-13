package orhestra.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MonitoringTabController {

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        statusLabel.setText("Мониторинг ещё не реализован.");
    }
}

