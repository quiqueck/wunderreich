package de.ambertation.wunderreich.recipes;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.gui.whisperer.WhisperRule;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.registries.WunderreichRecipes;
import de.ambertation.wunderreich.registries.WunderreichRules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;

import com.google.gson.JsonElement;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ImprinterRecipe extends WhisperRule implements Recipe<ImprinterRecipe.ImprinterInput> {
    public record ImprinterInput(ItemStack ingredient, ItemStack whisperer) implements RecipeInput {
        @Override
        public ItemStack getItem(int i) {
            if (i == 0) return ingredient;
            if (i == 1) return whisperer;
            return ItemStack.EMPTY;
        }

        @Override
        public int size() {
            return 2;
        }

        public boolean hasWhisperer() {
            return !whisperer.isEmpty() && whisperer.is(WunderreichItems.BLANK_WHISPERER);
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("ImprinterInput{");
            sb.append("ingredient=").append(ingredient);
            sb.append(", whisperer=").append(whisperer);
            sb.append('}');
            return sb.toString();
        }
    }

    public static final int COST_A_SLOT = 0;
    public static final int COST_B_SLOT = 1;
    private static final List<ImprinterRecipe> RECIPES = new LinkedList<>();
    public final Identifier id;

    private ImprinterRecipe(
            Identifier id,
            Holder<Enchantment> enchantment,
            ItemStack input,
            ItemStack output,
            int baseXP,
            ItemStack type
    ) {
        super(enchantment, input, output, baseXP, type);
        this.id = id;
    }

    private ImprinterRecipe(Holder<Enchantment> e) {
        super(e);

        this.id = makeID(e);
    }

    @NotNull
    private static Identifier makeID(Holder<Enchantment> e) {
        final var eID = e.unwrapKey().orElseThrow().identifier();
        if (eID.getNamespace().equals("minecraft"))
            return Wunderreich.ID(Type.ID.getPath() + "/" + eID.getPath());
        return Wunderreich.ID(Type.ID.getPath() + "/" + eID.getNamespace() + "/" + eID.getPath());
    }

    /**
     * Every imprinter recipe the given level knows about.
     * <p>
     * The list is read through Fabric's recipe sync (the serializer is opted in from
     * {@link #register()}), so this works on <b>both</b> logical sides: {@code Level#recipeAccess()}
     * is the server's {@code RecipeManager} on a {@link net.minecraft.server.level.ServerLevel} and a
     * {@code ClientRecipeContainer} on a client, and both expose a synchronized view. On a client
     * that view holds exactly the recipes the server sent for our serializer, which is why this
     * returns the full list on a <i>dedicated</i> server as well - reaching for
     * {@code Minecraft#getSingleplayerServer()} would silently yield nothing there.
     *
     * @param level the level to read from, usually {@code Minecraft.getInstance().level} on the
     *              client and {@code player.level()} on the server. A {@code null} level, or one
     *              whose sync has not arrived yet, falls back to the recipes this JVM generated in
     *              {@link #registerForLevel} - which are only populated where the logical server
     *              runs.
     */
    public static Stream<ImprinterRecipe> getAllVariants(@Nullable Level level) {
        if (level != null) {
            try {
                final var synced = level.recipeAccess()
                                        .getSynchronizedRecipes()
                                        .getAllOfType(Type.INSTANCE);
                if (!synced.isEmpty()) {
                    return synced
                            .stream()
                            .filter(holder -> holder != null && holder.value() != null)
                            .map(RecipeHolder::value)
                            .filter(r -> r.enchantment != null);
                }
            } catch (Throwable t) {
                Wunderreich.LOGGER.warn("Failed to enumerate imprinter recipes, falling back to static recipes", t);
            }
        }

        return RECIPES
                .stream()
                .filter(r -> r.enchantment != null);
    }

    public static List<ImprinterRecipe> getRecipes(@Nullable Level level) {
        return getAllVariants(level).toList();
    }

    public static List<ImprinterRecipe> getUISortedRecipes(@Nullable Level level) {
        return getAllVariants(level)
                .sorted(Comparator.comparing(a -> a.getCategory() + ":" + a.getName()))
                .collect(Collectors.toList());
    }

    @ApiStatus.Internal
    public static void register() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Serializer.ID, Serializer.INSTANCE);
        Registry.register(BuiltInRegistries.RECIPE_TYPE, Type.ID, Type.INSTANCE);

        // Vanilla only ships recipe *book* display data to a joining player, so without this the
        // client would know no imprinter recipes at all. Opting the serializer into Fabric's recipe
        // sync is what makes them exist client side - and therefore what makes the imprinter GUI and
        // the recipe viewers work against a dedicated server. This has to happen on both sides, so
        // it lives in the common register() and not in a client initializer.
        RecipeSynchronization.synchronizeRecipeSerializer(Serializer.INSTANCE);
    }

    private static HolderLookup.Provider REGISTRY_PROVIDER_OR_NULL = null;

    @ApiStatus.Internal
    public static void registerForLevel(HolderLookup.Provider provider) {
        // Make the loaded datapack override layer available to the lazy WhisperRule suppliers
        // (they decode overridden ItemStacks with this provider at runtime).
        ImprinterOverrides.setRegistryProvider(provider);

        // Avoid re-registering with the same provider to prevent duplicate recipes
        if (provider == REGISTRY_PROVIDER_OR_NULL) return;
        REGISTRY_PROVIDER_OR_NULL = provider;
        RECIPES.clear();

        if (WunderreichRules.Whispers.allowLibrarianSelection()) {
            List<Holder<Enchantment>> enchants = new LinkedList<>();
            final var enchantments = provider.lookup(Registries.ENCHANTMENT).orElse(null);
            if (enchantments != null) {
                try {
                    enchantments.listElements()
                                .forEach(e -> {
                                    // Datapack "enabled": false / "disabled": true suppresses this
                                    // auto-generated imprinter entirely. Keyed by the enchantment id
                                    // and read without materializing any ItemStack (safe here).
                                    final Identifier enchantmentId = e.unwrapKey().orElseThrow().identifier();
                                    if (ImprinterOverrides.isDisabled(enchantmentId)) return;
                                    Identifier ID = makeID(e);
                                    if (Configs.RECIPE_CONFIG.newBooleanFor(ID.getPath(), ID).get())
                                        enchants.add(e);
                                });

                    enchants.sort(Comparator.comparing(a -> WhisperRule.getFullname(a)
                                                                       .getString()));

                    // Build the recipe objects. Their ItemStacks are materialized lazily, so this
                    // is safe during the datapack reload's async prepare phase (item data
                    // components are not bound yet). The recipes are injected into the vanilla
                    // recipe map directly by RecipeManagerMixin (no JSON round-trip, which would
                    // force premature ItemStack materialization via the codec).
                    enchants.forEach(e -> RECIPES.add(new ImprinterRecipe(e)));
                } catch (Exception e) {
                    Wunderreich.LOGGER.error("Error during imprinter recipe registration", e);
                }
            }
        }
    }

    @ApiStatus.Internal
    public static List<ImprinterRecipe> getRegisteredRecipes() {
        return RECIPES;
    }

    // The getToastSymbol method is no longer part of the Recipe interface in 1.21.6
    // @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(WunderreichBlocks.WHISPER_IMPRINTER);
    }

    @Override
    public PlacementInfo placementInfo() {
        // Create placement info for the recipe ingredients
        return PlacementInfo.create(java.util.List.of(Ingredient.of(getInput().getItem()), WhisperRule.blankIngredient()));
    }

    @Override
    public java.util.List<RecipeDisplay> display() {
        // Return empty list for default display behavior
        return java.util.List.of();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        // Use an appropriate recipe book category that exists in 1.21.6
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public boolean canBuildFrom(@Nullable ImprinterRecipe.ImprinterInput inv) {
        if (inv == null || !inv.hasWhisperer()) return false;
        return isRequiredItem(this.getInput(), inv.ingredient);
    }

    @Override
    public boolean matches(ImprinterRecipe.ImprinterInput inv, Level level) {
        if (inv.size() < 2) return false;
        return isRequiredItem(this.getInput(), inv.getItem(COST_A_SLOT)) && isRequiredItem(
                blank(),
                inv.getItem(COST_B_SLOT)
        ) ||
                isRequiredItem(this.getInput(), inv.getItem(COST_B_SLOT)) && isRequiredItem(blank(), inv.getItem(COST_A_SLOT));
    }

    @Override
    public ItemStack assemble(ImprinterRecipe.ImprinterInput recipeInput) {
        return this.getOutput().copy();
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public @NotNull RecipeSerializer<? extends Recipe<ImprinterInput>> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<? extends Recipe<ImprinterInput>> getType() {
        return Type.INSTANCE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImprinterRecipe)) return false;
        ImprinterRecipe that = (ImprinterRecipe) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return this.id.toString();
    }

    public static class Type implements RecipeType<ImprinterRecipe> {
        public static final Identifier ID = Wunderreich.ID("imprinter");
        public static final RecipeType<ImprinterRecipe> INSTANCE = new Type();

        Type() {
        }

        @Override
        public String toString() {
            return ID.toString();
        }
    }

    private static class Serializer {
        private static final MapCodec<ImprinterRecipe> CODEC_SERIALIZER = RecordCodecBuilder.mapCodec(instance -> instance
                .group(
                        Codec.STRING.fieldOf("type").forGetter(r -> Type.ID.toString()),
                        Identifier.CODEC.fieldOf("id").forGetter(r -> r.id),
                        Identifier.CODEC
                                .fieldOf("enchantment")
                                .forGetter(r -> r.enchantment.unwrapKey().orElseThrow().identifier()),
                        ItemStack.CODEC.fieldOf("input").forGetter(r -> r.getInput()),
                        ItemStack.CODEC.fieldOf("output").forGetter(r -> r.getOutput()),
                        Codec.INT.fieldOf("baseXP").forGetter(r -> r.getBaseXP()),
                        ItemStack.CODEC.optionalFieldOf("icon", ItemStack.EMPTY).forGetter(r -> r.getIcon())
                )
                .apply(instance, (t, a, b, d, e, f, g) -> null));

        public static final MapCodec<ImprinterRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("id").forGetter(r -> r.id),
                Enchantment.CODEC.fieldOf("enchantment").forGetter(r -> r.enchantment),
                ItemStack.CODEC.fieldOf("input").forGetter(r -> r.getInput()),
                ItemStack.CODEC.fieldOf("output").forGetter(r -> r.getOutput()),
                Codec.INT.fieldOf("baseXP").forGetter(r -> r.getBaseXP()),
                ItemStack.CODEC.optionalFieldOf("icon", ItemStack.EMPTY).forGetter(r -> r.getIcon())
        ).apply(instance, ImprinterRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, ImprinterRecipe> STREAM_CODEC = StreamCodec.of(
                ImprinterRecipe.Serializer::toNetwork,
                ImprinterRecipe.Serializer::fromNetwork
        );

        public final static Identifier ID = Type.ID;
        public final static RecipeSerializer<ImprinterRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        public static @NotNull ImprinterRecipe fromNetwork(RegistryFriendlyByteBuf packetBuffer) {
            Identifier id = packetBuffer.readIdentifier();
            Holder<Enchantment> e = Enchantment.STREAM_CODEC.decode(packetBuffer);
            ItemStack input = ItemStack.STREAM_CODEC.decode(packetBuffer);
            ItemStack output = ItemStack.STREAM_CODEC.decode(packetBuffer);
            int baseXP = packetBuffer.readVarInt();
            ItemStack type = ItemStack.OPTIONAL_STREAM_CODEC.decode(packetBuffer);

            return new ImprinterRecipe(id, e, input, output, baseXP, type);
        }


        public static void toNetwork(RegistryFriendlyByteBuf packetBuffer, ImprinterRecipe recipe) {
//            if (recipe.input.isEmpty()) Wunderreich.LOGGER.error("Recipe " + recipe.id + " has no input");
//            if (recipe.output.isEmpty()) Wunderreich.LOGGER.error("Recipe " + recipe.id + " has no output");
//            if (recipe.icon.isEmpty()) Wunderreich.LOGGER.error("Recipe " + recipe.id + " has no icon");
            packetBuffer.writeIdentifier(recipe.id);
            Enchantment.STREAM_CODEC.encode(packetBuffer, recipe.enchantment);
            ItemStack.STREAM_CODEC.encode(packetBuffer, recipe.getInput());
            ItemStack.STREAM_CODEC.encode(packetBuffer, recipe.getOutput());
            packetBuffer.writeVarInt(recipe.getBaseXP());
            ItemStack.OPTIONAL_STREAM_CODEC.encode(packetBuffer, recipe.getIcon());
        }
    }
}
