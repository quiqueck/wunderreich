package de.ambertation.wunderreich.utils.math.sdf;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.utils.math.Float3;
import de.ambertation.wunderreich.utils.math.sdf.shapes.*;

import com.mojang.serialization.Codec;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.util.KeyDispatchDataCodec;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import org.jetbrains.annotations.ApiStatus;

public abstract class SDF {
    protected SDF(int inputCount) {
        inputSlots = new SDF[inputCount];
    }

    //---------------------- INPUT SLOTS ----------------------
    protected final SDF[] inputSlots;

    public int getInputSlotCount() {
        return inputSlots.length;
    }

    public boolean hasInputSlots() {
        return inputSlots.length > 0;
    }

    public SDF getSlot(int idx) {
        return inputSlots[idx];
    }

    void setSlotSilent(int idx, SDF sdf) {
        if (inputSlots[idx] != sdf && inputSlots[idx] != null) {
            inputSlots[idx].setParent(null);
        }
        inputSlots[idx] = sdf == null ? Empty.INSTANCE : sdf;
        inputSlots[idx].setParent(this);

    }

    public void setSlot(int idx, SDF sdf) {
        setSlotSilent(idx, sdf);
        this.emitChangeEvent();
    }

    //---------------------- CHANGE EVENTS ----------------------
    public interface OnChange {
        void didChange(SDF sdf);
    }

    private final Set<OnChange> changeEvent = new HashSet<>();
    private SDF parent;

    protected void emitChangeEvent() {
        if (changeEvent != null) changeEvent.forEach(e -> e.didChange(this));
        if (parent != null) parent.emitChangeEvent();
    }

    public void addChangeListener(OnChange listener) {
        changeEvent.add(listener);
    }

    public void removeChangeListener(OnChange listener) {
        changeEvent.remove(listener);
    }

    void setParent(SDF parent) {
        this.parent = parent;
    }

    //---------------------- ABSTRACT METHODS ----------------------

    public abstract double dist(Float3 pos);
    public abstract KeyDispatchDataCodec<? extends SDF> codec();


    //---------------------- SDF REGISTRY ----------------------
    public static final MappedRegistry<Codec<? extends SDF>> SDF_REGISTRY = FabricRegistryBuilder
            .<Codec<? extends SDF>>createSimple(null, Wunderreich.ID("sdf"))
            .attribute(RegistryAttribute.MODDED)
            .buildAndRegister();

    public static final Codec<SDF> CODEC = SDF_REGISTRY.byNameCodec()
                                                       .dispatch((sdf) -> sdf.codec().codec(), Function.identity());

    static void bootstrap(Registry<Codec<? extends SDF>> registry) {
        register(registry, "union", SDFUnion.CODEC);
        register(registry, "intersect", SDFIntersection.CODEC);
        register(registry, "dif", SDFDifference.CODEC);
        register(registry, "invert", SDFInvert.CODEC);

        register(registry, "empty", Empty.CODEC);
        register(registry, "sphere", Sphere.CODEC);
        register(registry, "box", Box.CODEC);
        register(registry, "cylinder", Cylinder.CODEC);
        register(registry, "prism", Prism.CODEC);
        register(registry, "ellipsoid", Ellipsoid.CODEC);
    }

    static Codec<? extends SDF> register(
            Registry<Codec<? extends SDF>> registry,
            String name,
            KeyDispatchDataCodec<? extends SDF> codec
    ) {
        return Registry.register(registry, Wunderreich.ID(name), codec.codec());
    }

    @ApiStatus.Internal
    public static void ensureStaticallyLoaded() {
        bootstrap(SDF_REGISTRY);
    }
}
