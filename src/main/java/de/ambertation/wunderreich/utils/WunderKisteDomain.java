package de.ambertation.wunderreich.utils;

import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.config.LevelData;
import de.ambertation.wunderreich.config.LevelDataFile;
import de.ambertation.wunderreich.items.WunderKisteItem;
import de.ambertation.wunderreich.registries.WunderreichBlocks;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public enum WunderKisteDomain implements StringRepresentable {
    WHITE(0, "white", Items.WHITE_DYE, 0xFFFFFFFF, false, "wunder_kiste"),
    ORANGE(1, "orange", Items.ORANGE_DYE, 0xFFF9932B, true),
    MAGENTA(2, "magenta", Items.MAGENTA_DYE, 0xFFD660D1, true),
    LIGHT_BLUE(3, "light_blue", Items.LIGHT_BLUE_DYE, 0xFF5CB7E7, false),
    YELLOW(4, "yellow", Items.YELLOW_DYE, 0xFFFED93F, true),
    LIME(5, "lime", Items.LIME_DYE, 0xFF86CC26, true),
    PINK(6, "pink", Items.PINK_DYE, 0xFFF4B2C9, true),
    GRAY(7, "gray", Items.GRAY_DYE, 0xFF474F52, false),
    LIGHT_GRAY(8, "light_gray", Items.LIGHT_GRAY_DYE, 0xFF9D9D97, false),
    CYAN(9, "cyan", Items.CYAN_DYE, 0xFF169B9C, true),
    PURPLE(10, "purple", Items.PURPLE_DYE, 0xFF9743CD, true),
    BLUE(11, "blue", Items.BLUE_DYE, 0xFF2C2F90, false),
    BROWN(12, "brown", Items.BROWN_DYE, 0xFF835432, true),
    GREEN(13, "green", Items.GREEN_DYE, 0xFF658619, true),
    RED(14, "red", Items.RED_DYE, 0xFFB8342C, true),
    BLACK(15, "black", Items.BLACK_DYE, 0xFF252529, false);

    public static final IntFunction<WunderKisteDomain> BY_ID = ByIdMap.continuous(domain -> domain.id, WunderKisteDomain.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<WunderKisteDomain> CODEC = StringRepresentable.fromEnum(WunderKisteDomain::values);
    public static final StreamCodec<ByteBuf, WunderKisteDomain> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, domain -> domain.id);

    private final int id;
    public final Item triggerItem;
    public final ID domainID;
    public final int color;
    public final TextColor textColor;
    public final int overlayColor;
    private final String name;
    public final boolean useMonochromeFallback;
    /**
     * Raw texture key; only ever resolved to an actual {@code Material} by client-only code
     * (see {@code WunderKisteDomainClient.getMaterialFor}).
     */
    public final String textureName;

    WunderKisteDomain(int id, String name, Item triggerItem, int color, boolean useMonochromeFallback, String texture) {
        this.id = id;
        this.name = name;
        this.domainID = new ID(name, false);
        this.triggerItem = triggerItem;
        this.color = color;
        this.textColor = TextColor.fromRgb(color);
        this.useMonochromeFallback = useMonochromeFallback;
        this.textureName = texture;

        if (Configs.MAIN.multiTexturedWunderkiste.get()) {
            overlayColor = 0xFFFFFFFF;
        } else {
            overlayColor = color;
        }
    }

    WunderKisteDomain(int id, String name, Item triggerItem, int color, boolean useMonochromeFallback) {
        this(id, name, triggerItem, color, useMonochromeFallback, "wunder_kiste_" + name);
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
