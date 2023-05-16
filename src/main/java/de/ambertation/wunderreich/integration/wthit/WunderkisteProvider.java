package de.ambertation.wunderreich.integration.wthit;

//import de.ambertation.wunderreich.blockentities.WunderKisteBlockEntity;
//import de.ambertation.wunderreich.blocks.WunderKisteBlock;
//import de.ambertation.wunderreich.items.WunderKisteItem;
//import de.ambertation.wunderreich.registries.WunderreichRules;
//import de.ambertation.wunderreich.utils.WunderKisteDomain;
//import de.ambertation.wunderreich.utils.WunderKisteServerExtension;
//
//import net.minecraft.ChatFormatting;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.nbt.Tag;
//import net.minecraft.network.chat.Component;
//import net.minecraft.network.chat.Style;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.world.level.block.state.BlockState;
//
//import mcp.mobius.waila.api.*;
//import mcp.mobius.waila.api.component.ItemComponent;
//
//import org.jetbrains.annotations.Nullable;
//
//public enum WunderkisteProvider implements IBlockComponentProvider {
//    INSTANCE;
//
//    private static Component readCustomName(CompoundTag compoundTag) {
//        if (compoundTag == null) return null;
//        if (compoundTag.contains("customName", Tag.TAG_STRING)) {
//            return Component.literal(compoundTag.getString("customName")).setStyle(Style.EMPTY.withBold(true));
//        }
//        return null;
//    }
//
//    private static void addNetworkTooltip(
//            ITooltip tooltip,
//            BlockState state,
//            WunderKisteBlockEntity kiste,
//            CompoundTag serverData
//    ) {
//        if (WunderreichRules.Wunderkiste.haveMultiple()) {
//            Component domainComponent = WunderreichRules.Wunderkiste.namedNetworks()
//                    ? readCustomName(serverData)
//                    : null;
//            if (domainComponent == null) {
//
//                domainComponent = WunderKisteItem.getDomainComponent(WunderKisteServerExtension.getDomain(state));
//            }
//
//            tooltip.addLine(Component
//                    .translatable("wunderreich.wunderkiste.domain.HoverText", domainComponent)
//                    .withStyle(ChatFormatting.GRAY)
//            );
//        }
//    }
//
//    @Nullable
//    @Override
//    public ITooltipComponent getIcon(IBlockAccessor accessor, IPluginConfig config) {
//        ItemStack stack = accessor.getStack();
//        WunderKisteDomain domain = WunderreichRules.Wunderkiste.showColors()
//                ? WunderKisteServerExtension.getDomain(accessor.getBlockState())
//                : WunderKisteBlock.DEFAULT_DOMAIN;
//        stack = WunderKisteItem.setDomain(stack, domain);
//        return new ItemComponent(stack);
//    }
//
//    @Override
//    public void appendHead(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
//        tooltip.setLine(
//                WailaConstants.OBJECT_NAME_TAG,
//                Component
//                        .translatable("block.wunderreich.wunder_kiste")
//                        .withStyle(
//                                Style.EMPTY
//                                        .withColor(0xffffffff)
//                        )
//        );
//    }
//
//    @Override
//    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
//        BlockEntity entity = accessor.getBlockEntity();
//        if (entity instanceof WunderKisteBlockEntity kiste) {
//            BlockState state = accessor.getBlockState();
//            CompoundTag serverData = accessor.getServerData();
//
//            addNetworkTooltip(tooltip, state, kiste, serverData);
//        }
//    }
//
//}