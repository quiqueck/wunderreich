package de.ambertation.wunderreich.items.construction;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.utils.math.Bounds;
import de.ambertation.wunderreich.utils.math.Float3;

public class Ruler extends Item {

    public Ruler() {
        super(WunderreichItems
                .makeItemSettings()
                .rarity(Rarity.UNCOMMON)
                .durability(1000));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        if (level.isClientSide) {
            ItemStack ruler = player.getItemInHand(interactionHand);
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
                    cd.shrink(ConstructionData.lastTarget);
                } else {
                    cd.addToBounds(ConstructionData.lastTarget);
                }
                return InteractionResultHolder.pass(ruler);
            }
            return InteractionResultHolder.fail(ruler);
        }
        return super.use(level, player, interactionHand);
    }

}
