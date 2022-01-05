package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.gui.whisperer.WhispererMenu;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import ru.bclib.api.TagAPI;
import ru.bclib.interfaces.TagProvider;

import java.util.List;

public class WhisperImprinter extends Block implements TagProvider {
    /**
     * Creates a new Block
     */
    public WhisperImprinter() {
        super(WunderreichBlocks.makeStoneBlockSettings()
                               .mapColor(MaterialColor.LAPIS)
                               .strength(5.0f, 1200.0f)
                               .luminance(8)
                               .breakByTool(FabricToolTags.PICKAXES)
                               .requiresTool()
        );
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        player.openMenu(blockState.getMenuProvider(level, blockPos));


        return InteractionResult.CONSUME;
    }

    @Override
    @Nullable
    public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
        return new SimpleMenuProvider((i, inventory, player) -> new WhispererMenu(i, inventory, ContainerLevelAccess.create(level, blockPos)), new TextComponent("Hello"));
    }
    
    @Override
    public void addTags(List<Named<Block>> blockTags, List<Named<Item>> itemTags) {
        blockTags.add(TagAPI.MINEABLE_PICKAXE);
    }
}
