package de.ambertation.wunderreich.integration.wthit;

import de.ambertation.wunderreich.blockentities.ChronariumBlockEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;

/**
 * Shows what a Chronarium is doing on the Waila/WTHIT tooltip.
 * <p>
 * No server data provider is needed: {@link ChronariumBlockEntity#getUpdateTag} already ships
 * progress and the total recipe time to every client that can see the block, which is what the
 * block entity renderer runs on.
 */
public enum ChronariumProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        final BlockEntity entity = accessor.getBlockEntity();
        if (!(entity instanceof ChronariumBlockEntity chronarium)) return;

        if (chronarium.getMaxProgress() <= 0) {
            tooltip.addLine(Component
                    .translatable("wunderreich.aging.idle")
                    .withStyle(ChatFormatting.GRAY)
            );
            return;
        }

        final int percent = Math.round(chronarium.getProgressFraction() * 100f);
        tooltip.addLine(Component
                .translatable("wunderreich.aging.progress", percent)
                .withStyle(ChatFormatting.GRAY)
        );
    }
}
