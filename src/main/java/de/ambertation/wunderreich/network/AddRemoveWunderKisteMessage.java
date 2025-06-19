package de.ambertation.wunderreich.network;

import de.ambertation.wunderlib.network.ServerBoundNetworkPayload;
import de.ambertation.wunderlib.network.ServerBoundPacketHandler;
import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.utils.LiveBlockManager.LiveBlock;
import de.ambertation.wunderreich.utils.WunderKisteDomain;
import de.ambertation.wunderreich.utils.WunderKisteServerExtension;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.networking.v1.PacketSender;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class AddRemoveWunderKisteMessage extends ServerBoundNetworkPayload<AddRemoveWunderKisteMessage> {
    public static final ServerBoundPacketHandler<AddRemoveWunderKisteMessage> HANDLER = new ServerBoundPacketHandler<>(
            Wunderreich.ID("wunder_kiste"),
            AddRemoveWunderKisteMessage::new
    );

    public final boolean didAdd;
    @NotNull
    public final BlockPos pos;

    @Nullable
    private ServerLevel level;

    public AddRemoveWunderKisteMessage(RegistryFriendlyByteBuf buf) {
        super(HANDLER);
        this.didAdd = buf.readBoolean();
        this.pos = buf.readBlockPos();
        this.level = null;
    }

    public AddRemoveWunderKisteMessage(boolean didAdd, BlockPos pos) {
        super(HANDLER);
        this.didAdd = didAdd;
        this.pos = pos;
        this.level = null;
    }

    @Override
    protected void prepareOnClient() {

    }

    @Override
    protected void write(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(this.didAdd);
        buf.writeBlockPos(this.pos);
    }

    @Override
    protected void processOnServer(ServerPlayer player, PacketSender responseSender) {
        this.level = player.level();
    }

    @Override
    protected void processOnGameThread(MinecraftServer server, ServerPlayer player) {
        if (this.didAdd) addedBox(this.level, this.pos);
        else removedBox(this.level, this.pos);
    }


    static {
        WunderKisteBlock.getLiveBlockManager().onChangeAt(WunderKisteBlock::updateNeighbours);
    }


    public static void addedBox(ServerLevel level, BlockPos pos) {
        final LiveBlock lb = new LiveBlock(pos, level);
        final BlockState state = level.getBlockState(pos);
        boolean wasManaged = WunderKisteBlock.getLiveBlockManager().contains(lb);
        WunderKisteDomain domain = WunderKisteServerExtension.getDomain(state);
        boolean result = WunderKisteBlock.getLiveBlockManager().add(lb);

        Wunderreich.LOGGER.info("Adding WunderKiste at " + pos + " (wasManaged: " + wasManaged + ", domain: " + domain + ", didAdd:" + result + ")");
    }

    public static void removedBox(ServerLevel level, BlockPos pos) {
        final LiveBlock lb = new LiveBlock(pos, level);

        boolean wasManaged = WunderKisteBlock.getLiveBlockManager().contains(lb);
        boolean result = WunderKisteBlock.getLiveBlockManager().remove(lb);

        Wunderreich.LOGGER.info("Removing WunderKiste at " + pos + " (wasManaged: " + wasManaged + ", didRemove:" + result + ")");

    }

    public static void send(boolean didAdd, BlockPos pos) {
        ServerBoundPacketHandler.sendToServer(new AddRemoveWunderKisteMessage(didAdd, pos));
    }
}
