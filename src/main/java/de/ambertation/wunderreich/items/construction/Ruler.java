package de.ambertation.wunderreich.items.construction;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.sdf.SDF;
import de.ambertation.lib.math.sdf.shapes.Empty;
import de.ambertation.wunderreich.gui.construction.RulerContainer;
import de.ambertation.wunderreich.gui.construction.RulerContainerMenu;
import de.ambertation.wunderreich.network.UpdateSDFTransformMessage;
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
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;

import org.jetbrains.annotations.NotNull;

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
            Float3 highlightedBlock = ConstructionData.getLastTargetInWorldSpace();

            //deselect Corner
            if (cd.getSelectedCorner() != null) {
                if (level.isClientSide) {
                    SDF sdf = cd.getActiveSDF();
                    if (sdf != null)
                        UpdateSDFTransformMessage.INSTANCE.send(sdf.getBoundingBox());
                }
                cd.setSelectedCorner(null);
                return InteractionResultHolder.success(ruler);
            }

            Bounds.Interpolate corner = cd.getActiveBoundingBoxInWorldSpace() == null
                    ? null
                    : cd.getActiveBoundingBoxInWorldSpace().blockAligned()
                        .isCornerOrCenter(highlightedBlock);

            if (corner != null) {
                cd.setSelectedCorner(corner);
                return InteractionResultHolder.success(ruler);
            }


            if (player.isShiftKeyDown()) {
                cd.CENTER.set(ConstructionData.getLastTargetInWorldSpace());
//                if (cd.getActiveSDF() instanceof Box box) {
//                    System.out.println("Bounds: " + box.getBoundingBox());
//                    box.rotate(Math.toRadians(15));
//                    cd.SDF_DATA.set(box.getRoot());
//
//                    System.out.println("new Bounds: " + box.transform);
//                    System.out.println("new Bounds: " + box.getBoundingBox());
//                    System.out.println("         -> " + box.getBoundingBox().rotate(box.transform.rotation.inverted()));
//                }
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
}
