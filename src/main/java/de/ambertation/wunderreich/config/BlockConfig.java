package de.ambertation.wunderreich.config;

import de.ambertation.wunderreich.Wunderreich;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import ru.bclib.config.NamedPathConfig;

public class BlockConfig extends NamedPathConfig {
	public BlockConfig() {
		super(Wunderreich.MOD_ID, "blocks");
	}
	
	public boolean isEnabled(Block block){
		ResourceLocation id = Registry.BLOCK.getKey(block);
		return id!=null;
	}
}
