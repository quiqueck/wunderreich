package de.ambertation.wunderreich;

import de.ambertation.wunderreich.blockentities.BoxOfEirBlockEntity;
import de.ambertation.wunderreich.blocks.BoxOfEirBlock;
import de.ambertation.wunderreich.network.CycleTradesMessage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

public class Wunderreich implements ModInitializer {
	public static final String MOD_ID = "wunderreich";
	
	public static final Block BOX_OF_EIR = new BoxOfEirBlock(
		BlockBehaviour.Properties
			.of(Material.STONE)
			.requiresCorrectToolForDrops()
			.strength(12.5F, 800.0F)
			.lightLevel((blockState) -> {
				return 7;
			})
	);
	
	public static BlockEntityType<BoxOfEirBlockEntity> BLOCK_ENTITY_BOX_OF_EIR;
	public static net.minecraft.client.resources.model.Material BOX_OF_EIR_LOCATION = chestMaterial("box_of_eir");
	
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		Registry.register(
			Registry.BLOCK,
			new ResourceLocation(MOD_ID, "box_of_eir"),
			BOX_OF_EIR
		);
		
		Registry.register(
			Registry.ITEM,
			new ResourceLocation(MOD_ID, "box_of_eir"),
			new BlockItem(BOX_OF_EIR, new FabricItemSettings().group(CreativeModeTab.TAB_DECORATIONS))
		);
		
		BLOCK_ENTITY_BOX_OF_EIR = Registry.register(
			Registry.BLOCK_ENTITY_TYPE,
			new ResourceLocation(Wunderreich.MOD_ID, "box_of_eir_block_entity"),
			FabricBlockEntityTypeBuilder.create(BoxOfEirBlockEntity::new, Wunderreich.BOX_OF_EIR).build(null)
		);
		
		CycleTradesMessage.register();
	}
	
	private static net.minecraft.client.resources.model.Material chestMaterial(String string) {
		return new net.minecraft.client.resources.model.Material(Sheets.CHEST_SHEET, new ResourceLocation(Wunderreich.MOD_ID, "entity/chest/" + string));
	}
}
