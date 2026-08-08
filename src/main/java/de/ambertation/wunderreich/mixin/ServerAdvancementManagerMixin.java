package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.registries.WunderreichAdvancements;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 26.3 turned advancements into a datapack registry: {@code ServerAdvancementManager} is no longer a
 * reload listener with an {@code apply(Map, ResourceManager, ProfilerFiller)} phase, it is built from a
 * {@code HolderLookup.Provider} in its constructor. So instead of adding our JSON advancements to the map
 * that is being applied, we append them to the finished map (and tree) at the end of the constructor.
 */
@Mixin(value = ServerAdvancementManager.class, priority = 200)
public class ServerAdvancementManagerMixin {
    @Shadow
    @Final
    @Mutable
    private Map<Identifier, AdvancementHolder> advancements;

    @Shadow
    @Final
    private AdvancementTree tree;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void wunder_addAdvancements(HolderLookup.Provider registries, CallbackInfo info) {
        final var dynamicOps = registries.createSerializationContext(JsonOps.INSTANCE);
        final List<AdvancementHolder> added = new ArrayList<>();
        final Map<Identifier, AdvancementHolder> merged = new LinkedHashMap<>(this.advancements);

        WunderreichAdvancements.ADVANCEMENTS
                .entrySet()
                .stream()
                .filter(e -> !merged.containsKey(e.getKey()))
                .forEach(e -> {
                    DataResult<Advancement> advancement = Advancement.CODEC.parse(dynamicOps, e.getValue());
                    AdvancementHolder holder = new AdvancementHolder(e.getKey(), advancement.getOrThrow());
                    merged.put(e.getKey(), holder);
                    added.add(holder);
                });

        if (added.isEmpty()) return;

        this.advancements = ImmutableMap.copyOf(merged);
        this.tree.addAll(added);
    }
}
