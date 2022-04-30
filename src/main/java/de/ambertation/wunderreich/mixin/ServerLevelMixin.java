package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.interfaces.WunderKisteExtensionProvider;
import de.ambertation.wunderreich.registries.WunderreichRules;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;
import org.jetbrains.annotations.NotNull;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Shadow
    @NotNull
    public abstract MinecraftServer getServer();

    @Shadow
    @Final
    private ServerLevelData serverLevelData;

    @Inject(method = "saveLevelData", at = @At("TAIL"))
    public void wunderreich_save(CallbackInfo ci) {
        if (getServer() instanceof WunderKisteExtensionProvider exWunderkiste) {
            exWunderkiste.getWunderKisteExtension().saveAll();
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void wunderreich_init(MinecraftServer minecraftServer,
                                 Executor executor,
                                 LevelStorageSource.LevelStorageAccess levelStorageAccess,
                                 ServerLevelData serverLevelData,
                                 ResourceKey resourceKey,
                                 LevelStem levelStem,
                                 ChunkProgressListener chunkProgressListener,
                                 boolean bl,
                                 long l,
                                 List list,
                                 boolean bl2,
                                 CallbackInfo ci) {
        WunderreichRules.onLevelLoad((ServerLevel) (Object) this, serverLevelData);
    }
}
