package de.ambertation.wunderreich.utils.math.sdf;

public abstract class SDFOperation extends SDF {
    protected SDFOperation(SDF sdf) {
        this(sdf, 0);
    }

    protected SDFOperation(SDF sdf, int additionalInputSlots) {
        super(1 + additionalInputSlots);
        setSlotSilent(0, sdf);
    }

    @Override
    public String toString() {
        return "(" + getFirst() + ")";
    }

    public SDF getFirst() {
        return getSlot(0);
    }

    public void setFirst(SDF a) {
        setSlot(0, a);
    }
}
