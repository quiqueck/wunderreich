package de.ambertation.wunderreich.integration;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/**
 * The bits of wording every recipe viewer needs for the {@code wunderreich:aging} category.
 * <p>
 * The two facts the category has to get across are the ones that are <em>not</em> obvious from a
 * plain "two slots and an arrow" layout:
 * <ul>
 *     <li>the catalyst is required but never consumed,</li>
 *     <li>the recipe takes real time, and</li>
 *     <li>the result is worth experience, like a smelted item.</li>
 * </ul>
 * Keeping the strings here means JEI, REI and EMI all say exactly the same thing.
 */
public final class AgingRecipeUI {
    private AgingRecipeUI() {
    }

    /**
     * Category title. Named after the machine, like the imprinter category is.
     */
    public static Component title() {
        return Component.translatable("block.wunderreich.chronarium");
    }

    /**
     * {@code 400} ticks &rarr; {@code "20s"}, {@code 2400} ticks &rarr; {@code "2m 0s"}.
     */
    public static String formatTime(int ticks) {
        final int seconds = Math.max(1, Math.round(ticks / 20.0f));
        if (seconds < 60) return seconds + "s";
        return (seconds / 60) + "m " + (seconds % 60) + "s";
    }

    /**
     * The line drawn underneath the recipe, e.g. {@code "Ages in 20s"}.
     */
    public static Component timeLabel(int ticks) {
        return Component.translatable("wunderreich.aging.time", formatTime(ticks));
    }

    /**
     * Whether a recipe is worth showing an experience line for at all. Datapacks may leave the
     * {@code experience} field out, in which case it is {@code 0} and there is nothing to say.
     */
    public static boolean hasExperience(float experience) {
        return experience > 0f;
    }

    /**
     * {@code 0.16f} &rarr; {@code "0.16"}, {@code 0.5f} &rarr; {@code "0.5"}, {@code 1f} &rarr;
     * {@code "1"}. {@link Locale#ROOT} so the number always uses a dot, matching the recipe JSON.
     */
    public static String formatExperience(float experience) {
        return new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(experience);
    }

    /**
     * The experience one completed result is worth, e.g. {@code "0.16 XP"}. Only meaningful when
     * {@link #hasExperience(float)}.
     */
    public static Component experienceLabel(float experience) {
        return Component.translatable("wunderreich.aging.experience", formatExperience(experience))
                        .withStyle(ChatFormatting.GRAY);
    }

    /**
     * The line drawn underneath {@link #timeLabel(int)}. This is the important one - it is the only
     * place the "catalyst survives" rule is stated without having to hover anything.
     */
    public static Component catalystLabel() {
        return Component.translatable("wunderreich.aging.catalyst.not_consumed")
                        .withStyle(ChatFormatting.GRAY);
    }

    /**
     * Tooltip for the catalyst slot.
     */
    public static List<Component> catalystTooltip() {
        return List.of(
                Component.translatable("wunderreich.aging.catalyst").withStyle(ChatFormatting.GOLD),
                Component.translatable("wunderreich.aging.catalyst.desc").withStyle(ChatFormatting.GRAY)
        );
    }

    /**
     * Tooltip for the input slot, the counterpart of {@link #catalystTooltip()}.
     */
    public static List<Component> inputTooltip() {
        return List.of(
                Component.translatable("wunderreich.aging.input").withStyle(ChatFormatting.GOLD),
                Component.translatable("wunderreich.aging.input.desc").withStyle(ChatFormatting.GRAY)
        );
    }

    /**
     * Tooltip for the arrow/time label, spelling out the raw tick count as well.
     */
    public static List<Component> timeTooltip(int ticks) {
        return List.of(
                Component.translatable("wunderreich.aging.time", formatTime(ticks)),
                Component.translatable("wunderreich.aging.time.ticks", ticks).withStyle(ChatFormatting.DARK_GRAY)
        );
    }
}
