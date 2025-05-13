package orhestra.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import orhestra.algoTab.service.AlgorithmVmManager;
import orhestra.createVmTab.manager.CloudVmManager;
import orhestra.createVmTab.service.AuthService;
import orhestra.createVmTab.service.VmCreationService;
import orhestra.createVmTab.util.SshExecutor;
import orhestra.createVmTab.util.VmInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CreateVmTabController {

    @FXML private TableView<VmInfo> vmTable;
    @FXML private VBox vmListBox;

    @FXML private TableColumn<VmInfo, String> nameColumn;
    @FXML private TableColumn<VmInfo, String> statusColumn;
    @FXML private TableColumn<VmInfo, String> ipColumn;
    @FXML private TableColumn<VmInfo, String> imageColumn;
    @FXML private TableColumn<VmInfo, String> configColumn;
    @FXML private TableColumn<VmInfo, Number> coresColumn;
    @FXML private TableColumn<VmInfo, Number> memoryColumn;
    @FXML private TableColumn<VmInfo, Number> diskColumn;
    @FXML private TableColumn<VmInfo, String> javaColumn;
    @FXML private TableColumn<VmInfo, String> serverColumn;
    @FXML private TableColumn<VmInfo, Void> actionColumn;
    @FXML private TableColumn<VmInfo, Boolean> useColumn;

    @FXML private Button sendIpsButton;
    @FXML private Button loadIniButton;
    @FXML private Button loadVmButton;
    @FXML private TextArea logArea;

    private final ObservableList<VmInfo> vmData = FXCollections.observableArrayList();
    private final AuthService authService = new AuthService();
    private AlgorithmVmManager vmManager;
    private final List<String> selectedVmIps = new ArrayList<>();

    @FXML
    public void initialize() {
        loadIniButton.setOnAction(e -> loadIniFile());
        loadVmButton.setOnAction(e -> loadExistingVms());

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        ipColumn.setCellValueFactory(new PropertyValueFactory<>("ip"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("imageId"));
        configColumn.setCellValueFactory(new PropertyValueFactory<>("configuration"));
        coresColumn.setCellValueFactory(new PropertyValueFactory<>("cores"));
        memoryColumn.setCellValueFactory(new PropertyValueFactory<>("memoryGb"));
        diskColumn.setCellValueFactory(new PropertyValueFactory<>("diskGb"));
        javaColumn.setCellValueFactory(new PropertyValueFactory<>("javaVersion"));
        serverColumn.setCellValueFactory(new PropertyValueFactory<>("serverStatus"));
        useColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        useColumn.setCellFactory(CheckBoxTableCell.forTableColumn(useColumn));
        vmTable.setEditable(true);
        useColumn.setEditable(true);

        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button button = new Button("▶ Запустить сервер");

            {
                button.setOnAction(e -> {
                    VmInfo vm = getTableView().getItems().get(getIndex());
                    runNettyServerOnVm(vm);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : button);
            }
        });

        vmTable.setItems(vmData);

        sendIpsButton.setOnAction(e -> sendIpsToAlgorithmTab());

        vmData.addListener((ListChangeListener<VmInfo>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (VmInfo vm : change.getAddedSubList()) {
                        setupSelectedListener(vm);
                    }
                }
            }
        });
    }

    private void sendIpsToAlgorithmTab() {
        List<String> ips = vmData.stream()
                .filter(VmInfo::isSelected)
                .map(VmInfo::getIp)
                .filter(ip -> ip != null && !ip.isEmpty())
                .toList();

        vmManager.setIps(ips);
        logArea.appendText("✅ IP отправлены на вкладку Алгоритм: " + ips + "\n");
    }

    private void setupSelectedListener(VmInfo vm) {
        vm.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                logArea.appendText("☑ Выбран: " + vm.getIp() + "\n");
            }
        });
    }

    private void loadIniFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите .ini файл конфигурации");
        File file = fileChooser.showOpenDialog(loadIniButton.getScene().getWindow());

        if (file != null) {
            logArea.appendText("📄 Загружаем конфиг: " + file.getName() + "\n");
            VmCreationService creationService = new VmCreationService(authService);
            creationService.createVmsFromIniFile(file, vmData, logArea);
        }
    }

    private void loadExistingVms() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Введите folderId");
        dialog.setHeaderText("Загрузить все ВМ из папки");
        dialog.setContentText("folderId:");

        dialog.showAndWait().ifPresent(folderId -> {
            logArea.appendText("📦 Загружаем ВМ из папки: " + folderId + "\n");
            VmCreationService service = new VmCreationService(authService);
            var found = service.listExistingVms(folderId);
            vmData.clear();
            vmData.addAll(found);
            logArea.appendText("✅ Загружено ВМ: " + found.size() + "\n");
        });
    }

    private void runNettyServerOnVm(VmInfo vm) {
        logArea.appendText("🚀 Подключение к ВМ: " + vm.getIp() + "\n");

        new Thread(() -> {
            try {
                // Сначала проверяем версию Java
                String javaVersionOutput = SshExecutor.runCommand(
                        vm.getIp(),
                        "rus",
                        "C:/Users/rus/.ssh/id_rsa",
                        "java -version"
                );

                // Парсим версию из STDERR, например: 'java version "17.0.9"'
                String javaVersion = extractJavaVersion(javaVersionOutput);

                Platform.runLater(() -> {
                    vm.setJavaVersion(javaVersion);
                    logArea.appendText("☕ Java: " + javaVersion + "\n");
                });

                // Потом запускаем сервер
                SshExecutor.restartNettyServer(
                        vm.getIp(),
                        "rus",
                        "C:/Users/rus/.ssh/id_rsa",
                        "NettyVMServer-1.0-SNAPSHOT-jar-with-dependencies.jar"
                );

                Platform.runLater(() -> {
                    vm.setServerStatus("✅ Запущен");
                    logArea.appendText("✅ Сервер успешно перезапущен!\n");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    vm.setServerStatus("❌ Ошибка");
                    logArea.appendText("❌ Ошибка запуска: " + e + "\n");
                });
            }
        }).start();
    }

    private String extractJavaVersion(String output) {
        // Обычно версия в STDERR, например: java version "17.0.9"
        for (String line : output.split("\\R")) {
            if (line.contains("version")) {
                int start = line.indexOf("\"") + 1;
                int end = line.lastIndexOf("\"");
                if (start > 0 && end > start) {
                    return line.substring(start, end);
                }
                return line;
            }
        }
        return "не определено";
    }


    public void setVmManager(AlgorithmVmManager vmManager) {
        this.vmManager = vmManager;
    }
}







