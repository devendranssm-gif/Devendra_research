package healthcare.utils;

/**
 * Constants.java
 * Global simulation constants for PPEC Healthcare Fog Framework.
 */
public class Constants {

    // ── Simulation Area ──────────────────────────────────────────────────────
    public static final int SIMULATION_AREA_WIDTH  = 3000;
    public static final int SIMULATION_AREA_HEIGHT = 3000;
    public static final double SIMULATION_TIME     = 10000.0;

    // ── Topology ──────────────────────────────────────────────────────────────
    public static final int NUM_FOG_NODES       = 6;
    public static final int NUM_IOT_DEVICES     = 30;
    public static final int DEVICES_PER_FOG     = 5;
    public static final double COVERAGE_RADIUS  = 600.0;

    // ── Cloud Resource ────────────────────────────────────────────────────────
    public static final long   CLOUD_MIPS          = 44800;
    public static final int    CLOUD_RAM           = 40000;
    public static final long   CLOUD_UPLINK_BW     = 10000;
    public static final long   CLOUD_DOWNLINK_BW   = 10000;
    public static final int    CLOUD_NUM_PES       = 4;
    public static final double CLOUD_RATE_PER_MIPS = 0.01;

    // ── Proxy Resource ────────────────────────────────────────────────────────
    public static final long PROXY_MIPS        = 40000;
    public static final int  PROXY_RAM         = 4000;
    public static final long PROXY_UPLINK_BW   = 10000;
    public static final long PROXY_DOWNLINK_BW = 10000;
    public static final int  PROXY_NUM_PES     = 2;

    // ── Fog Node Resource ─────────────────────────────────────────────────────
    public static final long   FOG_MIPS          = 40000;
    public static final int    FOG_RAM           = 4000;
    public static final long   FOG_UPLINK_BW     = 10000;
    public static final long   FOG_DOWNLINK_BW   = 10000;
    public static final int    FOG_NUM_PES       = 2;
    public static final double FOG_RATE_PER_MIPS = 0.005;

    // ── IoT Device ────────────────────────────────────────────────────────────
    public static final long   IOT_MIPS          = 1000;
    public static final int    IOT_RAM           = 512;
    public static final long   IOT_UPLINK_BW     = 1000;
    public static final long   IOT_DOWNLINK_BW   = 1000;
    public static final double TUPLE_GEN_RATE    = 5.0;   // seconds between tuples
    public static final int    IOT_NUM_PES       = 1;

    // ── Application Module Names ───────────────────────────────────────────────
    public static final String MODULE_SENSOR        = "Sensor";
    public static final String MODULE_DATA_COLLECTOR= "DataCollector";
    public static final String MODULE_PREPROCESSOR  = "PreProcessor";
    public static final String MODULE_DILP_OPTIMIZER= "DILPOptimizer";
    public static final String MODULE_ECC_SECURITY  = "ECCSecurityManager";
    public static final String MODULE_TASK_SCHEDULER= "TaskScheduler";
    public static final String MODULE_CLOUD_STORAGE = "CloudStorage";

    // ── Tuple Types ───────────────────────────────────────────────────────────
    public static final String TUPLE_SENSOR_DATA    = "SENSOR_DATA";
    public static final String TUPLE_COLLECTED      = "COLLECTED_DATA";
    public static final String TUPLE_PREPROCESSED   = "PREPROCESSED_DATA";
    public static final String TUPLE_OPTIMIZED      = "OPTIMIZED_DATA";
    public static final String TUPLE_ENCRYPTED      = "ENCRYPTED_DATA";
    public static final String TUPLE_SCHEDULED      = "SCHEDULED_DATA";
    public static final String TUPLE_RESULT         = "RESULT";

    // ── Tuple Sizes (bytes) ────────────────────────────────────────────────────
    public static final long TUPLE_SENSOR_SIZE  = 500;
    public static final long TUPLE_PROC_SIZE    = 1000;
    public static final long TUPLE_ENC_SIZE     = 1500;
    public static final long TUPLE_RESULT_SIZE  = 200;

    // ── Cloudlet (tuple CPU) ──────────────────────────────────────────────────
    public static final long SENSOR_CLOUDLET_LENGTH       = 1000;
    public static final long COLLECTOR_CLOUDLET_LENGTH    = 2000;
    public static final long PREPROCESSOR_CLOUDLET_LENGTH = 3000;
    public static final long OPTIMIZER_CLOUDLET_LENGTH    = 5000;
    public static final long SECURITY_CLOUDLET_LENGTH     = 4000;
    public static final long SCHEDULER_CLOUDLET_LENGTH    = 2000;
    public static final long STORAGE_CLOUDLET_LENGTH      = 1000;

    // ── ECC ───────────────────────────────────────────────────────────────────
    public static final String ECC_CURVE      = "secp256r1";
    public static final String ECC_ALGORITHM  = "ECIES";
    public static final String ECC_PROVIDER   = "BC";

    // ── DILP ──────────────────────────────────────────────────────────────────
    public static final int    DILP_MAX_ITERATIONS = 100;
    public static final double DILP_CPU_THRESHOLD  = 0.85;
    public static final double DILP_RAM_THRESHOLD  = 0.80;
    public static final double DILP_BW_THRESHOLD   = 0.75;

    // ── Energy (mW) ───────────────────────────────────────────────────────────
    public static final double FOG_BUSY_POWER  = 107.339;
    public static final double FOG_IDLE_POWER  = 83.4333;

    // ── Cost ──────────────────────────────────────────────────────────────────
    public static final double COST_PER_SEC    = 0.001;

    // ── Output Paths ──────────────────────────────────────────────────────────
    public static final String OUTPUT_DIR       = "output/";
    public static final String CSV_DIR          = "output/csv/";
    public static final String GRAPH_DIR        = "output/graphs/";
    public static final String REPORT_DIR       = "output/reports/";
    public static final String LOG_DIR          = "output/logs/";

    // ── Scenario IoT Counts ───────────────────────────────────────────────────
    public static final int[] IOT_SCENARIOS = {30, 42, 54, 66, 78};

    private Constants() { /* utility class */ }
}
