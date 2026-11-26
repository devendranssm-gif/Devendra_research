package healthcare.monitoring;

import java.util.HashMap;
import java.util.Map;

/**
 * LoadMonitor.java
 * Monitors task queue depth and load distribution across fog nodes.
 */
public class LoadMonitor {

    private final Map<Integer, Integer> taskCount = new HashMap<>();
    private final int numFogs;

    public LoadMonitor(int numFogs) {
        this.numFogs = numFogs;
        for (int i = 0; i < numFogs; i++) taskCount.put(i, 0);
    }

    public void increment(int fogIdx) {
        taskCount.merge(fogIdx, 1, Integer::sum);
    }
    public void decrement(int fogIdx) {
        taskCount.merge(fogIdx, -1, Integer::sum);
    }

    public int getLoad(int fogIdx) { return taskCount.getOrDefault(fogIdx, 0); }

    public int maxLoad() {
        return taskCount.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    }
    public double avgLoad() {
        return taskCount.values().stream().mapToInt(Integer::intValue).average().orElse(0);
    }

    /** Index of the least loaded fog node */
    public int leastLoaded() {
        return taskCount.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);
    }
}
