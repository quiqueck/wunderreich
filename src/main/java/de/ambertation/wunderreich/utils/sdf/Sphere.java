package de.ambertation.wunderreich.utils.sdf;

public class Sphere extends Shape {
    private final Pos center;
    private final double radius;

    public Sphere(Pos center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    @Override
    public double dist(Pos pos) {
        return pos.sub(center).length() - radius;
    }
}
