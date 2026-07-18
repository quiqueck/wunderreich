package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.data_components.WhisperData;
import de.ambertation.wunderreich.data_components.WunderKisteData;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.function.UnaryOperator;
import org.jetbrains.annotations.ApiStatus;

public class WunderreichDataComponents {
    public static final DataComponentType<WunderKisteData> WUNDERKISTE = registerDataComponent(
            Wunderreich.ID("wunderkiste"),
            (DataComponentType.Builder<WunderKisteData> builder) -> builder
                    .persistent(WunderKisteData.CODEC)
                    .networkSynchronized(WunderKisteData.STREAM_CODEC)
    );

    public static final DataComponentType<WhisperData> WHISPERER = registerDataComponent(
            Wunderreich.ID("whispered_enchantment"),
            (DataComponentType.Builder<WhisperData> builder) -> builder
                    .persistent(WhisperData.CODEC)
                    .networkSynchronized(WhisperData.STREAM_CODEC)
    );

    private static <T> DataComponentType<T> registerDataComponent(
            Identifier id,
            UnaryOperator<DataComponentType.Builder<T>> builder
    ) {
        return Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                id,
                builder.apply(DataComponentType.builder()).build()
        );
    }

    @ApiStatus.Internal
    public static void ensureStaticallyLoaded() {
        // NO-OP
    }
}
