package de.ambertation.wunderreich.network;

import org.wunder.lib.math.Transform;
import org.wunder.lib.math.sdf.SDF;
import org.wunder.lib.math.sdf.interfaces.MaterialProvider;
import org.wunder.lib.math.sdf.interfaces.Transformable;
import de.ambertation.wunderreich.gui.construction.RulerContainerMenu;
import de.ambertation.wunderreich.items.construction.ConstructionData;
import de.ambertation.wunderreich.registries.WunderreichItems;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.networking.v1.PacketSender;

import java.util.Objects;

public class ChangedSDFMessage extends ServerBoundPacketHandler<ChangedSDFMessage.Content> {
    public static final ChangedSDFMessage INSTANCE = ServerBoundPacketHandler.register(
            "chg_sdf",
            new ChangedSDFMessage()
    );

    protected ChangedSDFMessage() {
    }

    public void sendActive(AbstractContainerMenu menu, int id) {
        this.sendToServer(new Content(menu == null ? -1 : menu.containerId, Content.CHANGE_ACTIVE, id, -1, null));
    }

    public void sendMaterial(AbstractContainerMenu menu, int id) {
        this.sendToServer(new Content(menu == null ? -1 : menu.containerId, Content.CHANGE_MATERIAL, -1, id, null));
    }

    public void sendRealize(AbstractContainerMenu menu) {
        this.sendToServer(new Content(menu == null ? -1 : menu.containerId, Content.REALIZE, -1, -1, null));
    }

    public void sendTransform(AbstractContainerMenu menu, Transform t) {
        if (t == null) return;
        this.sendToServer(new Content(menu == null ? -1 : menu.containerId, Content.CHANGE_TRANSFORM, -1, -1, t));
    }

    @Override
    protected void serializeOnClient(FriendlyByteBuf buf, Content content) {
        buf.writeByte(content.containerId);
        buf.writeVarInt(content.stateFlag);

        if ((content.stateFlag & Content.CHANGE_ACTIVE) == Content.CHANGE_ACTIVE)
            buf.writeVarInt(content.active);

        if ((content.stateFlag & Content.CHANGE_MATERIAL) == Content.CHANGE_MATERIAL)
            buf.writeVarInt(content.material);

        if ((content.stateFlag & Content.CHANGE_TRANSFORM) == Content.CHANGE_TRANSFORM)
            content.transform.serializeToNetwork(buf);
    }

    @Override
    protected Content deserializeOnServer(
            FriendlyByteBuf buf,
            ServerPlayer player,
            PacketSender responseSender
    ) {
        return new Content(buf, player);
    }

    @Override
    protected void processOnGameThread(
            MinecraftServer server,
            ServerPlayer player,
            Content content
    ) {
        ConstructionData constructionData = null;
        if (content.containerId == player.containerMenu.containerId) {
            if (player.containerMenu instanceof RulerContainerMenu menu) {
                constructionData = menu.data;
            }
        } else if (content.containerId == -1 || content.containerId == 0xff) {
            ItemStack s = player.getMainHandItem();
            if (s.is(WunderreichItems.RULER)) {
                constructionData = ConstructionData.getConstructionData(s);
            }
        }
        if (constructionData == null) return;
        if ((content.stateFlag & Content.CHANGE_ACTIVE) == Content.CHANGE_ACTIVE) {
            constructionData.ACTIVE_SLOT.set(content.active);
            System.out.println(Integer.toHexString(constructionData.hashCode()) + "---- [ACTIVE_SLOT] ------------------");
            System.out.println(constructionData.SDF_DATA.get() + ", act=" + constructionData.ACTIVE_SLOT.get());
            System.out.println("-------------------------------------");
        }

        if ((content.stateFlag & Content.CHANGE_MATERIAL) == Content.CHANGE_MATERIAL) {
            SDF s = constructionData.getActiveSDF();
            if (s != null) {
                if (s instanceof MaterialProvider mp) {
                    mp.setMaterialIndex(content.material);
                    constructionData.SDF_DATA.set(s.getRoot());
                }
            }
        }

        if ((content.stateFlag & Content.REALIZE) == Content.REALIZE) {
            constructionData.realize(server, player);
        }

        if ((content.stateFlag & Content.CHANGE_TRANSFORM) == Content.CHANGE_TRANSFORM) {
            SDF s = constructionData.getActiveSDF();
            if (s != null) {
                if (s instanceof Transformable t) {
                    t.setLocalTransform(content.transform);
                    constructionData.SDF_DATA.set(s.getRoot());
                }
            }
        }
    }


    protected static final class Content {
        public static final int CHANGE_ACTIVE = 1 << 0;
        public static final int CHANGE_MATERIAL = 1 << 1;
        public static final int REALIZE = 1 << 2;
        public static final int CHANGE_TRANSFORM = 1 << 3;

        public final int containerId;
        public final int stateFlag;
        public final int active;
        public final int material;
        public final Transform transform;

        private Content(int containerId, int stateFlag, int active, int material, Transform transform) {
            this.containerId = containerId;
            this.stateFlag = stateFlag;
            this.active = active;
            this.material = material;
            this.transform = transform;
        }

        private Content(FriendlyByteBuf buf, ServerPlayer player) {
            this.containerId = buf.readUnsignedByte();
            this.stateFlag = buf.readVarInt();


            if ((stateFlag & Content.CHANGE_ACTIVE) == Content.CHANGE_ACTIVE)
                this.active = buf.readVarInt();
            else
                this.active = -1;

            if ((stateFlag & Content.CHANGE_MATERIAL) == Content.CHANGE_MATERIAL)
                this.material = buf.readVarInt();
            else
                this.material = -1;

            if ((stateFlag & Content.CHANGE_TRANSFORM) == Content.CHANGE_TRANSFORM)
                this.transform = Transform.deserializeFromNetwork(buf);
            else
                this.transform = null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Content) obj;
            return this.containerId == that.containerId &&
                    this.stateFlag == that.stateFlag &&
                    this.active == that.active &&
                    this.material == that.material;
        }

        @Override
        public int hashCode() {
            return Objects.hash(containerId, stateFlag, active, material);
        }

        @Override
        public String toString() {
            return "Content[" +
                    "containerId=" + containerId + ", " +
                    "stateFlag=" + stateFlag + ", " +
                    "active=" + active + ", " +
                    "material=" + material + ']';
        }
    }
}

