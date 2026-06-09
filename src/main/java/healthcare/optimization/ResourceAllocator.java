package healthcare.optimization;

import healthcare.utils.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * ResourceAllocator.java
 * Translates B&B task assignment into fog resource reservations and
 * tracks live utilisation per fog node.
 */
public class ResourceAllocator {

    private static final Logger LOG = Logger.getLogger(ResourceAllocator.class.getName());

    /** Remaining CPU capacity per fog (MIPS) */
    private final Map<Integer, Double> cpuRemaining = new HashMap<>();
    /** Remaining RAM capacity per fog (MB) */
    private final Map<Integer, Double> ramRemaining = new HashMap<>();
    /** Remaining BW capacity per fog (Mbps) */
    private final Map<Integer, Double> bwRemaining  = new HashMap<>();

    private final int numFogs;

    public ResourceAllocator(int numFogs) {
        this.numFogs = numFogs;
        reset();
    }

    /** Restore all capacities to defaults. */
    public void reset() {
        for (int i = 0; i < numFogs; i++) {
            cpuRemaining.put(i, (double) Constants.FOG_MIPS);
            ramRemaining.put(i, (double) Constants.FOG_RAM);
            bwRemaining.put(i,  (double) Constants.FOG_UPLINK_BW);
        }
    }

    /**
     * Apply a DILP assignment result, reserving resources on each fog.
     *
     * @param tasks      prioritised task list
     * @param assignment task[i] → fogIndex mapping from B&B
     */
    public void applyAssignment(List<TaskPrioritizer.Task> tasks, int[] assignment) {
        for (int i = 0; i < assignment.length && i < tasks.size(); i++) {
            int fog = assignment[i];
            TaskPrioritizer.Task task = tasks.get(i);

            double cpu = cpuRemaining.getOrDefault(fog, 0.0);
            double ram = ramRemaining.getOrDefault(fog, 0.0);
            double bw  = bwRemaining.getOrDefault(fog, 0.0);

            cpuRemaining.put(fog, Math.max(0, cpu - task.cpuReq));
            ramRemaining.put(fog, Math.max(0, ram - task.ramReq));
            bwRemaining.put(fog,  Math.max(0, bw  - task.bwReq));

            task.assignedFog = fog;
            LOG.fine(String.format("Task %d → Fog %d (cpu=%.0f ram=%.0f bw=%.0f)",
                    task.id, fog, task.cpuReq, task.ramReq, task.bwReq));
        }
    }

    /** CPU utilisation ratio for fog i ∈ [0,1] */
    public double cpuUtilisation(int fogIdx) {
        return 1.0 - cpuRemaining.getOrDefault(fogIdx, 0.0) / Constants.FOG_MIPS;
    }

    /** RAM utilisation ratio for fog i ∈ [0,1] */
    public double ramUtilisation(int fogIdx) {
        return 1.0 - ramRemaining.getOrDefault(fogIdx, 0.0) / Constants.FOG_RAM;
    }

    public double getCpuRemaining(int fogIdx) { return cpuRemaining.getOrDefault(fogIdx, 0.0); }
    public double getRamRemaining(int fogIdx) { return ramRemaining.getOrDefault(fogIdx, 0.0); }
    public double getBwRemaining(int fogIdx)  { return bwRemaining.getOrDefault(fogIdx, 0.0);  }
}
