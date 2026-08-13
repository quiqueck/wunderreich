package de.ambertation.wunderreich.blockentities;

import de.ambertation.wunderreich.blocks.Chronarium;
import de.ambertation.wunderreich.gui.chronarium.ChronariumMenu;
import de.ambertation.wunderreich.recipes.AgingRecipe;
import de.ambertation.wunderreich.registries.WunderreichBlockEntities;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The three slot inventory behind the {@link de.ambertation.wunderreich.blocks.Chronarium}.
 *
 * <h3>Slots</h3>
 * <ul>
 *     <li>{@link #INPUT_SLOT} (0) - the item that is aged. Shrunk by one per completed recipe.</li>
 *     <li>{@link #CATALYST_SLOT} (1) - <b>never</b> touched. Not shrunk, not damaged, not emptied
 *     (a water bucket stays a water bucket).</li>
 *     <li>{@link #OUTPUT_SLOT} (2) - the result. Take only from the menu.</li>
 * </ul>
 *
 * <h3>Automation</h3>
 * The block is a {@link WorldlyContainer}, which is what keeps a hopper honest: without it every
 * slot is reachable from every face, and a hopper underneath happily drags the catalyst out of a
 * running machine. See {@link #getSlotsForFace} for the face mapping.
 *
 * <h3>Logical sides</h3>
 * All recipe lookup happens on the logical server: {@code Level#recipeAccess()} only narrows to a
 * {@code RecipeManager} there, and {@link AgingRecipe#find} silently returns an empty optional on
 * the client. The block therefore only registers a server ticker.
 *
 * <h3>Client state (for the BlockEntityRenderer)</h3>
 * {@link #getInputStack()}, {@link #getCatalystStack()}, {@link #getProgress()} and
 * {@link #getMaxProgress()} are readable on the client. They are refreshed by a block entity update
 * packet whenever the input or catalyst stack changes, and additionally every
 * {@link #SYNC_INTERVAL} ticks while a recipe is running, so a renderer can interpolate a smooth
 * animation between two updates.
 *
 * <h3>Outside feedback</h3>
 * None of the above is visible without opening the machine. What the player sees from across the
 * room is {@link Chronarium#WORKING}, a blockstate flag this class writes on the two edges of a
 * recipe - see {@link #updateWorkingState}. It swaps the three clock dials for an animated vortex
 * and raises the block's light emission, and it costs one block update per recipe rather than one
 * per tick.
 *
 * <h3>Experience</h3>
 * Exactly the furnace mechanic, via {@link RecipeCraftingHolder}: every completed result bumps a
 * counter for its recipe ({@link #setRecipeUsed}), and the accumulated experience is only handed
 * out - as orbs, at the player - when a <b>player</b> takes something out of the output slot (see
 * {@link #awardUsedRecipesAndPopExperience}). Like vanilla, a hopper draining the output never
 * awards anything, because a hopper goes through the {@link net.minecraft.world.Container} and
 * never through the menu's result slot.
 */
public class ChronariumBlockEntity extends BaseContainerBlockEntity
        implements RecipeCraftingHolder, WorldlyContainer {
    /**
     * The item that is aged and consumed.
     */
    public static final int INPUT_SLOT = 0;
    /**
     * The item that has to be present but is never consumed.
     */
    public static final int CATALYST_SLOT = 1;
    /**
     * The finished result.
     */
    public static final int OUTPUT_SLOT = 2;
    public static final int CONTAINER_SIZE = 3;

    /**
     * Same layout as {@code AbstractFurnaceBlockEntity}: one array per face group, handed out by
     * {@link #getSlotsForFace}.
     */
    private static final int[] SLOTS_FOR_DOWN = new int[]{OUTPUT_SLOT};
    private static final int[] SLOTS_FOR_UP = new int[]{INPUT_SLOT};
    private static final int[] SLOTS_FOR_SIDES = new int[]{CATALYST_SLOT};

    /**
     * {@link ContainerData} index of the current progress in ticks.
     */
    public static final int DATA_PROGRESS = 0;
    /**
     * {@link ContainerData} index of the total time (in ticks) the current recipe needs.
     */
    public static final int DATA_MAX_PROGRESS = 1;
    public static final int NUM_DATA_VALUES = 2;

    /**
     * How often (in ticks) a running Chronarium re-syncs its progress to nearby clients.
     */
    private static final int SYNC_INTERVAL = 20;

    static final String TAG_PROGRESS = "Progress";
    static final String TAG_MAX_PROGRESS = "MaxProgress";
    /**
     * Same tag name and same layout as {@code AbstractFurnaceBlockEntity}.
     */
    static final String TAG_RECIPES_USED = "RecipesUsed";

    private static final Codec<Map<ResourceKey<Recipe<?>>, Integer>> RECIPES_USED_CODEC =
            Codec.unboundedMap(Recipe.KEY_CODEC, Codec.INT);

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

    /**
     * Recipe id to number of results finished since a player last emptied the output slot. This is
     * the furnace's {@code recipesUsed}; it is what turns "a recipe completed" into "some
     * experience is owed", without having to store a float.
     */
    private final Object2IntOpenHashMap<ResourceKey<Recipe<?>>> recipesUsed = new Object2IntOpenHashMap<>();

    private int progress = 0;
    private int maxProgress = 0;

    /**
     * Snapshots of the two ingredient slots taken when the current progress started. If either of
     * them changes, the progress is thrown away - a half aged cobblestone must not turn into a
     * finished copper block just because the slot was swapped.
     */
    private ItemStack progressInput = ItemStack.EMPTY;
    private ItemStack progressCatalyst = ItemStack.EMPTY;

    /**
     * What the clients were last told about, so a block update is only sent when it matters.
     */
    private ItemStack syncedInput = ItemStack.EMPTY;
    private ItemStack syncedCatalyst = ItemStack.EMPTY;
    private int syncCooldown = 0;

    /**
     * The recipe matching the current ingredient snapshot, so the recipe manager does not have to
     * be scanned on every single tick. Dropped whenever the snapshot changes. Never persisted.
     */
    private @Nullable RecipeHolder<AgingRecipe> cachedRecipe = null;
    private boolean recipeLookupDone = false;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_PROGRESS -> ChronariumBlockEntity.this.progress;
                case DATA_MAX_PROGRESS -> ChronariumBlockEntity.this.maxProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_PROGRESS -> ChronariumBlockEntity.this.progress = value;
                case DATA_MAX_PROGRESS -> ChronariumBlockEntity.this.maxProgress = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return NUM_DATA_VALUES;
        }
    };

    public ChronariumBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(WunderreichBlockEntities.BLOCK_ENTITY_CHRONARIUM, blockPos, blockState);
    }

    // ---------------------------------------------------------------- ticking

    public static void serverTick(Level level, BlockPos pos, BlockState state, ChronariumBlockEntity chronarium) {
        if (level.isClientSide()) return;
        chronarium.tick(level, pos, state);
    }

    private void tick(Level level, BlockPos pos, BlockState state) {
        final ItemStack input = this.items.get(INPUT_SLOT);
        final ItemStack catalyst = this.items.get(CATALYST_SLOT);

        boolean changed = false;
        boolean advancing = false;

        // Any change to either ingredient invalidates the running cycle.
        if (!sameIngredient(input, this.progressInput) || !sameIngredient(catalyst, this.progressCatalyst)) {
            changed |= resetProgress();
            snapshotIngredients();
        }

        final RecipeHolder<AgingRecipe> found = currentRecipe(level, input, catalyst);
        if (found == null) {
            // Nothing (or nothing matching) in the machine.
            changed |= resetProgress();
        } else {
            final AgingRecipe recipe = found.value();
            final ItemStack result = recipe.resultStack();

            if (canAcceptResult(result)) {
                if (this.maxProgress != recipe.time()) {
                    this.maxProgress = recipe.time();
                    changed = true;
                }

                // Progress alone must not mark the block entity dirty: setChanged() drives
                // Level.updateNeighbourForOutputSignal(), so dirtying every tick would poke every
                // neighbouring comparator 20x/s for the whole 400-2400 tick recipe. Vanilla's
                // furnace ticks cookingProgress the same way and only dirties on a real inventory
                // change. Progress still reaches the client via syncToClients() and the open menu
                // via ContainerData, neither of which depends on setChanged().
                this.progress++;
                advancing = true;

                if (this.progress >= this.maxProgress) {
                    finish(found, result);
                    resetProgress();
                    // The input slot just changed because we consumed from it.
                    snapshotIngredients();
                    changed = true;
                }
            }
            // else: the output slot is blocked - stall, but keep the progress we already have.
        }

        if (changed) {
            setChanged();
        }
        syncToClients(level, pos, updateWorkingState(level, pos, state, advancing));
    }

    /**
     * Keeps {@link Chronarium#WORKING} in step with what the machine is actually doing.
     *
     * <h3>Why this is not just {@code setBlock} every tick</h3>
     * {@code setBlock} is expensive in a way that scales badly: it re-lights the column (the block's
     * light emission depends on WORKING, so every write is a real light update), re-sections the
     * chunk, marks it for saving, notifies every neighbour and sends a block change packet to every
     * player tracking the chunk. Doing that 20x/s for a 400-2400 tick recipe, for every Chronarium in
     * a base, is exactly the kind of thing that makes a machine block a server-side liability. This
     * therefore compares against the state that is already in the world and returns early on the
     * overwhelmingly common no-change tick; over a whole recipe it writes twice, once on each edge.
     * <p>
     * For the same reason it deliberately does not call {@code setChanged()}. Nothing that needs
     * saving changed - WORKING lives in the blockstate, not in the block entity's NBT - and
     * {@code setChanged()} would poke every neighbouring comparator, which is the trap the progress
     * counter in {@link #tick} already documents.
     *
     * @param advancing whether progress moved this tick, i.e. a recipe matched and the output slot
     *                  had room. This is not {@link #isAging()}: that goes false for the single tick
     *                  in which a recipe finishes and the counters reset, which would make the dials
     *                  blink off and on again between two items of the same stack.
     * @return the state now in the world, so the caller keeps working with a fresh one.
     */
    private BlockState updateWorkingState(Level level, BlockPos pos, BlockState state, boolean advancing) {
        if (!state.hasProperty(Chronarium.WORKING)) return state;
        if (state.getValue(Chronarium.WORKING) == advancing) return state;

        final BlockState updated = state.setValue(Chronarium.WORKING, advancing);
        level.setBlock(pos, updated, Block.UPDATE_ALL);
        return updated;
    }

    /**
     * The recipe for the current ingredient snapshot. Looked up at most once per snapshot.
     * <p>
     * {@link AgingRecipe#find} only ever finds anything on a {@link net.minecraft.server.level.ServerLevel},
     * which is why the Chronarium has no client ticker at all.
     */
    private @Nullable RecipeHolder<AgingRecipe> currentRecipe(Level level, ItemStack input, ItemStack catalyst) {
        if (!this.recipeLookupDone) {
            final Optional<RecipeHolder<AgingRecipe>> found = AgingRecipe.find(level, input, catalyst);
            this.cachedRecipe = found.orElse(null);
            this.recipeLookupDone = true;
        }
        return this.cachedRecipe;
    }

    private void snapshotIngredients() {
        this.progressInput = this.items.get(INPUT_SLOT).copy();
        this.progressCatalyst = this.items.get(CATALYST_SLOT).copy();
        this.cachedRecipe = null;
        this.recipeLookupDone = false;
    }

    /**
     * Consumes exactly one input item, grows/sets the output and books the experience this
     * completion is worth.
     * <p>
     * The catalyst is deliberately neither read nor written here - it is never consumed, damaged or
     * emptied, no matter what item it is.
     */
    private void finish(RecipeHolder<AgingRecipe> recipe, ItemStack result) {
        // Counts this completion. Nothing is handed out here; that happens when a player takes the
        // result, see awardUsedRecipesAndPopExperience().
        setRecipeUsed(recipe);

        final ItemStack output = this.items.get(OUTPUT_SLOT);
        if (output.isEmpty()) {
            this.items.set(OUTPUT_SLOT, result);
        } else {
            output.grow(result.getCount());
        }

        final ItemStack input = this.items.get(INPUT_SLOT);
        input.shrink(1);
        if (input.isEmpty()) {
            this.items.set(INPUT_SLOT, ItemStack.EMPTY);
        }
    }

    /**
     * Whether the output slot has room for the given result.
     */
    private boolean canAcceptResult(ItemStack result) {
        if (result.isEmpty()) return false;

        final ItemStack output = this.items.get(OUTPUT_SLOT);
        if (output.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(output, result)) return false;

        final int limit = Math.min(this.getMaxStackSize(), output.getMaxStackSize());
        return output.getCount() + result.getCount() <= limit;
    }

    /**
     * @return {@code true} if anything was actually reset.
     */
    private boolean resetProgress() {
        if (this.progress == 0 && this.maxProgress == 0) return false;
        this.progress = 0;
        this.maxProgress = 0;
        return true;
    }

    /**
     * Ingredient slots are compared item+components only. A hopper topping up the input stack must
     * not throw away a running cycle.
     */
    private static boolean sameIngredient(ItemStack a, ItemStack b) {
        if (a.isEmpty() || b.isEmpty()) return a.isEmpty() && b.isEmpty();
        return ItemStack.isSameItemSameComponents(a, b);
    }

    // ---------------------------------------------------------------- experience

    /**
     * Books one completed result for {@code recipeUsed}. Called once per finished item, never with
     * a player - the machine works while nobody is watching, so the experience has to be remembered
     * until somebody shows up.
     */
    @Override
    public void setRecipeUsed(@Nullable RecipeHolder<?> recipeUsed) {
        if (recipeUsed != null) {
            this.recipesUsed.addTo(recipeUsed.id(), 1);
        }
    }

    /**
     * Always {@code null}: like a furnace, this machine tracks a whole multiset of used recipes
     * rather than a single "last recipe", so the single-recipe accessor of
     * {@link RecipeCraftingHolder} has nothing meaningful to return.
     */
    @Override
    public @Nullable RecipeHolder<?> getRecipeUsed() {
        return null;
    }

    /**
     * Deliberately empty, exactly as in {@code AbstractFurnaceBlockEntity} - the interface default
     * would award {@link #getRecipeUsed()}, which is always {@code null} here.
     * {@link #awardUsedRecipesAndPopExperience(ServerPlayer)} is the real entry point.
     */
    @Override
    public void awardUsedRecipes(Player player, List<ItemStack> itemStacks) {
    }

    /**
     * Unlocks the recipes used since the last take, drops the accumulated experience as orbs at the
     * player and resets the counters.
     * <p>
     * This is called from the menu's result slot only, which is what preserves vanilla's quirk that
     * automating a furnace (or a Chronarium) with a hopper forfeits the experience.
     */
    public void awardUsedRecipesAndPopExperience(ServerPlayer player) {
        final List<RecipeHolder<?>> awarded = getRecipesToAwardAndPopExperience(
                player.level(),
                player.position()
        );
        player.awardRecipes(awarded);

        for (RecipeHolder<?> recipe : awarded) {
            player.triggerRecipeCrafted(recipe, this.items);
        }

        this.recipesUsed.clear();
    }

    /**
     * Pops the orbs for everything booked so far and returns the recipes involved, so the caller can
     * unlock them. Does <b>not</b> clear the counters - {@link #awardUsedRecipesAndPopExperience}
     * does that.
     */
    public List<RecipeHolder<?>> getRecipesToAwardAndPopExperience(ServerLevel level, Vec3 position) {
        final List<RecipeHolder<?>> awarded = new ArrayList<>();

        for (Object2IntMap.Entry<ResourceKey<Recipe<?>>> entry : this.recipesUsed.object2IntEntrySet()) {
            level.recipeAccess().byKey(entry.getKey()).ifPresent(recipe -> {
                awarded.add(recipe);
                if (recipe.value() instanceof AgingRecipe aging) {
                    createExperience(level, position, entry.getIntValue(), aging.experience());
                }
            });
        }

        return awarded;
    }

    /**
     * Vanilla's {@code AbstractFurnaceBlockEntity#createExperience}, verbatim in behaviour: the
     * per-item value is multiplied by the number of completions <i>first</i> and only the total is
     * rounded, with the leftover fraction settled by a single random roll. Rounding per item would
     * silently throw away everything below half a point.
     */
    private static void createExperience(ServerLevel level, Vec3 position, int amount, float value) {
        int xpReward = Mth.floor(amount * value);
        final float xpFraction = Mth.frac(amount * value);
        if (xpFraction != 0.0f && level.getRandom().nextFloat() < xpFraction) {
            xpReward++;
        }

        ExperienceOrb.award(level, position, xpReward);
    }

    // ---------------------------------------------------------------- client sync

    private void syncToClients(Level level, BlockPos pos, BlockState state) {
        final ItemStack input = this.items.get(INPUT_SLOT);
        final ItemStack catalyst = this.items.get(CATALYST_SLOT);

        boolean stacksChanged = !ItemStack.matches(input, this.syncedInput)
                || !ItemStack.matches(catalyst, this.syncedCatalyst);

        if (this.syncCooldown > 0) this.syncCooldown--;
        final boolean periodic = this.maxProgress > 0 && this.syncCooldown <= 0;

        if (!stacksChanged && !periodic) return;

        this.syncedInput = input.copy();
        this.syncedCatalyst = catalyst.copy();
        this.syncCooldown = SYNC_INTERVAL;
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // Sends everything saveAdditional writes, i.e. the three slots plus progress/maxProgress.
        return this.saveCustomOnly(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ---------------------------------------------------------------- renderer API

    /**
     * The item currently being aged. Valid on both sides, see the class javadoc.
     */
    public ItemStack getInputStack() {
        return this.items.get(INPUT_SLOT);
    }

    /**
     * The catalyst. Never consumed. Valid on both sides, see the class javadoc.
     */
    public ItemStack getCatalystStack() {
        return this.items.get(CATALYST_SLOT);
    }

    /**
     * The finished result waiting to be taken out.
     */
    public ItemStack getOutputStack() {
        return this.items.get(OUTPUT_SLOT);
    }

    /**
     * Ticks spent on the current recipe.
     */
    public int getProgress() {
        return this.progress;
    }

    /**
     * Ticks the current recipe needs in total, or {@code 0} when nothing is running.
     */
    public int getMaxProgress() {
        return this.maxProgress;
    }

    /**
     * Progress in the range {@code [0, 1]}, or {@code 0} when nothing is running.
     */
    public float getProgressFraction() {
        if (this.maxProgress <= 0) return 0f;
        return Math.min(1f, (float) this.progress / (float) this.maxProgress);
    }

    /**
     * Whether a recipe is currently running.
     */
    public boolean isAging() {
        return this.maxProgress > 0 && this.progress > 0;
    }

    /**
     * Comparator output, driven by how full the machine is.
     */
    public int getRedstoneSignal() {
        return AbstractContainerMenu.getRedstoneSignalFromContainer(this);
    }

    // ---------------------------------------------------------------- persistence

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        ContainerHelper.saveAllItems(valueOutput, this.items);
        valueOutput.putInt(TAG_PROGRESS, this.progress);
        valueOutput.putInt(TAG_MAX_PROGRESS, this.maxProgress);
        valueOutput.store(TAG_RECIPES_USED, RECIPES_USED_CODEC, this.recipesUsed);
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(valueInput, this.items);
        this.progress = valueInput.getIntOr(TAG_PROGRESS, 0);
        this.maxProgress = valueInput.getIntOr(TAG_MAX_PROGRESS, 0);
        this.recipesUsed.clear();
        this.recipesUsed.putAll(valueInput.read(TAG_RECIPES_USED, RECIPES_USED_CODEC).orElse(Map.of()));

        snapshotIngredients();
        this.syncedInput = this.progressInput.copy();
        this.syncedCatalyst = this.progressCatalyst.copy();
    }

    // ---------------------------------------------------------------- container

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    protected @NotNull NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    /**
     * Governs the <b>menu</b> only - what a player may shift-click or drop into a slot. Automation
     * goes through {@link #canPlaceItemThroughFace} instead.
     */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == INPUT_SLOT || slot == CATALYST_SLOT;
    }

    // ---------------------------------------------------------------- automation (WorldlyContainer)

    /**
     * Which slots a given face exposes at all. A hopper (and anything else going through
     * {@link net.minecraft.world.Container}) can only ever see the slots listed here, so this is the
     * primary guard - the two {@code ...ThroughFace} methods below only narrow it further.
     * <ul>
     *     <li>{@link Direction#DOWN} - {@link #OUTPUT_SLOT}. A hopper underneath drains the result
     *     and <b>nothing else</b>; before this existed it also pulled the catalyst out of a running
     *     machine, which silently stalled the recipe.</li>
     *     <li>{@link Direction#UP} - {@link #INPUT_SLOT}, so a hopper on top feeds the item to age.</li>
     *     <li>all four sides - {@link #CATALYST_SLOT}, so the catalyst can be placed once by
     *     automation. It can never be taken back out again, see {@link #canTakeItemThroughFace}.</li>
     * </ul>
     */
    @Override
    public int @NotNull [] getSlotsForFace(Direction direction) {
        if (direction == Direction.DOWN) return SLOTS_FOR_DOWN;
        if (direction == Direction.UP) return SLOTS_FOR_UP;
        return SLOTS_FOR_SIDES;
    }

    /**
     * Insertion, exactly as the furnace does it: defer to the same rule the menu uses. Which slot is
     * reachable from which side is already decided by {@link #getSlotsForFace}.
     */
    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return canPlaceItem(slot, stack);
    }

    /**
     * Extraction. Only the {@link #OUTPUT_SLOT} ever leaves the machine by automation, from any
     * face - the catalyst is not consumed by the recipe and must not be removable by a pipe or
     * hopper either, or the class' central invariant would depend on nobody automating the block.
     * <p>
     * Taking the result this way is legitimate and unchanged in one respect: it awards no
     * experience, because the accumulated {@code recipesUsed} is only paid out through the menu's
     * result slot (see {@link #awardUsedRecipesAndPopExperience}). That is vanilla's furnace quirk.
     */
    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == OUTPUT_SLOT;
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("container.wunderreich.chronarium");
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new ChronariumMenu(containerId, inventory, this, this.dataAccess);
    }
}
