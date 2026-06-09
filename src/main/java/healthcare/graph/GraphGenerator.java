package healthcare.graph;

import healthcare.metrics.MetricsCollector.ScenarioResult;
import healthcare.utils.Constants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

/**
 * GraphGenerator.java
 * Generates all 5 performance graphs as PNG files using JFreeChart.
 *
 * Graphs produced:
 *   1. Latency vs IoT Devices
 *   2. Energy vs IoT Devices
 *   3. Execution Time vs IoT Devices
 *   4. Cost vs IoT Devices
 *   5. Network Usage vs IoT Devices
 */
public class GraphGenerator {

    private static final Logger LOG = Logger.getLogger(GraphGenerator.class.getName());

    private static final int    CHART_WIDTH  = 800;
    private static final int    CHART_HEIGHT = 500;
    private static final Color  LINE_COLOR   = new Color(31, 119, 180);
    private static final Color  BG_COLOR     = Color.WHITE;

    public GraphGenerator() {
        try { Files.createDirectories(Paths.get(Constants.GRAPH_DIR)); }
        catch (IOException e) { LOG.warning("Could not create graph dir: " + e.getMessage()); }
    }

    /** Generate all 5 graphs from simulation results. */
    public void generateAll(List<ScenarioResult> results) {
        generateLatencyGraph(results);
        generateEnergyGraph(results);
        generateExecutionTimeGraph(results);
        generateCostGraph(results);
        generateNetworkUsageGraph(results);
        System.out.println("[Graphs] All 5 graphs written to " + Constants.GRAPH_DIR);
    }

    // ─────────────────────────────────────────────────────────────────────────

    public void generateLatencyGraph(List<ScenarioResult> results) {
        XYSeries series = buildSeries("Avg Latency (ms)", results,
                r -> r.iotCount, r -> r.avgLatencyMs);
        saveChart(createChart("Latency vs IoT Devices",
                "Number of IoT Devices", "Average Latency (ms)", series),
                "latency.png");
    }

    public void generateEnergyGraph(List<ScenarioResult> results) {
        XYSeries series = buildSeries("Energy (J)", results,
                r -> r.iotCount, r -> r.energyJoules);
        saveChart(createChart("Energy Consumption vs IoT Devices",
                "Number of IoT Devices", "Energy Consumption (J)", series),
                "energy.png");
    }

    public void generateExecutionTimeGraph(List<ScenarioResult> results) {
        XYSeries series = buildSeries("Execution Time (ms)", results,
                r -> r.iotCount, r -> r.executionTimeMs);
        saveChart(createChart("Execution Time vs IoT Devices",
                "Number of IoT Devices", "Execution Time (ms)", series),
                "execution_time.png");
    }

    public void generateCostGraph(List<ScenarioResult> results) {
        XYSeries series = buildSeries("Cost ($)", results,
                r -> r.iotCount, r -> r.executionCostUSD);
        saveChart(createChart("Execution Cost vs IoT Devices",
                "Number of IoT Devices", "Execution Cost ($)", series),
                "cost.png");
    }

    public void generateNetworkUsageGraph(List<ScenarioResult> results) {
        XYSeries series = buildSeries("Network Usage (MB)", results,
                r -> r.iotCount, r -> r.networkUsageMB);
        saveChart(createChart("Network Usage vs IoT Devices",
                "Number of IoT Devices", "Network Usage (MB)", series),
                "network_usage.png");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    @FunctionalInterface
    interface ValueExtractor { double extract(ScenarioResult r); }

    private XYSeries buildSeries(String label, List<ScenarioResult> results,
                                  ValueExtractor xFn, ValueExtractor yFn) {
        XYSeries series = new XYSeries(label);
        for (ScenarioResult r : results) series.add(xFn.extract(r), yFn.extract(r));
        return series;
    }

    private JFreeChart createChart(String title, String xLabel, String yLabel, XYSeries series) {
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                title, xLabel, yLabel, dataset,
                PlotOrientation.VERTICAL, true, true, false);

        chart.setBackgroundPaint(BG_COLOR);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(248, 248, 248));
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, LINE_COLOR);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesShapesVisible(0, true);
        plot.setRenderer(renderer);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setAutoRangeIncludesZero(true);

        return chart;
    }

    private void saveChart(JFreeChart chart, String filename) {
        String path = Constants.GRAPH_DIR + filename;
        try {
            ChartUtils.saveChartAsPNG(new File(path), chart, CHART_WIDTH, CHART_HEIGHT);
            System.out.println("[Graph] Written: " + path);
        } catch (IOException e) {
            LOG.warning("[GraphGenerator] Failed to save " + filename + ": " + e.getMessage());
        }
    }
}
