package orhestra.algoTab.logic;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import orhestra.algoTab.model.OptimizationTask;
import orhestra.algoTab.model.ResultRow;

import java.util.List;
import java.util.Map;

public interface TaskDistributor {
    void distribute(
            List<OptimizationTask> tasks,
            List<String> vmIps,
            Map<String, String> jarPaths,
            TextArea outputArea,
            Map<String, GridPane> vmGrids,
            Map<String, Double> vmTimeTotals,
            Map<String, Label> timeLabels,
            List<ResultRow> allResults,
            HBox vmBlocksContainer,
            Runnable onAllComplete
    );


    default void logError(TextArea outputArea, String ip, Exception error) {
        Platform.runLater(() ->
                outputArea.appendText("❌ Ошибка на " + ip + ": " + error + "\n")
        );
    }
}


