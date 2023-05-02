package de.ambertation.wunderreich.gui.construction;

import org.wunder.lib.math.sdf.SDF;
import org.wunder.lib.math.sdf.interfaces.MaterialProvider;
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

    @FunctionalInterface
    interface ChangedContent {
        void didChange();
    }

    private ChangedActiveGraphIndex onActiveGraphIndexChange;
    private ChangedContent onChangedContent;
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

    public void setOnChangedContent(ChangedContent onChangedContent) {
        this.onChangedContent = onChangedContent;
    }

    private void setActiveGraphIndex(int newIdx) {
        System.out.println(Integer.toHexString(menu.data.hashCode()) + "---- [CHG ACTIVE] ------------------");
        int oldIdx = menu.data.ACTIVE_SLOT.get();
        System.out.println(oldIdx + " -> " + newIdx);
        if (newIdx != oldIdx) {
            menu.data.ACTIVE_SLOT.set(newIdx);
            if (onActiveGraphIndexChange != null) {
                onActiveGraphIndexChange.didChange(newIdx);
            }
            System.out.println(this.menu.data.SDF_DATA.get() + ", act=" + getContainerSlot() + " - " + this.menu.data.ACTIVE_SLOT.get());
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
        System.out.println(Integer.toHexString(menu.data.hashCode()) + "---- [SET] ------------------");
        System.out.println(getContainerSlot() + ": " + itemStack + ", master:" + isMaster());
        System.out.println(this.menu.data.SDF_DATA.get() + ", act=" + getContainerSlot() + " - " + this.menu.data.ACTIVE_SLOT.get());
        int idx = this.container().setItemAndGet(getContainerSlot(), itemStack);
        System.out.println(this.menu.data.SDF_DATA.get() + ", idx=" + idx + ", act=" + getContainerSlot() + " - " + this.menu.data.ACTIVE_SLOT.get());
        if (isMaster()) {
            setActiveGraphIndex(idx);
            System.out.println(this.menu.data.SDF_DATA.get() + ", act=" + getContainerSlot() + " - " + this.menu.data.ACTIVE_SLOT.get());
        }
        this.setChanged();
    }

    @Override
    public ItemStack remove(int i) {
        System.out.println(Integer.toHexString(menu.data.hashCode()) + "---- [REMOVE] ------------------");
        System.out.println(i);
        System.out.println(this.menu.data.SDF_DATA.get() + ", act=" + getContainerSlot() + " - " + this.menu.data.ACTIVE_SLOT.get());
        RulerDataContainer.RemoveData res = this.container().removeItemAndGet(getContainerSlot());
        System.out.println(this.menu.data.SDF_DATA.get() + ", idx=" + res.graphIdx() + ", act=" + getContainerSlot() + " - " + this.menu.data.ACTIVE_SLOT.get());
        if (isMaster()) {
            setActiveGraphIndex(res.graphIdx());
            System.out.println(this.menu.data.SDF_DATA.get() + ", act=" + getContainerSlot() + " - " + this.menu.data.ACTIVE_SLOT.get());
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
    public void setChanged() {

        if (isMaster()) {
            super.setChanged();
            if (onChangedContent != null) onChangedContent.didChange();
        } else if (parentSlot != null) {
            parentSlot.setChanged();
        }
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
        System.out.println(Integer.toHexString(menu.data.hashCode()) + "---- [SELECT INPUT] ------------------");
        System.out.println(inputSlot + "  " + hasInput(inputSlot) + ", act=" + getContainerSlot());
        if (hasInput(inputSlot))
            setActiveGraphIndex(inputs[inputSlot].getContainerSlot());
    }

    public SDF getActiveSdf() {
        return container().getSDF(getContainerSlot());
    }

    public void printDebugInfo() {
        System.out.println(Integer.toHexString(menu.data.hashCode()) + "---- [DEBUG] ------------------");
        System.out.println(this.menu.data.SDF_DATA.get() + ", act=" + this.menu.data.ACTIVE_SLOT.get());
        System.out.println("slot=" + getContainerSlot() + ", master:" + isMaster());
        for (int i = 0; i < inputs.length; i++)
            System.out.println("slot=" + inputs[i].getContainerSlot() + ", input:" + i);
        System.out.println("--------------------------------");
    }

    public void selectParent() {
        System.out.println(Integer.toHexString(menu.data.hashCode()) + "---- [SELECT PARENT] ------------------");
        System.out.println("act=" + getContainerSlot());

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
            ChangedSDFMessage.INSTANCE.sendMaterial(menu, idx);
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
