package healthcare.optimization;

import healthcare.utils.Constants;
import healthcare.utils.RandomGenerator;

import java.util.List;
import java.util.logging.Logger;

/**
 * DILPOptimizer.java
 * Dynamic Integer Linear Programming optimizer for resource allocation,
 * task scheduling, load balancing, and computation offloading.
 *
 * The ILP formulation:
 *   Minimise:  Σ_i Σ_j x_ij * latency_ij
 *   Subject to:
 *     Σ_j x_ij = 1           ∀ i   (each task on exactly one fog)
 *     Σ_i x_ij * cpu_i ≤ C_j ∀ j   (cpu capacity)
 *     Σ_i x_ij * ram_i ≤ R_j ∀ j   (ram capacity)
 *     Σ_i x_ij * bw_i  ≤ B_j ∀ j   (bandwidth capacity)
 *     x_ij ∈ {0,1}
 *
 * Solved via Branch-and-Bound (BranchAndBoundSolver).
 */
public class DILPOptimizer {

    private static final Logger LOG = Logger.getLogger(DILPOptimizer.class.getName());

    private final TaskPrioritizer  prioritizer;
    private final ResourceAllocator allocator;
    private final int numFogs;
    private int[] lastAssignment;

    public DILPOptimizer(int numFogs) {
        this.numFogs     = numFogs;
        this.prioritizer = new TaskPrioritizer();
        this.allocator   = new ResourceAllocator(numFogs);
    }

    /**
     * Run a full DILP optimisation cycle for the given number of IoT devices.
     *
     * @param numIoT number of IoT devices (= number of tasks in this cycle)
     * @return task→fog assignment array
     */
    public int[] optimise(int numIoT) {
        LOG.info("[DILP] Starting optimisation for " + numIoT + " tasks on " + numFogs + " fogs.");

        allocator.reset();

        // 1. Generate and prioritise tasks
        List<TaskPrioritizer.Task> tasks = prioritizer.generateTasks(numIoT);
        int n = tasks.size();

        // 2. Build latency matrix (estimated ms, based on resource mismatch)
        double[][] latencyMatrix = buildLatencyMatrix(tasks, n);

        // 3. Extract resource requirement vectors
        double[] cpuReq = new double[n];
        double[] ramReq = new double[n];
        double[] bwReq  = new double[n];
        for (int i = 0; i < n; i++) {
            cpuReq[i] = tasks.get(i).cpuReq;
            ramReq[i] = tasks.get(i).ramReq;
            bwReq[i]  = tasks.get(i).bwReq;
        }

        // 4. Build capacity vectors
        double[] cpuCap = new double[numFogs];
        double[] ramCap = new double[numFogs];
        double[] bwCap  = new double[numFogs];
        for (int j = 0; j < numFogs; j++) {
            cpuCap[j] = Constants.FOG_MIPS;
            ramCap[j] = Constants.FOG_RAM;
            bwCap[j]  = Constants.FOG_UPLINK_BW;
        }

        // 5. Solve ILP via B&B (cap at manageable size for publication demo)
        int solvableN = Math.min(n, 20); // B&B is NP-hard; cap for tractability
        double[][] latencySlice = new double[solvableN][numFogs];
        double[] cpuReqSlice = new double[solvableN];
        double[] ramReqSlice = new double[solvableN];
        double[] bwReqSlice  = new double[solvableN];
        for (int i = 0; i < solvableN; i++) {
            latencySlice[i] = latencyMatrix[i];
            cpuReqSlice[i]  = cpuReq[i];
            ramReqSlice[i]  = ramReq[i];
            bwReqSlice[i]   = bwReq[i];
        }

        BranchAndBoundSolver solver = new BranchAndBoundSolver(
                solvableN, numFogs,
                latencySlice, cpuReqSlice, ramReqSlice, bwReqSlice,
                cpuCap, ramCap, bwCap
        );
        int[] bbAssignment = solver.solve();

        // 6. For remaining tasks (>solvableN), fall back to greedy round-robin
        int[] fullAssignment = new int[n];
        for (int i = 0; i < solvableN; i++) fullAssignment[i] = bbAssignment[i];
        for (int i = solvableN; i < n; i++) fullAssignment[i] = i % numFogs;

        // 7. Apply to allocator
        allocator.applyAssignment(tasks, fullAssignment);

        lastAssignment = fullAssignment;
        LOG.info(String.format("[DILP] Optimisation complete. Best cost=%.2f, B&B nodes=%d",
                solver.getBestCost(), solver.getNodesExplored()));

        return fullAssignment;
    }

    /**
     * Build the latency matrix: latency[task][fog].
     * Estimated as: base_latency + cpu_penalty + ram_penalty
     */
    private double[][] buildLatencyMatrix(List<TaskPrioritizer.Task> tasks, int n) {
        double[][] matrix = new double[n][numFogs];
        for (int i = 0; i < n; i++) {
            TaskPrioritizer.Task task = tasks.get(i);
            for (int j = 0; j < numFogs; j++) {
                double cpuUtil = allocator.cpuUtilisation(j);
                double ramUtil = allocator.ramUtilisation(j);
                // Base propagation latency + contention penalty
                double base    = 5.0 + RandomGenerator.nextDouble(1.0, 3.0);
                double cpuPen  = cpuUtil * 20.0;
                double ramPen  = ramUtil * 10.0;
                double bwPen   = (task.bwReq / Constants.FOG_UPLINK_BW) * 5.0;
                matrix[i][j]   = base + cpuPen + ramPen + bwPen;
            }
        }
        return matrix;
    }

    public ResourceAllocator getAllocator() { return allocator; }
    public int[] getLastAssignment()       { return lastAssignment; }

    /** Average utilisation across all fog nodes (CPU) */
    public double avgCpuUtilisation() {
        double sum = 0;
        for (int j = 0; j < numFogs; j++) sum += allocator.cpuUtilisation(j);
        return sum / numFogs;
    }
}
