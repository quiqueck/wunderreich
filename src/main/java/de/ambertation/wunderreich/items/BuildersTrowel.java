package de.ambertation.wunderreich.items;

import de.ambertation.wunderreich.registries.WunderreichAdvancements;
import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.registries.WunderreichTags;
import de.ambertation.wunderreich.utils.RandomList;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

public class BuildersTrowel extends DiggerItem {

    public BuildersTrowel() {
        super(
                -2.5f, //attack DamageBase
                -0.5f, //attack Speed
                Tiers.IRON,
                WunderreichTags.MINEABLE_TROWEL,
                WunderreichItems
                        .makeItemSettings()
                        .rarity(Rarity.UNCOMMON)
                        .durability(Tiers.IRON.getUses() * 4)
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
        if (list.isEmpty()) return InteractionResult.FAIL;

        ItemStack item;
        InteractionResult result;
        int maxTries = 100;
        do { /* mache */
            item = list.getRandom();
            if /* wenn */ (item != null) {
                result = getInteractionResult(ctx, p, item);
                maxTries--;
            } else /* ansonsten */ {
                result = InteractionResult.FAIL;
            }
        } /* solange */ while (maxTries > 0 && (result == InteractionResult.FAIL || result == InteractionResult.PASS));

        return result;
    }

    private InteractionResult getInteractionResult(UseOnContext ctx, Player p, ItemStack item) {
        BlockPlaceContext bctx;
        bctx = new BlockPlaceContext(ctx.getPlayer(),
                                     ctx.getHand(),
                                     item,
                                     new BlockHitResult(ctx.getClickLocation(),
                                                        ctx.getClickedFace(),
                                                        ctx.getClickedPos(),
                                                        ctx.isInside()));
        BlockItem bi = (BlockItem) item.getItem();

        InteractionResult result = bi.place(bctx);
        if (result == InteractionResult.CONSUME) {
            if (p instanceof ServerPlayer sp) {
                WunderreichAdvancements.USE_TROWEL.trigger(sp);
            }
            if (!p.getAbilities().instabuild) {
                item.shrink(1);
                ctx.getItemInHand().hurtAndBreak(1, p, player -> player.broadcastBreakEvent(ctx.getHand()));
            }
        }

        return result;
    }
}
