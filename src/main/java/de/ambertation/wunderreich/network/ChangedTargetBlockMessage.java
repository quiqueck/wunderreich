package de.ambertation.wunderreich.network;

import de.ambertation.lib.math.Float3;
import de.ambertation.wunderreich.items.construction.ConstructionData;

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

    public void send(Float3 newTarget) {
        this.sendToServer(new Content(newTarget));
    }

    @Override
    protected void serializeOnClient(FriendlyByteBuf buf, Content content) {
        content.serializeToNetwork(buf);
    }

    @Override
    protected Content deserializeOnServer(FriendlyByteBuf buf, ServerPlayer player, PacketSender responseSender) {
        return new Content(buf);
    }

    @Override
    protected void processOnGameThread(MinecraftServer server, ServerPlayer player, Content content) {
        ConstructionData.setCursorPosOnServer(content.newTarget);
    }


    protected static final class Content {
        public final Float3 newTarget;

        protected Content(Float3 newTarget) {
            this.newTarget = newTarget;
        }

        Content(FriendlyByteBuf buf) {
            this.newTarget = Float3.deserializeFromNetwork(buf);
        }

        void serializeToNetwork(FriendlyByteBuf buf) {
            newTarget.serializeToNetwork(buf);
        }
    }
}
