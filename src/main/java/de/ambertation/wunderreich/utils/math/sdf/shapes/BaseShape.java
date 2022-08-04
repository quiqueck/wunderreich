package de.ambertation.wunderreich.utils.math.sdf.shapes;

import de.ambertation.wunderreich.utils.math.Float3;
import de.ambertation.wunderreich.utils.math.sdf.SDF;

public abstract class BaseShape extends SDF {
    private Float3 center;

    public BaseShape(Float3 center) {
        super(0);
        this.center = center;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    public Float3 getCenter() {
        return center;
    }

    public void setCenter(Float3 center) {
        this.center = center;
        this.emitChangeEvent();
    }
}
