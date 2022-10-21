package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blockentities.WunderKisteBlockEntity;

import net.minecraft.core.Registry;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

public class WunderreichBlockEntities {
    public static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(
            String id,
            FabricBlockEntityTypeBuilder<T> builder
    ) {
        return Registry.register(Registry.BLOCK_ENTITY_TYPE, Wunderreich.ID(id), builder.build(null));
    }

    public static void register() {

    }

    public static BlockEntityType<WunderKisteBlockEntity> BLOCK_ENTITY_WUNDER_KISTE = registerBlockEntity(
            "wunder_kiste_block_entity",
            FabricBlockEntityTypeBuilder.create(WunderKisteBlockEntity::new, WunderreichBlocks.WUNDER_KISTE)
    );
}
