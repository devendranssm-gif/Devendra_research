package healthcare.monitoring;

import java.util.ArrayList;
import java.util.List;

/**
 * UtilizationTracker.java
 * Time-series recorder for fog node resource utilisation used in post-simulation analysis.
 */
public class UtilizationTracker {

    public static class Snapshot {
        public final double simTime;
        public final int    fogIdx;
        public final double cpuUtil;
        public final double ramUtil;

        Snapshot(double simTime, int fogIdx, double cpu, double ram) {
            this.simTime = simTime; this.fogIdx = fogIdx;
            this.cpuUtil = cpu;     this.ramUtil = ram;
        }
    }

    private final List<Snapshot> snapshots = new ArrayList<>();

    public void record(double simTime, int fogIdx, double cpuUtil, double ramUtil) {
        snapshots.add(new Snapshot(simTime, fogIdx, cpuUtil, ramUtil));
    }

    public List<Snapshot> getSnapshots()  { return snapshots; }
    public int            getCount()      { return snapshots.size(); }

    public double avgCpuUtil(int fogIdx) {
        return snapshots.stream()
                .filter(s -> s.fogIdx == fogIdx)
                .mapToDouble(s -> s.cpuUtil)
                .average().orElse(0);
    }
}
