package de.ambertation.wunderreich.loot;

import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.interfaces.CanDropLoot;
import de.ambertation.wunderreich.registries.WunderreichBlocks;

import net.minecraft.advancements.predicates.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LootTableHelper {
    public record Drop(ItemLike item, int count) {}

    public static Drop drop(ItemLike item, int count) {
        return new Drop(item, count);
    }

    public static class BlockLootProvider {
        private final @NotNull BlockLootSubProvider provider;

        public BlockLootProvider(
                @NotNull BlockLootSubProvider provider
        ) {
            this.provider = provider;
        }

        public void dropSelf(Block block) {
            provider.dropSelf(block);
        }

        public void dropWhenSilkTouch(Block block) {
            provider.dropWhenSilkTouch(block);
        }

        public void dropSlab(SlabBlock block) {
            provider.add(block, provider.createSlabItemTable(block));
        }

        public void dropSlabWhenSilkTouch(@NotNull Block block) {
            dropSlabWhenSilkTouch(block, null);
        }

        public void dropSlabWhenSilkTouch(@NotNull Block block, @Nullable Block orElseBlock) {
            provider.add(block, createSlabItemTableWithSilkTouch(block, orElseBlock));
        }

        public void dropSilkTouchOrElse(
                Block block,
                Drop... elseDrops
        ) {
            provider.add(block, createSilkTouchOrElse(block, elseDrops));
        }

        private LootPoolSingletonContainer.Builder<? extends LootPoolSingletonContainer.Builder<?>>
        createSlabItemTable(Block block) {
            return provider
                    .applyExplosionDecay(
                            block,
                            LootItem.lootTableItem(block)
                                    .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F)).when(
                                            LootItemBlockStatePropertyCondition
                                                    .hasBlockStateProperties(block)
                                                    .setProperties(
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
                                                .when(provider.hasSilkTouch())
                                                .setRolls(ConstantValue.exactly(1.0F))
                                                .add(createSlabItemTable(block))
                                );
            }
            return LootTable.lootTable()
                            .withPool(
                                    LootPool.lootPool()
                                            .setRolls(ConstantValue.exactly(1.0F))
                                            .add(createSlabItemTable(block)
                                                    .when(provider.hasSilkTouch())
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
                                .when(provider.doesNotHaveSilkTouch())
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(provider.applyExplosionDecay(
                                        drop.item(),
                                        LootItem.lootTableItem(drop.item())
                                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(drop.count())))
                                ))
                );
            }

            return table.withPool(
                    LootPool.lootPool()
                            .when(provider.hasSilkTouch())
                            .setRolls(ConstantValue.exactly(1.0F))
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
