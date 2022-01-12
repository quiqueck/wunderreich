package de.ambertation.wunderreich.config;

import de.ambertation.wunderreich.Wunderreich;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import ru.bclib.config.NamedPathConfig;
import ru.bclib.config.NamedPathConfig.ConfigToken;
import ru.bclib.config.PathConfig;

public class ItemConfig extends NamedPathConfig {
	public static final String ITEMS_CATEGORY = "items";
	
	public static final ConfigToken<Boolean> WHISPERER_BLANK = ConfigToken.Boolean(true, "whisperer_blank", ITEMS_CATEGORY);
	public static final ConfigToken<Boolean> WHISPERER = ConfigToken.Boolean(true, "whisperer", ITEMS_CATEGORY);
	
	public ItemConfig() {
		super(Wunderreich.MOD_ID, "items");
	}
	
	public boolean isEnabled(Item item){
		ResourceLocation id = Registry.ITEM.getKey(item);
		if (id==null) return false;
		
		return this.getBoolean(ITEMS_CATEGORY, id.getPath());
	}
}
