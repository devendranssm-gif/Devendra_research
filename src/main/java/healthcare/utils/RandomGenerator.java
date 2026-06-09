package healthcare.utils;

import java.util.Random;

/**
 * RandomGenerator.java
 * Deterministic random helpers for reproducible simulation experiments.
 */
public class RandomGenerator {

    private static final long SEED = 42L;
    private static final Random rng = new Random(SEED);

    /** Random double in [min, max] */
    public static double nextDouble(double min, double max) {
        return min + (max - min) * rng.nextDouble();
    }

    /** Random int in [min, max] inclusive */
    public static int nextInt(int min, int max) {
        return min + rng.nextInt(max - min + 1);
    }

    /** Random Location within the simulation area */
    public static Location randomLocation() {
        return new Location(
            nextDouble(0, Constants.SIMULATION_AREA_WIDTH),
            nextDouble(0, Constants.SIMULATION_AREA_HEIGHT)
        );
    }

    /** Grid-based fog location (even spread across 3000×3000 area) */
    public static Location fogGridLocation(int fogIndex, int totalFogs) {
        int cols = (int) Math.ceil(Math.sqrt(totalFogs));
        int row  = fogIndex / cols;
        int col  = fogIndex % cols;
        double spacing = Constants.SIMULATION_AREA_WIDTH / (double) cols;
        return new Location(
            spacing * col + spacing / 2.0,
            spacing * row + spacing / 2.0
        );
    }

    /** IoT location clustered around a fog node centre */
    public static Location iotLocationNearFog(Location fogCentre) {
        double angle  = nextDouble(0, 2 * Math.PI);
        double radius = nextDouble(50, Constants.COVERAGE_RADIUS * 0.8);
        double x = Math.max(0, Math.min(Constants.SIMULATION_AREA_WIDTH,
                fogCentre.getX() + radius * Math.cos(angle)));
        double y = Math.max(0, Math.min(Constants.SIMULATION_AREA_HEIGHT,
                fogCentre.getY() + radius * Math.sin(angle)));
        return new Location(x, y);
    }

    private RandomGenerator() {}
}
