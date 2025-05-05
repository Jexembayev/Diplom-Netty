package orhestra.functions;

public class ObjectiveFunctions {

    // Sphere Function
    public static double sphere(double[] x) {
        double sum = 0.0;
        for (double xi : x) {
            sum += xi * xi;
        }
        return sum;
    }

    // Rastrigin Function
    public static double rastrigin(double[] x) {
        double sum = 10 * x.length;
        for (double xi : x) {
            sum += xi * xi - 10 * Math.cos(2 * Math.PI * xi);
        }
        return sum;
    }

    // Rosenbrock Function
    public static double rosenbrock(double[] x) {
        double sum = 0.0;
        for (int i = 0; i < x.length - 1; i++) {
            sum += 100 * Math.pow(x[i + 1] - x[i] * x[i], 2) + Math.pow(x[i] - 1, 2);
        }
        return sum;
    }

    // Ackley Function
    public static double ackley(double[] x) {
        double sum1 = 0.0;
        double sum2 = 0.0;
        for (double xi : x) {
            sum1 += xi * xi;
            sum2 += Math.cos(2 * Math.PI * xi);
        }

        int d = x.length;
        return -20.0 * Math.exp(-0.2 * Math.sqrt(sum1 / d)) -
                Math.exp(sum2 / d) + 20 + Math.E;
    }
}

