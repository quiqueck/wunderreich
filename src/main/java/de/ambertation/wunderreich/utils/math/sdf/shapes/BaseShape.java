package de.ambertation.wunderreich.utils.math.sdf.shapes;

import de.ambertation.wunderreich.utils.math.Pos;
import de.ambertation.wunderreich.utils.math.sdf.SDF;

public abstract class BaseShape extends SDF {
    public final Pos center;

    public BaseShape(Pos center) {
        this.center = center;
    }
}
