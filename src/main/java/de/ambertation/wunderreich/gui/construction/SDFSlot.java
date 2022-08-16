package de.ambertation.wunderreich.gui.construction;

import de.ambertation.lib.math.sdf.SDF;
import de.ambertation.lib.math.sdf.interfaces.MaterialProvider;
import de.ambertation.lib.math.sdf.shapes.Empty;
import de.ambertation.wunderreich.items.construction.BluePrint;
import de.ambertation.wunderreich.items.construction.BluePrintData;
import de.ambertation.wunderreich.network.ChangedSDFMessage;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class SDFSlot extends Slot {
    Consumer<SDFSlot> onChange;
    public final RulerContainerMenu menu;
    int currentSlot;
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

        setCurrentSlot(container.slotForGrahIndex(activeGraphIndex));
        updateInputSlots();
    }

    public SDFSlot(Container container, RulerContainerMenu menu, int x, int y) {
        super(container, 0, x, y);
        this.menu = menu;
        currentSlot = -1;
        inputs = new SDFSlot[0];
    }

    public boolean isMain() {
        return inputs != null && inputs.length > 0;
    }

    private void setSDF(SDF sdf) {
        int slot = ((RulerDataContainer) container).slotForSDF(sdf);
        if (slot < 0) slot = ((RulerDataContainer) container).addRecursive(sdf);
        this.setCurrentSlot(slot);

        for (int i = 0; i < inputs.length; i++) {
            if (sdf.getInputSlotCount() > i) {
                inputs[i].setSDF(sdf.getSlot(i));
            }
        }
    }

    private void setCurrentSlot(int newSlot) {
        if (newSlot < 0) this.currentSlot = 0;
        else if (newSlot >= container.getContainerSize()) this.currentSlot = container.getContainerSize() - 1;
        else this.currentSlot = newSlot;

        if (isMain()) {
            RulerDataContainer.SDFMap m = ((RulerDataContainer) container).getSDF(getContainerSlot());
            if (m != null && m.sdf() != null) {
                int newIdx = m.sdf().getGraphIndex();
                menu.data.ACTIVE_SLOT.set(newIdx);
                ChangedSDFMessage.INSTANCE.sendActive(newIdx);
            }
        }
    }

    public boolean isEmpty() {
        RulerDataContainer.SDFMap m = ((RulerDataContainer) container).getSDF(getContainerSlot());
        if (m != null && m.sdf() != null) {
            return m.sdf() instanceof Empty;
        }
        return true;
    }

    public void selectInput(int inputIndex) {
        if (inputs[inputIndex].isEmpty()) return;

        setCurrentSlot(inputs[inputIndex].currentSlot);
        updateInputSlots();
        setChanged();
    }

    public void selectParent() {
        RulerDataContainer.SDFMap m = ((RulerDataContainer) container).getSDF(getContainerSlot());
        if (m != null && m.sdf() != null) {
            int newSlot = ((RulerDataContainer) container).slotForSDF(m.sdf().getParent());
            if (newSlot >= 0) {
                setCurrentSlot(newSlot);
                updateInputSlots();
                setChanged();
            }
        }
    }

    public int selectNextMaterial() {
        RulerDataContainer.SDFMap m = ((RulerDataContainer) container).getSDF(getContainerSlot());
        if (m != null && m.sdf() != null && m.sdf() instanceof MaterialProvider mp) {
            int mIdx = (mp.getMaterialIndex() + 1) % RulerContainer.MAX_CATEGORIES;
            mp.setMaterialIndex(mIdx);
            ChangedSDFMessage.INSTANCE.sendMaterial(mIdx);
            setChanged();
            return mIdx;
        }
        return -1;
    }

    public int getMaterialIndex() {
        RulerDataContainer.SDFMap m = ((RulerDataContainer) container).getSDF(getContainerSlot());
        if (m != null && m.sdf() != null && m.sdf() instanceof MaterialProvider mp) {
            return mp.getMaterialIndex();
        }
        return -1;
    }


    @Override
    public int getContainerSlot() {
        return this.currentSlot;
    }

    @Override
    public void set(ItemStack itemStack) {
        this.container.setItem(getContainerSlot(), itemStack);
        updateInputSlots();
        this.setChanged();
    }

    private void updateInputSlots() {
        RulerDataContainer.SDFMap m = ((RulerDataContainer) container).getSDF(getContainerSlot());
        if (m != null && m.sdf() != null) {
            for (int i = 0; i < inputs.length; i++) {
                if (m.sdf().getInputSlotCount() > i) {
                    inputs[i].setSDF(m.sdf().getSlot(i));
                } else {
                    inputs[i].setSDF(new Empty());
                }
            }
        }
    }

    @Override
    public ItemStack getItem() {
        return this.container.getItem(getContainerSlot());
    }

    @Override
    public void initialize(ItemStack itemStack) {
        this.container.setItem(getContainerSlot(), itemStack);
        this.setChanged();
    }

    @Override
    public ItemStack remove(int i) {
        return this.container.removeItem(getContainerSlot(), i);
    }

    public void callOnChange(Consumer<SDFSlot> cc) {
        this.onChange = cc;
    }

    @Override
    public void setChanged() {
        RulerDataContainer.SDFMap m = ((RulerDataContainer) container).getSDF(currentSlot);
        if (m != null) {
            for (int i = 0; i < inputs.length; i++) {
                if (m.sdf().getInputSlotCount() > i) {
                    inputs[i].setSDF(m.sdf().getSlot(i));
                }
            }
        }
        container.setChanged();

//        SDF sdf = menu.data.SDF_DATA.get();
//        ItemStack slotedItem = getItem();
//        SDF newSDF = null;
//        if (slotedItem != null) {
//            BluePrintData bpd = BluePrintData.getBluePrintData(slotedItem);
//            if (bpd != null && bpd.SDF_DATA.get() != null) {
//                newSDF = bpd.SDF_DATA.get();
//            }
//        }
//        if (sdf == null || sdf instanceof Empty) {
//            menu.data.SDF_DATA.set(newSDF);
//            menu.slotsChanged(container);
//            container.setChanged();
//        } else {
//
//        }
        if (onChange != null) onChange.accept(this);

        menu.printInfo();

        super.setChanged();
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
                SDF sdf = data.SDF_DATA.get();
                return !sdf.hasInputs();
            }
        }

        return super.mayPickup(player);
    }
}
