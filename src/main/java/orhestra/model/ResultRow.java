package orhestra.model;

public record ResultRow(String function, int nAgents, int dim, int nIterations, String fitness) {

    @Override
    public String toString() {
        return String.format("%-12s | %8d | %11d | %12d | %s",
                function, nAgents, dim, nIterations, fitness);
    }
}


