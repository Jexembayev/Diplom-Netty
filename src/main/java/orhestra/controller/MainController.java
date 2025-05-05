package orhestra.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import orhestra.logic.RoundRobinDistributor;
import orhestra.logic.SingleVmDistributor;
import orhestra.logic.TaskDistributor;
import orhestra.model.OptimizationTask;
import orhestra.model.ResultRow;
import orhestra.service.TaskExecutionService;
import orhestra.service.TaskLoader;

import java.io.File;
import java.util.*;

public class MainController {

    @FXML private VBox vmListBox;
    @FXML private Label jarLabel;
    @FXML private Label functionsLabel;
    @FXML private Label paramsLabel;
    @FXML private ChoiceBox<String> modeChoiceBox;
    @FXML private TextArea outputArea;
    @FXML private HBox vmBlocksContainer;

    //private final VmManager vmManager = new VmManager(vmListBox);
    private final JarUploader jarUploader = new JarUploader();
    private final TaskLoader taskLoader = new TaskLoader();
    private final Map<String, TaskDistributor> distributorMap = new LinkedHashMap<>();
    private final Map<String, GridPane> vmGrids = new HashMap<>();
    private final Map<String, Double> vmTimeTotals = new HashMap<>();
    private final List<ResultRow> allResults = new ArrayList<>();
    private final Map<String, Label> timeLabels = new HashMap<>();


    private File selectedJar;
    private File selectedFunctionsJson;
    private File selectedParamsJson;

    private FileSelector fileSelector;
    private VmManager vmManager;

    @FXML
    public void initialize() {
        vmManager = new VmManager(vmListBox);
        distributorMap.put("Одна ВМ (по очереди)", new SingleVmDistributor());
        distributorMap.put("Распараллеливание (Round Robin)", new RoundRobinDistributor());

        modeChoiceBox.getItems().addAll(distributorMap.keySet());
        modeChoiceBox.setValue("Одна ВМ (по очереди)");

        vmManager.addVmIpField();
    }

    @FXML
    public void addVmIpField() {
        vmManager.addVmIpField();
    }

    @FXML
    public void selectJar() {
        selectedJar = getFileSelector().chooseJar();
        updateFileLabel(jarLabel, selectedJar);
    }

    @FXML
    public void selectFunctionsJson() {
        selectedFunctionsJson = getFileSelector().chooseJson("Выберите JSON с функциями");
        updateFileLabel(functionsLabel, selectedFunctionsJson);
    }

    @FXML
    public void selectParamsJson() {
        selectedParamsJson = getFileSelector().chooseJson("Выберите JSON с параметрами");
        updateFileLabel(paramsLabel, selectedParamsJson);
    }

    private FileSelector getFileSelector() {
        if (fileSelector == null) {
            fileSelector = new FileSelector(jarLabel.getScene().getWindow());
        }
        return fileSelector;
    }

    @FXML
    public void uploadJarToAllVMs() {
        vmBlocksContainer.getChildren().clear();
        jarUploader.uploadToAll(vmManager.getValidIps(), selectedJar, outputArea, ip ->
                Platform.runLater(() -> createVmBlock(ip))
        );
    }

    @FXML
    public void runTasks() {
        if (!validateInputs()) return;

        try {
            List<OptimizationTask> tasks = taskLoader.generateTasks(selectedFunctionsJson, selectedParamsJson);
            outputArea.appendText("📦 Всего задач: " + tasks.size() + "\n");

            TaskDistributor distributor = distributorMap.get(modeChoiceBox.getValue());

            prepareForTaskRun();

            distributor.distribute(
                    tasks,
                    vmManager.getValidIps(),
                    jarUploader.getUploadedJarPaths(),
                    outputArea,
                    vmGrids,
                    vmTimeTotals,
                    timeLabels,
                    allResults,
                    vmBlocksContainer,
                    this::showSummaryResults);

        } catch (Exception e) {
            outputArea.appendText("❌ Ошибка: " + e.getMessage() + "\n");
        }
    }

    private void prepareForTaskRun() {
        vmTimeTotals.clear();
        allResults.clear();
    }

    private boolean validateInputs() {
        if (selectedJar == null || selectedFunctionsJson == null || selectedParamsJson == null) {
            outputArea.appendText("❗ Выберите JAR, функции и параметры.\n");
            return false;
        }
        if (vmManager.getValidIps().isEmpty()) {
            outputArea.appendText("❗ Введите хотя бы один IP.\n");
            return false;
        }
        return true;
    }

    private void showSummaryResults() {
        outputArea.appendText("\n✅ Все задачи завершены.\n\n📊 Сводка результатов:\n");
        allResults.forEach(row -> outputArea.appendText(row + "\n"));

        outputArea.appendText("\n⏱ Общее время по ВМ:\n");
        vmTimeTotals.forEach((ip, time) ->
                outputArea.appendText(ip + ": " + String.format("%.2f сек", time) + "\n"));
    }

    private void createVmBlock(String ip) {
        VBox block = new VBox(5);
        Label header = new Label("ВМ: " + ip);
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.addRow(0,
                new Label("Задача"),
                new Label("Dim"),
                new Label("Agent"),
                new Label("Iter"),
                new Label("Sec"),
                new Label("Status")
        );

        Label timeLabel = new Label("Общее время: 0.00 сек");
        timeLabel.setId("timeLabel_" + ip);

        // Сохраняем ссылку на timeLabel в Map
        timeLabels.put(ip, timeLabel);

        block.getChildren().addAll(header, grid, timeLabel);

        vmGrids.put(ip, grid);
        vmBlocksContainer.getChildren().add(block);
    }


    private void updateFileLabel(Label label, File file) {
        label.setText(file != null ? file.getName() : "Файл не выбран");
    }
}


