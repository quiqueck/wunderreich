package de.ambertation.wunderreich.items;

import de.ambertation.wunderreich.registries.WunderreichAdvancements;
import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.registries.WunderreichTags;
import de.ambertation.wunderreich.utils.RandomList;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BuildersTrowel extends Item {
    /** How a trowel chooses which hotbar block it places where. */
    public enum Pattern {
        /** A random block for every placement. */
        RANDOM,
        /** Bands along a noise field ({@link TrowelPattern}): stripes and rings, stable per world. */
        BANDS,
        /** Loose patches ({@link CellularTrowelPattern}), stable per world. */
        CELLS;

        /** The tooltip line that tells players how a trowel with this pattern places blocks. */
        public String translationKey() {
            return "wunderreich.trowel.pattern." + name().toLowerCase(Locale.ROOT);
        }
    }

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

    private final Pattern pattern;

    public BuildersTrowel(ToolMaterial toolMaterial, ResourceKey<Item> key, Pattern pattern) {
        // The trowel is deliberately a poor weapon, but the attack speed modifier is added to the
        // player's base of 4: -5 ended up at -1 (shown as "-1 Attack Speed", clamped to 0). -3.5 gives
        // 0.5 attacks per second, slower than any vanilla tool.
        super(trowel(WunderreichItems.makeItemSettings(), toolMaterial, 1.0F, -3.5F).setId(key));
        this.pattern = pattern;
    }

    public Pattern pattern() {
        return pattern;
    }

    @Override
    public void appendHoverText(
            ItemStack itemStack,
            TooltipContext tooltipContext,
            TooltipDisplay tooltipDisplay,
            Consumer<Component> consumer,
            TooltipFlag tooltipFlag
    ) {
        super.appendHoverText(itemStack, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        consumer.accept(Component.translatable(pattern.translationKey()).withStyle(ChatFormatting.GRAY));
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
        // the patterns only depend on the world seed and the position, so they survive restarts
        final Supplier<Float> noise = switch (pattern) {
            case BANDS -> {
                final TrowelPattern bands = TrowelPattern.forWorldSeed(level.getSeed());
                yield () -> bands.sample(cPos, list.size());
            }
            case CELLS -> {
                final CellularTrowelPattern cells = CellularTrowelPattern.forWorldSeed(level.getSeed());
                final double spacing = CellularTrowelPattern.spacing(list);
                yield () -> cells.sample(cPos, spacing);
            }
            case RANDOM -> RandomList::random;
        };

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
