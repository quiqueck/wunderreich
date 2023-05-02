package de.ambertation.wunderreich.mixin.client.overlay;

import net.minecraft.client.MouseHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {
    @Accessor("xpos")
    double wunder_getXPos();
    @Accessor("xpos")
    void wunder_setXPos(double n);

    @Accessor("ypos")
    double wunder_getYPos();
    @Accessor("ypos")
    void wunder_setYPos(double n);

    @Accessor("accumulatedDX")
    double wunder_getAccumulatedDX();
    @Accessor("accumulatedDX")
    void wunder_setAccumulatedDX(double n);

    @Accessor("accumulatedDY")
    double wunder_getAccumulatedDY();
    @Accessor("accumulatedDY")
    void wunder_setAccumulatedDY(double n);
}
