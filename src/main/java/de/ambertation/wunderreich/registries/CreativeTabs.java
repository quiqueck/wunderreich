package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.items.TrainedVillagerWhisperer;
import de.ambertation.wunderreich.items.WunderKisteItem;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;

public class CreativeTabs {
    public static final CreativeModeTab TAB_BLOCKS;
    public static final CreativeModeTab TAB_ITEMS;

    static {
        TAB_BLOCKS = FabricItemGroupBuilder.create(Wunderreich.ID("blocks"))
                                           .icon(() -> new ItemStack(getBlockIcon()))
                                           .appendItems(stacks -> {
                                               stacks.addAll(WunderreichBlocks.getAllBlocks()
                                                                              .stream()
                                                                              .filter(block -> block != WunderreichBlocks.WUNDER_KISTE)
                                                                              .map(ItemStack::new).toList());
                                               WunderKisteItem.addAllVariants(stacks);
                                           })
                                           .build();


        TAB_ITEMS = FabricItemGroupBuilder.create(Wunderreich.ID("items"))
                                          .icon(() -> new ItemStack(getItemIcon()))
                                          .appendItems(stacks -> {
                                              stacks.addAll(WunderreichItems.getAllItems()
                                                                            .stream()
                                                                            .filter(item -> item != WunderreichItems.WHISPERER)
                                                                            .map(ItemStack::new).toList());
                                              TrainedVillagerWhisperer.addAllVariants(stacks);
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
