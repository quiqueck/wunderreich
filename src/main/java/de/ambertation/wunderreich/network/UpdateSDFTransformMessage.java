package de.ambertation.wunderreich.network;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.sdf.SDF;
import de.ambertation.lib.math.sdf.interfaces.BoundedShape;
import de.ambertation.wunderreich.items.construction.ConstructionData;
import de.ambertation.wunderreich.registries.WunderreichItems;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.networking.v1.PacketSender;

public class UpdateSDFTransformMessage extends ServerBoundPacketHandler<UpdateSDFTransformMessage.Content> {
    public static final UpdateSDFTransformMessage INSTANCE = ServerBoundPacketHandler.register(
            "upd_sdf_trans",
            new UpdateSDFTransformMessage()
    );

    public void send(Bounds b) {
        this.sendToServer(new Content(b));
    }

    @Override
    protected void serializeOnClient(FriendlyByteBuf buf, Content content) {
        content.bounds.serializeToNetwork(buf);
    }

    @Override
    protected Content deserializeOnServer(FriendlyByteBuf buf, ServerPlayer player, PacketSender responseSender) {
        Bounds b = Bounds.deserializeFromNetwork(buf);
        return new Content(b);
    }

    @Override
    protected void processOnGameThread(MinecraftServer server, ServerPlayer player, Content content) {
        ItemStack ruler = player.getMainHandItem();
        if (ruler == null || !ruler.is(WunderreichItems.RULER)) return;
        ConstructionData constructionData = ConstructionData.getConstructionData(ruler);
        SDF sdf = constructionData.getActiveSDF();
        if (sdf instanceof BoundedShape bs) {
            bs.setFromBoundingBox(content.bounds);
        }
        System.out.println("---- [NEW SDF] ------------------");
        System.out.println(constructionData.SDF_DATA.get() + ", act=" + constructionData.ACTIVE_SLOT.get());
        constructionData.SDF_DATA.set(sdf.getRoot());
        System.out.println(constructionData.SDF_DATA.get() + ", act=" + constructionData.ACTIVE_SLOT.get());
        System.out.println("-------------------------------------");
    }

    protected record Content(Bounds bounds) {
    }
}
