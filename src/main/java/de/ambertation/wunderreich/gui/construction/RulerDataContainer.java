package de.ambertation.wunderreich.gui.construction;

import net.minecraft.world.SimpleContainer;

public class RulerDataContainer extends SimpleContainer {
    static final int MAX_SDF_NODES = 64;
    static final int RULER_SLOT = 0;
    static final int SDF_SLOT_START = RULER_SLOT + 1;
    static final int SDF_SLOT_END = SDF_SLOT_START + MAX_SDF_NODES;

    private final RulerContainerMenu menu;

    RulerDataContainer(RulerContainerMenu menu) {
        super(SDF_SLOT_END);
        this.menu = menu;
    }
}
