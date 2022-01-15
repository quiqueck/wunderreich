package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.blockentities.WunderKisteBlockEntity;
import de.ambertation.wunderreich.interfaces.ActiveChestStorage;

import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;

import org.jetbrains.annotations.Nullable;

@Mixin(Player.class)
public abstract class PlayerMixin implements ActiveChestStorage {
    @Nullable
    private WunderKisteBlockEntity activeChest;

    public boolean isActiveWunderKiste(WunderKisteBlockEntity wunderKisteBlockEntity) {
        return this.activeChest == wunderKisteBlockEntity;
    }

    public WunderKisteBlockEntity getActiveWunderKiste() {
        return this.activeChest;
    }

    public void setActiveWunderKiste(WunderKisteBlockEntity wunderKisteBlockEntity) {
        this.activeChest = wunderKisteBlockEntity;
    }
}
