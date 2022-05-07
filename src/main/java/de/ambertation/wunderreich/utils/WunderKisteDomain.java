package de.ambertation.wunderreich.utils;

import de.ambertation.wunderreich.client.WunderreichClient;

import net.minecraft.client.resources.model.Material;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

public enum WunderKisteDomain implements StringRepresentable {
    WHITE("white", Items.WHITE_DYE, 0xFFFFFF, "wunder_kiste"),
    ORANGE("orange", Items.ORANGE_DYE, 0xF9932B, "wunder_kiste_bw"),
    MAGENTA("magenta", Items.MAGENTA_DYE, 0xD660D1, "wunder_kiste_bw"),
    LIGHT_BLUE("light_blue", Items.LIGHT_BLUE_DYE, 0x5CB7E7),
    YELLOW("yellow", Items.YELLOW_DYE, 0xFED93F, "wunder_kiste_bw"),
    LIME("lime", Items.LIME_DYE, 0x86CC26, "wunder_kiste_bw"),
    PINK("pink", Items.PINK_DYE, 0xF4B2C9, "wunder_kiste_bw"),
    GRAY("gray", Items.GRAY_DYE, 0x474F52, "wunder_kiste_bw"),
    LIGHT_GRAY("light_gray", Items.LIGHT_GRAY_DYE, 0x9D9D97, "wunder_kiste_bw"),
    CYAN("cyan", Items.CYAN_DYE, 0x169B9C, "wunder_kiste_bw"),
    PURPLE("purple", Items.PURPLE_DYE, 0x9743CD, "wunder_kiste_bw"),
    BLUE("blue", Items.BLUE_DYE, 0x2C2F90),
    BROWN("brown", Items.BROWN_DYE, 0x835432, "wunder_kiste_bw"),
    GREEN("green", Items.GREEN_DYE, 0x658619, "wunder_kiste_bw"),
    RED("red", Items.RED_DYE, 0xB8342C, "wunder_kiste_bw"),
    BLACK("black", Items.BLACK_DYE, 0x252529, "wunder_kiste_bw");

    public final Item triggerItem;
    public final int color;
    public final TextColor textColor;
    public final int overlayColor;
    private final String name;
    @Environment(EnvType.CLIENT)
    private final Object texture;

    WunderKisteDomain(String name, Item triggerItem, int color, String texture) {
        this.name = name;
        this.triggerItem = triggerItem;
        this.color = color;
        this.textColor = TextColor.fromRgb(color);
        if (texture.equals("wunder_kiste_bw") || texture.equals("wunder_kiste"))
            overlayColor = color;
        else overlayColor = 0xFFFFFF;
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            this.texture = WunderreichClient.getWunderkisteColor(texture);
        } else {
            this.texture = null;
        }
    }

    WunderKisteDomain(String name, Item triggerItem, int color) {
        this(name, triggerItem, color, "wunder_kiste_" + name);
    }

    @Environment(EnvType.CLIENT)
    public Material getMaterial() {
        return (Material) texture;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
