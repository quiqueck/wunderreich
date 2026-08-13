package de.ambertation.wunderreich.recipes;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.recipes.catalyst.CatalystRecipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
            ItemStackTemplate result,
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

    @ApiStatus.Internal
    public static void register() {
        CatalystRecipe.register(Type.ID, Type.INSTANCE, Serializer.INSTANCE);

        // Vanilla stopped shipping the full recipe list to the client; a joining player only
        // receives recipe *book* display data. Opting the serializer into Fabric's recipe sync is
        // what makes the aging recipes exist on the client at all - and therefore what makes them
        // enumerable by JEI/REI/EMI. This has to happen on both sides, so it lives in the common
        // register() and not in a client initializer.
        RecipeSynchronization.synchronizeRecipeSerializer(Serializer.INSTANCE);
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
     * This works on <b>both</b> logical sides, which {@link #find(Level, ItemStack, ItemStack)}
     * does not: {@code Level#recipeAccess()} is a {@code RecipeManager} only on a
     * {@link net.minecraft.server.level.ServerLevel}; on a client it is a
     * {@code ClientRecipeContainer}. Fabric's recipe sync bridges that gap - both implement
     * {@code FabricRecipeAccess}, and on the client the returned view holds exactly the recipes the
     * server sent for the serializers registered in {@link #register()}. That is why this also
     * returns the full list on a client connected to a <i>dedicated</i> server, where reaching for
     * {@code Minecraft#getSingleplayerServer()} would silently yield nothing.
     *
     * @param level the level to read from, usually {@code Minecraft.getInstance().level}. A
     *              {@code null} level (no world loaded) yields an empty list.
     */
    public static List<RecipeHolder<AgingRecipe>> getUISortedRecipes(@Nullable Level level) {
        if (level == null) return List.of();

        try {
            return level.recipeAccess()
                        .getSynchronizedRecipes()
                        .getAllOfType(Type.INSTANCE)
                        .stream()
                        .filter(holder -> holder != null && holder.value() != null)
                        // Ids read "<input>_to_<result>", so this groups the families together.
                        .sorted(Comparator.comparing(holder -> holder.id().identifier().toString()))
                        .toList();
        } catch (Throwable t) {
            Wunderreich.LOGGER.warn("Unable to enumerate aging recipes", t);
            return List.of();
        }
    }

    public static class Type {
        public static final Identifier ID = Wunderreich.ID("aging");
        public static final RecipeType<AgingRecipe> INSTANCE = CatalystRecipe.type(ID);

        private Type() {
        }
    }

    public static class Serializer {
        public static final MapCodec<AgingRecipe> CODEC = CatalystRecipe.mapCodec(AgingRecipe::new, DEFAULT_TIME);
        public static final StreamCodec<RegistryFriendlyByteBuf, AgingRecipe> STREAM_CODEC = CatalystRecipe.streamCodec(
                AgingRecipe::new);

        public static final Identifier ID = Type.ID;
        public static final RecipeSerializer<AgingRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        private Serializer() {
        }
    }
}
