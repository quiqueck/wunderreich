package de.ambertation.wunderreich.gui.construction;

import de.ambertation.wunderlib.math.sdf.SDF;
import de.ambertation.wunderlib.math.sdf.shapes.Empty;
import de.ambertation.wunderreich.items.construction.BluePrintData;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class RulerDataContainer implements Container {
    record RemoveData(ItemStack stack, int graphIdx) {
    }

    static final int MAX_SDF_NODES = 64;

    private final RulerContainerMenu menu;


    RulerDataContainer(RulerContainerMenu menu) {
        this.menu = menu;
    }


    @Override
    public int getContainerSize() {
        return MAX_SDF_NODES;
    }

    @Override
    public boolean isEmpty() {
        return menu == null || menu.data.SDF_DATA.get() == null || menu.data.SDF_DATA.get().isEmpty();
    }

    public SDF getSDF(int i) {
        if (menu == null || menu.data == null || menu.data.SDF_DATA.get() == null) return new Empty();
        SDF s = menu.data.SDF_DATA.get().getChildWithGraphIndex(i);
        if (s == null) return new Empty();
        return s;
    }

    public ItemStack create(SDF s) {
        return BluePrintData.bluePrintWithSDF(s);
    }

    @Override
    public ItemStack getItem(int i) {
        SDF s = getSDF(i);
        //this may break things, as we loose the parent?
        if (s == null || s.isEmpty()) return ItemStack.EMPTY;
        return create(s);
    }

    public int setItemAndGet(int slotIndex, @NotNull ItemStack itemStack) {
        System.out.println(Integer.toHexString(menu.data.hashCode()) + "---- [SET CONTAINER] ------------------");
        System.out.println("slot: " + slotIndex + ", " + itemStack);
        System.out.println(this.menu.data.SDF_DATA.get() + ", act=" + this.menu.data.ACTIVE_SLOT.get());

        //we need to return the graphID to ensure the correct one stays selected
        SDF currentSDF = slotIndex < 0 ? null : getSDF(slotIndex);
        if (currentSDF != null) {
            BluePrintData bd = BluePrintData.getBluePrintData(itemStack);
            SDF newSDF = null;
            if (bd != null) newSDF = bd.SDF_DATA.get();
            if (newSDF == null) newSDF = new Empty();

            SDF parent = currentSDF.getParent();
            if (parent != null) {
                //this is any child SDF, we need to chang it in the parent
                parent.replaceInputSlot(currentSDF, newSDF);
                menu.data.SDF_DATA.set(parent.getRoot());
            } else if (menu.data != null) {
                //this is the root SDF, we need to change it directly in the ruler
                menu.data.SDF_DATA.set(newSDF);
            }

            System.out.println(this.menu.data.SDF_DATA.get() + ", act=" + this.menu.data.ACTIVE_SLOT.get());
            System.out.println("---------------------------------");
            return newSDF.getGraphIndex();
        }

        System.out.println("---------------------------------");
        return -1;
    }

    public RemoveData removeItemAndGet(int slotIndex) {
        ItemStack old = getItem(slotIndex);
        int gIdx = setItemAndGet(slotIndex, ItemStack.EMPTY);
        return new RemoveData(old, gIdx);
    }

    @Override
    public void setItem(int slotIndex, @NotNull ItemStack itemStack) {
        setItemAndGet(slotIndex, itemStack);
    }

    @Override
    public ItemStack removeItem(int slotIndex, int count) {
        return removeItemNoUpdate(slotIndex);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slotIndex) {
        return removeItemAndGet(slotIndex).stack;
    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {

    }
}
