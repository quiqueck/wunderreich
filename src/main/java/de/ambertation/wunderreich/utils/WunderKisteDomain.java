package de.ambertation.wunderreich.utils;

import de.ambertation.wunderreich.client.WunderKisteMaterials;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.config.LevelData;
import de.ambertation.wunderreich.config.LevelDataFile;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

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
    public final ID domainID;
    public final int color;
    public final TextColor textColor;
    public final int overlayColor;
    private final String name;
    public final boolean useMonochromeFallback;
    private final String textureName;
    @Environment(EnvType.CLIENT)
    private Object texture;

    WunderKisteDomain(String name, Item triggerItem, int color, boolean useMonochromeFallback, String texture) {
        this.name = name;
        this.domainID = new ID(name, false);
        this.triggerItem = triggerItem;
        this.color = color;
        this.textColor = TextColor.fromRgb(color);
        this.useMonochromeFallback = useMonochromeFallback;

        if (Configs.MAIN.multiTexturedWunderkiste.get()) {
            overlayColor = 0xFFFFFF;
        } else {
            overlayColor = color;
        }
        //The texture Material is resolved lazily in getMaterial() (client, render time). Building it here would
        //load net.minecraft.client.renderer.Sheets during block registration, before other mods registered their
        //banner patterns / pottery sherds (quiqueck/wunderreich#18).
        this.textureName = texture;
    }

    WunderKisteDomain(String name, Item triggerItem, int color, boolean useMonochromeFallback) {
        this(name, triggerItem, color, useMonochromeFallback, "wunder_kiste_" + name);
    }

    @Environment(EnvType.CLIENT)
    public Material getMaterial() {
        if (texture == null) {
            if (Configs.MAIN.multiTexturedWunderkiste.get()) {
                texture = WunderKisteMaterials.getWunderkisteColor(textureName);
            } else if (useMonochromeFallback) {
                texture = WunderKisteMaterials.getWunderkisteColor("wunder_kiste_bw");
            } else {
                texture = WunderKisteMaterials.getWunderkisteColor("wunder_kiste");
            }
        }
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

    public static class ID {
        public final String id;
        public final LevelDataFile extraFile;

        ID(String id, boolean extraFile) {
            this.id = id;
            if (extraFile) this.extraFile = LevelData.getInstance().fileForName(id);
            else this.extraFile = null;
        }

        private static final Map<String, ID> ID_MAP = new HashMap<>();

        public static ID forString(String s) {
            return ID_MAP.computeIfAbsent(s, key -> new ID("_n_" + key, true));
        }

        public static ID forDomain(WunderKisteDomain d) {
            return d.domainID;
        }


        public static void forAll(Consumer<ID> idConsumer) {
            for (WunderKisteDomain domain : WunderKisteDomain.values()) {
                idConsumer.accept(domain.domainID);
            }
            for (ID id : ID_MAP.values()) {
                idConsumer.accept(id);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof String s) {
                return ("_n_" + s).equals(this.id);
            } else if (o instanceof WunderKisteDomain d) {
                return this.equals(d.domainID);
            }
            if (o == null || getClass() != o.getClass()) return false;
            ID id1 = (ID) o;
            return id.equals(id1.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return id;
        }

        public boolean isEmpty() {
            return id == null || id.isEmpty();
        }

        public boolean isBuiltIn() {
            return extraFile == null;
        }

        public static void loadNewLevel() {
            ID_MAP.entrySet().removeIf(i -> i != null && i.getValue() != null && !i.getValue().isBuiltIn());
        }
    }
}
