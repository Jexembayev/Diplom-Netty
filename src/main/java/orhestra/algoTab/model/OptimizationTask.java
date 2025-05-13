package orhestra.algoTab.model;

public class OptimizationTask {
    private final String function;
    private final int nAgents;
    private final int dim;
    private final int nIterations;
    private int taskNumber;
    private double durationSeconds = 0.0;

    public OptimizationTask(String function, int nAgents, int dim, int nIterations) {
        this.function = function;
        this.nAgents = nAgents;
        this.dim = dim;
        this.nIterations = nIterations;
    }
    public void setTaskNumber(int n) { this.taskNumber = n; }
    public int getTaskNumber() { return taskNumber; }

    public void setDurationSeconds(double s) { this.durationSeconds = s; }
    public double getDurationSeconds() { return durationSeconds; }

    public String getFunction() {
        return function;
    }

    public int getnAgents() {
        return nAgents;
    }

    public int getDim() {
        return dim;
    }

    public int getnIterations() {
        return nIterations;
    }

    @Override
    public String toString() {
        return String.format("Функция: %s | Агенты: %d | Размерность: %d | Итерации: %d",
                function, nAgents, dim, nIterations);
    }
}

