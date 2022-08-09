package de.ambertation.wunderreich.items.construction;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float3;
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
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;

import org.jetbrains.annotations.Nullable;

public class Ruler extends Item implements FabricItem {

    public Ruler() {
        super(WunderreichItems
                .makeItemSettings()
                .rarity(Rarity.UNCOMMON)
                .maxCount(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack ruler = player.getItemInHand(interactionHand);
        if (level.isClientSide) {
            ConstructionData cd = ConstructionData.getConstructionData(ruler);
            if (cd != null) {
                Float3 highlightedBlock = Float3.of(ConstructionData.lastTarget);

                //deselect Corner
                if (cd.getSelectedCorner() != null) {
                    cd.setBoundingBox(cd.getNewBoundsForSelectedCorner());
                    cd.setSelectedCorner(null);
                    return InteractionResultHolder.pass(ruler);
                }

                Bounds.Interpolate corner = cd.getBoundingBox() == null
                        ? null
                        : cd.getBoundingBox().isCornerOrCenter(highlightedBlock);

                if (corner != null) {
                    cd.setSelectedCorner(corner);
                    return InteractionResultHolder.pass(ruler);
                }

                if (player.isShiftKeyDown()) {
                    player.startUsingItem(interactionHand);
                    return InteractionResultHolder.success(ruler);
                } else {
                    cd.addToBounds(ConstructionData.lastTarget);
                }
                return InteractionResultHolder.pass(ruler);
            }
            return InteractionResultHolder.fail(ruler);
        } else {
            if (player.isShiftKeyDown()) {
                player.startUsingItem(interactionHand);
                openScreen(player, ruler, interactionHand);
                return InteractionResultHolder.success(ruler);
            }
        }
        return InteractionResultHolder.pass(ruler);
    }

    @Override
    public void verifyTagAfterLoad(CompoundTag compoundTag) {
        super.verifyTagAfterLoad(compoundTag);
        ConstructionData data = ConstructionData.getConstructionData(compoundTag);
        if (data.SDF_DATA.get() == null) {
            data.SDF_DATA.set(Empty.INSTANCE);
        }

        if (data.MATERIAL_DATA.get() == null) {
            data.MATERIAL_DATA.set(new RulerContainer());
        }
    }

    public static void openScreen(Player player, ItemStack rulerStack, InteractionHand interactionHand) {
        if (player != null && player.level != null && !player.level.isClientSide) {
            System.out.println("open");
            player.openMenu(new ExtendedScreenHandlerFactory() {
                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int syncID, Inventory inventory, Player player) {
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
