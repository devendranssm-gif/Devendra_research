package healthcare.metrics;

/**
 * LatencyCalculator.java
 * Utility for computing per-tuple and aggregate latency metrics.
 */
public class LatencyCalculator {
    /** Latency = FinishTime − ArrivalTime (ms) */
    public static double compute(double finishTime, double arrivalTime) {
        return Math.max(0.0, finishTime - arrivalTime);
    }
    /** Average latency from individual samples */
    public static double average(double[] samples) {
        if (samples.length == 0) return 0.0;
        double sum = 0;
        for (double s : samples) sum += s;
        return sum / samples.length;
    }
}
