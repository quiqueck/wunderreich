package de.ambertation.wunderreich.network;

import de.ambertation.wunderreich.items.construction.ConstructionData;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.networking.v1.PacketSender;

public class ChangedTargetBlockMessage extends ServerBoundPacketHandler<ChangedTargetBlockMessage.Content> {
    public static final ChangedTargetBlockMessage INSTANCE = ServerBoundPacketHandler.register(
            "chg_target_bl",
            new ChangedTargetBlockMessage()
    );

    protected ChangedTargetBlockMessage() {
    }

    public void send(BlockPos newTarget) {
        this.sendToServer(new Content(newTarget));
    }

    @Override
    protected void serializeOnClient(FriendlyByteBuf buf, Content content) {
        buf.writeBlockPos(content.newTarget);
    }

    @Override
    protected Content deserializeOnServer(FriendlyByteBuf buf, ServerPlayer player, PacketSender responseSender) {
        BlockPos newPos = buf.readBlockPos();
        return new Content(newPos);
    }

    @Override
    protected void processOnGameThread(MinecraftServer server, ServerPlayer player, Content content) {
        ConstructionData.setLastTargetOnServer(content.newTarget);
    }


    protected record Content(BlockPos newTarget) {
    }
}
