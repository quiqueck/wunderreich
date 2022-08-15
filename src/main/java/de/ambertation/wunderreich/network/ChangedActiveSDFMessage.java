package de.ambertation.wunderreich.network;

import de.ambertation.wunderreich.items.construction.ConstructionData;
import de.ambertation.wunderreich.registries.WunderreichItems;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.networking.v1.PacketSender;

public class ChangedActiveSDFMessage extends ServerBoundPacketHandler<ChangedActiveSDFMessage.Content> {
    public static final ChangedActiveSDFMessage INSTANCE = ServerBoundPacketHandler.register(
            "chg_act_sdf",
            new ChangedActiveSDFMessage()
    );

    protected ChangedActiveSDFMessage() {
    }

    public void send(int id) {
        this.sendToServer(new Content(id));
    }

    @Override
    protected void serializeOnClient(FriendlyByteBuf buf, Content content) {
        buf.writeVarInt(content.active);
    }

    @Override
    protected Content deserializeOnServer(
            FriendlyByteBuf buf,
            ServerPlayer player,
            PacketSender responseSender
    ) {
        int newIdx = buf.readVarInt();
        return new Content(newIdx);
    }

    @Override
    protected void processOnGameThread(
            MinecraftServer server,
            ServerPlayer player,
            Content content
    ) {
        ItemStack ruler = player.getMainHandItem();
        if (ruler == null || !ruler.is(WunderreichItems.RULER)) return;
        ConstructionData constructionData = ConstructionData.getConstructionData(ruler);
        constructionData.ACTIVE_SLOT.set(content.active);
    }


    protected record Content(int active) {
    }
}

