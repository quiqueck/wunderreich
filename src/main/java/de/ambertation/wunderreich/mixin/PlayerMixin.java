package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.blockentities.BoxOfEirBlockEntity;
import de.ambertation.wunderreich.interfaces.ActiveChestStorage;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Player.class)
public abstract class PlayerMixin implements ActiveChestStorage {
    @Nullable
    private BoxOfEirBlockEntity activeChest;

    public void setActiveBoxOfEir(BoxOfEirBlockEntity boxOfEirBlockEntity) {
        this.activeChest = boxOfEirBlockEntity;
    }

    public boolean isActiveBoxOfEir(BoxOfEirBlockEntity boxOfEirBlockEntity) {
        return this.activeChest == boxOfEirBlockEntity;
    }

    public BoxOfEirBlockEntity getActiveBoxOfEir() {
        return this.activeChest;
    }
}
