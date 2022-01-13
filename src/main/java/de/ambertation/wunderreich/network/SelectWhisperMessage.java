package de.ambertation.wunderreich.network;

import de.ambertation.wunderreich.gui.whisperer.WhispererMenu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import net.fabricmc.fabric.api.networking.v1.PacketSender;

public class SelectWhisperMessage extends ServerBoundPacketHandler<SelectWhisperMessage.Content> {
    public static final SelectWhisperMessage INSTANCE = ServerBoundPacketHandler.register("select_whisper",
            new SelectWhisperMessage());

    protected SelectWhisperMessage() {
    }

    protected static record Content(int itemIndex) {
    }

    public void send(int itemIndex) {
        this.sendToServer(new Content(itemIndex));
    }

    @Override
    protected void serializeOnClient(FriendlyByteBuf buf, Content content) {
        buf.writeVarInt(content.itemIndex);
    }

    @Override
    protected Content deserializeOnServer(FriendlyByteBuf buf, ServerPlayer player, PacketSender responseSender) {
        int itemIndex = buf.readVarInt();
        return new Content(itemIndex);
    }

    @Override
    protected void processOnGameThread(MinecraftServer server, ServerPlayer player, Content content) {
        int itemIndex = content.itemIndex;
        AbstractContainerMenu abstractContainerMenu = player.containerMenu;

        if (abstractContainerMenu instanceof WhispererMenu menu) {
            menu.setSelectionHint(itemIndex);
            menu.tryMoveItems(itemIndex);
        }
    }
}
