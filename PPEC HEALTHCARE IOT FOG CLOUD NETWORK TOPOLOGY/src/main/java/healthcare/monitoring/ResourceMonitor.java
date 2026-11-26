package healthcare.monitoring;

import healthcare.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * ResourceMonitor.java
 * Tracks resource utilisation snapshots across all fog nodes.
 */
public class ResourceMonitor {

    private final int numFogs;
    private final Map<Integer, Double> cpuUtil = new HashMap<>();
    private final Map<Integer, Double> ramUtil = new HashMap<>();
    private final Map<Integer, Double> bwUtil  = new HashMap<>();

    public ResourceMonitor(int numFogs) {
        this.numFogs = numFogs;
        for (int i = 0; i < numFogs; i++) {
            cpuUtil.put(i, 0.0);
            ramUtil.put(i, 0.0);
            bwUtil.put(i, 0.0);
        }
    }

    public void updateCpu(int fogIdx, double utilRatio) { cpuUtil.put(fogIdx, utilRatio); }
    public void updateRam(int fogIdx, double utilRatio) { ramUtil.put(fogIdx, utilRatio); }
    public void updateBw(int fogIdx,  double utilRatio) { bwUtil.put(fogIdx, utilRatio);  }

    public double getCpuUtil(int fogIdx) { return cpuUtil.getOrDefault(fogIdx, 0.0); }
    public double getRamUtil(int fogIdx) { return ramUtil.getOrDefault(fogIdx, 0.0); }

    public double avgCpuUtil() {
        return cpuUtil.values().stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }
    public double avgRamUtil() {
        return ramUtil.values().stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }
    public int getNumFogs() { return numFogs; }
}
