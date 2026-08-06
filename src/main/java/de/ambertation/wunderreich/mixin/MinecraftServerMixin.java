package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.config.LevelData;
import de.ambertation.wunderreich.interfaces.WunderKisteExtensionProvider;
import de.ambertation.wunderreich.utils.WunderKisteDomain;
import de.ambertation.wunderreich.utils.WunderKisteServerExtension;

import com.mojang.datafixers.DataFixer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.notifications.NotificationManager;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.util.Map;
import java.util.Optional;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements WunderKisteExtensionProvider {
    private final WunderKisteServerExtension wunderkiste = new WunderKisteServerExtension();

    public WunderKisteServerExtension getWunderKisteExtension() {
        return wunderkiste;
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    public void wunderreich_stop(CallbackInfo ci) {
        wunderkiste.onCloseServer();
    }

    /**
     * 26.2 appended a {@link NotificationManager} to the {@link MinecraftServer} constructor. An
     * {@code <init>} handler has to mirror the constructor descriptor exactly, so the parameter is
     * repeated here even though we do not use it - without it the injector is rejected at
     * mixin-apply time, which nothing catches at compile time.
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    public void wunderreich_init(
            Thread thread,
            LevelStorageAccess levelStorageAccess,
            PackRepository packRepository,
            WorldStem worldStem,
            Optional<GameRules> gameRules,
            Proxy proxy,
            DataFixer dataFixer,
            Services services,
            LevelLoadListener levelLoadListener,
            boolean bl,
            NotificationManager notificationManager,
            CallbackInfo ci
    ) {
        WunderKisteDomain.ID.loadNewLevel();
        LevelData.getInstance().loadNewLevel(levelStorageAccess);

        wunderkiste.onStartServer(worldStem.registries());
    }

    @Shadow
    @Final
    private Map<ResourceKey<Level>, ServerLevel> levels;

    @Inject(method = "createLevels", at = @At("TAIL"))
    public void wunderreich_create(CallbackInfo ci) {
        wunderkiste.onLevelsCreated(levels);
    }

}
