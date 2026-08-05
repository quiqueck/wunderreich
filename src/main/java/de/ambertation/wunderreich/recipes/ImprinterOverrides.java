package de.ambertation.wunderreich.recipes;

import de.ambertation.wunderreich.Wunderreich;

import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.enchantment.Enchantment;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads and applies the datapack-driven override layer for the auto-generated imprinter recipes.
 * <p>
 * Override files live at {@code data/<ench_namespace>/wunderreich/imprinter_overrides/<ench_path>.json}
 * i.e. keyed by the SOURCE ENCHANTMENT's namespace. The {@link FileToIdConverter} therefore maps a
 * file such as {@code data/minecraft/wunderreich/imprinter_overrides/sharpness.json} to the resource
 * id {@code minecraft:sharpness} — a 1:1 enchantment→resource id, so multiple packs resolve via the
 * normal pack ordering (last wins), with no custom merge/priority.
 * <p>
 * The raw JSON is stored as-is at load time; the {@link ImprinterOverride} (which contains
 * {@link net.minecraft.world.item.ItemStack}s) is only decoded lazily at true runtime — see
 * {@link #get(Holder)} — to avoid materializing ItemStacks during the async recipe reload where
 * item data components are not yet bound.
 */
public class ImprinterOverrides {
    public static final String DIRECTORY = "wunderreich/imprinter_overrides";
    private static final FileToIdConverter CONVERTER = new FileToIdConverter(DIRECTORY, ".json");

    private static volatile Map<Identifier, JsonElement> RAW = Map.of();
    private static volatile ResourceManager lastLoaded = null;
    private static volatile HolderLookup.Provider registryProvider = null;

    private ImprinterOverrides() {
    }

    /**
     * The registry provider used to decode overridden {@link net.minecraft.world.item.ItemStack}s.
     * Set from {@link ImprinterRecipe#registerForLevel} during the same reload the recipes are built.
     */
    public static void setRegistryProvider(HolderLookup.Provider provider) {
        registryProvider = provider;
    }

    /**
     * Read all override files from the given resource manager once per reload. Only the raw JSON is
     * parsed here (cheap, no ItemStack materialization), so this is safe to call from the recipe
     * reload's async prepare phase.
     */
    public static synchronized void ensureLoaded(ResourceManager resourceManager) {
        if (resourceManager == null || resourceManager == lastLoaded) return;
        load(resourceManager);
    }

    /**
     * Force a (re)read of all override files. Callers should normally use {@link #ensureLoaded}.
     */
    public static synchronized void load(ResourceManager resourceManager) {
        final Map<Identifier, JsonElement> map = new HashMap<>();
        for (Map.Entry<Identifier, Resource> entry : CONVERTER.listMatchingResources(resourceManager).entrySet()) {
            final Identifier enchantmentId = CONVERTER.fileToId(entry.getKey());
            try (BufferedReader reader = entry.getValue().openAsReader()) {
                map.put(enchantmentId, JsonParser.parseReader(reader));
            } catch (Exception e) {
                Wunderreich.LOGGER.error(
                        "Failed to read imprinter override " + entry.getKey() + " for enchantment " + enchantmentId,
                        e
                );
            }
        }
        RAW = map;
        lastLoaded = resourceManager;
        if (!map.isEmpty()) {
            Wunderreich.LOGGER.info("Loaded " + map.size() + " imprinter override(s)");
        }
    }

    /**
     * Cheap, ItemStack-free check whether the auto-generated imprinter for {@code enchantmentId}
     * is suppressed. Reads the {@code enabled}/{@code disabled} boolean directly from the raw JSON,
     * so it is safe to call while building recipes (before components are bound).
     */
    public static boolean isDisabled(Identifier enchantmentId) {
        final JsonElement element = RAW.get(enchantmentId);
        if (element == null || !element.isJsonObject()) return false;
        final JsonObject object = element.getAsJsonObject();
        if (object.has("disabled") && object.get("disabled").isJsonPrimitive()
                && object.getAsJsonPrimitive("disabled").isBoolean()) {
            if (object.get("disabled").getAsBoolean()) return true;
        }
        if (object.has("enabled") && object.get("enabled").isJsonPrimitive()
                && object.getAsJsonPrimitive("enabled").isBoolean()) {
            return !object.get("enabled").getAsBoolean();
        }
        return false;
    }

    /**
     * Decode the override (if any) for the given enchantment. This materializes ItemStacks, so it
     * MUST only be called lazily at runtime (never during the async recipe reload). Returns
     * {@link ImprinterOverride#EMPTY} when there is no override, no registry provider is available,
     * or the JSON fails to decode.
     */
    public static ImprinterOverride get(Holder<Enchantment> enchantment) {
        if (enchantment == null || enchantment.unwrapKey().isEmpty()) return ImprinterOverride.EMPTY;
        final Identifier enchantmentId = enchantment.unwrapKey().get().identifier();
        final JsonElement element = RAW.get(enchantmentId);
        if (element == null) return ImprinterOverride.EMPTY;

        final HolderLookup.Provider provider = registryProvider;
        if (provider == null) {
            Wunderreich.LOGGER.warn("Imprinter override for " + enchantmentId + " skipped: no registry provider yet");
            return ImprinterOverride.EMPTY;
        }

        final RegistryOps<JsonElement> ops = provider.createSerializationContext(JsonOps.INSTANCE);
        return ImprinterOverride.CODEC
                .parse(ops, element)
                .resultOrPartial(err -> Wunderreich.LOGGER.error(
                        "Failed to decode imprinter override for " + enchantmentId + ": " + err))
                .orElse(ImprinterOverride.EMPTY);
    }
}
