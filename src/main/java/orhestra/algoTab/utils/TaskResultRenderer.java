package orhestra.algoTab.utils;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import orhestra.algoTab.model.OptimizationTask;
import orhestra.algoTab.model.ResultRow;

import java.util.List;
import java.util.Map;

public class TaskResultRenderer {

    public static void render(OptimizationTask task,
                              Map<String, Object> response,
                              String ip,
                              Map<String, GridPane> vmGrids,
                              Map<String, Double> vmTimeTotals,
                              Map<String, Label> timeLabels,
                              List<ResultRow> allResults,
                              TextArea outputArea) {

        GridPane grid = vmGrids.get(ip);
        Label timeLabel = timeLabels.get(ip);

        if (grid == null) {
            outputArea.appendText("⚠️ Не найден GridPane для IP: " + ip + "\n");
            return;
        }

        if (timeLabel == null) {
            outputArea.appendText("⚠️ Не найден timeLabel для IP: " + ip + "\n");
            return;
        }

        double duration = parseDuration(response.get("duration"), ip, outputArea);
        String fitnessValue = extractFitness(response.get("output"));

        updateTotalTime(ip, duration, vmTimeTotals, timeLabel);
        addResultRowToGrid(task, grid, duration, fitnessValue);
        addResultRowToSummary(task, fitnessValue, allResults);

        outputArea.appendText("✅ " + ip + " → " + response.toString() + "\n");
    }

    private static String extractFitness(Object outputObj) {
        if (outputObj == null) return "-";
        String output = outputObj.toString();
        for (String line : output.split("\n")) {
            if (line.toLowerCase().contains("fitness")) {
                int colonIndex = line.indexOf(":");
                if (colonIndex != -1 && colonIndex + 1 < line.length()) {
                    return line.substring(colonIndex + 1).trim().replace(",", ".");
                }
            }
        }
        return "-";
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

    private static void updateTotalTime(String ip, double duration,
                                        Map<String, Double> vmTimeTotals,
                                        Label timeLabel) {
        double total = vmTimeTotals.getOrDefault(ip, 0.0) + duration;
        vmTimeTotals.put(ip, total);
        timeLabel.setText("Общее время: " + String.format("%.2f", total) + " сек");
    }

    private static Label createCell(String text, int width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setMinWidth(width);
        label.setMaxWidth(width);
        label.setStyle("""
            -fx-alignment: CENTER;
            -fx-font-family: monospace;
            -fx-font-size: 12px;
        """);
        return label;
    }

    private static void addResultRowToGrid(OptimizationTask task, GridPane grid, double duration, String fitness) {
        int rowIndex = grid.getRowCount();
        grid.addRow(rowIndex,
                createCell("#" + task.getTaskNumber(), 40),
                createCell(String.valueOf(task.getDim()), 40),
                createCell(String.valueOf(task.getnAgents()), 40),
                createCell(String.valueOf(task.getnIterations()), 40),
                createCell(duration > 0 ? String.format("%.2f", duration) : "-", 40),
                createCell(fitness, 80),
                createCell("✓", 40)
        );
    }

    private static void addResultRowToSummary(OptimizationTask task, String fitness,
                                              List<ResultRow> allResults) {
        allResults.add(new ResultRow(
                task.getFunction(),
                task.getnAgents(),
                task.getDim(),
                task.getnIterations(),
                fitness
        ));
    }
}







