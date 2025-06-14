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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

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
    public final ResourceLocation id;

    private ImprinterRecipe(
            ResourceLocation id,
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
    private static ResourceLocation makeID(Holder<Enchantment> e) {
        final var eID = e.unwrapKey().orElseThrow().location();
        if (eID.getNamespace().equals("minecraft"))
            return Wunderreich.ID(Type.ID.getPath() + "/" + eID.getPath());
        return Wunderreich.ID(Type.ID.getPath() + "/" + eID.getNamespace() + "/" + eID.getPath());
    }

    public static RecipeManager GLOBAL_RECIPE_MANAGER;

    public static Stream<ImprinterRecipe> getAllVariants() {
        // In 1.21.6, use the recipe manager if available, otherwise fall back to static generation
        if (GLOBAL_RECIPE_MANAGER != null) {
            // In 1.21.6, we need to get all recipes and filter by type manually
            try {
                return GLOBAL_RECIPE_MANAGER
                        .getRecipes()
                        .stream()
                        .filter(recipeHolder -> recipeHolder != null && recipeHolder.value() != null)
                        .filter(recipeHolder -> recipeHolder.value().getType() == ImprinterRecipe.Type.INSTANCE)
                        .map(recipeHolder -> (ImprinterRecipe) recipeHolder.value())
                        .filter(r -> r.enchantment != null && r.enchantment.is(EnchantmentTags.TRADEABLE));
            } catch (Exception e) {
                // If recipe manager access fails, fall back to static registration
                Wunderreich.LOGGER.warn("Failed to access recipe manager, falling back to static recipes", e);
            }
        }

        // Fall back to static recipe generation when recipe manager is not available or fails
        return ImprinterRecipe
                .getRecipes()
                .stream()
                .filter(r -> r.enchantment != null && r.enchantment.is(EnchantmentTags.TRADEABLE));
    }

    public static List<ImprinterRecipe> getRecipes() {
        return getAllVariants().toList();
    }

    public static List<ImprinterRecipe> getUISortedRecipes() {
        return getAllVariants()
                .sorted(Comparator.comparing(a -> a.getCategory() + ":" + a.getName()))
                .collect(Collectors.toList());
    }

    @ApiStatus.Internal
    public static void register() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Serializer.ID, Serializer.INSTANCE);
        Registry.register(BuiltInRegistries.RECIPE_TYPE, Type.ID, Type.INSTANCE);
    }

    private static HolderLookup.Provider REGISTRY_PROVIDER_OR_NULL = null;

    @ApiStatus.Internal
    public static void registerForLevel(RecipeManager manager, HolderLookup.Provider provider) {
        // Store the recipe manager reference for later use in getAllVariants()
        GLOBAL_RECIPE_MANAGER = manager;

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
                                    ResourceLocation ID = makeID(e);
                                    if (Configs.RECIPE_CONFIG.newBooleanFor(ID.getPath(), ID).get())
                                        enchants.add(e);
                                });

                    enchants.sort(Comparator.comparing(a -> WhisperRule.getFullname(a)
                                                                       .getString()));

                    RegistryOps<JsonElement> registryOps = REGISTRY_PROVIDER_OR_NULL == null
                            ? null
                            : REGISTRY_PROVIDER_OR_NULL.createSerializationContext(JsonOps.INSTANCE);

                    enchants.forEach(e -> {
                        ImprinterRecipe r = new ImprinterRecipe(e);
                        RECIPES.add(r);
                        if (registryOps == null) {
                            Wunderreich.LOGGER.error("Registry provider is null. Can not create Imprinter Recipes.");
                            return;
                        }
                        var res = Serializer.CODEC_SERIALIZER.codec()
                                                             .encodeStart(registryOps, r);
                        if (res.isError()) {
                            Wunderreich.LOGGER.error("Error creating Imprinter Recipe: " + res
                                    .error()
                                    .get() + " for " + r.id);
                            return;
                        }
                        //Wunderreich.LOGGER.info("Created Imprinter Recipe: " + r + " for " + WhisperRule.getFullname(e));
                        WunderreichRecipes.RECIPES.put(r.id, res.getOrThrow());
                    });
                } catch (Exception e) {
                    Wunderreich.LOGGER.error("Error during imprinter recipe registration", e);
                }
            }
        }
    }

    // The getToastSymbol method is no longer part of the Recipe interface in 1.21.6
    // @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(WunderreichBlocks.WHISPER_IMPRINTER);
    }

    @Override
    public PlacementInfo placementInfo() {
        // Create placement info for the recipe ingredients
        return PlacementInfo.create(java.util.List.of(Ingredient.of(input.getItem()), WhisperRule.BLANK_INGREDIENT));
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
        return isRequiredItem(this.input, inv.ingredient);
    }

    @Override
    public boolean matches(ImprinterRecipe.ImprinterInput inv, Level level) {
        if (inv.size() < 2) return false;
        return isRequiredItem(this.input, inv.getItem(COST_A_SLOT)) && isRequiredItem(
                BLANK,
                inv.getItem(COST_B_SLOT)
        ) ||
                isRequiredItem(this.input, inv.getItem(COST_B_SLOT)) && isRequiredItem(BLANK, inv.getItem(COST_A_SLOT));
    }

    @Override
    public ItemStack assemble(ImprinterRecipe.ImprinterInput recipeInput, HolderLookup.Provider provider) {
        return this.output.copy();
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
        public static final ResourceLocation ID = Wunderreich.ID("imprinter");
        public static final RecipeType<ImprinterRecipe> INSTANCE = new Type();

        Type() {
        }

        @Override
        public String toString() {
            return ID.toString();
        }
    }

    private static class Serializer implements RecipeSerializer<ImprinterRecipe> {
        private static final MapCodec<ImprinterRecipe> CODEC_SERIALIZER = RecordCodecBuilder.mapCodec(instance -> instance
                .group(
                        Codec.STRING.fieldOf("type").forGetter(r -> Type.ID.toString()),
                        ResourceLocation.CODEC.fieldOf("id").forGetter(r -> r.id),
                        ResourceLocation.CODEC
                                .fieldOf("enchantment")
                                .forGetter(r -> r.enchantment.unwrapKey().orElseThrow().location()),
                        ItemStack.CODEC.fieldOf("input").forGetter(r -> r.input),
                        ItemStack.CODEC.fieldOf("output").forGetter(r -> r.output),
                        Codec.INT.fieldOf("baseXP").forGetter(r -> r.baseXP),
                        ItemStack.CODEC.optionalFieldOf("icon", ItemStack.EMPTY).forGetter(r -> r.icon)
                )
                .apply(instance, (t, a, b, d, e, f, g) -> null));

        public static final MapCodec<ImprinterRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(r -> r.id),
                Enchantment.CODEC.fieldOf("enchantment").forGetter(r -> r.enchantment),
                ItemStack.CODEC.fieldOf("input").forGetter(r -> r.input),
                ItemStack.CODEC.fieldOf("output").forGetter(r -> r.output),
                Codec.INT.fieldOf("baseXP").forGetter(r -> r.baseXP),
                ItemStack.CODEC.optionalFieldOf("icon", ItemStack.EMPTY).forGetter(r -> r.icon)
        ).apply(instance, ImprinterRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, ImprinterRecipe> STREAM_CODEC = StreamCodec.of(
                ImprinterRecipe.Serializer::toNetwork,
                ImprinterRecipe.Serializer::fromNetwork
        );

        public final static ResourceLocation ID = Type.ID;
        public final static Serializer INSTANCE = new Serializer();

        @Override
        public @NotNull MapCodec<ImprinterRecipe> codec() {
            return CODEC;
        }


        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ImprinterRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        public static @NotNull ImprinterRecipe fromNetwork(RegistryFriendlyByteBuf packetBuffer) {
            ResourceLocation id = packetBuffer.readResourceLocation();
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
            packetBuffer.writeResourceLocation(recipe.id);
            Enchantment.STREAM_CODEC.encode(packetBuffer, recipe.enchantment);
            ItemStack.STREAM_CODEC.encode(packetBuffer, recipe.input);
            ItemStack.STREAM_CODEC.encode(packetBuffer, recipe.output);
            packetBuffer.writeVarInt(recipe.baseXP);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(packetBuffer, recipe.icon);
        }
    }
}
