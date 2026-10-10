package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blocks.Chronarium;
import de.ambertation.wunderreich.blocks.SuctionTube;
import de.ambertation.wunderreich.blocks.WhisperImprinter;
import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.items.WunderKisteItem;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

public class WunderreichBlocks {
    private static final List<Block> BLOCKS = new ArrayList<>(64);
    /**
     * Blocks this class added to the {@link FlammableBlockRegistry}. Used to answer "is it already
     * flammable?" before tags are loaded, when Fabric's registry can't be queried. Declared before the
     * block fields below, which register while this class initializes.
     */
    private static final Set<Block> REGISTERED_AS_FLAMMABLE = Collections.newSetFromMap(new IdentityHashMap<>());
    private static volatile boolean tagsLoaded = false;

    static {
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> tagsLoaded = true);
    }

    // Blocks vanilla now ships its own version of (e.g. wool slabs/stairs since 26.3), mapped to
    // that vanilla equivalent. Kept registered so existing worlds/inventories keep resolving them,
    // but hidden from the creative tab and, when broken, drop the vanilla replacement instead of
    // themselves - see CreativeTabs and CanDropLoot.
    private static final Map<Block, Block> VANILLA_REPLACEMENTS = new HashMap<>();

    static void registerVanillaReplacement(Block block, Block vanillaReplacement) {
        if (block != null && vanillaReplacement != null) {
            VANILLA_REPLACEMENTS.put(block, vanillaReplacement);
        }
    }

    public static Block getVanillaReplacement(Block block) {
        return VANILLA_REPLACEMENTS.get(block);
    }

    public static boolean isSupersededByVanilla(Block block) {
        return VANILLA_REPLACEMENTS.containsKey(block);
    }
    public static final Block WUNDER_KISTE = registerBlock(
            "wunder_kiste",
            null,
            (baseBlock, key) -> new WunderKisteBlock(key),
            (baseBlock, key) -> new WunderKisteItem(baseBlock, key)
    );
    public static final Block WHISPER_IMPRINTER = registerBlock(
            "whisper_imprinter",
            null,
            (baseBlock, key) -> new WhisperImprinter(key), Configs.MAIN.enableWhispers.get()
    );
    public static final Block SUCTION_TUBE = registerBlock(
            "suction_tube",
            null,
            (baseBlock, key) -> new SuctionTube(key)
    );
    public static final Block CHRONARIUM = registerBlock(
            "chronarium",
            null,
            (baseBlock, key) -> new Chronarium(key)
    );


    public static Collection<Block> getAllBlocks() {
        return Configs.BLOCK_CONFIG.getAllObjects();
    }

    static Block registerBlock(
            String name,
            Block baseBlock,
            BiFunction<Block, ResourceKey<Block>, Block> creator,
            boolean register
    ) {
        if (register) {
            return registerBlock(name, baseBlock, creator);
        }
        return null;
    }

    private static Block registerBlock(
            String name,
            Block baseBlock,
            BiFunction<Block, ResourceKey<Block>, Block> creator
    ) {
        return registerBlock(
                name,
                baseBlock,
                creator,
                (block, key) -> new BlockItem(block, WunderreichItems.makeBlockItemSettings().setId(key))
        );
    }

    private static Block registerBlock(
            String name,
            Block baseBlock,
            BiFunction<Block, ResourceKey<Block>, Block> creator,
            BiFunction<Block, ResourceKey<Item>, BlockItem> itemCreator
    ) {
        if (Configs.BLOCK_CONFIG.booleanOrDefault(name).get()) {
            final Identifier id = Wunderreich.ID(name);
            final ResourceKey<Block> key = ResourceKey.create(BuiltInRegistries.BLOCK.key(), id);


            final Block block = creator.apply(baseBlock, key);
            Configs.BLOCK_CONFIG.newBooleanFor(name, block);
            BLOCKS.add(block);


            if (block.defaultBlockState().ignitedByLava() && !isAlreadyFlammable(block)) {
                FlammableBlockRegistry.getDefaultInstance().add(block, 5, 5);
                REGISTERED_AS_FLAMMABLE.add(block);
            }

            Registry.register(BuiltInRegistries.BLOCK, id, block);

            final ResourceKey<Item> itemKey = ResourceKey.create(BuiltInRegistries.ITEM.key(), id);
            BlockItem item = itemCreator.apply(block, itemKey);
            if (item != Items.AIR) {
                Registry.register(BuiltInRegistries.ITEM, id, item);
                WunderreichItems.processItem(id, item);
            }

            processBlock(id, block);

            return block;
        }
        return null;
    }

    private static boolean isAlreadyFlammable(Block block) {
        // Fabric's FlammableBlockRegistry#get resolves every tag-based entry another mod registered, which
        // throws "Tags not bound" during mod initialization (blocks are registered long before tags load).
        // Until then, tag membership is unknowable anyway, so only our own block-level entries count.
        if (!tagsLoaded) {
            return REGISTERED_AS_FLAMMABLE.contains(block);
        }
        return FlammableBlockRegistry.getDefaultInstance().get(block).getBurnOdds() != 0;
    }

    public static void processBlock(Identifier id, Block bl) {

    }

    public static BlockBehaviour.Properties makeStoneBlockSettings() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE);
    }

    public static void register() {
        WunderreichSlabBlocks.register();
        WunderreichStairBlocks.register();
        WunderreichWallBlocks.register();
    }

}
