package de.ambertation.wunderreich.gui.construction;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SDFSlot extends Slot {
    public SDFSlot(Container container, int firstSDFSlot, int x, int y) {
        super(container, firstSDFSlot, x, y);
    }


    @Override
    public int getContainerSlot() {
        return super.getContainerSlot();
    }

    @Override
    public void set(ItemStack itemStack) {
        super.set(itemStack);
    }

    @Override
    public ItemStack getItem() {
        return super.getItem();
    }

    @Override
    public void initialize(ItemStack itemStack) {
        super.initialize(itemStack);
    }

    @Override
    public ItemStack remove(int i) {
        return super.remove(i);
    }
}
