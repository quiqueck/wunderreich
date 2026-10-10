package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blockentities.ChronariumBlockEntity;
import de.ambertation.wunderreich.blockentities.SuctionTubeBlockEntity;
import de.ambertation.wunderreich.blockentities.WunderKisteBlockEntity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

public class WunderreichBlockEntities {
    public static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(
            String id,
            FabricBlockEntityTypeBuilder<T> builder
    ) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Wunderreich.ID(id), builder.build(null));
    }

    public static void register() {

    }

    public static BlockEntityType<WunderKisteBlockEntity> BLOCK_ENTITY_WUNDER_KISTE = registerBlockEntity(
            "wunder_kiste_block_entity",
            FabricBlockEntityTypeBuilder.create(WunderKisteBlockEntity::new, WunderreichBlocks.WUNDER_KISTE)
    );

    public static BlockEntityType<SuctionTubeBlockEntity> BLOCK_ENTITY_SUCTION_TUBE = registerBlockEntity(
            "suction_tube_block_entity",
            FabricBlockEntityTypeBuilder.create(SuctionTubeBlockEntity::new, WunderreichBlocks.SUCTION_TUBE)
    );

    public static BlockEntityType<ChronariumBlockEntity> BLOCK_ENTITY_CHRONARIUM = registerBlockEntity(
            "chronarium_block_entity",
            FabricBlockEntityTypeBuilder.create(ChronariumBlockEntity::new, WunderreichBlocks.CHRONARIUM)
    );
}
