package de.ambertation.wunderreich.gui.whisperer;

import de.ambertation.wunderreich.network.SelectWhisperMessage;
import de.ambertation.wunderreich.recipes.ImprinterRecipe;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.registries.WunderreichMenuTypes;
import de.ambertation.wunderreich.registries.WunderreichRules;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

class RuleDataSlot extends DataSlot {

    @Override
    public int get() {
        return 0;
    }

    @Override
    public void set(int i) {

    }
}

public class WhispererMenu
        extends ItemCombinerMenu {
    protected static final int INGREDIENT_SLOT_A = 0;
    protected static final int INGREDIENT_SLOT_B = 1;
    protected static final int RESULT_SLOT = 2;
    public static final int INV_SLOT_START = 3;
    private static final int INV_SLOT_END = 30;
    private static final int HOTBAR_SLOT_START = 30;
    private static final int HOTBAR_SLOT_END = 39;
    private static final int INGREDIENT_SLOT_A_X = 136;
    private static final int INGREDIENT_SLOT_B_X = 162;
    private static final int RESULT_SLOT_X = 220;
    private static final int ROW_Y = 37;

    @Nullable
    private ImprinterRecipe selectedRule;
    private final List<ImprinterRecipe> recipes;

    public WhispererMenu(int i, Inventory inventory) {
        this(i, inventory, ContainerLevelAccess.NULL);
    }


    public WhispererMenu(int containerId, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(
                WunderreichMenuTypes.WHISPERER,
                containerId,
                inventory,
                containerLevelAccess,
                createInputSlotDefinitions()
        );
        recipes = ImprinterRecipe.getUISortedRecipes();
    }

    protected static ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
        return ItemCombinerMenuSlotDefinition
                .create()
                .withSlot(INGREDIENT_SLOT_A, INGREDIENT_SLOT_A_X, ROW_Y, itemStack -> true)
                .withSlot(INGREDIENT_SLOT_B, INGREDIENT_SLOT_B_X, ROW_Y, itemStack -> true)
                .withResultSlot(RESULT_SLOT, RESULT_SLOT_X, ROW_Y).build();
    }

    @ApiStatus.Internal
    public void createCustomInventorySlots(Container container) {
        for (int i = 0; i < WhispererMenu.INV_SLOT_START; ++i) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(container, k + i * 9 + 9, 108 + k * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(container, i, 108 + i * 18, 142));
        }
    }

    @Override
    protected boolean isValidBlock(BlockState blockState) {
        return blockState.is(WunderreichBlocks.WHISPER_IMPRINTER);
    }

    public void selectBestRecipe(boolean andCreate) {
//        Wunderreich.LOGGER.info("MENU SELECT BEST Creating result: " + selectedRule + ", " + this.inputSlots.getItem(INGREDIENT_SLOT_A) + ", " + this.inputSlots.getItem(INGREDIENT_SLOT_B) + " | SERVER? " + (this.player instanceof ServerPlayer));
        final ImprinterRecipe.ImprinterInput recipeInput = getImprinterInput();
//        Wunderreich.LOGGER.info("MENU SELECT BEST Input: " + selectedRule + ", " + recipeInput + " | SERVER? " + (this.player instanceof ServerPlayer));

        var suggestedRule = selectedRule;
        //not even a valid input anymore, reset the selected Rule
        if (recipeInput == null) {
            suggestedRule = null;
            if (andCreate) this.resultSlots.setItem(0, ItemStack.EMPTY);
        } else {
            //try to find the best matching rule
            if (suggestedRule == null) {
                suggestedRule = getEnchants().stream()
                                             .filter(rule -> rule.satisfiedBy(recipeInput))
                                             .sorted((a, b) -> b.input.getCount() - a.input.getCount())
                                             .findFirst()
                                             .orElse(null);
            }

            //we have a selected Rule, check if the input can still fulfill the rule
            if (suggestedRule != null) {
                if (suggestedRule.satisfiedBy(recipeInput)) {
                    if (andCreate) this.resultSlots.setItem(0, suggestedRule.assemble());
                } else {
                    suggestedRule = null;
                    if (andCreate) this.resultSlots.setItem(0, ItemStack.EMPTY);
                }
            } else {
                if (andCreate) this.resultSlots.setItem(0, ItemStack.EMPTY);
            }
        }
        if ((recipeInput != null || suggestedRule != null) && suggestedRule != selectedRule) {
            setSelectedRule(suggestedRule);
        }
    }

    private boolean updating = false;

    @Override
    public void createResult() {
        if (!updating) {
            this.updating = true;
            selectBestRecipe(true);
            this.updating = false;
            this.broadcastChanges();
        }
    }

    private ImprinterRecipe.@Nullable ImprinterInput getImprinterInput() {
        final ImprinterRecipe.ImprinterInput recipeInput;
        final var inputA = this.inputSlots.getItem(INGREDIENT_SLOT_A);
        final var inputB = this.inputSlots.getItem(INGREDIENT_SLOT_B);


        if (!inputA.isEmpty() && inputA.is(WunderreichItems.BLANK_WHISPERER)) {
            recipeInput = new ImprinterRecipe.ImprinterInput(inputB, inputA);
        } else if (!inputB.isEmpty() && inputB.is(WunderreichItems.BLANK_WHISPERER)) {
            recipeInput = new ImprinterRecipe.ImprinterInput(inputA, inputB);
        } else {
            recipeInput = null;
        }
        return recipeInput;
    }


    @Override
    protected boolean mayPickup(Player player, boolean bl) {
        return !this.resultSlots.getItem(0).isEmpty();
    }

    @Override
    protected void onTake(Player player, ItemStack itemStack) {
//        Wunderreich.LOGGER.info("MENU ONTAKE" + selectedRule + ", " + this.inputSlots.getItem(INGREDIENT_SLOT_A) + ", " + this.inputSlots.getItem(INGREDIENT_SLOT_B) + ", " + itemStack + " | SERVER? " + (this.player instanceof ServerPlayer));
        final ImprinterRecipe.ImprinterInput recipeInput = getImprinterInput();
//        Wunderreich.LOGGER.info("MENU ONTAKE Input: " + selectedRule + ", " + recipeInput + " | SERVER? " + (this.player instanceof ServerPlayer));
        if (selectedRule != null) {
            final int xp = selectedRule.baseXP;
            if (selectedRule.canBuildFrom(recipeInput)) {
                ItemStack stackA = this.inputSlots.getItem(INGREDIENT_SLOT_A);
                ItemStack stackB = this.inputSlots.getItem(INGREDIENT_SLOT_B);
                if (stackA.is(WunderreichItems.BLANK_WHISPERER)) {
                    stackB.shrink(selectedRule.input.getCount());
                    this.inputSlots.setItem(INGREDIENT_SLOT_A, ItemStack.EMPTY);
                    this.inputSlots.setItem(INGREDIENT_SLOT_B, stackB);
                } else {
                    stackA.shrink(selectedRule.input.getCount());
                    this.inputSlots.setItem(INGREDIENT_SLOT_A, stackA);
                    this.inputSlots.setItem(INGREDIENT_SLOT_B, ItemStack.EMPTY);
                }
                if (player.level() instanceof ServerLevel serverLevel) {
                    createExperience(serverLevel, xp);
                }
                this.playImprintSound();

                this.setSelectedRule(null);
            }
        }
    }

    void playImprintSound() {
        this.access.execute((level, blockPos) -> {
            level.playSound(
                    null,
                    blockPos,
                    SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.BLOCKS,
                    1.0f,
                    level.random.nextFloat() * 0.1f + 0.9f
            );
        });
    }

    void createExperience(ServerLevel level, int maxXP) {
        if (this.player instanceof ServerPlayer) {
            final double delta = WunderreichRules.Whispers.maxXPMultiplier() - WunderreichRules.Whispers.minXPMultiplier();
            final int xp = (int) (maxXP * (Math.random() * delta + WunderreichRules.Whispers.minXPMultiplier()));
            ExperienceOrb.award(level, player.position(), xp);
        }
    }

    private ImprinterRecipe setSelectedRule(ImprinterRecipe rule) {
        final ResourceLocation selectedID = this.selectedRule != null ? this.selectedRule.id : null;
        final ResourceLocation ruleID = rule != null ? rule.id : null;
        if (
                (selectedID == null && ruleID != null) ||
                        (selectedID != null && !selectedID.equals(ruleID))
        ) {
            this.selectedRule = rule;
//            Wunderreich.LOGGER.info("MENU Selecting RULE: " + rule + " | SERVER? " + (this.player instanceof ServerPlayer));
        }

        return this.selectedRule;
    }

    public ImprinterRecipe selectByID(ResourceLocation ruleID) {
        ImprinterRecipe newRule = getRuleByID(ruleID);
//        Wunderreich.LOGGER.info("MENU Selecting by ID: " + ruleID + ", " + newRule);

        return setSelectedRule(newRule);
    }

    public @Nullable ImprinterRecipe getRuleByID(@Nullable ResourceLocation ruleID) {
        if (ruleID == null) return null;
        return this
                .getEnchants()
                .stream()
                .filter(rule -> rule.id != null && rule.id.equals(ruleID))
                .findFirst()
                .orElse(null);
    }

    public ImprinterRecipe selectByIndex(int ruleIndex) {
        ImprinterRecipe newRule = null;
        if (ruleIndex < this.getEnchants().size()) newRule = this.getEnchants().get(ruleIndex);
//        Wunderreich.LOGGER.info("MENU Selecting by Index: " + ruleIndex + ", " + newRule);

        var res = setSelectedRule(newRule);
        return res;
    }

    public void tryMoveItems(WhisperRule rule) {
        if (rule != null) {
            ItemStack slotItem = this.inputSlots.getItem(INGREDIENT_SLOT_A);
            boolean didMove = true;
            if (!slotItem.isEmpty()) {
                didMove &= this.moveItemStackTo(slotItem, INV_SLOT_START, HOTBAR_SLOT_END, true);
            }

            slotItem = this.inputSlots.getItem(INGREDIENT_SLOT_B);
            if (!slotItem.isEmpty()) {
                didMove &= this.moveItemStackTo(slotItem, INV_SLOT_START, HOTBAR_SLOT_END, true);
            }

            if (didMove &&
                    this.inputSlots.getItem(INGREDIENT_SLOT_A).isEmpty() &&
                    this.inputSlots.getItem(INGREDIENT_SLOT_B).isEmpty()) {
                if (inventoryHas(rule.getInput()) && inventoryHas(WhisperRule.BLANK)) {
                    this.updating = true;
                    this.moveFromInventoryToPaymentSlot(INGREDIENT_SLOT_A, rule.getInput());
                    this.moveFromInventoryToPaymentSlot(INGREDIENT_SLOT_B, WhisperRule.BLANK);
                    this.updating = false;
                    createResult();
                }
            }
        }
    }

    private boolean inventoryHas(ItemStack itemStack) {
        if (itemStack.isEmpty()) return false;
        int count = itemStack.getCount();
        for (int i = INV_SLOT_START; i < HOTBAR_SLOT_END; ++i) {
            final ItemStack slotStack = this.slots.get(i).getItem();
            if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, slotStack)) {
                count -= slotStack.getCount();
            }
            if (count <= 0) return true;
        }
        return false;
    }

    private void moveFromInventoryToPaymentSlot(int containerIndex, ItemStack recipeStack) {
        if (!recipeStack.isEmpty()) {
            for (int j = INV_SLOT_START; j < HOTBAR_SLOT_END; ++j) {
                final ItemStack slotStack = this.slots.get(j).getItem();
                if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(recipeStack, slotStack)) {
                    final ItemStack containerStack = this.inputSlots.getItem(containerIndex);
                    final int occupiedCount = containerStack.isEmpty() ? 0 : containerStack.getCount();
                    final int moveCount = Math.min(recipeStack.getCount() - occupiedCount, slotStack.getCount());
                    final ItemStack result = slotStack.copy();
                    final int count = occupiedCount + moveCount;

                    if (count > recipeStack.getMaxStackSize()) break;

                    slotStack.shrink(moveCount);
                    result.setCount(count);

                    this.inputSlots.setItem(containerIndex, result);
                }
            }
        }
    }

    public List<ImprinterRecipe> getEnchants() {
        return this.recipes;
    }

    ResourceLocation lastSentRule = null;

    private void broadcastSelectedRule(boolean force) {
        final var selectedId = selectedRule != null ? selectedRule.id : null;
        if (force ||
                selectedId == null && lastSentRule != null ||
                selectedId != null && !selectedId.equals(lastSentRule)) {
            if (player.level().isClientSide()) {
                SelectWhisperMessage.send(selectedRule);
            }
//            Wunderreich.LOGGER.info("MENU Broadcast RULE: " + selectedRule + " | SERVER? " + (this.player instanceof ServerPlayer));
            lastSentRule = selectedId;
        }
    }

    @Override
    public void broadcastFullState() {
        if (this.updating) return;
        //broadcastSelectedRule(true);
        super.broadcastFullState();
    }

    @Override
    public void broadcastChanges() {
        if (this.updating) return;
        //broadcastSelectedRule(false);
        super.broadcastChanges();
    }
}

