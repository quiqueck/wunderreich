package de.ambertation.wunderreich.network;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.gui.whisperer.WhispererMenu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import net.fabricmc.fabric.api.networking.v1.PacketSender;

import ru.bclib.api.dataexchange.DataHandler;
import ru.bclib.api.dataexchange.DataHandlerDescriptor;

public class SelectWhisperMessage extends DataHandler.FromClient {
    public static final DataHandlerDescriptor DESCRIPTOR = new DataHandlerDescriptor(new ResourceLocation(Wunderreich.MOD_ID,
            "select_whisper"), SelectWhisperMessage::new, false, false);

    protected int itemIndex = 0;

    public SelectWhisperMessage() {
        this(0);
    }

    public SelectWhisperMessage(int idx) {
        super(DESCRIPTOR.IDENTIFIER);
        this.itemIndex = idx;
    }

    @Override
    protected void serializeDataOnClient(FriendlyByteBuf buf) {
        buf.writeVarInt(itemIndex);
    }

    @Override
    protected void deserializeIncomingDataOnServer(FriendlyByteBuf buf, Player player, PacketSender responseSender) {
        this.itemIndex = buf.readVarInt();
    }

    @Override
    protected void runOnServerGameThread(MinecraftServer server, Player player) {
        int itemIndex = getItemIndex();
        AbstractContainerMenu abstractContainerMenu = player.containerMenu;
        if (abstractContainerMenu instanceof WhispererMenu menu) {
            menu.setSelectionHint(itemIndex);
            menu.tryMoveItems(itemIndex);
        }
    }

    public int getItemIndex() {
        return this.itemIndex;
    }
}
