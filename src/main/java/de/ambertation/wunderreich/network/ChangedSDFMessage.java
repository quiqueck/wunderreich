package de.ambertation.wunderreich.network;

import de.ambertation.lib.math.sdf.SDF;
import de.ambertation.lib.math.sdf.interfaces.MaterialProvider;
import de.ambertation.wunderreich.items.construction.ConstructionData;
import de.ambertation.wunderreich.registries.WunderreichItems;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.networking.v1.PacketSender;

public class ChangedSDFMessage extends ServerBoundPacketHandler<ChangedSDFMessage.Content> {
    public static final ChangedSDFMessage INSTANCE = ServerBoundPacketHandler.register(
            "chg_sdf",
            new ChangedSDFMessage()
    );

    protected ChangedSDFMessage() {
    }

    public void sendActive(int id) {
        this.sendToServer(new Content(Content.CHANGE_ACTIVE, id, -1));
    }

    public void sendMaterial(int id) {
        this.sendToServer(new Content(Content.CHANGE_MATERIAL, -1, id));
    }

    public void sendRealize() {
        this.sendToServer(new Content(Content.REALIZE, -1, -1));
    }

    @Override
    protected void serializeOnClient(FriendlyByteBuf buf, Content content) {
        buf.writeVarInt(content.stateFlag);

        if ((content.stateFlag & Content.CHANGE_ACTIVE) == Content.CHANGE_ACTIVE)
            buf.writeVarInt(content.active);

        if ((content.stateFlag & Content.CHANGE_MATERIAL) == Content.CHANGE_MATERIAL)
            buf.writeVarInt(content.material);
    }

    @Override
    protected Content deserializeOnServer(
            FriendlyByteBuf buf,
            ServerPlayer player,
            PacketSender responseSender
    ) {
        int stateFlag = buf.readVarInt();
        int activeId = -1;
        int matID = -1;

        if ((stateFlag & Content.CHANGE_ACTIVE) == Content.CHANGE_ACTIVE)
            activeId = buf.readVarInt();

        if ((stateFlag & Content.CHANGE_MATERIAL) == Content.CHANGE_MATERIAL)
            matID = buf.readVarInt();

        return new Content(stateFlag, activeId, matID);
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

        if ((content.stateFlag & Content.CHANGE_ACTIVE) == Content.CHANGE_ACTIVE) {
            constructionData.ACTIVE_SLOT.set(content.active);
        }

        if ((content.stateFlag & Content.CHANGE_MATERIAL) == Content.CHANGE_MATERIAL) {
            SDF s = constructionData.getActiveSDF();
            if (s != null) {
                if (s instanceof MaterialProvider mp) mp.setMaterialIndex(content.material);
                constructionData.SDF_DATA.set(s.getRoot());
            }
        }

        if ((content.stateFlag & Content.REALIZE) == Content.REALIZE) {
            constructionData.realize(server, player);
        }
    }


    protected record Content(int stateFlag, int active, int material) {
        public static final int CHANGE_ACTIVE = 1 << 0;
        public static final int CHANGE_MATERIAL = 1 << 1;
        public static final int REALIZE = 1 << 2;
    }
}

