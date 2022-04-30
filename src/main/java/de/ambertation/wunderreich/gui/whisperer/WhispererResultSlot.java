package de.ambertation.wunderreich.gui.whisperer;

import de.ambertation.wunderreich.registries.WunderreichRules;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class WhispererResultSlot extends Slot {
    private final WhisperContainer slots;
    private final Player player;
    private final WhispererMenu owner;
    private int removeCount;

    public WhispererResultSlot(WhispererMenu owner,
                               Player player,
                               WhisperContainer container,
                               int slotIndex,
                               int x,
                               int y) {
        super(container, slotIndex, x, y);

        this.player = player;
        this.slots = container;
        this.owner = owner;
    }


    void createExperience(ServerLevel level, int maxXP) {
        if (this.player instanceof ServerPlayer) {
            final double delta = WunderreichRules.Whispers.maxXPMultiplier() - WunderreichRules.Whispers.minXPMultiplier();
            final int xp = (int) (maxXP * (Math.random() * delta + WunderreichRules.Whispers.minXPMultiplier()));
            ExperienceOrb.award(level, player.position(), xp);
        }
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        return false;
    }

    @Override
    public ItemStack remove(int slotIndex) {
        if (this.hasItem()) {
            this.removeCount += Math.min(slotIndex, this.getItem().getCount());
        }

        return super.remove(slotIndex);
    }

    @Override
    protected void onQuickCraft(ItemStack itemStack, int i) {
        this.removeCount += i;
        this.checkTakeAchievements(itemStack);
    }

    @Override
    protected void checkTakeAchievements(ItemStack itemStack) {
        itemStack.onCraftedBy(this.player.level, this.player, this.removeCount);
        owner.playImprintSound();

        if (player.level instanceof ServerLevel serverLevel) {
            WhisperRule rules = this.slots.getActiveRule();

            if (rules != null)
                createExperience(serverLevel, rules.baseXP);
        }

        this.removeCount = 0;
    }

    @Override
    public void onTake(Player player, ItemStack itemStack) {
        this.checkTakeAchievements(itemStack);

        WhisperRule rules = this.slots.getActiveRule();
        if (rules != null) {
            ItemStack costA = this.slots.getItem(0);
            ItemStack costB = this.slots.getItem(1);
            if (rules.take(costA, costB) || rules.take(costB, costA)) {
                this.slots.setItem(0, costA);
                this.slots.setItem(1, costB);
            }
        }
    }
}
