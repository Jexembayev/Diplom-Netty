package orhestra.service;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import orhestra.model.OptimizationTask;
import orhestra.model.ResultRow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskExecutionService {

    private final ExecutorService executor;
    private final AtomicInteger completedTasks = new AtomicInteger();
    private final Map<String, Map<Integer, Label>> vmTaskDurations = new HashMap<>();
    private final Map<String, Map<Integer, Label>> vmTaskStatuses = new HashMap<>();
    private final Map<String, Long> vmStartTimes = new HashMap<>();
    private final Map<String, Integer> vmCompletedCounts = new HashMap<>();
    private final Map<String, Integer> vmTotalCounts = new HashMap<>();



    public TaskExecutionService(int threadCount) {
        this.executor = Executors.newFixedThreadPool(threadCount);
    }

    public void executeTask(
            OptimizationTask task,
            String ip,
            String jarPath,
            GridPane vmGrid,
            Map<String, Double> vmTimeTotals,
            Label timeLabel,
            TextArea outputArea,
            List<ResultRow> allResults,
            Runnable onAllComplete,
            int totalTaskCount
    ) {
        Platform.runLater(() -> addTaskToGrid(ip, task, vmGrid));

        long startTime = System.currentTimeMillis();
        vmStartTimes.putIfAbsent(ip, System.currentTimeMillis());
        vmTotalCounts.put(ip, vmTotalCounts.getOrDefault(ip, 0) + 1);

        executor.submit(() -> VmHttpService.sendTaskToVm(ip, jarPath, task,
                response -> {
                    double durationSec = (System.currentTimeMillis() - startTime) / 1000.0;

                    Platform.runLater(() -> {
                        updateGridSuccess(ip, task, durationSec);
                        updateTotalTimeLabel(durationSec, timeLabel);
                        outputArea.appendText("✅ Выполнено: " + response + "\n");
                    });

                    synchronized (allResults) {
                        allResults.add(new ResultRow(
                                task.getFunction(),
                                task.getnAgents(),
                                task.getDim(),
                                task.getnIterations(),
                                response.getOrDefault("fitness", "-").toString()
                        ));
                    }

                    if (completedTasks.incrementAndGet() == totalTaskCount) {
                        Platform.runLater(onAllComplete);
                        executor.shutdown();
                    }

                    vmCompletedCounts.put(ip, vmCompletedCounts.getOrDefault(ip, 0) + 1);
                    int completed = vmCompletedCounts.get(ip);
                    int total = vmTotalCounts.get(ip);

                    if (completed == total) {
                        long start = vmStartTimes.get(ip);
                        double realTime = (System.currentTimeMillis() - start) / 1000.0;

                        Platform.runLater(() -> {
                            timeLabel.setText(String.format("Общее время: %.2f сек", realTime));
                            vmTimeTotals.put(ip, realTime);
                        });
                    }

                },
                error -> {
                    Platform.runLater(() -> {
                        updateGridError(task, vmGrid);
                        outputArea.appendText("❌ Ошибка на " + ip + ": " + error + "\n");
                    });

                    if (completedTasks.incrementAndGet() == totalTaskCount) {
                        Platform.runLater(onAllComplete);
                        executor.shutdown();
                    }
                })
        );
    }

    private void addTaskToGrid(String ip, OptimizationTask task, GridPane grid) {
        Label durationLabel = new Label("-");
        Label statusLabel = new Label("В процессе");

        grid.addRow(grid.getRowCount(),
                new Label("Задача #" + task.getTaskNumber()),
                new Label(String.valueOf(task.getDim())),
                new Label(String.valueOf(task.getnAgents())),
                new Label(String.valueOf(task.getnIterations())),
                durationLabel,
                statusLabel
        );

        vmTaskDurations.computeIfAbsent(ip, k -> new HashMap<>())
                .put(task.getTaskNumber(), durationLabel);
        vmTaskStatuses.computeIfAbsent(ip, k -> new HashMap<>())
                .put(task.getTaskNumber(), statusLabel);
    }


    private void updateGridSuccess(String ip, OptimizationTask task, double durationSec) {
        Label durationLabel = vmTaskDurations.getOrDefault(ip, Map.of()).get(task.getTaskNumber());
        Label statusLabel = vmTaskStatuses.getOrDefault(ip, Map.of()).get(task.getTaskNumber());

        if (durationLabel != null) {
            durationLabel.setText(String.format("%.2f", durationSec));
        }
        if (statusLabel != null) {
            statusLabel.setText("✅ Готово");
        }
    }

    private void updateGridError(OptimizationTask task, GridPane grid) {
        int rowIndex = task.getTaskNumber() + 1;
        ((Label) grid.getChildren().get(rowIndex * 6 + 5)).setText("❌ Ошибка");
    }

    private void updateTotalTimeLabel(double duration, Label timeLabel) {
        String text = timeLabel.getText().replace("Общее время: ", "").replace(" сек", "").replace(",", ".");
        double currentTime = text.isEmpty() ? 0.0 : Double.parseDouble(text);
        double newTime = currentTime + duration;
        timeLabel.setText(String.format("Общее время: %.2f сек", newTime));
    }

    /**
     * Вызвать при закрытии приложения, чтобы корректно завершить потоки.
     */
    public void shutdownNow() {
        executor.shutdownNow();
    }
}
