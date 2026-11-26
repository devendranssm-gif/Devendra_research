package healthcare.placement;

import healthcare.monitoring.ResourceMonitor;
import healthcare.utils.Constants;

import java.util.logging.Logger;

/**
 * MigrationManager.java
 * Detects overloaded fog nodes and triggers task migration to under-loaded nodes.
 */
public class MigrationManager {

    private static final Logger LOG = Logger.getLogger(MigrationManager.class.getName());
    private final ResourceMonitor resourceMonitor;

    public MigrationManager(ResourceMonitor monitor) {
        this.resourceMonitor = monitor;
    }

    /**
     * Check all fog nodes and migrate tasks from overloaded to underloaded.
     *
     * @return number of migrations performed
     */
    public int checkAndMigrate() {
        int migrations = 0;
        int numFogs = resourceMonitor.getNumFogs();

        for (int i = 0; i < numFogs; i++) {
            double cpuUtil = resourceMonitor.getCpuUtil(i);
            if (cpuUtil > Constants.DILP_CPU_THRESHOLD) {
                // Find least loaded fog
                int    target     = -1;
                double minUtil    = Double.MAX_VALUE;
                for (int j = 0; j < numFogs; j++) {
                    if (j == i) continue;
                    double u = resourceMonitor.getCpuUtil(j);
                    if (u < minUtil) { minUtil = u; target = j; }
                }
                if (target >= 0 && minUtil < Constants.DILP_CPU_THRESHOLD - 0.2) {
                    LOG.info(String.format("[Migration] Fog_%d (%.0f%%) → Fog_%d (%.0f%%)",
                            i, cpuUtil*100, target, minUtil*100));
                    migrations++;
                }
            }
        }
        return migrations;
    }
}
