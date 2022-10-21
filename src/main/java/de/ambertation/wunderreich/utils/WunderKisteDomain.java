package de.ambertation.wunderreich.utils;

import de.ambertation.wunderreich.client.WunderreichClient;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.items.WunderKisteItem;
import de.ambertation.wunderreich.registries.WunderreichBlocks;

import net.minecraft.client.resources.model.Material;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

public enum WunderKisteDomain implements StringRepresentable {
    WHITE("white", Items.WHITE_DYE, 0xFFFFFF, false, "wunder_kiste"),
    ORANGE("orange", Items.ORANGE_DYE, 0xF9932B, true),
    MAGENTA("magenta", Items.MAGENTA_DYE, 0xD660D1, true),
    LIGHT_BLUE("light_blue", Items.LIGHT_BLUE_DYE, 0x5CB7E7, false),
    YELLOW("yellow", Items.YELLOW_DYE, 0xFED93F, true),
    LIME("lime", Items.LIME_DYE, 0x86CC26, true),
    PINK("pink", Items.PINK_DYE, 0xF4B2C9, true),
    GRAY("gray", Items.GRAY_DYE, 0x474F52, false),
    LIGHT_GRAY("light_gray", Items.LIGHT_GRAY_DYE, 0x9D9D97, false),
    CYAN("cyan", Items.CYAN_DYE, 0x169B9C, true),
    PURPLE("purple", Items.PURPLE_DYE, 0x9743CD, true),
    BLUE("blue", Items.BLUE_DYE, 0x2C2F90, false),
    BROWN("brown", Items.BROWN_DYE, 0x835432, true),
    GREEN("green", Items.GREEN_DYE, 0x658619, true),
    RED("red", Items.RED_DYE, 0xB8342C, true),
    BLACK("black", Items.BLACK_DYE, 0x252529, false);

    public final Item triggerItem;
    public final int color;
    public final TextColor textColor;
    public final int overlayColor;
    private final String name;
    public final boolean useMonochromeFallback;
    @Environment(EnvType.CLIENT)
    private final Object texture;

    WunderKisteDomain(String name, Item triggerItem, int color, boolean useMonochromeFallback, String texture) {
        this.name = name;
        this.triggerItem = triggerItem;
        this.color = color;
        this.textColor = TextColor.fromRgb(color);
        this.useMonochromeFallback = useMonochromeFallback;

        if (Configs.MAIN.multiTexturedWunderkiste.get()) {
            overlayColor = 0xFFFFFF;
        } else {
            overlayColor = color;
        }
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            if (Configs.MAIN.multiTexturedWunderkiste.get()) {
                this.texture = WunderreichClient.getWunderkisteColor(texture);
            } else {
                if (useMonochromeFallback) this.texture = WunderreichClient.getWunderkisteColor("wunder_kiste_bw");
                else this.texture = WunderreichClient.getWunderkisteColor("wunder_kiste");
            }
        } else {
            this.texture = null;
        }
    }

    WunderKisteDomain(String name, Item triggerItem, int color, boolean useMonochromeFallback) {
        this(name, triggerItem, color, useMonochromeFallback, "wunder_kiste_" + name);
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

    public ItemStack createStack() {
        ItemStack stack = new ItemStack(WunderreichBlocks.WUNDER_KISTE.asItem(), 1);
        return WunderKisteItem.setDomain(stack, this);
    }
}
