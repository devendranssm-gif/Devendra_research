package healthcare.metrics;

import healthcare.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * MetricsCollector.java
 * Central aggregator for all PPEC simulation performance metrics.
 *
 * Metrics collected per scenario run:
 *   - Latency (ms)
 *   - Energy Consumption (J)
 *   - Execution Cost ($)
 *   - Network Usage (MB)
 *   - Execution Time (ms)
 */
public class MetricsCollector {

    /** Holds the complete result for one scenario (IoT device count). */
    public static class ScenarioResult {
        public final int    iotCount;
        public final double avgLatencyMs;
        public final double energyJoules;
        public final double executionCostUSD;
        public final double networkUsageMB;
        public final double executionTimeMs;

        public ScenarioResult(int iotCount,
                              double avgLatencyMs,
                              double energyJoules,
                              double executionCostUSD,
                              double networkUsageMB,
                              double executionTimeMs) {
            this.iotCount          = iotCount;
            this.avgLatencyMs      = avgLatencyMs;
            this.energyJoules      = energyJoules;
            this.executionCostUSD  = executionCostUSD;
            this.networkUsageMB    = networkUsageMB;
            this.executionTimeMs   = executionTimeMs;
        }

        @Override
        public String toString() {
            return String.format(
                "IoT=%-3d | Latency=%-8.2f ms | Energy=%-10.4f J | Cost=%-8.4f $ | Net=%-8.2f MB | ExecTime=%-8.2f ms",
                iotCount, avgLatencyMs, energyJoules, executionCostUSD, networkUsageMB, executionTimeMs);
        }
    }

    private final List<ScenarioResult> results = new ArrayList<>();

    // Per-run accumulators
    private final List<Double> latencySamples = new ArrayList<>();
    private double totalEnergy       = 0.0;
    private double totalExecTime     = 0.0;
    private double totalNetworkBytes = 0.0;
    private int    tupleCount        = 0;

    // ── Per-tuple recording ──────────────────────────────────────────────────

    /** Record one tuple's end-to-end latency. */
    public void recordLatency(double finishTime, double arrivalTime) {
        latencySamples.add(finishTime - arrivalTime);
    }

    /** Record energy for one fog device. */
    public void recordEnergy(double joules) {
        totalEnergy += joules;
    }

    /** Record execution time for one cloudlet. */
    public void recordExecutionTime(double execTimeMs) {
        totalExecTime += execTimeMs;
        tupleCount++;
    }

    /** Record network bytes for one transmitted tuple. */
    public void recordNetworkUsage(long tupleBytes, int hopCount) {
        totalNetworkBytes += (double) tupleBytes * hopCount;
    }

    // ── Scenario finalisation ────────────────────────────────────────────────

    /**
     * Finalise the current run as a scenario result and reset accumulators.
     *
     * @param iotCount number of IoT devices in this scenario
     * @return the sealed ScenarioResult
     */
    public ScenarioResult finaliseScenario(int iotCount) {
        double avgLatency   = latencySamples.stream().mapToDouble(Double::doubleValue)
                                            .average().orElse(0.0);
        double avgExecTime  = (tupleCount > 0) ? totalExecTime / tupleCount : 0.0;
        double execCost     = avgExecTime * Constants.COST_PER_SEC;
        double networkMB    = totalNetworkBytes / (1024.0 * 1024.0);

        ScenarioResult result = new ScenarioResult(
                iotCount, avgLatency, totalEnergy, execCost, networkMB, avgExecTime);
        results.add(result);

        // Reset for next scenario
        latencySamples.clear();
        totalEnergy       = 0.0;
        totalExecTime     = 0.0;
        totalNetworkBytes = 0.0;
        tupleCount        = 0;

        return result;
    }

    public List<ScenarioResult> getAllResults() { return results; }

    /** Print a formatted summary table. */
    public void printSummaryTable() {
        System.out.println();
        System.out.println("=================================================================================================");
        System.out.println("  PPEC SIMULATION — RESULTS SUMMARY");
        System.out.println("=================================================================================================");
        System.out.printf("  %-6s | %-14s | %-14s | %-12s | %-12s | %-14s%n",
                "IoT", "Latency (ms)", "Energy (J)", "Cost ($)", "Net (MB)", "ExecTime (ms)");
        System.out.println("-------------------------------------------------------------------------------------------------");
        for (ScenarioResult r : results) {
            System.out.printf("  %-6d | %-14.2f | %-14.4f | %-12.4f | %-12.2f | %-14.2f%n",
                    r.iotCount, r.avgLatencyMs, r.energyJoules,
                    r.executionCostUSD, r.networkUsageMB, r.executionTimeMs);
        }
        System.out.println("=================================================================================================");
    }
}
