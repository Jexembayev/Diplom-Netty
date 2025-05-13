package orhestra.algoTab.service;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import orhestra.algoTab.model.OptimizationTask;
import orhestra.algoTab.model.ResultRow;
import orhestra.algoTab.utils.TaskResultRenderer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskExecutionService {

    private final ExecutorService executor;
    private final AtomicInteger completedTasks = new AtomicInteger();
    private final Map<String, Long> vmStartTimes = new HashMap<>();
    private final Map<String, Long> vmEndTimes = new HashMap<>();

    public TaskExecutionService(int threadCount) {
        this.executor = Executors.newFixedThreadPool(threadCount);
    }

    public void executeTask(
            OptimizationTask task,
            String ip,
            String jarPath,
            Map<String, GridPane> vmGrids,
            Map<String, Double> vmTimeTotals,
            Map<String, Label> timeLabels,
            TextArea outputArea,
            List<ResultRow> allResults,
            Runnable onAllComplete,
            int totalTaskCount
    ) {
        // Запоминаем время запуска ВМ
        vmStartTimes.putIfAbsent(ip, System.currentTimeMillis());

        executor.submit(() -> VmHttpService.sendTaskToVm(ip, jarPath, task,
                response -> Platform.runLater(() -> {
                    TaskResultRenderer.render(task, response, ip, vmGrids, vmTimeTotals, timeLabels, allResults, outputArea);
                    vmEndTimes.put(ip, System.currentTimeMillis());
                    checkIfAllComplete(onAllComplete, totalTaskCount, vmTimeTotals, timeLabels);
                }),
                error -> Platform.runLater(() -> {
                    outputArea.appendText("❌ Ошибка на " + ip + ": " + error + "\n");
                    vmEndTimes.put(ip, System.currentTimeMillis());
                    checkIfAllComplete(onAllComplete, totalTaskCount, vmTimeTotals, timeLabels);
                })
        ));
    }

    private void checkIfAllComplete(Runnable onAllComplete,
                                    int totalTaskCount,
                                    Map<String, Double> vmTimeTotals,
                                    Map<String, Label> timeLabels) {

        if (completedTasks.incrementAndGet() == totalTaskCount) {
            executor.shutdown();

            // Обновим точное время выполнения на каждую ВМ
            for (String ip : vmTimeTotals.keySet()) {
                long start = vmStartTimes.getOrDefault(ip, 0L);
                long end = vmEndTimes.getOrDefault(ip, start);
                double realTime = (end - start) / 1000.0;

                vmTimeTotals.put(ip, realTime);
                Label label = timeLabels.get(ip);
                if (label != null) {
                    label.setText("Общее время: " + String.format("%.2f", realTime) + " сек");
                }
            }

            onAllComplete.run();
        }
    }

    public void shutdownNow() {
        executor.shutdownNow();
    }
}


