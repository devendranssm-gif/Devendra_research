package healthcare.metrics;

import healthcare.utils.Constants;

/**
 * EnergyCalculator.java
 * Calculates energy consumption for fog nodes using the linear power model.
 * Energy(J) = BusyPower × ActiveTime + IdlePower × IdleTime
 */
public class EnergyCalculator {

    /** Energy in Joules for a fog node over given active/idle durations (seconds) */
    public static double compute(double activeTimeSec, double idleTimeSec) {
        return (Constants.FOG_BUSY_POWER * activeTimeSec)
             + (Constants.FOG_IDLE_POWER  * idleTimeSec);
    }

    /**
     * Estimate energy for an IoT scenario.
     * Each fog handles (numIoT / numFogs) tasks over the simulation.
     */
    public static double estimateScenarioEnergy(int numIoT, int numFogs, double simTimeSec) {
        double tasksPerFog   = (double) numIoT / numFogs;
        double avgActiveTime = tasksPerFog * Constants.TUPLE_GEN_RATE;
        double idleTime      = Math.max(0, simTimeSec - avgActiveTime);
        return numFogs * compute(avgActiveTime, idleTime);
    }
}
