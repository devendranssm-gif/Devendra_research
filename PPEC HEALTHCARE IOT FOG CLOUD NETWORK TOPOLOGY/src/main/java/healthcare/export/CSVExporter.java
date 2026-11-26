package healthcare.export;

import healthcare.metrics.MetricsCollector.ScenarioResult;
import healthcare.utils.Constants;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

/**
 * CSVExporter.java
 * Writes per-metric CSV files to output/csv/ for all scenario results.
 */
public class CSVExporter {

    private static final Logger LOG = Logger.getLogger(CSVExporter.class.getName());

    public CSVExporter() {
        try { Files.createDirectories(Paths.get(Constants.CSV_DIR)); }
        catch (IOException e) { LOG.warning("Could not create CSV dir: " + e.getMessage()); }
    }

    public void exportAll(List<ScenarioResult> results) {
        exportLatency(results);
        exportEnergy(results);
        exportCost(results);
        exportExecutionTime(results);
        exportNetworkUsage(results);
        LOG.info("[CSVExporter] All CSV files written to " + Constants.CSV_DIR);
    }

    private void write(String filename, String header, List<ScenarioResult> results,
                       java.util.function.Function<ScenarioResult, String> rowFn) {
        String path = Constants.CSV_DIR + filename;
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            pw.println(header);
            for (ScenarioResult r : results) pw.println(rowFn.apply(r));
            System.out.println("[CSV] Written: " + path);
        } catch (IOException e) {
            LOG.warning("[CSVExporter] Failed to write " + filename + ": " + e.getMessage());
        }
    }

    public void exportLatency(List<ScenarioResult> results) {
        write("latency.csv", "IoT_Devices,Average_Latency_ms", results,
              r -> r.iotCount + "," + String.format("%.4f", r.avgLatencyMs));
    }

    public void exportEnergy(List<ScenarioResult> results) {
        write("energy.csv", "IoT_Devices,Energy_Consumption_J", results,
              r -> r.iotCount + "," + String.format("%.4f", r.energyJoules));
    }

    public void exportCost(List<ScenarioResult> results) {
        write("cost.csv", "IoT_Devices,Execution_Cost_USD", results,
              r -> r.iotCount + "," + String.format("%.6f", r.executionCostUSD));
    }

    public void exportExecutionTime(List<ScenarioResult> results) {
        write("execution_time.csv", "IoT_Devices,Execution_Time_ms", results,
              r -> r.iotCount + "," + String.format("%.4f", r.executionTimeMs));
    }

    public void exportNetworkUsage(List<ScenarioResult> results) {
        write("network_usage.csv", "IoT_Devices,Network_Usage_MB", results,
              r -> r.iotCount + "," + String.format("%.4f", r.networkUsageMB));
    }
}
