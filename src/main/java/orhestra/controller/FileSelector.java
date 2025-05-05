package orhestra.controller;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

public class FileSelector {

    private final Window ownerWindow;

    public FileSelector(Window ownerWindow) {
        this.ownerWindow = ownerWindow;
    }

    public File chooseFile(String title, String description, String... extensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(description, extensions)
        );
        return fileChooser.showOpenDialog(ownerWindow);
    }

    public File chooseJar() {
        return chooseFile("Выберите JAR-файл алгоритма", "JAR-файлы (*.jar)", "*.jar");
    }

    public File chooseJson(String prompt) {
        return chooseFile(prompt, "JSON-файлы (*.json)", "*.json");
    }
}
