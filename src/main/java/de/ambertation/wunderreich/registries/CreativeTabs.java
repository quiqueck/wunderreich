package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blocks.*;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.items.TrainedVillagerWhisperer;
import de.ambertation.wunderreich.items.WunderKisteItem;

import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CreativeTabs {
    public static final CreativeModeTab TAB_BLOCKS;
    public static final CreativeModeTab TAB_ITEMS;

    public static void ensureStaticallyLoaded() {
    }

    static {
        TAB_BLOCKS = FabricItemGroup
                .builder(Wunderreich.ID("blocks"))
                .icon(() -> new ItemStack(getBlockIcon()))
                .displayItems((itemDisplayParameters, output) -> {
                    List<ItemStack> stacks = new ArrayList<>(32);
                    stacks.addAll(WunderreichBlocks.getAllBlocks()
                                                   .stream()
                                                   .filter(block -> block != WunderreichBlocks.WUNDER_KISTE)
                                                   .map(ItemStack::new)
                                                   .toList()
                    );
                    WunderKisteItem.addAllVariants(stacks);

                    stacks.sort(Comparator.comparing(stack -> {
                        String prefix = "";
                        if (stack.getItem() instanceof BlockItem blockItem) {
                            Block bl = blockItem.getBlock();
                            if (bl instanceof WoodWallBlock)
                                prefix = "wall_wood";
                            else if (bl instanceof WoolWallBlock)
                                prefix = "wall_wool";
                            else if (bl instanceof AbstractWallBlock)
                                prefix = "wall_a";
                            else if (bl instanceof WoolStairBlock)
                                prefix = "stair_wool";
                            else if (bl instanceof AbstractStairBlock)
                                prefix = "stair_a";
                            else if (bl instanceof SlabBlock)
                                prefix = "slab";
                            else
                                prefix = bl.getClass().getSimpleName();
                        }
                        if (stack.hasCustomHoverName())
                            return prefix + stack.getHoverName().getString();
                        else return prefix + stack.getItem().getName(stack).getString();
                    }));

                    output.acceptAll(stacks);
                })
                .build();

        TAB_ITEMS = FabricItemGroup
                .builder(Wunderreich.ID("items"))
                .icon(() -> new ItemStack(getItemIcon()))
                .displayItems((itemDisplayParameters, output) -> {
                    List<ItemStack> stacks = new ArrayList<>(32);
                    stacks.addAll(WunderreichItems.getAllItems()
                                                  .stream()
                                                  .filter(item -> item != WunderreichItems.WHISPERER)
                                                  .map(ItemStack::new)
                                                  .toList()
                    );
                    TrainedVillagerWhisperer.addAllVariants(stacks);

                    stacks.sort(Comparator.comparing(stack -> {
                        String prefix = stack.getItem().getClass().getSimpleName();
                        if (stack.hasCustomHoverName())
                            return prefix + stack.getHoverName().getString();
                        else return prefix + stack.getItem().getName(stack).getString();
                    }));

                    output.acceptAll(stacks);
                })
                .build();
    }

    public static Block getBlockIcon() {
        if (Configs.BLOCK_CONFIG.isEnabled(WunderreichBlocks.WUNDER_KISTE))
            return WunderreichBlocks.WUNDER_KISTE;
        if (Configs.BLOCK_CONFIG.isEnabled(WunderreichBlocks.WHISPER_IMPRINTER))
            return WunderreichBlocks.WHISPER_IMPRINTER;
        return WunderreichBlocks.getAllBlocks()
                                .stream()
                                .filter(Configs.BLOCK_CONFIG::isEnabled)
                                .findFirst()
                                .orElse(Blocks.LAPIS_BLOCK);
    }

    public static Item getItemIcon() {
        if (WunderreichRules.Whispers.allowLibrarianSelection() && Configs.ITEM_CONFIG.isEnabled(
                WunderreichItems.WHISPERER))
            return WunderreichItems.WHISPERER;

        if (Configs.MAIN.allowBuilderTools.get() && Configs.ITEM_CONFIG.isEnabled(WunderreichItems.BUILDERS_TROWEL))
            return WunderreichItems.BUILDERS_TROWEL;

        return WunderreichItems.getAllItems()
                               .stream()
                               .filter(Configs.ITEM_CONFIG::isEnabled)
                               .findFirst()
                               .orElse(Items.BOOK);
    }
}
