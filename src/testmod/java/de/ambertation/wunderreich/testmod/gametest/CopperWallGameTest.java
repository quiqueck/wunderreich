package de.ambertation.wunderreich.testmod.gametest;

import de.ambertation.wunderreich.registries.WunderreichWallBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WallSide;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

/**
 * The weathering copper walls take part in vanilla's oxidation: they age on random ticks and an axe
 * scrapes them back one stage, like the vanilla copper blocks.
 * <p>
 * Regression: the walls implemented {@code WeatheringCopper} but were never put into the oxidation
 * map, so they never aged and an axe did nothing.
 */
public class CopperWallGameTest {
    private static final Block[][] CHAINS = {
            {
                    WunderreichWallBlocks.COPPER_WALL,
                    WunderreichWallBlocks.EXPOSED_COPPER_WALL,
                    WunderreichWallBlocks.WEATHERED_COPPER_WALL,
                    WunderreichWallBlocks.OXIDIZED_COPPER_WALL
            },
            {
                    WunderreichWallBlocks.COPPER_GRATE_WALL,
                    WunderreichWallBlocks.EXPOSED_COPPER_GRATE_WALL,
                    WunderreichWallBlocks.WEATHERED_COPPER_GRATE_WALL,
                    WunderreichWallBlocks.OXIDIZED_COPPER_GRATE_WALL
            },
            {
                    WunderreichWallBlocks.CHISELED_COPPER_WALL,
                    WunderreichWallBlocks.EXPOSED_CHISELED_COPPER_WALL,
                    WunderreichWallBlocks.WEATHERED_CHISELED_COPPER_WALL,
                    WunderreichWallBlocks.OXIDIZED_CHISELED_COPPER_WALL
            }
    };

    @GameTest
    public void copperWallsAreInTheOxidationMap(GameTestHelper helper) {
        final List<String> failures = new ArrayList<>();
        for (Block[] chain : CHAINS) {
            for (int i = 0; i < chain.length; i++) {
                final Block block = chain[i];
                final Optional<Block> next = WeatheringCopper.getNext(block);
                final Optional<Block> previous = WeatheringCopper.getPrevious(block);
                final Block expectedNext = i + 1 < chain.length ? chain[i + 1] : null;
                final Block expectedPrevious = i > 0 ? chain[i - 1] : null;

                if (next.orElse(null) != expectedNext) {
                    failures.add(block + " ages into " + next.orElse(null) + ", expected " + expectedNext);
                }
                if (previous.orElse(null) != expectedPrevious) {
                    failures.add(block + " scrapes to " + previous.orElse(null) + ", expected " + expectedPrevious);
                }
                if (block.defaultBlockState().isRandomlyTicking() != (expectedNext != null)) {
                    failures.add(block + " randomly ticking: " + block.defaultBlockState().isRandomlyTicking()
                            + ", expected " + (expectedNext != null));
                }
            }
        }
        ChronariumTestSupport.failIfAny(helper, "copper wall oxidation map", failures);
        helper.succeed();
    }

    @GameTest
    public void anAxeScrapesACopperWallAndKeepsItsShape(GameTestHelper helper) {
        final BlockPos wall = new BlockPos(2, 2, 2);
        final BlockPos neighbour = wall.east();
        helper.setBlock(wall.below(), Blocks.STONE);
        helper.setBlock(neighbour, Blocks.STONE);
        helper.setBlock(wall, WunderreichWallBlocks.EXPOSED_COPPER_WALL);
        // let the wall connect to its neighbour the way a placed wall would
        final BlockState connected = Block.updateFromNeighbourShapes(
                helper.getBlockState(wall), helper.getLevel(), helper.absolutePos(wall));
        helper.setBlock(wall, connected);

        final Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
        helper.useBlock(wall, player);

        final BlockState after = helper.getBlockState(wall);
        final List<String> failures = new ArrayList<>();
        if (!after.is(WunderreichWallBlocks.COPPER_WALL)) {
            failures.add("an axe on an exposed copper wall gave " + after + ", expected a copper wall");
        } else {
            for (var property : List.of(WallBlock.EAST, WallBlock.WEST, WallBlock.NORTH, WallBlock.SOUTH)) {
                if (after.getValue(property) != connected.getValue(property)) {
                    failures.add("scraping changed the wall's " + property.getName() + " side from "
                            + connected.getValue(property) + " to " + after.getValue(property));
                }
            }
            if (connected.getValue(WallBlock.EAST) == WallSide.NONE) {
                failures.add("the rig is broken: the wall did not connect to the stone next to it");
            }
        }
        ChronariumTestSupport.failIfAny(helper, "copper wall axe scraping", failures);
        helper.succeed();
    }
}
