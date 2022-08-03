package de.ambertation.wunderreich.utils.math.sdf;

public abstract class SDFBinaryOperation extends SDFOperation {
    protected final SDF b;

    public SDFBinaryOperation(SDF a, SDF b) {
        super(a);
        this.b = b;
    }
}
