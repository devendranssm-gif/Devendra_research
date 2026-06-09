package healthcare.export;

import healthcare.metrics.MetricsCollector.ScenarioResult;
import healthcare.utils.Constants;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

/**
 * ResultWriter.java
 * Writes a plain-text summary report to output/reports/summary.txt.
 */
public class ResultWriter {

    private static final Logger LOG = Logger.getLogger(ResultWriter.class.getName());

    public ResultWriter() {
        try { Files.createDirectories(Paths.get(Constants.REPORT_DIR)); }
        catch (IOException e) { LOG.warning("Could not create report dir: " + e.getMessage()); }
    }

    public void writeSummary(List<ScenarioResult> results) {
        String path = Constants.REPORT_DIR + "summary.txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            pw.println("=================================");
            pw.println("PPEC HEALTHCARE FOG SIMULATION");
            pw.println("=================================");
            pw.println("Fog Nodes    : " + Constants.NUM_FOG_NODES);
            pw.println("ECC Curve    : " + Constants.ECC_CURVE);
            pw.println("DILP Solver  : Branch-and-Bound ILP");
            pw.println("---------------------------------");
            for (ScenarioResult r : results) {
                pw.println();
                pw.println("IoT Devices       : " + r.iotCount);
                pw.printf ("Average Latency   : %.2f ms%n",  r.avgLatencyMs);
                pw.printf ("Energy Consumption: %.4f J%n",   r.energyJoules);
                pw.printf ("Execution Cost    : %.6f $%n",   r.executionCostUSD);
                pw.printf ("Network Usage     : %.2f MB%n",  r.networkUsageMB);
                pw.printf ("Execution Time    : %.2f ms%n",  r.executionTimeMs);
                pw.println("---------------------------------");
            }
            System.out.println("[ResultWriter] Summary written: " + path);
        } catch (IOException e) {
            LOG.warning("[ResultWriter] Failed: " + e.getMessage());
        }
    }
}
