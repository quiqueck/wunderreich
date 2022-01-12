package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.config.LevelData;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ServerResources;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DedicatedServer.class, priority = 200)
public class DedicatedServerMixin {
    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private static void wunderreich_loadLevel(Thread thread,
                                              RegistryAccess.RegistryHolder registryHolder,
                                              LevelStorageSource.LevelStorageAccess levelStorageAccess,
                                              PackRepository packRepository,
                                              ServerResources serverResources,
                                              WorldData worldData,
                                              DedicatedServerSettings dedicatedServerSettings,
                                              DataFixer dataFixer,
                                              MinecraftSessionService minecraftSessionService,
                                              GameProfileRepository gameProfileRepository,
                                              GameProfileCache gameProfileCache,
                                              ChunkProgressListenerFactory chunkProgressListenerFactory,
                                              CallbackInfo ci) {

        LevelData.getInstance().loadNewLevel(levelStorageAccess);
    }
}
