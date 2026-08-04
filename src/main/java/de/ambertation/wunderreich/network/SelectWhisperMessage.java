package de.ambertation.wunderreich.network;

import de.ambertation.wunderlib.network.NetworkRegistry;
import de.ambertation.wunderlib.network.ServerBoundMessage;
import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.gui.whisperer.WhispererMenu;
import de.ambertation.wunderreich.recipes.ImprinterRecipe;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record SelectWhisperMessage(ResourceLocation ruleID) {
    private static final StreamCodec<RegistryFriendlyByteBuf, SelectWhisperMessage> CODEC = StreamCodec.of(
            (buf, msg) -> {
                boolean isNull = msg.ruleID == null;
                buf.writeBoolean(isNull);
                if (!isNull) ResourceLocation.STREAM_CODEC.encode(buf, msg.ruleID);
            },
            buf -> {
                boolean isNull = buf.readBoolean();
                return new SelectWhisperMessage(isNull ? null : ResourceLocation.STREAM_CODEC.decode(buf));
            }
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

    public static void send(ResourceLocation ruleID) {
        NetworkRegistry.sendToServer(KEY, new SelectWhisperMessage(ruleID));
    }

    public static void send(ImprinterRecipe rule) {
        send(rule == null ? null : rule.id);
    }
}
