package de.ambertation.wunderreich.loot;

import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.interfaces.CanDropLoot;
import de.ambertation.wunderreich.registries.WunderreichBlocks;

import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.advancements.predicates.EnchantmentPredicate;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.advancements.predicates.StatePropertiesPredicate;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchBlock;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LootTableHelper {
    public record Drop(ItemLike item, int count) {}

    public static Drop drop(ItemLike item, int count) {
        return new Drop(item, count);
    }

    public static class BlockLootProvider {
        private final @NotNull BlockLootSubProvider provider;
        private final Map<Block, Set<ItemLike>> references = new LinkedHashMap<>();

        public BlockLootProvider(
                @NotNull BlockLootSubProvider provider
        ) {
            this.provider = provider;
        }

        /**
         * Blocks and items (besides the block itself) that the loot table of {@code block} refers to. The data
         * generator uses this to make each loot table conditional on all of them, since 26.3 refuses to load
         * the whole datapack registry if a loot table refers to a block or item that was disabled in the config.
         */
        public Set<ItemLike> referencesOf(Block block) {
            return references.getOrDefault(block, Collections.emptySet());
        }

        private void reference(Block block, ItemLike... others) {
            final Set<ItemLike> refs = references.computeIfAbsent(block, b -> new LinkedHashSet<>());
            for (ItemLike other : others) {
                if (other != null && other != block) refs.add(other);
            }
        }

        /**
         * 26.3 turned the vanilla silk-touch loot condition into a {@code minecraft:predicate} registry
         * entry ({@code minecraft:tool/can_silk_touch}), which {@code BlockLootSubProvider#hasSilkTouch()}
         * resolves through the registry. Fabric's data generator builds the reloadable registries (incl.
         * {@code minecraft:predicate}) EMPTY, so that lookup throws during datagen. Rebuild the exact same
         * condition inline instead - see {@code LootPredicates#bootstrap}.
         */
        private LootItemCondition.Builder hasSilkTouch() {
            return MatchTool.toolMatches(
                    ItemPredicate.Builder
                            .item()
                            .withComponents(
                                    DataComponentMatchers.Builder
                                            .components()
                                            .partial(
                                                    DataComponentPredicates.ENCHANTMENTS,
                                                    EnchantmentsPredicate.enchantments(List.of(
                                                            new EnchantmentPredicate(
                                                                    provider.enchantments.getOrThrow(Enchantments.SILK_TOUCH),
                                                                    MinMaxBounds.Ints.atLeast(1)
                                                            )
                                                    ))
                                            )
                                            .build()
                            )
            );
        }

        private LootItemCondition.Builder doesNotHaveSilkTouch() {
            return InvertedLootItemCondition.invert(hasSilkTouch());
        }

        public void dropSelf(Block block) {
            provider.dropSelf(block);
        }

        public void dropOther(Block block, ItemLike replacement) {
            reference(block, replacement);
            provider.dropOther(block, replacement);
        }

        public void dropWhenSilkTouch(Block block) {
            provider.dropWhenSilkTouch(block);
        }

        public void dropSlab(SlabBlock block) {
            provider.add(block, provider.createSlabItemTable(block));
        }

        public void dropSlabReplacement(SlabBlock block, ItemLike replacement) {
            reference(block, replacement);
            provider.add(
                    block,
                    LootTable.lootTable()
                             .withPool(
                                     LootPool.lootPool()
                                             .setRolls(ContextIntProviders.exactly(1))
                                             .add(createSlabItemTable(block, replacement))
                             )
            );
        }

        public void dropSlabWhenSilkTouch(@NotNull Block block) {
            dropSlabWhenSilkTouch(block, null);
        }

        public void dropSlabWhenSilkTouch(@NotNull Block block, @Nullable Block orElseBlock) {
            reference(block, orElseBlock);
            provider.add(block, createSlabItemTableWithSilkTouch(block, orElseBlock));
        }

        public void dropSilkTouchOrElse(
                Block block,
                Drop... elseDrops
        ) {
            for (Drop drop : elseDrops) reference(block, drop.item());
            provider.add(block, createSilkTouchOrElse(block, elseDrops));
        }

        private UniformContainerBase.Builder<? extends UniformContainerBase.Builder<?>>
        createSlabItemTable(Block block) {
            return createSlabItemTable(block, block);
        }

        private UniformContainerBase.Builder<? extends UniformContainerBase.Builder<?>>
        createSlabItemTable(Block block, ItemLike dropItem) {
            return provider
                    .applyExplosionDecay(
                            block,
                            LootItem.lootTableItem(dropItem)
                                    .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(2)).when(
                                            MatchBlock.blockMatches(
                                                    provider.blocks,
                                                    block,
                                                    StatePropertiesPredicate.Builder
                                                            .properties()
                                                            .hasProperty(
                                                                    SlabBlock.TYPE,
                                                                    SlabType.DOUBLE
                                                            )
                                            )
                                    ))
                    );
        }


        private LootTable.Builder createSlabItemTableWithSilkTouch(
                @NotNull Block block,
                @Nullable Block orElseBlock
        ) {
            if (orElseBlock == null) {
                return LootTable.lootTable()
                                .withPool(
                                        LootPool.lootPool()
                                                .when(hasSilkTouch())
                                                .setRolls(ContextIntProviders.exactly(1))
                                                .add(createSlabItemTable(block))
                                );
            }
            return LootTable.lootTable()
                            .withPool(
                                    LootPool.lootPool()
                                            .setRolls(ContextIntProviders.exactly(1))
                                            .add(createSlabItemTable(block)
                                                    .when(hasSilkTouch())
                                                    .otherwise(createSlabItemTable(orElseBlock))
                                            )
                            );
        }

        /**
         * With silk touch the block drops itself, otherwise it drops <em>every</em> one of
         * {@code elseDrops}. Each else-drop gets its own pool: entries inside one pool compete for the
         * pool's single roll, so putting them all into one pool would hand out only one of them.
         */
        private LootTable.Builder createSilkTouchOrElse(
                Block blockWithSilkTouch,
                Drop[] elseDrops
        ) {
            LootTable.Builder table = LootTable.lootTable();
            for (Drop drop : elseDrops) {
                table.withPool(
                        LootPool.lootPool()
                                .when(doesNotHaveSilkTouch())
                                .setRolls(ContextIntProviders.exactly(1))
                                .add(provider.applyExplosionDecay(
                                        drop.item(),
                                        LootItem.lootTableItem(drop.item())
                                                .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(drop.count())))
                                ))
                );
            }

            return table.withPool(
                    LootPool.lootPool()
                            .when(hasSilkTouch())
                            .setRolls(ContextIntProviders.exactly(1))
                            .add(provider.applyExplosionDecay(
                                    blockWithSilkTouch,
                                    LootItem.lootTableItem(blockWithSilkTouch)
                            ))
            );
        }
    }

    public static void provideAllBlocks(LootTableHelper.BlockLootProvider provider) {
        WunderreichBlocks
                .getAllBlocks()
                .stream()
                .filter(bl -> bl instanceof CanDropLoot)
                .filter(Configs.BLOCK_CONFIG::isEnabled)
                .forEach(bl -> ((CanDropLoot) bl).buildLootTable(provider));
    }
}
