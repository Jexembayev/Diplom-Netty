package orhestra.controller;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import orhestra.algoTab.logic.RoundRobinDistributor;
import orhestra.algoTab.logic.SingleVmDistributor;
import orhestra.algoTab.logic.TaskDistributor;
import orhestra.algoTab.model.OptimizationTask;
import orhestra.algoTab.model.ResultRow;
import orhestra.algoTab.service.AlgorithmVmManager;
import orhestra.algoTab.service.FileSelector;
import orhestra.algoTab.service.JarUploader;
import orhestra.algoTab.service.TaskLoader;

import java.io.File;
import java.util.*;

public class AlgorithmTabController {

    @FXML private VBox vmListBox;
    @FXML private Label jarLabel;
    @FXML private Label functionsLabel;
    @FXML private Label paramsLabel;
    @FXML private ChoiceBox<String> modeChoiceBox;
    @FXML private TextArea outputArea;
    @FXML private HBox vmBlocksContainer;

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
    private AlgorithmVmManager vmManager;

    @FXML
    public void initialize() {
        distributorMap.put("Одна ВМ (по очереди)", new SingleVmDistributor());
        distributorMap.put("Распараллеливание (Round Robin)", new RoundRobinDistributor());

        modeChoiceBox.getItems().addAll(distributorMap.keySet());
        modeChoiceBox.setValue("Одна ВМ (по очереди)");
    }

    private void updateIpFields() {
        vmListBox.getChildren().clear();
        for (String ip : vmManager.getIps()) {
            HBox ipBox = new HBox(5);
            ipBox.setStyle("-fx-padding: 5; -fx-border-color: lightgray; -fx-border-radius: 5;");

            TextField ipField = new TextField(ip);
            ipField.setEditable(false);
            Button checkBtn = new Button("Проверить");

            checkBtn.setOnAction(e -> outputArea.appendText("🔍 Проверка IP: " + ip + "\n"));

            ipBox.getChildren().addAll(ipField, checkBtn);
            vmListBox.getChildren().add(ipBox);
        }
    }

    private FileSelector getFileSelector() {
        if (fileSelector == null) {
            var scene = jarLabel.getScene();
            if (scene == null || scene.getWindow() == null) {
                outputArea.appendText("⚠ Невозможно открыть диалог выбора файла: окно ещё не загружено.\n");
                return null;
            }
            fileSelector = new FileSelector(scene.getWindow());
        }
        return fileSelector;
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
            outputArea.appendText("📆 Всего задач: " + tasks.size() + "\n");

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

        outputArea.appendText("\n🏆 Лучшие результаты по функциям (по времени):\n");

        Map<String, ResultRow> bestByFunction = new HashMap<>();

        for (ResultRow row : allResults) {
            String function = row.function();
            double time = parseTime(row.fitness());

            if (!bestByFunction.containsKey(function) ||
                    time < parseTime(bestByFunction.get(function).fitness())) {
                bestByFunction.put(function, row);
            }
        }

        bestByFunction.values().forEach(row -> outputArea.appendText("🔹 " + row + "\n"));
    }

    private double parseTime(String fitness) {
        try {
            return Double.parseDouble(fitness);
        } catch (NumberFormatException e) {
            return Double.MAX_VALUE;
        }
    }

    private Label createHeaderLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold;");
        return label;
    }


    private void createVmBlock(String ip) {
        VBox block = new VBox(5);
        block.setStyle("-fx-background-color: white; -fx-border-color: lightgray; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        vmBlocksContainer.setSpacing(20);

        Label header = new Label("ВМ: " + ip);
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.addRow(0,
                createHeaderLabel("Task"),
                createHeaderLabel("Dim"),
                createHeaderLabel("Agent"),
                createHeaderLabel("Iter"),
                createHeaderLabel("Sec"),
                createHeaderLabel("Fitness"),
                createHeaderLabel("Status")
        );

        Label timeLabel = new Label("Total time: 0.00 сек");
        timeLabel.setId("timeLabel_" + ip);

        timeLabels.put(ip, timeLabel);
        block.getChildren().addAll(header, grid, timeLabel);

        vmGrids.put(ip, grid);
        vmBlocksContainer.getChildren().add(block);
    }

    private void updateFileLabel(Label label, File file) {
        label.setText(file != null ? file.getName() : "Файл не выбран");
    }

    public void setVmManager(AlgorithmVmManager vmManager) {
        this.vmManager = vmManager;
        vmManager.getIps().addListener((ListChangeListener<String>) change -> updateIpFields());
        updateIpFields();
    }

    public VBox getVmListBox() {
        return vmListBox;
    }
}





