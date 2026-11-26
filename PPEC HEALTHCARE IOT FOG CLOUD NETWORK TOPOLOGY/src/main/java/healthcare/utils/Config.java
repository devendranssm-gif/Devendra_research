package healthcare.utils;

/**
 * Config.java
 * Runtime-adjustable configuration for PPEC simulation.
 */
public class Config {

    // Current scenario IoT device count (updated per experiment)
    public static int currentIoTCount = Constants.NUM_IOT_DEVICES;

    // Enable/disable ECC encryption layer
    public static boolean eccEnabled = true;

    // Enable/disable DILP optimizer
    public static boolean dilpEnabled = true;

    // Enable graph generation
    public static boolean generateGraphs = true;

    // Enable CSV export
    public static boolean exportCSV = true;

    // Enable PDF report
    public static boolean exportPDF = true;

    // Verbose simulation log output
    public static boolean verboseLog = false;

    // Fog nodes count (can vary per experiment)
    public static int fogNodeCount = Constants.NUM_FOG_NODES;

    // Simulation time limit
    public static double simTime = Constants.SIMULATION_TIME;

    private Config() {}
}
