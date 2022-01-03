package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.stream.Collectors;

public class CreativeTabs {
    public static final CreativeModeTab TAB_BLOCKS;
    public static final CreativeModeTab TAB_ITEMS;

    static {
        TAB_BLOCKS = FabricItemGroupBuilder
                .create(Wunderreich.makeID("blocks"))
                .icon(() -> new ItemStack(WunderreichBlocks.BOX_OF_EIR))
                .appendItems(stacks -> stacks.addAll(WunderreichBlocks.getModBlockItems()
                        .stream()
                        .map(ItemStack::new)
                        .collect(Collectors.toList())))
                .build();
        TAB_ITEMS = FabricItemGroupBuilder
                .create(Wunderreich.makeID("items"))
                .icon(() -> new ItemStack(WunderreichItems.WHISPERER))
                .appendItems(stacks -> stacks.addAll(WunderreichItems.getModItems()
                        .stream()
                        .map(ItemStack::new)
                        .collect(Collectors.toList())))
                .build();
    }
}
