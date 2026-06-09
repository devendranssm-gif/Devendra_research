package healthcare.metrics;

/**
 * NetworkUsageCalculator.java
 * Computes network usage: NetworkUsage = TupleSize × HopCount
 */
public class NetworkUsageCalculator {
    /** Returns bytes transferred */
    public static long compute(long tupleSizeBytes, int hopCount) {
        return tupleSizeBytes * hopCount;
    }
    /** Converts bytes to megabytes */
    public static double toMB(long bytes) {
        return bytes / (1024.0 * 1024.0);
    }
}
