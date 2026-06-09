package healthcare.optimization;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * TaskPrioritizer.java
 * Assigns priority scores to healthcare tasks based on sensor type,
 * deadline urgency, and resource requirements for DILP input ordering.
 */
public class TaskPrioritizer {

    public enum SensorType {
        ECG(5), HEART_RATE(4), BLOOD_PRESSURE(4), BODY_TEMPERATURE(3), OXYGEN_SATURATION(5);

        final int basePriority;
        SensorType(int p) { this.basePriority = p; }
    }

    public static class Task {
        public final int    id;
        public final String name;
        public final SensorType sensorType;
        public double cpuReq;
        public double ramReq;
        public double bwReq;
        public double deadline;   // ms
        public double priority;   // computed
        public int    assignedFog = -1;

        public Task(int id, String name, SensorType type,
                    double cpu, double ram, double bw, double deadline) {
            this.id         = id;
            this.name       = name;
            this.sensorType = type;
            this.cpuReq     = cpu;
            this.ramReq     = ram;
            this.bwReq      = bw;
            this.deadline   = deadline;
        }
    }

    /**
     * Compute composite priority for each task.
     * priority = sensorBasePriority * urgencyFactor / (cpuReq + 1)
     */
    public List<Task> prioritize(List<Task> tasks) {
        double now = System.currentTimeMillis();
        for (Task t : tasks) {
            double urgency = 1.0 / Math.max(1.0, t.deadline - now);
            t.priority = t.sensorType.basePriority * urgency * 1e6 / (t.cpuReq + 1.0);
        }
        // Sort descending by priority
        tasks.sort(Comparator.comparingDouble((Task t) -> t.priority).reversed());
        return tasks;
    }

    /**
     * Generate a default set of tasks for N IoT devices.
     */
    public List<Task> generateTasks(int numDevices) {
        List<Task> tasks = new ArrayList<>();
        SensorType[] types = SensorType.values();
        for (int i = 0; i < numDevices; i++) {
            SensorType type = types[i % types.length];
            tasks.add(new Task(
                i,
                "Task_" + i + "_" + type.name(),
                type,
                500 + (i % 5) * 200,   // cpuReq MIPS
                128 + (i % 4) * 64,    // ramReq MB
                100 + (i % 3) * 50,    // bwReq Mbps
                System.currentTimeMillis() + 500 + (i % 10) * 100
            ));
        }
        return prioritize(tasks);
    }
}
