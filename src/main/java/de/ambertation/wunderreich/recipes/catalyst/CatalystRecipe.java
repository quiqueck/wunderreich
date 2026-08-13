package de.ambertation.wunderreich.recipes.catalyst;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

/**
 * A generic two-slot recipe: one {@code input} that is consumed, one {@code catalyst} that is
 * <b>not</b> consumed, a {@code time} in ticks the machine needs to process it, a {@code result}
 * and the {@code experience} a player is awarded for one completed result.
 * <p>
 * This class is deliberately free of any mod specific references so it can be lifted into a
 * shared library later. A concrete recipe type only has to extend it and supply its own
 * {@link RecipeType} and {@link RecipeSerializer} (see {@link #type(Identifier)},
 * {@link #mapCodec(Factory, int)} and {@link #streamCodec(Factory)}).
 * <p>
 * <b>The catalyst is never consumed.</b> This API intentionally offers no
 * {@code assembleRemaining}/{@code getRemainingItems} style contract, because there is nothing
 * to give back: an implementing machine keeps the catalyst stack exactly as it found it and only
 * shrinks the input stack.
 */
public abstract class CatalystRecipe implements Recipe<CatalystRecipeInput> {
    private final Ingredient input;
    private final Ingredient catalyst;
    private final ItemStackTemplate result;
    private final int time;
    private final float experience;

    private @Nullable PlacementInfo placementInfo;

    protected CatalystRecipe(
            Ingredient input,
            Ingredient catalyst,
            ItemStackTemplate result,
            int time,
            float experience
    ) {
        this.input = input;
        this.catalyst = catalyst;
        this.result = result;
        this.time = time;
        this.experience = experience;
    }

    /**
     * The ingredient that is consumed when the recipe finishes.
     */
    public Ingredient input() {
        return input;
    }

    /**
     * The ingredient that has to be present but is never consumed.
     */
    public Ingredient catalyst() {
        return catalyst;
    }

    /**
     * The (lazy) result template. Use {@link #resultStack()} to materialize it.
     */
    public ItemStackTemplate result() {
        return result;
    }

    /**
     * A fresh copy of the result. Only safe once item components are bound (i.e. at runtime,
     * not during the datapack prepare phase).
     */
    public ItemStack resultStack() {
        return result.create();
    }

    /**
     * The number of ticks a machine needs to process this recipe.
     */
    public int time() {
        return time;
    }

    /**
     * The experience awarded for <b>one</b> completed result, exactly like
     * {@code AbstractCookingRecipe#experience()}.
     * <p>
     * Vanilla does not derive this value from anything - a smelting recipe simply carries an
     * {@code "experience"} float - and neither do we. A machine is expected to accumulate the
     * per-completion values and only round once, when the experience is actually handed out; see
     * {@code AbstractFurnaceBlockEntity#createExperience}. {@code 0} means "no experience".
     */
    public float experience() {
        return experience;
    }

    /**
     * Tests both slots without needing a {@link Level}.
     */
    public boolean matches(CatalystRecipeInput in) {
        return input.test(in.input()) && catalyst.test(in.catalyst());
    }

    @Override
    public boolean matches(CatalystRecipeInput in, Level level) {
        return matches(in);
    }

    @Override
    public ItemStack assemble(CatalystRecipeInput in) {
        return resultStack();
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(List.of(input, catalyst));
        }

        return this.placementInfo;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public abstract RecipeSerializer<? extends CatalystRecipe> getSerializer();

    @Override
    public abstract RecipeType<? extends CatalystRecipe> getType();

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer(getType().toString());
        sb.append("{input=").append(input);
        sb.append(", catalyst=").append(catalyst);
        sb.append(", result=").append(result);
        sb.append(", time=").append(time);
        sb.append(", experience=").append(experience);
        sb.append('}');
        return sb.toString();
    }

    /**
     * Creates a named {@link RecipeType} for a concrete catalyst recipe. The type still has to be
     * registered, see {@link #register(Identifier, RecipeType, RecipeSerializer)}.
     */
    public static <T extends CatalystRecipe> RecipeType<T> type(Identifier id) {
        return new RecipeType<T>() {
            @Override
            public String toString() {
                return id.toString();
            }
        };
    }

    /**
     * Registers type and serializer under the same {@code id}. Call this from the mod initializer.
     */
    public static <T extends CatalystRecipe> void register(
            Identifier id,
            RecipeType<T> type,
            RecipeSerializer<T> serializer
    ) {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, serializer);
        Registry.register(BuiltInRegistries.RECIPE_TYPE, id, type);
    }

    /**
     * Looks up a matching recipe. Only works on the logical server, because only there does
     * {@link Level#recipeAccess()} give us a {@link RecipeManager}. Returns an empty optional on
     * the client or when nothing matches.
     */
    public static <T extends CatalystRecipe> Optional<RecipeHolder<T>> find(
            RecipeType<T> type,
            @Nullable Level level,
            ItemStack input,
            ItemStack catalyst
    ) {
        if (level == null || input.isEmpty() || catalyst.isEmpty()) return Optional.empty();
        if (!(level.recipeAccess() instanceof RecipeManager manager)) return Optional.empty();

        return manager.getRecipeFor(type, new CatalystRecipeInput(input, catalyst), level);
    }

    /**
     * The datapack format:
     * <pre>{@code
     * {
     *   "type": "<namespace>:<path>",
     *   "input": <ingredient>,
     *   "catalyst": <ingredient>,
     *   "result": {"id": "minecraft:mossy_cobblestone", "count": 1},
     *   "time": 200,
     *   "experience": 0.1
     * }
     * }</pre>
     * {@code time} is optional and falls back to {@code defaultTime}. {@code experience} is
     * optional too and defaults to {@code 0}, the same default (and the same field name) vanilla's
     * cooking recipes use, so a datapack that does not care about experience can leave it out.
     */
    public static <T extends CatalystRecipe> MapCodec<T> mapCodec(Factory<T> factory, int defaultTime) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("input").forGetter(CatalystRecipe::input),
                Ingredient.CODEC.fieldOf("catalyst").forGetter(CatalystRecipe::catalyst),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(CatalystRecipe::result),
                Codec.intRange(1, Integer.MAX_VALUE)
                     .optionalFieldOf("time", defaultTime)
                     .forGetter(CatalystRecipe::time),
                ExtraCodecs.NON_NEGATIVE_FLOAT
                        .optionalFieldOf("experience", 0.0f)
                        .forGetter(CatalystRecipe::experience)
        ).apply(instance, factory::create));
    }

    public static <T extends CatalystRecipe> StreamCodec<RegistryFriendlyByteBuf, T> streamCodec(Factory<T> factory) {
        return StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC,
                CatalystRecipe::input,
                Ingredient.CONTENTS_STREAM_CODEC,
                CatalystRecipe::catalyst,
                ItemStackTemplate.STREAM_CODEC,
                CatalystRecipe::result,
                ByteBufCodecs.VAR_INT,
                CatalystRecipe::time,
                ByteBufCodecs.FLOAT,
                CatalystRecipe::experience,
                factory::create
        );
    }

    @FunctionalInterface
    public interface Factory<T extends CatalystRecipe> {
        T create(Ingredient input, Ingredient catalyst, ItemStackTemplate result, int time, float experience);
    }
}
