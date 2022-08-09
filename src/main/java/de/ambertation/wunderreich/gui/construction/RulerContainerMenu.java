package de.ambertation.wunderreich.gui.construction;

import de.ambertation.lib.ui.layout.values.Rectangle;
import de.ambertation.wunderreich.items.construction.BluePrintData;
import de.ambertation.wunderreich.items.construction.ConstructionData;
import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.registries.WunderreichMenuTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.loader.api.FabricLoader;

public class RulerContainerMenu extends AbstractContainerMenu {
    final ItemStack rulerStack;

    final int INVENTORY_OFFSET = 9;
    final int ROW_SIZE = 9;
    final int HOTBAR_OFFSET = 0;
    private final Inventory inventory;
    final RulerContainer container;
    final RulerDataContainer dataContainer;
    final ConstructionData data;

    final SDFSlot sdfSlot;

    static final Rectangle MATERIAL_PANEL = new Rectangle(5, 52, 177, 70);
    static final Rectangle INVENTORY_PANEL = new Rectangle(6, 132, 174, 96);
    static final Rectangle SDF_PANEL = new Rectangle(301, 67, 51, 47);

    public RulerContainerMenu(int synchronizationID, Inventory inventory, FriendlyByteBuf packetByteBuf) {
        this(synchronizationID, inventory, packetByteBuf.readItem());
    }

    void printInfo() {
        System.out.println(FabricLoader.getInstance().getEnvironmentType());
        if (rulerStack != null) {
            System.out.println("RulerStack :" + rulerStack + "(" + Integer.toHexString(rulerStack.hashCode()) + ")");
            System.out.println("            " + rulerStack.getTag());
        }
        System.out.println("Inventory 5:" + inventory.getItem(5) + "(" + Integer.toHexString(inventory.getItem(5)
                                                                                                      .hashCode()) + ")");
        System.out.println("            " + inventory.getItem(5).getTag());
    }

    public RulerContainerMenu(int synchronizationID, Inventory inventory, ItemStack rulerStack) {
        super(WunderreichMenuTypes.RULER, synchronizationID);

        this.inventory = inventory;
        dataContainer = new RulerDataContainer(this);
        data = ConstructionData.getConstructionData(rulerStack);
        RulerContainer rc = data.MATERIAL_DATA.get();
        if (rc == null) {
            rc = new RulerContainer();
            data.MATERIAL_DATA.set(rc);
        }
        container = rc;
        this.dataContainer.setItem(RulerDataContainer.RULER_SLOT, rulerStack);
        if (data != null && data.SDF_DATA.get() != null) {
            ItemStack bp = new ItemStack(WunderreichItems.BLUE_PRINT, 1);
            BluePrintData bData = BluePrintData.getBluePrintData(bp);
            bData.SDF_DATA.set(data.SDF_DATA.get());
            this.dataContainer.setItem(
                    RulerDataContainer.SDF_SLOT_START,
                    bp
            );
        }
        this.rulerStack = rulerStack;
        container.callOnChange((c) -> {
            data.MATERIAL_DATA.set(c);
            System.out.println("---DIRTY");
            printInfo();
        });
        System.out.println("---INIT");

        addInventorySlots(INVENTORY_PANEL);
        addMaterialSlots(MATERIAL_PANEL);
        sdfSlot = addSDFSlots(SDF_PANEL);
        printInfo();
    }

    SDFSlot addSDFSlots(Rectangle screenBounds) {
        System.out.println("---SLOTS");
        printInfo();
//        this.addSlot(new MaterialSlot(container, 0, screenBounds.left + 1, screenBounds.top + 7, 0));
//        this.addSlot(new MaterialSlot(container, 0, screenBounds.left + 0, screenBounds.top + 32, 0));
//
//        this.addSlot(new MaterialSlot(container, 0, screenBounds.left + 12, screenBounds.top + 19, 0));
//        this.addSlot(new MaterialSlot(container, 0, screenBounds.left + 36, screenBounds.top + 20, 0));
        this.addSlot(new MaterialSlot(container, 0, screenBounds.left + 28, screenBounds.top + 1, 0));
        SDFSlot slot = new SDFSlot(
                dataContainer,
                this,
                RulerDataContainer.SDF_SLOT_START,
                screenBounds.left + 12,
                screenBounds.top + 19
        );
        this.addSlot(slot);
        return slot;
    }

    void addMaterialSlots(Rectangle screenBounds) {
        final int INV_SLOT_X = screenBounds.left + 6;
        final int INV_SLOT_Y = screenBounds.top + 16;
        final int INV_SLOT_WIDTH = 18;
        final int INV_SLOT_HEIGHT = INV_SLOT_WIDTH;
        final int INV_SLOT_PAD = 1;

        for (int i = RulerContainer.CATEGORIES_SLOT_START; i < RulerContainer.CATEGORIES_SLOT_END; i++) {
            int idx = i - RulerContainer.CATEGORIES_SLOT_START;
            int page = idx / RulerContainer.ITEMS_PER_CATEGORY;
            int pageIdx = idx % RulerContainer.ITEMS_PER_CATEGORY;
            int x = INV_SLOT_X + (pageIdx % ROW_SIZE) * INV_SLOT_WIDTH + INV_SLOT_PAD;
            int y = INV_SLOT_Y + (pageIdx / ROW_SIZE) * INV_SLOT_HEIGHT + INV_SLOT_PAD;
            this.addSlot(new MaterialSlot(container, idx, x, y, page));
        }
    }

    void addInventorySlots(Rectangle screenBounds) {
        final int INV_SLOT_X = screenBounds.left + 6;
        final int INV_SLOT_Y = screenBounds.top + 6 + 9;
        final int INV_SLOT_WIDTH = 18;
        final int INV_SLOT_HEIGHT = INV_SLOT_WIDTH;
        final int INV_SLOT_PAD = 1;

        final int HOTBAR_SLOT_X = INV_SLOT_X;
        final int HOTBAR_SLOT_Y = screenBounds.bottom() - 5 - INV_SLOT_HEIGHT;

        for (int i = RulerContainer.INV_SLOT_START; i < RulerContainer.INV_SLOT_END; i++) {
            int idx = i - RulerContainer.INV_SLOT_START;
            int x = INV_SLOT_X + (idx % ROW_SIZE) * INV_SLOT_WIDTH + INV_SLOT_PAD;
            int y = INV_SLOT_Y + (idx / ROW_SIZE) * INV_SLOT_HEIGHT + INV_SLOT_PAD;
            this.addSlot(new Slot(inventory, INVENTORY_OFFSET + idx, x, y));
        }

        for (int i = RulerContainer.HOTBAR_SLOT_START; i < RulerContainer.HOTBAR_SLOT_END; i++) {
            int idx = i - RulerContainer.HOTBAR_SLOT_START;
            int x = HOTBAR_SLOT_X + (idx % ROW_SIZE) * INV_SLOT_WIDTH + INV_SLOT_PAD;
            int y = HOTBAR_SLOT_Y + (idx / ROW_SIZE) * INV_SLOT_HEIGHT + INV_SLOT_PAD;
            this.addSlot(new Slot(inventory, HOTBAR_OFFSET + idx, x, y));
        }
    }


    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
