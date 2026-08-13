package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.advancements.AdvancementsJsonBuilder;
import de.ambertation.wunderreich.recipes.AgingRecipeJsonBuilder;
import de.ambertation.wunderreich.recipes.RecipeJsonBuilder;
import de.ambertation.wunderreich.recipes.StonecutterJsonBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopperCollection;

import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WunderreichRecipes {
    public static final Map<Identifier, JsonElement> RECIPES = new HashMap<>();

    public static void register() {
        RecipeJsonBuilder
                .create("wunder_kiste")
                .result(WunderreichBlocks.WUNDER_KISTE)
                .pattern(
                        "LLL",
                        "#N#",
                        "###"
                )
                .material('#', Blocks.QUARTZ_BRICKS)
                .material('N', Blocks.DIAMOND_BLOCK)
                .material('L', Blocks.LAPIS_BLOCK)
                .registerAndCreateAdvancement(
                        AdvancementsJsonBuilder.AdvancementType.RECIPE_TOOL, List.of(
                                Items.QUARTZ,
                                Items.DIAMOND,
                                Items.DIAMOND_BLOCK,
                                Items.LAPIS_LAZULI
                        )
                );

        RecipeJsonBuilder
                .create("whisperer_blank")
                .result(WunderreichItems.BLANK_WHISPERER)
                .pattern(
                        "GAG",
                        "ABA",
                        "GAG"
                )
                .material('G', Blocks.GLASS)
                .material('B', Blocks.LAPIS_BLOCK)
                .material('A', Items.AMETHYST_SHARD)
                .registerAndCreateAdvancement(
                        AdvancementsJsonBuilder.AdvancementType.RECIPE_TOOL, List.of(
                                Items.GLASS,
                                Items.LAPIS_LAZULI,
                                Items.AMETHYST_SHARD
                        )
                );

        RecipeJsonBuilder
                .create("whisper_imprinter")
                .result(WunderreichBlocks.WHISPER_IMPRINTER)
                .pattern(
                        "ABA",
                        "DCD",
                        "DDD"
                )
                .material('C', Blocks.COPPER_BLOCK.weathering().oxidized())
                .material('B', Blocks.LAPIS_BLOCK)
                .material('A', Blocks.AMETHYST_BLOCK)
                .material('D', Blocks.COBBLED_DEEPSLATE)
                .registerAndCreateAdvancement(
                        AdvancementsJsonBuilder.AdvancementType.RECIPE_TOOL, List.of(
                                Items.COPPER_INGOT,
                                Items.LAPIS_LAZULI,
                                Blocks.AMETHYST_BLOCK.asItem(),
                                Blocks.COBBLED_DEEPSLATE.asItem()
                        )
                );

        RecipeJsonBuilder
                .create("chronarium")
                .result(WunderreichBlocks.CHRONARIUM)
                .pattern(
                        "DGD",
                        "OAO",
                        "DDD"
                )
                .material('G', Blocks.GLASS)
                .material('A', Blocks.AMETHYST_BLOCK)
                .material('O', Blocks.COPPER_BLOCK.weathering().oxidized())
                .material('D', Blocks.COBBLED_DEEPSLATE)
                .registerAndCreateAdvancement(
                        AdvancementsJsonBuilder.AdvancementType.RECIPE_TOOL, List.of(
                                Items.GLASS,
                                Blocks.AMETHYST_BLOCK.asItem(),
                                Items.COPPER_INGOT,
                                Blocks.COBBLED_DEEPSLATE.asItem()
                        )
                );

        RecipeJsonBuilder
                .create("builders_trowel")
                .result(WunderreichItems.BUILDERS_TROWEL)
                .pattern(
                        "***",
                        " * ",
                        " # "
                )
                .material('#', Items.STICK)
                .material('*', Items.IRON_INGOT)
                .registerAndCreateAdvancement(
                        AdvancementsJsonBuilder.AdvancementType.RECIPE_TOOL, List.of(
                                Items.IRON_INGOT
                        )
                );

        RecipeJsonBuilder
                .create("diamond_builders_trowel")
                .result(WunderreichItems.DIAMOND_BUILDERS_TROWEL)
                .pattern(
                        "***",
                        " * ",
                        " # "
                )
                .material('#', Items.STICK)
                .material('*', Items.DIAMOND)
                .registerAndCreateAdvancement(
                        AdvancementsJsonBuilder.AdvancementType.RECIPE_TOOL, List.of(
                                Items.DIAMOND
                        )
                );

        RecipeJsonBuilder
                .create("suction_tube")
                .result(WunderreichBlocks.SUCTION_TUBE)
                .pattern(
                        "LPL",
                        "LHL",
                        "IRI"
                )
                .material('L', Items.LAPIS_LAZULI)
                .material('I', Items.IRON_INGOT)
                .material('P', Items.PISTON)
                .material('H', Blocks.HOPPER)
                .material('R', Items.REDSTONE)
                .registerAndCreateAdvancement(
                        AdvancementsJsonBuilder.AdvancementType.RECIPE_TOOL, List.of(
                                Items.LAPIS_LAZULI,
                                Items.PISTON,
                                Blocks.HOPPER.asItem(),
                                Items.REDSTONE
                        )
                );

        registerAgingRecipes();
    }

    /**
     * The recipes the Chronarium can run. See
     * {@link de.ambertation.wunderreich.recipes.AgingRecipe}.
     */
    private static void registerAgingRecipes() {
        // A bucket of water is the universal catalyst - every aging recipe accepts one, and it is
        // never consumed: the bucket comes back out of the machine still full.
        //
        // Note a water bucket has a max stack size of 1, so the catalyst slot only ever holds one.
        // Anything seeding that slot (commands, tests, hoppers) has to account for that: asking for
        // more than one silently leaves the slot empty rather than erroring.
        final ItemLike[] waterCatalyst = {Items.WATER_BUCKET};
        // The mossy conversions additionally accept anything that grows moss. That is a *separate*
        // recipe per pair rather than one recipe with both catalysts, so each shows up on its own in
        // JEI/REI and can be toggled or retimed independently.
        final ItemLike[] mossCatalyst = {Blocks.VINE, Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET};
        final String MOSS = "_with_moss";

        createAgingRecipe(Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE, 400, waterCatalyst);
        createAgingRecipe(Blocks.COBBLESTONE_STAIRS, Blocks.MOSSY_COBBLESTONE_STAIRS, 400, waterCatalyst);
        createAgingRecipe(Blocks.COBBLESTONE_SLAB, Blocks.MOSSY_COBBLESTONE_SLAB, 200, waterCatalyst);
        createAgingRecipe(Blocks.COBBLESTONE_WALL, Blocks.MOSSY_COBBLESTONE_WALL, 400, waterCatalyst);

        createAgingRecipe(Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS, 600, waterCatalyst);
        createAgingRecipe(Blocks.STONE_BRICK_STAIRS, Blocks.MOSSY_STONE_BRICK_STAIRS, 600, waterCatalyst);
        createAgingRecipe(Blocks.STONE_BRICK_SLAB, Blocks.MOSSY_STONE_BRICK_SLAB, 300, waterCatalyst);
        createAgingRecipe(Blocks.STONE_BRICK_WALL, Blocks.MOSSY_STONE_BRICK_WALL, 600, waterCatalyst);

        createAgingVariant(Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE, 400, MOSS, mossCatalyst);
        createAgingVariant(Blocks.COBBLESTONE_STAIRS, Blocks.MOSSY_COBBLESTONE_STAIRS, 400, MOSS, mossCatalyst);
        createAgingVariant(Blocks.COBBLESTONE_SLAB, Blocks.MOSSY_COBBLESTONE_SLAB, 200, MOSS, mossCatalyst);
        createAgingVariant(Blocks.COBBLESTONE_WALL, Blocks.MOSSY_COBBLESTONE_WALL, 400, MOSS, mossCatalyst);

        createAgingVariant(Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS, 600, MOSS, mossCatalyst);
        createAgingVariant(Blocks.STONE_BRICK_STAIRS, Blocks.MOSSY_STONE_BRICK_STAIRS, 600, MOSS, mossCatalyst);
        createAgingVariant(Blocks.STONE_BRICK_SLAB, Blocks.MOSSY_STONE_BRICK_SLAB, 300, MOSS, mossCatalyst);
        createAgingVariant(Blocks.STONE_BRICK_WALL, Blocks.MOSSY_STONE_BRICK_WALL, 600, MOSS, mossCatalyst);

        // Grass spreads onto bare dirt. The catalyst here is literally the thing doing the
        // spreading, and it survives - which is exactly what a grass block does in the world.
        createAgingRecipe(Blocks.DIRT, Blocks.GRASS_BLOCK, 300, Blocks.GRASS_BLOCK);

        // Copper oxidizes in water. Every step up the chain costs more than the one before, but the
        // increase itself tapers off (3x, then 2x): the first tarnish is nearly free, and getting all
        // the way to oxidized is what you pay for.
        final int[] copperTimes = {100, 300, 600};

        createWeatheringChain(Blocks.COPPER_BLOCK, copperTimes, waterCatalyst);
        createWeatheringChain(Blocks.CUT_COPPER, copperTimes, waterCatalyst);
        createWeatheringChain(Blocks.CUT_COPPER_STAIRS, copperTimes, waterCatalyst);
        createWeatheringChain(Blocks.CUT_COPPER_SLAB, copperTimes, waterCatalyst);
    }

    /**
     * Ages the un-waxed states of a copper family one step at a time
     * (unaffected &rarr; exposed &rarr; weathered &rarr; oxidized). {@code times} holds one
     * duration per step.
     */
    private static void createWeatheringChain(
            WeatheringCopperCollection<Block> family,
            int[] times,
            ItemLike... catalyst
    ) {
        final var states = family.weathering();
        final Block[] chain = {states.unaffected(), states.exposed(), states.weathered(), states.oxidized()};

        for (int i = 0; i < chain.length - 1; i++) {
            createAgingRecipe(chain[i], chain[i + 1], times[Math.min(i, times.length - 1)], catalyst);
        }
    }

    /**
     * The experience one completed aging step is worth, derived from how long it takes.
     * <p>
     * Vanilla does not derive this at all - a smelting recipe simply carries an
     * {@code "experience"} float - so the only thing to get right is the resulting <i>range</i>,
     * and that it never has to be typed out twenty times by hand. The reference point is
     * {@link #AGING_XP_REFERENCE_TIME} ticks being worth {@link #AGING_XP_AT_REFERENCE_TIME},
     * which is what vanilla pays for a stone-tier smelt. From there the value grows with the
     * {@link #AGING_XP_EXPONENT}th power of the time rather than linearly, so doubling the
     * duration is worth roughly 1.6x, not 2x - long recipes stay worth doing without turning the
     * Chronarium into a better experience farm than a furnace.
     * <p>
     * With the durations we ship that yields 0.06 (100t) / 0.10 (200t) / 0.13 (300t) / 0.16 (400t)
     * / 0.20 (600t) per item.
     */
    private static final int AGING_XP_REFERENCE_TIME = 200;
    private static final float AGING_XP_AT_REFERENCE_TIME = 0.1f;
    private static final float AGING_XP_EXPONENT = 0.65f;

    /**
     * @see #AGING_XP_REFERENCE_TIME
     */
    private static float agingExperience(int time) {
        final double scale = Math.pow((double) time / AGING_XP_REFERENCE_TIME, AGING_XP_EXPONENT);
        // Two decimals: the value ends up in a JSON file, and 0.16000000238418579 helps nobody.
        return Math.round(AGING_XP_AT_REFERENCE_TIME * scale * 100.0) / 100f;
    }

    /**
     * Registers a single aging recipe. The recipe is named after the two blocks it converts, so
     * the name is stable and unique without having to be spelled out at every call site. The
     * experience follows from the time, see {@link #agingExperience(int)}.
     */
    private static void createAgingRecipe(Block input, Block result, int time, ItemLike... catalyst) {
        createAgingVariant(input, result, time, "", catalyst);
    }

    /**
     * A second (or third) way to make the same result, distinguished by its catalyst.
     * <p>
     * The recipe name is derived from the two blocks, so two recipes converting the same pair would
     * collide on the same id and one would silently replace the other. {@code variant} is appended
     * to keep them distinct - and it has to stay stable, because the id is also the key of the
     * per-recipe config toggle.
     */
    private static void createAgingVariant(
            Block input,
            Block result,
            int time,
            String variant,
            ItemLike... catalyst
    ) {
        final Identifier inputId = BuiltInRegistries.BLOCK.getKey(input);
        final Identifier resultId = BuiltInRegistries.BLOCK.getKey(result);
        if (inputId == null || resultId == null) return;

        AgingRecipeJsonBuilder
                .create(inputId.getPath() + "_to_" + resultId.getPath() + variant)
                .input(input)
                .catalyst(catalyst)
                .result(result)
                .time(time)
                .experience(agingExperience(time))
                .register();
    }

    public static void createSlabRecipe(String name, Block baseBlock, Block block) {
        RecipeJsonBuilder
                .create(name)
                .result(block)
                .pattern("***")
                .material('*', baseBlock)
                .count(6)
                .registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType.RECIPE_DECORATIONS);

        StonecutterJsonBuilder
                .create(name)
                .result(block)
                .ingredient(baseBlock)
                .count(2)
                .registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType.RECIPE_DECORATIONS);
    }

    public static void createStairsRecipe(String name, Block baseBlock, Block block) {
        RecipeJsonBuilder
                .create(name)
                .result(block)
                .pattern("*  ", "** ", "***")
                .material('*', baseBlock)
                .count(6)
                .registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType.RECIPE_DECORATIONS);

        StonecutterJsonBuilder
                .create(name)
                .result(block)
                .ingredient(baseBlock)
                .count(1)
                .registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType.RECIPE_DECORATIONS);
    }

    public static void createWallRecipe(String name, Block baseBlock, Block block) {
        RecipeJsonBuilder
                .create(name)
                .result(block)
                .pattern("***", "***")
                .material('*', baseBlock)
                .count(6)
                .registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType.RECIPE_DECORATIONS);

        StonecutterJsonBuilder
                .create(name)
                .result(block)
                .ingredient(baseBlock)
                .count(1)
                .registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType.RECIPE_DECORATIONS);
    }


    public static void createWoodWallRecipe(String name, Block baseBlock, Block fenceBlock, Block block) {
        RecipeJsonBuilder
                .create(name)
                .result(block)
                .pattern("* *", "|||")
                .material('*', baseBlock)
                .material('|', fenceBlock)
                .count(6)
                .registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType.RECIPE_DECORATIONS);

        StonecutterJsonBuilder
                .create(name)
                .result(block)
                .ingredient(baseBlock)
                .count(1)
                .registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType.RECIPE_DECORATIONS);
    }
}
