package de.ambertation.wunderreich.recipes;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.recipes.catalyst.CatalystRecipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * The recipe type used by the Chronarium: an {@code input} item is aged into a {@code result}
 * over {@code time} ticks while a {@code catalyst} item sits in the machine. Each completed result
 * is worth {@code experience}, which the machine hands out when a player takes the output - exactly
 * like a furnace.
 * <p>
 * The catalyst is <b>never</b> consumed, see {@link CatalystRecipe}.
 */
public class AgingRecipe extends CatalystRecipe {
    /**
     * Ticks a Chronarium needs when a recipe does not declare a {@code time}.
     */
    public static final int DEFAULT_TIME = 200;

    public AgingRecipe(
            Ingredient input,
            Ingredient catalyst,
            ItemStack result,
            int time,
            float experience
    ) {
        super(input, catalyst, result, time, experience);
    }

    @Override
    public RecipeSerializer<AgingRecipe> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<AgingRecipe> getType() {
        return Type.INSTANCE;
    }

    /**
     * Set once from client-only code (see {@code WunderreichClient}) so this common class never has
     * to reference {@code Minecraft} directly - the same pattern {@code ImprinterRecipe} uses.
     * <p>
     * 1.21.6/1.21.8 has no equivalent of 26.2's Fabric {@code RecipeSynchronization} (that
     * fabric-recipe-api-v1 module does not exist for this branch's Fabric API version - see the port
     * notes on {@link #getUISortedRecipes}). Vanilla itself still sends every decoded
     * {@link RecipeHolder} to the client on this version via the classic recipe packet, which is what
     * makes {@code ClientPacketListener#recipes()} usable here at all.
     */
    @ApiStatus.Internal
    public static Supplier<RecipeManager> CLIENT_RECIPE_MANAGER_SUPPLIER;

    @ApiStatus.Internal
    public static void register() {
        CatalystRecipe.register(Type.ID, Type.INSTANCE, Serializer.INSTANCE);
    }

    /**
     * Finds the aging recipe for the given slot contents. Server side only, see
     * {@link CatalystRecipe#find(RecipeType, Level, ItemStack, ItemStack)}.
     */
    public static Optional<RecipeHolder<AgingRecipe>> find(
            @Nullable Level level,
            ItemStack input,
            ItemStack catalyst
    ) {
        return CatalystRecipe.find(Type.INSTANCE, level, input, catalyst);
    }

    /**
     * Every known aging recipe, in a stable order suitable for a recipe viewer.
     * <p>
     * <b>Porting note (26.2 -&gt; 1.21.6):</b> the 26.2 version reads
     * {@code level.recipeAccess().getSynchronizedRecipes()}, backed by Fabric's
     * {@code net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization}. That module does not
     * exist in the fabric-recipe-api-v1 shipped with this branch's Fabric API (checked against the
     * actual jar: fabric-recipe-api-v1 8.1.14, bundled in fabric-api 0.130.0+1.21.8, only offers
     * {@code CustomIngredient}/{@code FabricServerRecipeManager} - no {@code sync} package at all).
     * <p>
     * It is also not needed here: on 1.21.6/1.21.8 vanilla itself still sends every decoded
     * {@link RecipeHolder} to the client via the classic recipe packet (this is what makes
     * {@code ClientPacketListener#recipes()} return a full {@link RecipeManager} - see
     * {@code ImprinterRecipe#CLIENT_RECIPE_MANAGER_SUPPLIER} for the established pattern this
     * mirrors). This method therefore tries the level's own recipe access first (works on the
     * logical server, and possibly the client depending on level type), then falls back to
     * {@link #CLIENT_RECIPE_MANAGER_SUPPLIER}, wired from client-only code so this common-sourceset
     * class never references {@code Minecraft} directly.
     *
     * @param level the level to read from, usually {@code Minecraft.getInstance().level}. A
     *              {@code null} level (no world loaded) is fine - the client fallback does not need
     *              one.
     */
    public static List<RecipeHolder<AgingRecipe>> getUISortedRecipes(@Nullable Level level) {
        RecipeManager manager = null;
        if (level != null && level.recipeAccess() instanceof RecipeManager rm) {
            manager = rm;
        }
        if (manager == null && CLIENT_RECIPE_MANAGER_SUPPLIER != null) {
            try {
                manager = CLIENT_RECIPE_MANAGER_SUPPLIER.get();
            } catch (Throwable ignored) {
            }
        }
        if (manager == null) return List.of();

        try {
            // Filtered from the whole collection (mirrors ImprinterRecipe#getAllVariants) rather
            // than RecipeManager#getAllRecipesFor(RecipeType), which on this branch's RecipeManager
            // does not have a public overload keyed by our custom RecipeType.
            return manager.getRecipes()
                          .stream()
                          .filter(holder -> holder != null && holder.value() instanceof AgingRecipe)
                          .map(holder -> (RecipeHolder<AgingRecipe>) (RecipeHolder<?>) holder)
                          // Ids read "<input>_to_<result>", so this groups the families together.
                          .sorted(Comparator.comparing(holder -> holder.id().location().toString()))
                          .toList();
        } catch (Throwable t) {
            Wunderreich.LOGGER.warn("Unable to enumerate aging recipes", t);
            return List.of();
        }
    }

    public static class Type {
        public static final ResourceLocation ID = Wunderreich.ID("aging");
        public static final RecipeType<AgingRecipe> INSTANCE = CatalystRecipe.type(ID);

        private Type() {
        }
    }

    public static class Serializer implements RecipeSerializer<AgingRecipe> {
        public static final MapCodec<AgingRecipe> CODEC = CatalystRecipe.mapCodec(AgingRecipe::new, DEFAULT_TIME);
        public static final StreamCodec<RegistryFriendlyByteBuf, AgingRecipe> STREAM_CODEC = CatalystRecipe.streamCodec(
                AgingRecipe::new);

        public static final ResourceLocation ID = Type.ID;
        // 1.21.6's RecipeSerializer is a plain interface (no all-args constructor like 26.2's), so
        // this needs a small concrete implementation - the same shape as ImprinterRecipe.Serializer.
        public static final RecipeSerializer<AgingRecipe> INSTANCE = new Serializer();

        private Serializer() {
        }

        @Override
        public MapCodec<AgingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AgingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
