package de.ambertation.wunderreich.items;

import de.ambertation.wunderreich.registries.WunderreichAdvancements;
import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.registries.WunderreichTags;
import de.ambertation.wunderreich.utils.RandomList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

public class BuildersTrowel extends Item {
    public static Item.Properties trowel(
            Item.Properties inProps,
            ToolMaterial toolMaterial,
            float attackDamage,
            float attackSpeed
    ) {
        return inProps.tool(toolMaterial, WunderreichTags.MINEABLE_TROWEL, attackDamage, attackSpeed, 0.0F)
                      .rarity(Rarity.UNCOMMON)
                      .durability(toolMaterial.durability() * 4);
    }

    public BuildersTrowel(ToolMaterial toolMaterial, ResourceKey<Item> key) {
        // The trowel is deliberately a poor weapon, but the attack speed modifier is added to the
        // player's base of 4: -5 ended up at -1 (shown as "-1 Attack Speed", clamped to 0). -3.5 gives
        // 0.5 attacks per second, slower than any vanilla tool.
        super(trowel(WunderreichItems.makeItemSettings(), toolMaterial, 1.0F, -3.5F).setId(key));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {

        final Player p = ctx.getPlayer();
        // Placement is server authoritative: the client never predicts which block gets placed.
        if (!(p instanceof ServerPlayer) || !(ctx.getLevel() instanceof ServerLevel level)) return InteractionResult.FAIL;

        BlockPlaceContext bctx = new BlockPlaceContext(ctx);


        final RandomList<ItemStack> list = new RandomList<>(9);


        for (int i = 0; i < Math.min(9, p.getInventory().getNonEquipmentItems().size()); i++) {
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
        final BlockPos cPos = ctx.getClickedPos().relative(ctx.getClickedFace(), 1);
        final Supplier<Float> noise;
        final Integer duration = this.components().get(DataComponents.MAX_DAMAGE);
        if (duration != null && duration >= ToolMaterial.DIAMOND.durability() * 4) {
            // the pattern only depends on the world seed and the position, so it survives restarts
            final TrowelPattern pattern = TrowelPattern.forWorldSeed(level.getSeed());
            noise = () -> pattern.sample(cPos, list.size());
        }
        else noise = RandomList::random;

        do {
            item = list.getRandom(noise);
            if (item != null) {
                result = getInteractionResult(ctx, p, item);
                maxTries--;
            } else {
                result = InteractionResult.FAIL;
            }
        } while (maxTries > 0 && (result == InteractionResult.FAIL || result == InteractionResult.PASS));

        return result;
    }

    private InteractionResult getInteractionResult(UseOnContext ctx, Player p, ItemStack item) {
        BlockPlaceContext bctx;
        bctx = new BlockPlaceContext(
                ctx.getPlayer(),
                ctx.getHand(),
                item,
                new BlockHitResult(
                        ctx.getClickLocation(),
                        ctx.getClickedFace(),
                        ctx.getClickedPos(),
                        ctx.isInside()
                )
        );

        BlockItem bi = (BlockItem) item.getItem();

        InteractionResult result = bi.place(bctx);
        if (result.consumesAction()) {
            if (p instanceof ServerPlayer sp) {
                WunderreichAdvancements.USE_TROWEL.trigger(sp);
            }
            if (!p.getAbilities().instabuild) {
                ctx.getItemInHand().hurtAndBreak(1, p, ctx.getHand());
            }
        }

        return result;
    }
}
