package de.ambertation.wunderreich.items;

import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.utils.RandomList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

public class BuildersTrowel extends Item {
    public BuildersTrowel() {
        super(WunderreichItems
                .makeItemSettings()
                .rarity(Rarity.UNCOMMON)
        );
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        final Player p = ctx.getPlayer();
        if (!(p instanceof ServerPlayer)) return InteractionResult.FAIL;

        BlockPlaceContext bctx = new BlockPlaceContext(ctx);

        final BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace(), 1);
        final RandomList<ItemStack> list = new RandomList<>(9);


        for (int i = 0; i < Math.min(9, p.getInventory().items.size()); i++) {
            ItemStack stack = p.getInventory().getItem(i);
            if (stack.getItem() instanceof BlockItem item) {
                if (bctx.canPlace()) {
                    BlockPlaceContext updatedbctx = item.updatePlacementContext(bctx);
                    if (updatedbctx != null) {
                        list.add(stack, stack.getCount());
                    }
                }
            }
        }


        ItemStack item = list.getRandom();
        if (item!=null) {
            bctx = new BlockPlaceContext(ctx.getPlayer(), ctx.getHand(),item, new BlockHitResult(ctx.getClickLocation(), ctx.getClickedFace(), ctx.getClickedPos(), ctx.isInside()));
            BlockItem bi = (BlockItem)item.getItem();
            System.out.println(list + " - " + bi);

            InteractionResult result = bi.place(bctx);
            if (result==InteractionResult.CONSUME) {
                item.shrink(1);
            }
            System.out.println(list + " - " + bi);
            return result;
        }
        return InteractionResult.FAIL;
    }
}
