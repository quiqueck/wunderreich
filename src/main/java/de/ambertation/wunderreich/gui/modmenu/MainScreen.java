package de.ambertation.wunderreich.gui.modmenu;

import de.ambertation.wunderreich.config.ConfigFile;
import de.ambertation.wunderreich.config.WunderreichConfigs;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;

import ru.bclib.gui.gridlayout.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public class MainScreen extends GridScreen {

    Map<GridWidgetWithEnabledState, Supplier<Boolean>> dependentWidgets = new HashMap<>();

    public MainScreen(@Nullable Screen parent) {
        super(parent, new TranslatableComponent("title.wunderreich.modmenu.main"));
    }

    protected <T> TranslatableComponent getComponent(ConfigFile config,
                                                     ConfigFile.Value<T> option,
                                                     String type) {
        return new TranslatableComponent(type + ".config." + config.category + "." + option.token.path() + "." + option.token.key());
    }

    protected void updateEnabledState() {
        dependentWidgets.forEach((cb, supl) -> cb.setEnabled(supl.get()));
    }

    @SuppressWarnings("unchecked")
    protected <T> void addRow(GridColumn grid, ConfigFile config, ConfigFile.Value<T> option) {
        if (option instanceof ConfigFile.BooleanValue bool) {
            addCheckbox(grid, config, bool);
        }

        grid.addSpacerRow(2);
    }


    protected void addCheckbox(GridColumn grid, ConfigFile config, ConfigFile.BooleanValue option) {
        //TODO: Margins
//        if (option.topPadding > 0) {
//            grid.addSpacerRow(option.topPadding);
//        }
        GridRow row = grid.addRow();
        if (option.getIsValidSupplier() != null) {
            row.addSpacer(12);
        }
//        if (option.leftPadding > 0) {
//            row.addSpacer(option.leftPadding);
//        }
        GridCheckboxCell cb = row.addCheckbox(getComponent(config, option, "title"),
                option.getRaw(),
                font,
                (state) -> {
                    option.set(state);
                    updateEnabledState();
                });


        if (option.getIsValidSupplier() != null) {
            dependentWidgets.put(cb, option.getIsValidSupplier());
            cb.setEnabled(option.getIsValidSupplier().get());
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void initLayout() {
        final int BUTTON_HEIGHT = 20;

        WunderreichConfigs.MAIN
                .getAllValues()
                .stream()
                .filter(o -> !o.isHiddenInUI())
                .forEach(o -> addRow(grid, WunderreichConfigs.MAIN, o));

        grid.addSpacerRow(15);
        GridRow row = grid.addRow();
        row.addFiller();
        row.addButton(CommonComponents.GUI_DONE, BUTTON_HEIGHT, font, (button) -> {
            WunderreichConfigs.MAIN.save();
            onClose();
        });
    }
}

