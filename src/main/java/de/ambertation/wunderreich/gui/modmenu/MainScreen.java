package de.ambertation.wunderreich.gui.modmenu;

import de.ambertation.wunderreich.config.ConfigFile;
import de.ambertation.wunderreich.config.Configs;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
class EventCheckBox extends Checkbox {
    private final Consumer<Boolean> onChange;
    private boolean enabled;

    public EventCheckBox(
            int left,
            int top,
            int width,
            int height,
            Component component,
            boolean checked,
            Consumer<Boolean> onChange
    ) {
        super(left, top, width, height, component, checked);
        this.onChange = onChange;
        this.enabled = true;
        if (onChange != null)
            onChange.accept(checked);
    }

    @Override
    public void onPress() {
        super.onPress();
        if (onChange != null)
            onChange.accept(this.selected());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.active = enabled;
        this.setAlpha(enabled ? 1.0f : 0.5f);

        this.enabled = enabled;
    }
}

public class MainScreen extends Screen {
    public final Screen parent;
    Map<EventCheckBox, Supplier<Boolean>> dependentWidgets = new HashMap<>();

    public MainScreen(@Nullable Screen parent) {
        super(Component.translatable("title.wunderreich.modmenu.main"));
        this.parent = parent;
    }

    protected <T> Component getComponent(
            ConfigFile config,
            ConfigFile.Value<T> option,
            String type
    ) {
        return Component.translatable(type + ".config." + config.category + "." + option.token.path() + "." + option.token.key());
    }

    protected void updateEnabledState() {
        dependentWidgets.forEach((cb, supl) -> cb.setEnabled(supl.get()));
    }


    @SuppressWarnings("unchecked")
    protected <T> void addRow(LayoutState state, ConfigFile config, ConfigFile.Value<T> option, int maxRight) {
        if (option instanceof ConfigFile.BooleanValue bool) {
            addCheckbox(state, config, bool, maxRight);
        }

        state.top += 2;
    }


    protected void addCheckbox(LayoutState state, ConfigFile config, ConfigFile.BooleanValue option, int maxRight) {
        EventCheckBox cb = new EventCheckBox(
                state.left + (option.getIsValidSupplier() != null ? 12 : 0),
                state.top,
                maxRight - state.left,
                20,
                getComponent(config, option, "title"),
                option.getRaw(),
                (st) -> {
                    option.set(st);
                    updateEnabledState();
                }
        );
        if (option.getIsValidSupplier() != null) {
            dependentWidgets.put(cb, option.getIsValidSupplier());
            cb.setEnabled(option.getIsValidSupplier().get());
        }
        state.top += 20;
        this.addRenderableWidget(cb);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    protected void init() {
        super.init();
        final int BUTTON_HEIGHT = 20;
        LayoutState state = new LayoutState(55);
        final int buttonWidth = font.width(CommonComponents.GUI_DONE.getVisualOrderText()) + 24;
        final int buttonLeft = this.width - buttonWidth - state.left;

        Configs.MAIN
                .getAllValues()
                .stream()
                .filter(o -> !o.isDeprecated() && !o.isHiddenInUI())
                .forEach(o -> addRow(state, Configs.MAIN, o, buttonLeft));

        state.top += 15;

        Button b = Button
                .builder(CommonComponents.GUI_DONE, (button) -> {
                    Configs.MAIN.save();
                    onClose();
                }).bounds(
                        buttonLeft,
                        this.height - BUTTON_HEIGHT - 20,
                        buttonWidth,
                        BUTTON_HEIGHT
                )
                .build();
        this.addRenderableWidget(b);
    }

    public void render(@NotNull PoseStack poseStack, int i, int j, float f) {
        this.renderDirtBackground(i);
        drawCenteredString(poseStack, this.font, this.title, width / 2, 20, 0xFFFFFFFF);
        super.render(poseStack, i, j, f);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    private static class LayoutState {
        public int top, left;

        LayoutState(int top) {
            this.top = top;
            this.left = 20;
        }
    }
}

