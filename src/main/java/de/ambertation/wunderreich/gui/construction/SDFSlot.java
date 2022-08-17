package de.ambertation.wunderreich.gui.construction;

import de.ambertation.lib.math.sdf.SDF;
import de.ambertation.lib.math.sdf.interfaces.MaterialProvider;
import de.ambertation.wunderreich.items.construction.BluePrint;
import de.ambertation.wunderreich.items.construction.BluePrintData;
import de.ambertation.wunderreich.network.ChangedSDFMessage;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class SDFSlot extends Slot {
    @FunctionalInterface
    interface ChangedActiveGraphIndex {
        void didChange(int newIndex);
    }

    private ChangedActiveGraphIndex onActiveGraphIndexChange;
    public final RulerContainerMenu menu;
    private final SDFSlot[] inputs;
    private SDFSlot parentSlot;

    public SDFSlot(
            RulerDataContainer container,
            RulerContainerMenu menu,
            int x,
            int y,
            SDFSlot inp0,
            SDFSlot inp1,
            int activeGraphIndex
    ) {
        super(container, 0, x, y);
        this.menu = menu;

        inp0.parentSlot = this;
        inp1.parentSlot = this;
        inputs = new SDFSlot[]{inp0, inp1};
    }

    public SDFSlot(Container container, RulerContainerMenu menu, int x, int y) {
        super(container, 0, x, y);
        this.menu = menu;
        inputs = new SDFSlot[0];
    }

    public boolean isMaster() {
        return parentSlot == null;
    }

    public int inputSlotInMaster() {
        if (isMaster()) return -1;
        for (int i = 0; i < parentSlot.inputs.length; i++) {
            if (parentSlot.inputs[i] == this) return i;
        }
        return -1;
    }

    public RulerDataContainer container() {
        return (RulerDataContainer) container;
    }

    public void setOnActiveGraphIndexChange(ChangedActiveGraphIndex callback) {
        this.onActiveGraphIndexChange = callback;
    }

    private void setActiveGraphIndex(int newIdx) {
        int oldIdx = menu.data.ACTIVE_SLOT.get();
        if (newIdx != oldIdx) {
            menu.data.ACTIVE_SLOT.set(newIdx);
            if (onActiveGraphIndexChange != null) {
                onActiveGraphIndexChange.didChange(newIdx);
            }
        }
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public ItemStack getItem() {
        return container.getItem(getContainerSlot());
    }

    @Override
    public void set(@NotNull ItemStack itemStack) {
        int idx = this.container().setItemAndGet(getContainerSlot(), itemStack);
        if (isMaster()) {
            setActiveGraphIndex(idx);
        }
        this.setChanged();
    }

    @Override
    public void initialize(@NotNull ItemStack itemStack) {
        //this.container.setItem(getContainerSlot(), itemStack);
        this.setChanged();
    }

    @Override
    public ItemStack remove(int i) {
        RulerDataContainer.RemoveData res = this.container().removeItemAndGet(getContainerSlot());
        if (isMaster()) {
            setActiveGraphIndex(res.graphIdx());
        }
        this.setChanged();
        return res.stack();
    }

    @Override
    public int getContainerSlot() {
        if (isMaster()) {
            return menu.data.ACTIVE_SLOT.get();
        }

        int inputSlot = inputSlotInMaster();
        SDF parentSDF = container().getSDF(parentSlot.getContainerSlot());
        if (inputSlot < parentSDF.getInputSlotCount()) return parentSDF.getSlot(inputSlot).getGraphIndex();
        return -1;
    }

    @Override
    public boolean isActive() {
        return getContainerSlot() >= 0;
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        if (itemStack.getItem() instanceof BluePrint) {
            BluePrintData data = BluePrintData.getBluePrintData(itemStack);
            return (data != null && data.SDF_DATA.get() != null);
        }

        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        ItemStack itemStack = getItem();
        if (itemStack.getItem() instanceof BluePrint) {
            BluePrintData data = BluePrintData.getBluePrintData(itemStack);
            if (data != null && data.SDF_DATA.get() != null) {
                return true;
            }
        }

        return super.mayPickup(player);
    }

    public boolean hasInput(int inputSlot) {
        if (inputSlot < 0 || inputSlot >= inputs.length) return false;
        SDF s = getActiveSdf();
        if (s != null && inputSlot >= s.getInputSlotCount()) return false;

        return true;
    }

    public void selectInput(int inputSlot) {
        if (hasInput(inputSlot))
            setActiveGraphIndex(inputs[inputSlot].getContainerSlot());
    }

    public SDF getActiveSdf() {
        return container().getSDF(getContainerSlot());
    }

    public void selectParent() {
        SDF s = getActiveSdf();
        if (s != null && s.getParent() != null) {
            setActiveGraphIndex(s.getParent().getGraphIndex());
        }
    }

    public int selectNextMaterialOnClient() {
        SDF s = getActiveSdf();
        if (s != null && s instanceof MaterialProvider mp) {
            int idx = (mp.getMaterialIndex() + 1) % RulerContainer.MAX_CATEGORIES;
            mp.setMaterialIndex(idx);
            ChangedSDFMessage.INSTANCE.sendMaterial(idx);
            return idx;
        }
        return -1;
    }

    public int getMaterialIndex() {
        SDF s = getActiveSdf();
        if (s != null && s instanceof MaterialProvider mp) {
            return mp.getMaterialIndex();
        }
        return -1;
    }

    public boolean hasMaterial() {
        SDF s = getActiveSdf();
        if (s != null && s instanceof MaterialProvider) {
            return true;
        }
        return false;
    }
}
