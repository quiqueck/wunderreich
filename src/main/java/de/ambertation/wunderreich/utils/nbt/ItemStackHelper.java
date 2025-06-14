package de.ambertation.wunderreich.utils.nbt;

import de.ambertation.wunderreich.Wunderreich;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class ItemStackHelper {
    public static Optional<ItemStack> parse(HolderLookup.Provider provider, Tag tag) {
        return ItemStack.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag)
                              .resultOrPartial(string -> Wunderreich.LOGGER.error(
                                      "Tried to load invalid item: '{}'",
                                      string
                              ));
    }

    public static ItemStack parseOptional(HolderLookup.Provider provider, CompoundTag compoundTag) {
        if (compoundTag.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return ItemStackHelper.parse(provider, compoundTag).orElse(ItemStack.EMPTY);
    }

    public static Tag save(ItemStack stack, HolderLookup.Provider provider, Tag tag) {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Cannot encode empty ItemStack");
        }
        return ItemStack.CODEC.encode(stack, provider.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow();
    }
}
