package healthcare.optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * BranchAndBoundSolver.java
 * Implements a Branch-and-Bound solver used by the DILP optimizer to find
 * optimal integer task-to-fog assignments.
 *
 * Problem: assign N tasks to M fog nodes minimising total weighted latency
 * subject to CPU, RAM, and bandwidth capacity constraints.
 */
public class BranchAndBoundSolver {

    private static final Logger LOG = Logger.getLogger(BranchAndBoundSolver.class.getName());

    private final int numTasks;
    private final int numNodes;

    /** latency[i][j] = estimated ms to run task i on fog j */
    private final double[][] latency;

    /** cpuReq[i] = MIPS required by task i */
    private final double[] cpuReq;
    /** ramReq[i] = MB required by task i */
    private final double[] ramReq;
    /** bwReq[i]  = Mbps required by task i */
    private final double[] bwReq;

    /** Capacity per fog node */
    private final double[] cpuCap;
    private final double[] ramCap;
    private final double[] bwCap;

    // B&B state
    private int[]  bestAssignment;
    private double bestCost;
    private int    nodesExplored;

    public BranchAndBoundSolver(int numTasks, int numNodes,
                                 double[][] latency,
                                 double[] cpuReq, double[] ramReq, double[] bwReq,
                                 double[] cpuCap, double[] ramCap, double[] bwCap) {
        this.numTasks  = numTasks;
        this.numNodes  = numNodes;
        this.latency   = latency;
        this.cpuReq    = cpuReq;
        this.ramReq    = ramReq;
        this.bwReq     = bwReq;
        this.cpuCap    = cpuCap;
        this.ramCap    = ramCap;
        this.bwCap     = bwCap;
    }

    /**
     * Run branch-and-bound and return the optimal task→fog mapping.
     * Returns int[] where result[taskIndex] = fogNodeIndex.
     */
    public int[] solve() {
        bestAssignment = new int[numTasks];
        bestCost       = Double.MAX_VALUE;
        nodesExplored  = 0;

        double[] cpuUsed = new double[numNodes];
        double[] ramUsed = new double[numNodes];
        double[] bwUsed  = new double[numNodes];

        branchAndBound(0, new int[numTasks], cpuUsed, ramUsed, bwUsed, 0.0);

        LOG.info(String.format("[B&B] Solved %d tasks → %d nodes. Explored %d nodes. Best cost=%.2f",
                numTasks, numNodes, nodesExplored, bestCost));
        return bestAssignment;
    }

    private void branchAndBound(int taskIdx, int[] assignment,
                                 double[] cpuUsed, double[] ramUsed, double[] bwUsed,
                                 double currentCost) {

        nodesExplored++;

        if (taskIdx == numTasks) {
            if (currentCost < bestCost) {
                bestCost = currentCost;
                bestAssignment = assignment.clone();
            }
            return;
        }

        // Pruning: lower bound estimate is currentCost + min remaining latency
        double lb = currentCost + lowerBound(taskIdx);
        if (lb >= bestCost) return; // prune

        for (int fog = 0; fog < numNodes; fog++) {
            // Feasibility check
            if (cpuUsed[fog] + cpuReq[taskIdx] > cpuCap[fog]) continue;
            if (ramUsed[fog] + ramReq[taskIdx] > ramCap[fog]) continue;
            if (bwUsed[fog]  + bwReq[taskIdx]  > bwCap[fog])  continue;

            // Branch
            assignment[taskIdx] = fog;
            cpuUsed[fog] += cpuReq[taskIdx];
            ramUsed[fog] += ramReq[taskIdx];
            bwUsed[fog]  += bwReq[taskIdx];

            branchAndBound(taskIdx + 1, assignment, cpuUsed, ramUsed, bwUsed,
                    currentCost + latency[taskIdx][fog]);

            // Backtrack
            cpuUsed[fog] -= cpuReq[taskIdx];
            ramUsed[fog] -= ramReq[taskIdx];
            bwUsed[fog]  -= bwReq[taskIdx];
        }
    }

    /** Lower bound: sum of minimum latency for each unassigned task */
    private double lowerBound(int fromTask) {
        double lb = 0.0;
        for (int t = fromTask; t < numTasks; t++) {
            double minLat = Double.MAX_VALUE;
            for (int f = 0; f < numNodes; f++) {
                if (latency[t][f] < minLat) minLat = latency[t][f];
            }
            lb += minLat;
        }
        return lb;
    }

    public double getBestCost()  { return bestCost;       }
    public int    getNodesExplored() { return nodesExplored; }
}
