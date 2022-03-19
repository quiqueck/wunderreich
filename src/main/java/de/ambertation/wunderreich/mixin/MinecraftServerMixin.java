package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.config.LevelData;
import de.ambertation.wunderreich.interfaces.WunderKisteContainerProvider;
import de.ambertation.wunderreich.inventory.WunderKisteContainer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements WunderKisteContainerProvider {
    private WunderKisteContainer wunderKisteContainer;

    public WunderKisteContainer getWunderKisteContainer() {
        return wunderKisteContainer;
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    public void wunderreich_stop(CallbackInfo ci) {
        System.out.println("Unloading Cache for Box of Eir");
        //Make sure the levels can unload when the server closes
        WunderKisteBlock.liveBlocks.clear();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void wunderreich_init(Thread thread, LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer dataFixer, MinecraftSessionService minecraftSessionService, GameProfileRepository gameProfileRepository, GameProfileCache gameProfileCache, ChunkProgressListenerFactory chunkProgressListenerFactory, CallbackInfo ci) {
        LevelData.getInstance().loadNewLevel(levelStorageAccess);

        //we start a new world, so clear any old block
        WunderKisteBlock.liveBlocks.clear();
        wunderKisteContainer = new WunderKisteContainer();
        wunderKisteContainer.load();
        wunderKisteContainer.addListener((container) -> {
            WunderKisteBlock.updateAllBoxes((MinecraftServer) (Object) this, false, true);
        });
    }

}
