package orhestra.utils;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import orhestra.model.OptimizationTask;
import orhestra.model.ResultRow;

import java.util.List;
import java.util.Map;

public class TaskResultRenderer {

    private static final String TIME_LABEL_PREFIX = "#timeLabel_";

    public static void render(OptimizationTask task,
                              Map<String, Object> response,
                              String ip,
                              Map<String, GridPane> vmGrids,
                              Map<String, Double> vmTimeTotals,
                              List<ResultRow> allResults,
                              TextArea outputArea) {

        GridPane grid = vmGrids.get(ip);
        if (grid == null) {
            outputArea.appendText("⚠️ Не найден GridPane для IP: " + ip + "\n");
            return;
        }

        double duration = parseDuration(response.get("duration"), ip, outputArea);
        updateTotalTime(ip, duration, vmTimeTotals, grid, outputArea);

        addResultRowToGrid(task, grid, duration);
        addResultRowToSummary(task, response, allResults);

        outputArea.appendText(response.toString() + "\n");
    }

    private static double parseDuration(Object durationObj, String ip, TextArea outputArea) {
        if (durationObj == null) return 0.0;

        try {
            return Double.parseDouble(durationObj.toString().replace(",", "."));
        } catch (NumberFormatException e) {
            outputArea.appendText("⚠️ IP " + ip + ": Не удалось распарсить duration: " + durationObj + "\n");
            return 0.0;
        }
    }

    private static void updateTotalTime(String ip, double duration, Map<String, Double> vmTimeTotals,
                                        GridPane grid, TextArea outputArea) {
        double total = vmTimeTotals.getOrDefault(ip, 0.0) + duration;
        vmTimeTotals.put(ip, total);

        Label timeLabel = (Label) ((VBox) grid.getParent()).lookup(TIME_LABEL_PREFIX + ip);
        if (timeLabel != null) {
            timeLabel.setText("Общее время: " + String.format("%.2f", total) + " сек");
        } else {
            outputArea.appendText("⚠️ IP " + ip + ": Не найден timeLabel\n");
        }
    }

    private static void addResultRowToGrid(OptimizationTask task, GridPane grid, double duration) {
        grid.addRow(grid.getRowCount(),
                new Label("Задача #" + task.getTaskNumber()),
                new Label(String.valueOf(task.getDim())),
                new Label(String.valueOf(task.getnAgents())),
                new Label(String.valueOf(task.getnIterations())),
                new Label(duration > 0 ? String.format("%.2f", duration) : "-"),
                new Label("✅")
        );
    }

    private static void addResultRowToSummary(OptimizationTask task, Map<String, Object> response,
                                              List<ResultRow> allResults) {
        String fitness = response.getOrDefault("fitness", "-").toString();

        allResults.add(new ResultRow(
                task.getFunction(),
                task.getnAgents(),
                task.getDim(),
                task.getnIterations(),
                fitness
        ));
    }
}




