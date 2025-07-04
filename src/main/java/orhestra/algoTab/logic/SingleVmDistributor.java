package orhestra.algoTab.logic;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import orhestra.algoTab.model.OptimizationTask;
import orhestra.algoTab.model.ResultRow;
import orhestra.algoTab.service.TaskExecutionService;

import java.util.List;
import java.util.Map;

public class SingleVmDistributor implements TaskDistributor {

    @Override
    public void distribute(List<OptimizationTask> tasks,
                           List<String> vmIps,
                           Map<String, String> jarPaths,
                           TextArea outputArea,
                           Map<String, GridPane> vmGrids,
                           Map<String, Double> vmTimeTotals,
                           Map<String, Label> timeLabels,
                           List<ResultRow> allResults,
                           HBox vmBlocksContainer,
                           Runnable onAllComplete) {

        if (vmIps.isEmpty()) {
            Platform.runLater(() -> outputArea.appendText("❗ Нет IP-адресов.\n"));
            return;
        }

        String ip = vmIps.get(0);
        String jarPath = jarPaths.get(ip);
        GridPane grid = vmGrids.get(ip);
        Label timeLabel = timeLabels.get(ip);

        TaskExecutionService executionService = new TaskExecutionService(1);

        for (OptimizationTask task : tasks) {
            executionService.executeTask(
                    task,
                    ip,
                    jarPath,
                    vmGrids,
                    vmTimeTotals,
                    timeLabels, // <--- добавили!
                    outputArea,
                    allResults,
                    onAllComplete,
                    tasks.size()
            );



        }
    }
}




