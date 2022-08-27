package de.ambertation.wunderreich.items.construction;

import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.sdf.SDF;
import de.ambertation.lib.math.sdf.interfaces.Rotatable;
import de.ambertation.lib.math.sdf.interfaces.Transformable;
import de.ambertation.lib.math.sdf.shapes.Empty;
import de.ambertation.wunderreich.gui.construction.RulerContainer;
import de.ambertation.wunderreich.gui.construction.RulerContainerMenu;
import de.ambertation.wunderreich.registries.WunderreichItems;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Ruler extends Item implements FabricItem {

    public Ruler() {
        super(WunderreichItems
                .makeItemSettings()
                .rarity(Rarity.UNCOMMON)
                .maxCount(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            @NotNull Level level,
            Player player,
            @NotNull InteractionHand interactionHand
    ) {
        ItemStack ruler = player.getItemInHand(interactionHand);
        ConstructionData cd = ConstructionData.getConstructionData(ruler);
        //if (!level.isClientSide) return InteractionResultHolder.pass(ruler);
        if (cd != null) {
            var widget = cd.getActiveTransformWidget();
            Float3 cursorPos = ConstructionData.getCursorPos();
            System.out.println("Cursor: " + cursorPos);
            System.out.println("ATrans: " + widget);
            if (widget != null) {
                widget.cursorTick(cursorPos);
                if (widget.hasSelection()) {
                    SDF active = cd.getActiveSDF();
                    if (active instanceof Transformable tf) {
                        tf.setLocalTransform(widget.getChangedTransform());
                        cd.SDF_DATA.set(active.getRoot());
                    }
                }
                if (widget.click()) {
                    return InteractionResultHolder.success(ruler);
                }
            }

            if (player.isShiftKeyDown()) {
//                cd.CENTER.set(ConstructionData.getCursorPos());
                if (cd.getActiveSDF() instanceof Rotatable rot) {
                    rot.rotate(Math.toRadians(15));
                    cd.SDF_DATA.set(rot.getRoot());
                }
            } else {
                player.startUsingItem(interactionHand);
                openScreen(player, ruler);
            }
            return InteractionResultHolder.success(ruler);
        }
        return InteractionResultHolder.pass(ruler);
    }

    @Override
    public void verifyTagAfterLoad(@NotNull CompoundTag compoundTag) {
        super.verifyTagAfterLoad(compoundTag);
        ConstructionData data = ConstructionData.getConstructionData(compoundTag);
        if (data.SDF_DATA.get() == null) {
            data.SDF_DATA.set(new Empty());
        }

        if (data.MATERIAL_DATA.get() == null) {
            data.MATERIAL_DATA.set(new RulerContainer());
        }
    }

    public static void openScreen(Player player, ItemStack rulerStack) {
        if (player != null && player.level != null && !player.level.isClientSide) {
            System.out.println("open");
            player.openMenu(new ExtendedScreenHandlerFactory() {
                @Override
                public @NotNull AbstractContainerMenu createMenu(
                        int syncID,
                        @NotNull Inventory inventory,
                        @NotNull Player player
                ) {
                    return new RulerContainerMenu(syncID, inventory, rulerStack);
                }

                @Override
                public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
                    buf.writeItem(rulerStack);
                }

                @Override
                public Component getDisplayName() {
                    return Component.literal("Ruler Menu");
                }
            });
        }
    }

    @Override
    public void appendHoverText(
            ItemStack itemStack,
            @Nullable Level level,
            List<Component> list,
            TooltipFlag tooltipFlag
    ) {
        list.add(Component.literal(ConstructionData.getConstructionData(itemStack).SDF_DATA.get().toString()));
        list.add(Component.literal(ConstructionData.getConstructionData(itemStack).CENTER.get().toString()));
        super.appendHoverText(itemStack, level, list, tooltipFlag);
    }
}
