package de.ambertation.wunderreich.utils.sdf;

//based on https://iquilezles.org/articles/ellipsoids/
public class Ellipsoid extends Shape {
    private final Pos center;
    private final Pos radia;

    public Ellipsoid(Pos center, Pos radia) {
        this.center = center;
        this.radia = radia;
    }

    @Override
    public double dist(Pos pos) {
        pos = pos.sub(center);
        double k1 = pos.div(radia).length();
        double k2 = pos.div(radia.square()).length();
        
        return k1 * (k1 - 1.0) / k2;
    }
}

