package de.ambertation.wunderreich.integration.rei;

import de.ambertation.wunderreich.recipes.AgingRecipe;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import java.util.List;
import java.util.Optional;

/**
 * REI display for one {@code wunderreich:aging} recipe.
 * <p>
 * Input slot {@link #INPUT_SLOT} is consumed, {@link #CATALYST_SLOT} is not - see
 * {@link AgingRecipe}. Both are reported as inputs so an "usages of vine" lookup finds the aging
 * recipes; the category draws the distinction (see the client side {@code AgingCategory}).
 * <p>
 * Unlike {@link ImprinterDisplay} this display carries two extra values, the processing
 * {@link #getTime() time} in ticks and the {@link #getExperience() experience} per result, which
 * the serializer therefore has to round-trip - in <b>both</b> of its codecs.
 */
public class AgingDisplay extends BasicDisplay {
    /**
     * Index of the consumed ingredient in {@link #getInputEntries()}.
     */
    public static final int INPUT_SLOT = 0;
    /**
     * Index of the never-consumed ingredient in {@link #getInputEntries()}.
     */
    public static final int CATALYST_SLOT = 1;

    public static final DisplaySerializer<AgingDisplay> SERIALIZER = DisplaySerializer.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(AgingDisplay::getInputEntries),
                    EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(AgingDisplay::getOutputEntries),
                    Codec.INT.fieldOf("time").forGetter(AgingDisplay::getTime),
                    Codec.FLOAT.fieldOf("experience").forGetter(AgingDisplay::getExperience)
            ).apply(instance, AgingDisplay::new)),
            StreamCodec.composite(
                    EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), AgingDisplay::getInputEntries,
                    EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), AgingDisplay::getOutputEntries,
                    ByteBufCodecs.VAR_INT, AgingDisplay::getTime,
                    ByteBufCodecs.FLOAT, AgingDisplay::getExperience,
                    AgingDisplay::new
            )
    );

    private final int time;
    private final float experience;

    public AgingDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, int time, float experience) {
        super(inputs, outputs);
        this.time = time;
        this.experience = experience;
    }

    public AgingDisplay(
            List<EntryIngredient> inputs,
            List<EntryIngredient> outputs,
            int time,
            float experience,
            Optional<Identifier> location
    ) {
        super(inputs, outputs, location);
        this.time = time;
        this.experience = experience;
    }

    public static AgingDisplay of(RecipeHolder<AgingRecipe> holder) {
        final AgingRecipe recipe = holder.value();

        return new AgingDisplay(
                List.of(
                        // INPUT_SLOT: aged away
                        EntryIngredients.ofIngredient(recipe.input()),
                        // CATALYST_SLOT: required, handed back untouched
                        EntryIngredients.ofIngredient(recipe.catalyst())
                ),
                List.of(EntryIngredients.of(recipe.resultStack())),
                recipe.time(),
                recipe.experience(),
                Optional.of(holder.id().identifier())
        );
    }

    /**
     * How long the Chronarium needs for this recipe, in ticks.
     */
    public int getTime() {
        return time;
    }

    /**
     * Experience per completed result, handed out when a player takes the output.
     */
    public float getExperience() {
        return experience;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ServerPlugin.AGING;
    }

    @Override
    public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
