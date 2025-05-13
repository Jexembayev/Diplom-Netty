package orhestra.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import orhestra.algoTab.service.AlgorithmVmManager;

import java.io.IOException;

public class MainController {

    @FXML private TabPane tabPane;

    @FXML
    public void initialize() {
        try {
            // Загружаем algorithm_tab.fxml
            FXMLLoader algoLoader = new FXMLLoader(getClass().getResource("/orhestra/algorithm_tab.fxml"));
            Parent algoRoot = algoLoader.load();
            AlgorithmTabController algorithmTabController = algoLoader.getController();

            // Загружаем vm_creation_tab.fxml
            FXMLLoader createLoader = new FXMLLoader(getClass().getResource("/orhestra/vm_creation_tab.fxml"));
            Parent vmCreationRoot = createLoader.load();
            CreateVmTabController createVmTabController = createLoader.getController();

            // Создаем общий AlgorithmVmManager и передаем его в оба контроллера
            AlgorithmVmManager vmManager = new AlgorithmVmManager(algorithmTabController.getVmListBox());
            createVmTabController.setVmManager(vmManager);
            algorithmTabController.setVmManager(vmManager);

            // Устанавливаем контенты во вкладки
            Tab createTab = tabPane.getTabs().get(0);
            createTab.setContent(vmCreationRoot);

            Tab algoTab = tabPane.getTabs().get(1);
            algoTab.setContent(algoRoot);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}








