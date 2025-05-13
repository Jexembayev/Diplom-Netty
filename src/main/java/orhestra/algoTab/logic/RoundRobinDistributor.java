package orhestra.algoTab.logic;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import orhestra.algoTab.model.OptimizationTask;
import orhestra.algoTab.model.ResultRow;
import orhestra.algoTab.service.TaskExecutionService;

import java.util.List;
import java.util.Map;

public class RoundRobinDistributor implements TaskDistributor {

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

        TaskExecutionService executionService = new TaskExecutionService(vmIps.size());

        for (int i = 0; i < tasks.size(); i++) {
            OptimizationTask task = tasks.get(i);
            String ip = vmIps.get(i % vmIps.size());
            String jarPath = jarPaths.get(ip);
            GridPane grid = vmGrids.get(ip);
            Label timeLabel = timeLabels.get(ip);


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








