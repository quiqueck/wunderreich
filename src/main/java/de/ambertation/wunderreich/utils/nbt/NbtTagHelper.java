package de.ambertation.wunderreich.utils.nbt;

import org.wunder.lib.math.Bounds;
import org.wunder.lib.math.Float3;
import org.wunder.lib.math.sdf.SDF;
import de.ambertation.wunderreich.Wunderreich;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.*;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.List;

public class NbtTagHelper {
    public static ListTag writeContainer(SimpleContainer container) {
        ListTag listTag = new ListTag();
        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack itemStack = container.getItem(i);
            if (!itemStack.isEmpty()) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putShort("Slot", (short) i);
                itemStack.save(compoundTag);
                listTag.add(compoundTag);
            }
        }
        return listTag;
    }

    public static void readContainer(ListTag listTag, SimpleContainer container) {
        container.clearContent();
        for (int j = 0; j < listTag.size(); ++j) {
            CompoundTag compoundTag = listTag.getCompound(j);
            int k = compoundTag.getShort("Slot") & 0xFFFF;
            if (k < container.getContainerSize()) {
                container.setItem(k, ItemStack.of(compoundTag));
            }
        }
    }

    public static ListTag writeItems(List<ItemStack> items) {
        ListTag listTag = new ListTag();
        for (int i = 0; i < items.size(); ++i) {
            ItemStack itemStack = items.get(i);
            if (!itemStack.isEmpty()) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putByte("Slot", (byte) i);
                itemStack.save(compoundTag);
                listTag.add(compoundTag);
            }
        }
        return listTag;
    }

    public static NonNullList<ItemStack> readItems(ListTag listTag, int size) {
        NonNullList<ItemStack> list = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int j = 0; j < listTag.size(); ++j) {
            CompoundTag compoundTag = listTag.getCompound(j);
            int k = compoundTag.getByte("Slot") & 255;
            if (k < list.size()) {
                list.set(k, ItemStack.of(compoundTag));
            }
        }
        return list;
    }

    public static Float3 readPos(CompoundTag tag) {
        if (!tag.contains("x") || !tag.contains("y") || !tag.contains("z")) return null;
        return Float3.of(tag.getFloat("x"), tag.getFloat("y"), tag.getFloat("z"));
    }

    public static CompoundTag writePos(Float3 pos) {
        CompoundTag bp = new CompoundTag();
        bp.putFloat("x", (float) pos.x);
        bp.putFloat("y", (float) pos.y);
        bp.putFloat("z", (float) pos.z);
        return bp;
    }

    public static BlockPos readBlockPos(CompoundTag tag) {
        if (!tag.contains("x") || !tag.contains("y") || !tag.contains("z")) return null;
        return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }

    public static CompoundTag writeBlockPos(BlockPos pos) {
        CompoundTag bp = new CompoundTag();
        bp.putInt("x", pos.getX());
        bp.putInt("y", pos.getY());
        bp.putInt("z", pos.getZ());
        return bp;
    }

    public static BoundingBox readBoundingBox(CompoundTag tag) {
        if (!tag.contains("lx") || !tag.contains("ly") || !tag.contains("lz") || !tag.contains("hx") || !tag.contains(
                "hy") || !tag.contains("hz")) return null;
        return new BoundingBox(
                tag.getInt("lx"),
                tag.getInt("ly"),
                tag.getInt("lz"),
                tag.getInt("hx"),
                tag.getInt("hy"),
                tag.getInt("hz")
        );
    }

    public static CompoundTag writeBoundingBox(BoundingBox bb) {
        CompoundTag bp = new CompoundTag();
        bp.putInt("lx", bb.minX());
        bp.putInt("ly", bb.minY());
        bp.putInt("lz", bb.minZ());
        bp.putInt("hx", bb.maxX());
        bp.putInt("hy", bb.maxY());
        bp.putInt("hz", bb.maxZ());
        return bp;
    }

    public static Bounds readBounds(CompoundTag tag) {
        if (!tag.contains("lx") || !tag.contains("ly") || !tag.contains("lz") || !tag.contains("hx") || !tag.contains(
                "hy") || !tag.contains("hz")) return null;
        return Bounds.of(
                tag.getFloat("lx"),
                tag.getFloat("ly"),
                tag.getFloat("lz"),
                tag.getFloat("hx"),
                tag.getFloat("hy"),
                tag.getFloat("hz")
        );
    }

    public static CompoundTag writeBounds(Bounds bb) {
        CompoundTag bp = new CompoundTag();
        bp.putFloat("lx", (float) bb.min.x);
        bp.putFloat("ly", (float) bb.min.y);
        bp.putFloat("lz", (float) bb.min.z);
        bp.putFloat("hx", (float) bb.max.x);
        bp.putFloat("hy", (float) bb.max.y);
        bp.putFloat("hz", (float) bb.max.z);
        return bp;
    }

    public static Bounds.Interpolate readInterpolated(ByteTag in) {
        int idx = in.getAsByte();
        if (idx >= 0 && idx < Bounds.Interpolate.CORNERS_AND_CENTER.length)
            return Bounds.Interpolate.CORNERS_AND_CENTER[idx];
        return null;
    }

    public static ByteTag writeInterpolated(Bounds.Interpolate p) {
        return ByteTag.valueOf(p.idx);
    }


    public static Tag writeSDF(SDF sdf) {
        DataResult<Tag> result = SDF.CODEC.encode(sdf, NbtOps.INSTANCE, null);
        if (result.result().isPresent()) {
            return result.result().get();
        } else {
            Wunderreich.LOGGER.error(result.error().toString());
        }
        return null;
    }

    public static SDF readSDF(Tag in) {
        var result = SDF.CODEC
                .parse(new Dynamic<>(NbtOps.INSTANCE, in))
                .resultOrPartial(Wunderreich.LOGGER::error);
        if (result.isPresent()) {
            return result.get();
        }
        return null;
    }
}
