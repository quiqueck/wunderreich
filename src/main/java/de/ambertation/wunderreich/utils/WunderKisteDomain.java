package de.ambertation.wunderreich.utils;

import net.minecraft.network.chat.TextColor;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum WunderKisteDomain implements StringRepresentable {
    WHITE("white", Items.WHITE_DYE, 0xFEFEFE),
    ORANGE("orange", Items.ORANGE_DYE, 0xF9932B),
    MAGENTA("magenta", Items.MAGENTA_DYE, 0xD660D1),
    LIGHT_BLUE("light_blue", Items.LIGHT_BLUE_DYE, 0x4EC5E7),
    YELLOW("yellow", Items.YELLOW_DYE, 0xFED93F),
    LIME("lime", Items.LIME_DYE, 0x86CC26),
    PINK("pink", Items.PINK_DYE, 0xF4B2C9),
    GRAY("gray", Items.GRAY_DYE, 0x474F52),
    LIGHT_GRAY("light_gray", Items.LIGHT_GRAY_DYE, 0x9D9D97),
    CYAN("cyan", Items.CYAN_DYE, 0x169B9C),
    PURPLE("purple", Items.PURPLE_DYE, 0x9743CD),
    BLUE("blue", Items.BLUE_DYE, 0x3E4DB2),
    BROWN("brown", Items.BROWN_DYE, 0x835432),
    GREEN("green", Items.GREEN_DYE, 0x658619),
    RED("red", Items.RED_DYE, 0xB8342C),
    BLACK("black", Items.BLACK_DYE, 0x252529);

    private final String name;
    public final Item triggerItem;
    public final int color;
    public final TextColor textColor;

    private WunderKisteDomain(String name, Item triggerItem, int color) {
        this.name = name;
        this.triggerItem = triggerItem;
        this.color = color;
        this.textColor = TextColor.fromRgb(color);
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
