package de.ambertation.wunderreich.items.construction;

import org.wunder.lib.math.sdf.SDF;
import org.wunder.lib.math.sdf.SDFUnion;
import org.wunder.lib.math.sdf.shapes.Box;
import org.wunder.lib.math.sdf.shapes.Cylinder;
import org.wunder.lib.math.sdf.shapes.Sphere;
import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.utils.nbt.CachedNBTValue;
import de.ambertation.wunderreich.utils.nbt.NbtTagHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BluePrintData {
    private static final String SDF_TAG = "sdf";
    private static final String BLUEPRINT_DATA_TAG = "blueprint";

    public final CachedNBTValue<SDF, Tag> SDF_DATA;

    protected BluePrintData(CompoundTag baseTag) {
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
    }

    void sdfContentDidChange(SDF sdf) {
        //System.out.println("SDF did change: " + sdf);
        //SDF_DATA.set(sdf);
    }

    public static BluePrintData getBluePrintData(ItemStack itemStack) {
        if (itemStack == null) return null;
        if (itemStack.getItem() instanceof BluePrint) {
            CompoundTag tag = itemStack.getOrCreateTag();
            return getBluePrintData(tag);
        }
        return null;
    }

    public static BluePrintData getBluePrintData(CompoundTag tag) {
        if (tag == null) return null;

        if (!tag.contains(BLUEPRINT_DATA_TAG)) {
            tag.put(BLUEPRINT_DATA_TAG, new CompoundTag());
        }
        return new BluePrintData(tag.getCompound(BLUEPRINT_DATA_TAG));
    }

    public static ItemStack bluePrintWithSDF(SDF sdf) {
        Item baseBluePrint = WunderreichItems.BLUE_PRINT;

        if (sdf instanceof Sphere) baseBluePrint = WunderreichItems.BLUE_PRINT_SPHERE;
        else if (sdf instanceof Box) baseBluePrint = WunderreichItems.BLUE_PRINT_BOX;
        else if (sdf instanceof Cylinder) baseBluePrint = WunderreichItems.BLUE_PRINT_CYLINDER;
        else if (sdf instanceof SDFUnion) baseBluePrint = WunderreichItems.BLUE_PRINT_UNION;

        ItemStack stack = new ItemStack(baseBluePrint);
        BluePrintData d = getBluePrintData(stack);
        d.SDF_DATA.set(sdf);

        return stack;
    }
}
