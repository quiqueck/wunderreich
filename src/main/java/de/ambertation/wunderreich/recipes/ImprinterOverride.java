package de.ambertation.wunderreich.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * A datapack-driven, partial override for an auto-generated {@link ImprinterRecipe}.
 * <p>
 * Every field is optional: a field that is present in the JSON replaces the auto-generated
 * value, while an absent field keeps the auto-generated value (partial merge). The
 * {@link #enabled} / {@code disabled} flag can be used to suppress the auto-generated
 * imprinter entirely.
 * <p>
 * NOTE (26.1): {@link ItemStack} decoding reads the item's bound data components. This record
 * must therefore only be decoded lazily, at true runtime (e.g. when the whisperer GUI opens or a
 * recipe is synced), NEVER during the async datapack reload's prepare phase where components are
 * not yet bound. {@link #EMPTY} contains no {@link ItemStack}s and is safe at any time.
 */
public record ImprinterOverride(
        Optional<ItemStack> input,
        Optional<ItemStack> output,
        Optional<Integer> baseXP,
        Optional<ItemStack> icon,
        Optional<Boolean> enabled,
        Optional<Boolean> disabled
) {
    public static final ImprinterOverride EMPTY = new ImprinterOverride(
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
    );

    public static final Codec<ImprinterOverride> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    // "input" doubles as the trade/imprint cost (item + count the player pays).
                    ItemStack.CODEC.optionalFieldOf("input").forGetter(ImprinterOverride::input),
                    ItemStack.CODEC.optionalFieldOf("output").forGetter(ImprinterOverride::output),
                    Codec.INT.optionalFieldOf("baseXP").forGetter(ImprinterOverride::baseXP),
                    ItemStack.CODEC.optionalFieldOf("icon").forGetter(ImprinterOverride::icon),
                    Codec.BOOL.optionalFieldOf("enabled").forGetter(ImprinterOverride::enabled),
                    Codec.BOOL.optionalFieldOf("disabled").forGetter(ImprinterOverride::disabled)
            )
            .apply(instance, ImprinterOverride::new));

    /**
     * @return {@code true} if this override suppresses the auto-generated imprinter
     * ({@code "enabled": false} or {@code "disabled": true}).
     */
    public boolean isDisabled() {
        if (disabled.orElse(false)) return true;
        return !enabled.orElse(true);
    }
}
