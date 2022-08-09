package de.ambertation.wunderreich.gui.construction;

import de.ambertation.lib.math.sdf.SDF;
import de.ambertation.lib.math.sdf.shapes.Empty;
import de.ambertation.wunderreich.items.construction.BluePrint;
import de.ambertation.wunderreich.items.construction.BluePrintData;
import de.ambertation.wunderreich.registries.WunderreichItems;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class SDFSlot extends Slot {
    Consumer<SDFSlot> onChange;
    public final RulerContainerMenu menu;

    public SDFSlot(Container container, RulerContainerMenu menu, int firstSDFSlot, int x, int y) {
        super(container, firstSDFSlot, x, y);
        this.menu = menu;
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

    public void callOnChange(Consumer<SDFSlot> cc) {
        this.onChange = cc;
    }

    @Override
    public void setChanged() {
        SDF sdf = menu.data.SDF_DATA.get();
        ItemStack slotedItem = getItem();
        SDF newSDF = null;
        if (slotedItem != null) {
            BluePrintData bpd = BluePrintData.getBluePrintData(slotedItem);
            if (bpd != null && bpd.SDF_DATA.get() != null) {
                newSDF = bpd.SDF_DATA.get();
            }
        }
        if (sdf == null || sdf instanceof Empty) {
            menu.data.SDF_DATA.set(newSDF);
            menu.slotsChanged(container);
            container.setChanged();
        } else {

        }
        if (onChange != null) onChange.accept(this);

        menu.printInfo();

        super.setChanged();
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        if (itemStack.is(WunderreichItems.BLUE_PRINT)) return false;

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
