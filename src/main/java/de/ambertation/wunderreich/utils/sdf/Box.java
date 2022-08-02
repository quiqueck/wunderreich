package de.ambertation.wunderreich.utils.sdf;

// https://iquilezles.org/articles/distfunctions/
public class Box extends Shape {
    private final Pos center;
    private final Pos radia;

    public Box(Pos center, Pos radia) {
        this.center = center;
        this.radia = radia;
    }

    @Override
    public double dist(Pos pos) {
        Pos q = pos.sub(center).abs().sub(radia);
        return q.max(0.0).length() + Math.min(Math.max(q.x, Math.max(q.y, q.z)), 0.0);
    }
}
