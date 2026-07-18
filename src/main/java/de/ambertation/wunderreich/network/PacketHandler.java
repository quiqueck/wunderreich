package de.ambertation.wunderreich.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.NotNull;

public class PacketHandler<D extends NetworkPayload<D>> {
    public interface NetworkPayloadFactory<T extends NetworkPayload<T>> {
        T create(FriendlyByteBuf buf);
    }

    @NotNull
    public final CustomPacketPayload.Type<D> CHANNEL;
    @NotNull
    public final StreamCodec<FriendlyByteBuf, D> STREAM_CODEC;

    protected PacketHandler(
            Identifier channel,
            @NotNull NetworkPayloadFactory<D> factory
    ) {
        this.CHANNEL = new CustomPacketPayload.Type<>(channel);
        this.STREAM_CODEC = CustomPacketPayload.codec(
                NetworkPayload::write,
                factory::create
        );
    }
}
