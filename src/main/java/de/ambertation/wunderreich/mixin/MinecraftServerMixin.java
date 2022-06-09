package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.config.LevelData;
import de.ambertation.wunderreich.interfaces.WunderKisteExtensionProvider;
import de.ambertation.wunderreich.utils.WunderKisteServerExtension;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.WorldData;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.util.Map;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements WunderKisteExtensionProvider {
    private final WunderKisteServerExtension wunderkiste = new WunderKisteServerExtension();

    public WunderKisteServerExtension getWunderKisteExtension() {
        return wunderkiste;
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    public void wunderreich_stop(CallbackInfo ci) {
        if (wunderkiste != null) wunderkiste.onCloseServer();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void wunderreich_init(Thread thread,
                                 RegistryAccess.RegistryHolder registryHolder,
                                 LevelStorageAccess levelStorageAccess,
                                 WorldData worldData,
                                 PackRepository packRepository,
                                 Proxy proxy,
                                 DataFixer dataFixer,
                                 ServerResources serverResources,
                                 MinecraftSessionService minecraftSessionService,
                                 GameProfileRepository gameProfileRepository,
                                 GameProfileCache gameProfileCache,
                                 ChunkProgressListenerFactory chunkProgressListenerFactory,
                                 CallbackInfo ci) {

        LevelData.getInstance().loadNewLevel(levelStorageAccess);

        if (wunderkiste != null) {
            wunderkiste.onStartServer(registryHolder);
        }
    }

    @Shadow
    @Final
    private Map<ResourceKey<Level>, ServerLevel> levels;

    @Shadow
    @Final
    protected WorldData worldData;

    @Shadow
    public abstract RegistryAccess registryAccess();

    @Shadow
    @Final
    protected RegistryAccess.RegistryHolder registryHolder;

    @Inject(method = "createLevels", at = @At("TAIL"))
    public void wunderreich_create(ChunkProgressListener chunkProgressListener, CallbackInfo ci) {
        wunderkiste.onLevelsCreated(levels);
    }

}
