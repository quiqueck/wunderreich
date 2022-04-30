package de.ambertation.wunderreich.rei;

//import dev.architectury.utils.NbtType;
//import me.shedaniel.rei.api.common.category.CategoryIdentifier;
//import me.shedaniel.rei.api.common.entry.comparison.EntryComparator;
//import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
//import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
//
//import java.util.function.Function;
//
//public class ServerPlugin implements REIServerPlugin {
//    public static CategoryIdentifier<ImprinterDisplay> IMPRINTER = CategoryIdentifier.of(Wunderreich.MOD_ID,
//                                                                                         ImprinterRecipe.Type.ID.getPath());
//
//    @Override
//    public void registerItemComparators(ItemComparatorRegistry registry) {
//        EntryComparator<Tag> nbtHasher = EntryComparator.nbt();
//        Function<ItemStack, CompoundTag> enchantmentTag = stack -> {
//            CompoundTag tag = stack.getTag();
//            if (tag == null) return null;
//            if (tag.contains(TrainedVillagerWhisperer.TAG_NAME, NbtType.COMPOUND)) {
//                return tag.getCompound(TrainedVillagerWhisperer.TAG_NAME);
//            }
//            return new CompoundTag();
//        };
//
//        registry.register((context, stack) -> nbtHasher.hash(context, enchantmentTag.apply(stack)),
//                          WunderreichItems.WHISPERER);
//    }
//}
