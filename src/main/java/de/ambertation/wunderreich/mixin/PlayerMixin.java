package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.blockentities.WunderKisteBlockEntity;
import de.ambertation.wunderreich.interfaces.ActiveChestStorage;

import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;

import org.jetbrains.annotations.Nullable;

@Mixin(Player.class)
public abstract class PlayerMixin implements ActiveChestStorage {
    @Nullable
    private WunderKisteBlockEntity wunder_activeChest;

    public boolean isActiveWunderKiste(WunderKisteBlockEntity wunderKisteBlockEntity) {
        return this.wunder_activeChest == wunderKisteBlockEntity;
    }

    public WunderKisteBlockEntity getActiveWunderKiste() {
        return this.wunder_activeChest;
    }

    public void setActiveWunderKiste(WunderKisteBlockEntity wunderKisteBlockEntity) {
        this.wunder_activeChest = wunderKisteBlockEntity;
    }
}
