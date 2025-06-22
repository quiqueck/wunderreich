package de.ambertation.wunderreich.blocks;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheringCopperWall extends WallBlock implements WeatheringCopper {
    public static final MapCodec<WeatheringCopperWall> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                                        WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state").forGetter(
                                                WeatheringCopper::getAge), propertiesCodec()
                                )
                                .apply(instance, WeatheringCopperWall::new)
    );
    private final WeatheringCopper.WeatherState weatherState;


    @Override
    public MapCodec<net.minecraft.world.level.block.WallBlock> codec() {
        return (MapCodec<net.minecraft.world.level.block.WallBlock>) (Object) CODEC;
    }

    public WeatheringCopperWall(WeatheringCopper.WeatherState weatherState, BlockBehaviour.Properties properties) {
        super(properties);
        this.weatherState = weatherState;
    }

    public WeatheringCopperWall(
            WeatheringCopper.WeatherState weatherState,
            Block baseBlock,
            ResourceKey<Block> key
    ) {
        super(baseBlock, key);
        this.weatherState = weatherState;
    }

    @Override
    protected void randomTick(
            BlockState blockState,
            ServerLevel serverLevel,
            BlockPos blockPos,
            RandomSource randomSource
    ) {
        this.changeOverTime(blockState, serverLevel, blockPos, randomSource);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState blockState) {
        return WeatheringCopper.getNext(blockState.getBlock()).isPresent();
    }

    public WeatheringCopper.WeatherState getAge() {
        return this.weatherState;
    }
}
