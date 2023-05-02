package de.ambertation.wunderreich.gui.construction;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class RulerContainer extends SimpleContainer {
    public static final int MAX_CATEGORIES = 8;
    public static final int ITEMS_PER_CATEGORY = 2 * 9;
    static final int INVENTORY_SIZE = 3 * 9;
    static final int HOTBAR_SIZE = 9;

    static final int INV_SLOT_START = 0;
    static final int INV_SLOT_END = INV_SLOT_START + INVENTORY_SIZE;
    static final int HOTBAR_SLOT_START = INV_SLOT_END;
    static final int HOTBAR_SLOT_END = HOTBAR_SLOT_START + HOTBAR_SIZE;

    public static final int CATEGORIES_SLOT_START = 0;
    static final int CATEGORIES_SLOT_END = CATEGORIES_SLOT_START + (MAX_CATEGORIES * ITEMS_PER_CATEGORY);

    public static final int SIZE = CATEGORIES_SLOT_END;

    Consumer<RulerContainer> onChange;

    public RulerContainer() {
        super(SIZE);
    }

    private int activePage = 0;

    public int activePage() {
        return activePage;
    }

    public void setActivePage(int page) {
        activePage = page;
    }

    public void callOnChange(Consumer<RulerContainer> onChange) {
        this.onChange = onChange;
    }

    @Override
    public void setChanged() {
        if (onChange != null) {
            onChange.accept(this);
        }
        super.setChanged();
    }

    public int pageForIndex(int categorySlotsIndex) {
        return categorySlotsIndex / RulerContainer.ITEMS_PER_CATEGORY;
    }

    public int indexOnPage(int categorySlotsIndex) {
        return categorySlotsIndex % RulerContainer.ITEMS_PER_CATEGORY;
    }

    public int getCategorySlotIndex(int page, int pageIdx) {
        return page * RulerContainer.ITEMS_PER_CATEGORY + pageIdx;
    }

    public ItemStack getPageItem(int page, int pageIdx) {
        return getItem(getCategorySlotIndex(page, pageIdx) + CATEGORIES_SLOT_START);
    }
}
