package de.ambertation.wunderreich.utils;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum WunderKisteDomain implements StringRepresentable {
    WHITE("white", Items.WHITE_DYE, 0xFFFFFF),
    ORANGE("orange", Items.ORANGE_DYE, 0xFF7700),
    MAGENTA("magenta", Items.MAGENTA_DYE, 0xFF00FF),
    LIGHT_BLUE("light_blue", Items.LIGHT_BLUE_DYE, 0x7777FF),
    YELLOW("yellow", Items.YELLOW_DYE, 0xFFFF00),
    LIME("lime", Items.LIME_DYE, 0x22FF22),
    PINK("pink", Items.PINK_DYE, 0xFF7777),
    GRAY("gray", Items.GRAY_DYE, 0x222222),
    LIGHT_GRAY("light_gray", Items.LIGHT_GRAY_DYE, 0x777777),
    CYAN("cyan", Items.CYAN_DYE, 0x00FFFF),
    PURPLE("purple", Items.PURPLE_DYE, 0x7700FF),
    BLUE("blue", Items.BLUE_DYE, 0x0000FF),
    BROWN("brown", Items.BROWN_DYE, 0x77FF00),
    GREEN("green", Items.GREEN_DYE, 0x22FF33),
    RED("red", Items.RED_DYE, 0xFF5555),
    BLACK("black", Items.BLACK_DYE, 0x000000);

    private final String name;
    public final Item triggerItem;
    public final int color;

    private WunderKisteDomain(String name, Item triggerItem, int color) {
        this.name = name;
        this.triggerItem = triggerItem;
        this.color = color;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
