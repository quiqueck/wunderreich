package de.ambertation.wunderreich.utils.math.sdf;

public abstract class SDFBinaryOperation extends SDFOperation {
    public SDFBinaryOperation(SDF a, SDF b) {
        super(a, 1);
        setSlotSilent(1, b);
    }

    @Override
    public String toString() {
        return "(" + getFirst() + ", " + getSecond() + ")";
    }

    public SDF getSecond() {
        return getSlot(1);
    }

    public void setSecond(SDF b) {
        setSlot(1, b);
    }
}
