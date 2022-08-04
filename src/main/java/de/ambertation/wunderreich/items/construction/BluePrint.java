package de.ambertation.wunderreich.items.construction;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.utils.math.sdf.SDF;

import java.util.List;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public class BluePrint extends Item {
    private final Supplier<SDF> builder;

    public BluePrint(Supplier<SDF> builder) {
        super(WunderreichItems
                .makeItemSettings()
                .rarity(Rarity.UNCOMMON)
                .durability(1000));
        this.builder = builder == null ? () -> null : builder;
    }


    @Override
    public void onCraftedBy(ItemStack itemStack, Level level, Player player) {
        super.onCraftedBy(itemStack, level, player);
        BluePrintData bpd = BluePrintData.getBluePrintData(itemStack);
        if (bpd != null)
            bpd.SDF_DATA.set(builder.get());
        System.out.println("onCraftedBy:" + itemStack);
    }

    public void appendHoverText(
            ItemStack itemStack, @Nullable Level level,
            List<Component> list, TooltipFlag tooltipFlag
    ) {
        super.appendHoverText(itemStack, level, list, tooltipFlag);
        BluePrintData bpd = BluePrintData.getBluePrintData(itemStack);

        if (bpd != null) {
            SDF sdf = bpd.SDF_DATA.get();
            if (sdf != null) {
                list.add(Component.literal(sdf.toString()));
            }
        }

    }
}
