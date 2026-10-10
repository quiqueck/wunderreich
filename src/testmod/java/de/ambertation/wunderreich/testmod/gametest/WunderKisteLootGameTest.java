package de.ambertation.wunderreich.testmod.gametest;

import de.ambertation.wunderreich.registries.WunderreichBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

/**
 * The Wunderkiste loot table: with silk touch the box drops itself, without silk touch it drops
 * <em>all</em> of its ingredients back (8 diamonds, 5 quartz bricks, 3 lapis blocks).
 * <p>
 * Regression: all three ingredient entries used to share one pool with a single roll, so every break
 * handed out just one of them at random. The loot is rolled many times because a one-of-three pick
 * passes a single roll a third of the time.
 */
public class WunderKisteLootGameTest {
    private static final BlockPos KISTE = new BlockPos(2, 2, 2);
    private static final int ROLLS = 30;

    @GameTest
    public void breakingWithoutSilkTouchDropsEveryIngredient(GameTestHelper helper) {
        final BlockState state = placeKiste(helper);
        final ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        final Map<Item, Integer> expected = new LinkedHashMap<>();
        expected.put(Items.DIAMOND, 8);
        expected.put(Items.QUARTZ_BRICKS, 5);
        expected.put(Items.LAPIS_BLOCK, 3);

        final List<String> failures = new ArrayList<>();
        for (int i = 0; i < ROLLS && failures.isEmpty(); i++) {
            final Map<Item, Integer> got = count(drops(helper, state, pickaxe));
            if (!got.equals(expected)) {
                failures.add("roll " + i + ": expected " + expected + ", got " + got);
            }
        }
        ChronariumTestSupport.failIfAny(helper, "Wunderkiste non-silk-touch drops", failures);
        helper.succeed();
    }

    @GameTest
    public void breakingWithSilkTouchDropsOnlyTheKiste(GameTestHelper helper) {
        final BlockState state = placeKiste(helper);
        final ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        pickaxe.enchant(
                helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH),
                1
        );

        final Map<Item, Integer> got = count(drops(helper, state, pickaxe));
        final Map<Item, Integer> expected = Map.of(WunderreichBlocks.WUNDER_KISTE.asItem(), 1);
        if (!got.equals(expected)) {
            throw helper.assertionException(Component.literal(
                    "with silk touch the Wunderkiste should drop exactly itself, got " + got
            ));
        }
        helper.succeed();
    }

    private static BlockState placeKiste(GameTestHelper helper) {
        helper.setBlock(KISTE, WunderreichBlocks.WUNDER_KISTE);
        return helper.getBlockState(KISTE);
    }

    private static List<ItemStack> drops(GameTestHelper helper, BlockState state, ItemStack tool) {
        final BlockPos abs = helper.absolutePos(KISTE);
        return Block.getDrops(state, helper.getLevel(), abs, helper.getLevel().getBlockEntity(abs), null, tool);
    }

    private static Map<Item, Integer> count(List<ItemStack> stacks) {
        final Map<Item, Integer> result = new LinkedHashMap<>();
        for (ItemStack stack : stacks) {
            result.merge(stack.getItem(), stack.getCount(), Integer::sum);
        }
        return result;
    }
}
