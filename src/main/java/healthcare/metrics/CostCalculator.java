package healthcare.metrics;

import healthcare.utils.Constants;

/**
 * CostCalculator.java
 * Computes execution cost: Cost = ExecutionTime × CostPerSec
 */
public class CostCalculator {
    public static double compute(double executionTimeMs) {
        return (executionTimeMs / 1000.0) * Constants.COST_PER_SEC;
    }
}
