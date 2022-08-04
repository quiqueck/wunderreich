package de.ambertation.wunderreich.items.construction;

import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.utils.math.sdf.SDF;
import de.ambertation.wunderreich.utils.nbt.CachedNBTValue;
import de.ambertation.wunderreich.utils.nbt.NbtTagHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class BluePrintData {
    private static final String SDF_TAG = "sdf";
    private static final String BLUEPRINT_DATA_TAG = "blueprint";

    public final CachedNBTValue<SDF, Tag> SDF_DATA;

    public BluePrintData(CompoundTag baseTag) {
        SDF_DATA = new CachedNBTValue<>(
                baseTag,
                SDF_TAG,
                NbtTagHelper::readSDF,
                NbtTagHelper::writeSDF,
                this::sdfObjectDidChange
        );
    }


    public void sdfObjectDidChange(SDF old, SDF fresh) {
        if (old == fresh) return;

        if (old != null) old.removeChangeListener(this::sdfContentDidChange);
        if (fresh != null) fresh.addChangeListener(this::sdfContentDidChange);
    }

    void sdfContentDidChange(SDF sdf) {
        System.out.println("SDF did change: " + sdf);
        SDF_DATA.set(sdf);
    }

    public static BluePrintData getBluePrintData(ItemStack itemStack) {
        if (itemStack.getItem() instanceof BluePrint) {

            CompoundTag tag = itemStack.getOrCreateTag();

            if (!tag.contains(BLUEPRINT_DATA_TAG)) {
                tag.put(BLUEPRINT_DATA_TAG, new CompoundTag());
            }
            return new BluePrintData(tag.getCompound(BLUEPRINT_DATA_TAG));
        }
        return null;
    }

    public static ItemStack bluePrintWithSDF(SDF sdf) {
        ItemStack stack = new ItemStack(WunderreichItems.BLUE_PRINT);
        BluePrintData d = getBluePrintData(stack);
        d.SDF_DATA.set(sdf);

        return stack;
    }
}
