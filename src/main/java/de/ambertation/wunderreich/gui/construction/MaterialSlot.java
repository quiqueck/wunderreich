package de.ambertation.wunderreich.gui.construction;

import net.minecraft.world.inventory.Slot;

public class MaterialSlot extends Slot {
    private int page;


    public MaterialSlot(RulerContainer container, int slotIndex, int x, int y, int page) {
        super(container, slotIndex, x, y);
        this.page = page;
    }

    @Override
    public boolean isActive() {
        return page == ((RulerContainer) container).activePage();
    }
}
