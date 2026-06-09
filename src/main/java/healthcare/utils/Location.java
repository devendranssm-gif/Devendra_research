package healthcare.utils;

/**
 * Location.java
 * Represents a 2D geographic coordinate within the simulation area.
 */
public class Location {

    private final double x;
    private final double y;

    public Location(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    /**
     * Euclidean distance between two locations.
     */
    public double distanceTo(Location other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Whether this location is within radius of a centre point.
     */
    public boolean isWithinRadius(Location centre, double radius) {
        return distanceTo(centre) <= radius;
    }

    @Override
    public String toString() {
        return String.format("(%.1f, %.1f)", x, y);
    }
}
