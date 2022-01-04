package de.ambertation.wunderreich.gui.whisperer;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class WhispererResultSlot extends Slot {
    private final WhisperContainer slots;
    private final Player player;
    private int removeCount;

    public WhispererResultSlot(Player player, WhisperContainer container, int slotIndex, int x, int y) {
        super(container, slotIndex, x, y);

        this.player = player;
        this.slots = container;
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
