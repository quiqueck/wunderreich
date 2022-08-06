package de.ambertation.wunderreich.gui.construction;

import net.minecraft.world.SimpleContainer;

public class RulerContainer extends SimpleContainer {
    static final int MAX_SDF_NODES = 64;
    static final int MAX_CATEGORIES = 8;
    static final int ITEMS_PER_CATEGORY = 2 * 9;
    static final int INVENTORY_SIZE = 3 * 9;
    static final int HOTBAR_SIZE = 9;

    static final int INV_SLOT_START = 0;
    static final int INV_SLOT_END = INV_SLOT_START + INVENTORY_SIZE;
    static final int HOTBAR_SLOT_START = INV_SLOT_END;
    static final int HOTBAR_SLOT_END = HOTBAR_SLOT_START + HOTBAR_SIZE;

    static final int CATEGORIES_SLOT_START = 0;
    static final int CATEGORIES_SLOT_END = CATEGORIES_SLOT_START + (MAX_CATEGORIES * ITEMS_PER_CATEGORY);

    static final int SDF_SLOT_START = CATEGORIES_SLOT_END;
    static final int SDF_SLOT_END = SDF_SLOT_START + MAX_SDF_NODES;

    public RulerContainer() {
        super(SDF_SLOT_END);
    }

    private int activePage = 0;

    public int activePage() {
        return activePage;
    }

    public void setActivePage(int page) {
        activePage = page;
    }
}
