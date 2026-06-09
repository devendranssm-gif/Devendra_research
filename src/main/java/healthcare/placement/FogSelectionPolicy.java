package healthcare.placement;

import healthcare.utils.Constants;
import healthcare.utils.Location;
import org.fog.entities.FogDevice;

import java.util.List;

/**
 * FogSelectionPolicy.java
 * Selects the best fog node for a given IoT device based on proximity
 * and current resource availability.
 */
public class FogSelectionPolicy {

    /**
     * Select fog index for an IoT device at the given location.
     * Strategy: nearest fog node within coverage radius.
     *
     * @param iotLocation  IoT device location
     * @param fogLocations fog node locations
     * @return best fog index
     */
    public int selectFog(Location iotLocation, List<Location> fogLocations) {
        int    bestIdx  = 0;
        double bestDist = Double.MAX_VALUE;

        for (int i = 0; i < fogLocations.size(); i++) {
            double dist = iotLocation.distanceTo(fogLocations.get(i));
            if (dist < bestDist) {
                bestDist = dist;
                bestIdx  = i;
            }
        }
        return bestIdx;
    }

    /**
     * Check if an IoT location is within any fog's coverage radius.
     */
    public boolean hasCoverage(Location iotLocation, List<Location> fogLocations) {
        for (Location fl : fogLocations) {
            if (iotLocation.distanceTo(fl) <= Constants.COVERAGE_RADIUS) return true;
        }
        return false;
    }
}
