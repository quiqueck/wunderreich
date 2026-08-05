package de.ambertation.wunderreich.network;

import de.ambertation.wunderlib.network.NetworkRegistry;
import de.ambertation.wunderlib.network.ServerBoundMessage;
import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.gui.whisperer.WhispererMenu;
import de.ambertation.wunderreich.recipes.ImprinterRecipe;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Optional;

public record SelectWhisperMessage(Identifier ruleID) {
    private static final StreamCodec<RegistryFriendlyByteBuf, Optional<Identifier>> OPTIONAL_ID_CODEC =
            ByteBufCodecs.optional(Identifier.STREAM_CODEC);

    private static final StreamCodec<RegistryFriendlyByteBuf, SelectWhisperMessage> CODEC = OPTIONAL_ID_CODEC.map(
            opt -> new SelectWhisperMessage(opt.orElse(null)),
            msg -> Optional.ofNullable(msg.ruleID)
    );

    public static final ServerBoundMessage<SelectWhisperMessage> KEY = NetworkRegistry.registerServerBound(
            Wunderreich.ID("select_whisper"),
            CODEC,
            (msg, ctx) -> {
                AbstractContainerMenu abstractContainerMenu = ctx.player().containerMenu;

                if (abstractContainerMenu instanceof WhispererMenu menu) {
                    Wunderreich.LOGGER.info("Selecting whisperer recipe: " + msg.ruleID);
                    ImprinterRecipe selected = menu.selectByID(msg.ruleID);
                    menu.tryMoveItems(selected);
                }
            }
    );

    public static void send(Identifier ruleID) {
        NetworkRegistry.sendToServer(KEY, new SelectWhisperMessage(ruleID));
    }

    public static void send(ImprinterRecipe rule) {
        send(rule == null ? null : rule.id);
    }
}
