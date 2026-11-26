package healthcare.placement;

import healthcare.monitoring.LoadMonitor;
import healthcare.utils.Constants;
import org.fog.entities.FogDevice;

import java.util.List;
import java.util.logging.Logger;

/**
 * DynamicTaskScheduler.java
 * Runtime scheduler that re-routes incoming tuples based on current fog load.
 */
public class DynamicTaskScheduler {

    private static final Logger LOG = Logger.getLogger(DynamicTaskScheduler.class.getName());
    private final LoadMonitor   loadMonitor;
    private final List<FogDevice> fogDevices;

    public DynamicTaskScheduler(List<FogDevice> fogDevices) {
        this.fogDevices  = fogDevices;
        this.loadMonitor = new LoadMonitor(fogDevices.size());
    }

    /**
     * Schedule a task to a fog node. Returns the selected fog device index.
     * Uses least-loaded selection with DILP result as hint.
     *
     * @param preferredFogIdx DILP-preferred fog index
     * @param cpuRequired     MIPS required by the task
     * @return actual fog index chosen
     */
    public int scheduleTask(int preferredFogIdx, double cpuRequired) {
        int chosen = preferredFogIdx;

        // If preferred fog is overloaded, pick least loaded
        if (loadMonitor.getLoad(preferredFogIdx) > Constants.DILP_CPU_THRESHOLD * 100) {
            chosen = loadMonitor.leastLoaded();
            LOG.fine("[Scheduler] Re-routing from Fog_" + preferredFogIdx + " → Fog_" + chosen);
        }

        loadMonitor.increment(chosen);
        return chosen;
    }

    public void taskCompleted(int fogIdx) {
        loadMonitor.decrement(fogIdx);
    }

    public LoadMonitor getLoadMonitor() { return loadMonitor; }
}
