package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.config.WunderreichConfigs;
import de.ambertation.wunderreich.items.TrainedVillagerWhisperer;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;

import java.util.stream.Collectors;

public class CreativeTabs {
    public static final CreativeModeTab TAB_BLOCKS;
    public static final CreativeModeTab TAB_ITEMS;

    public static Block getBlockIcon() {
        return WunderreichBlocks.getAllBlocks()
                                .stream()
                                .filter(WunderreichConfigs.BLOCK_CONFIG::isEnabled)
                                .findFirst()
                                .orElse(Blocks.LAPIS_BLOCK);
    }

    public static Item getItemIcon() {
        if (WunderreichConfigs.MAIN.allowLibrarianSelection() && WunderreichConfigs.ITEM_CONFIG.isEnabled(
                WunderreichItems.WHISPERER))
            return WunderreichItems.WHISPERER;

        if (WunderreichConfigs.MAIN.allowBuilderTools.get() && WunderreichConfigs.ITEM_CONFIG.isEnabled(WunderreichItems.BUILDERS_TROWEL))
            return WunderreichItems.BUILDERS_TROWEL;

        return WunderreichItems.getAllItems()
                               .stream()
                               .filter(WunderreichConfigs.ITEM_CONFIG::isEnabled)
                               .findFirst()
                               .orElse(Items.BOOK);
    }

    static {
        TAB_BLOCKS = FabricItemGroupBuilder.create(Wunderreich.loc("blocks"))
                                           .icon(() -> new ItemStack(getBlockIcon()))
                                           .appendItems(stacks -> stacks.addAll(WunderreichBlocks.getAllBlocks()
                                                                                                 .stream()
                                                                                                 .map(ItemStack::new)
                                                                                                 .collect(Collectors.toList())))
                                           .build();


        TAB_ITEMS = FabricItemGroupBuilder.create(Wunderreich.loc("items"))
                                          .icon(() -> new ItemStack(getItemIcon()))
                                          .appendItems(stacks -> {
                                              stacks.addAll(WunderreichItems.getAllItems()
                                                                            .stream()
                                                                            .filter(item -> item != WunderreichItems.WHISPERER)
                                                                            .map(ItemStack::new)
                                                                            .collect(Collectors.toList()));
                                              TrainedVillagerWhisperer.addAllVariants(stacks);
                                          })
                                          .build();

    }
}
