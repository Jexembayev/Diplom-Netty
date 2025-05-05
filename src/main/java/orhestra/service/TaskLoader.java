package orhestra.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import orhestra.model.OptimizationTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TaskLoader {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<OptimizationTask> generateTasks(File functionsJson, File paramsJson) throws Exception {
        List<String> functions = objectMapper.readValue(functionsJson, new TypeReference<>() {});
        List<Map<String, Integer>> paramSets = objectMapper.readValue(paramsJson, new TypeReference<>() {});

        List<OptimizationTask> tasks = new ArrayList<>();
        int taskId = 0;

        for (String function : functions) {
            for (Map<String, Integer> paramSet : paramSets) {
                OptimizationTask task = new OptimizationTask(
                        function,
                        paramSet.getOrDefault("nAgents", 50),
                        paramSet.getOrDefault("dim", 10),
                        paramSet.getOrDefault("nIterations", 100)
                );
                task.setTaskNumber(taskId++);
                tasks.add(task);
            }
        }

        return tasks;
    }
}
