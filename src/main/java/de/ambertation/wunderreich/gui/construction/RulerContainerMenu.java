package de.ambertation.wunderreich.gui.construction;

import de.ambertation.lib.ui.layout.values.Rectangle;
import de.ambertation.wunderreich.items.construction.ConstructionData;
import de.ambertation.wunderreich.registries.WunderreichMenuTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RulerContainerMenu extends AbstractContainerMenu {
    public final ItemStack rulerStack;

    final int INVENTORY_OFFSET = 9;
    final int ROW_SIZE = 9;
    final int HOTBAR_OFFSET = 0;
    private final Inventory inventory;
    final RulerContainer container;
    final RulerDataContainer dataContainer;
    public final ConstructionData data;

    final SDFSlot sdfSlot;

    static final Rectangle MATERIAL_PANEL = new Rectangle(5, 52, 177, 70);
    static final Rectangle INVENTORY_PANEL = new Rectangle(6, 132, 174, 96);
    static final Rectangle SDF_PANEL = new Rectangle(301, 67, 51 + 20, 47 + 40);
    static final Rectangle WIDGET_PANEL = new Rectangle(0, 0, 400, 40);

    public RulerContainerMenu(int synchronizationID, Inventory inventory, FriendlyByteBuf packetByteBuf) {
        this(synchronizationID, inventory, packetByteBuf.readItem());
    }

    void printInfo() {
//        System.out.println(FabricLoader.getInstance().getEnvironmentType());
//        if (rulerStack != null) {
//            System.out.println("RulerStack :" + rulerStack + "(" + Integer.toHexString(rulerStack.hashCode()) + ")");
//            System.out.println("            " + rulerStack.getTag());
//        }
//        System.out.println("Inventory 5:" + inventory.getItem(5) + "(" + Integer.toHexString(inventory.getItem(5)
//                                                                                                      .hashCode()) + ")");
//        System.out.println("            " + inventory.getItem(5).getTag());
    }

    public RulerContainerMenu(int synchronizationID, Inventory inventory, ItemStack rulerStack) {
        super(WunderreichMenuTypes.RULER, synchronizationID);

        this.inventory = inventory;
        dataContainer = new RulerDataContainer(this);
        data = ConstructionData.getConstructionData(rulerStack);
        RulerContainer rc = data == null ? null : data.MATERIAL_DATA.get();
        if (rc == null) {
            rc = new RulerContainer();
            if (data != null)
                data.MATERIAL_DATA.set(rc);
        }
        container = rc;

        this.rulerStack = rulerStack;
        container.callOnChange((c) -> {
//            if (data != null) {
//                data.MATERIAL_DATA.set(c);
//                System.out.println("---DIRTY");
//                printInfo();
//            }
        });
        System.out.println("---INIT");


        addInventorySlots(INVENTORY_PANEL);
        addMaterialSlots(MATERIAL_PANEL);
        sdfSlot = addSDFSlots(SDF_PANEL);
        printInfo();

        System.out.println(this.data.SDF_DATA.get() + ", act=" + sdfSlot.getContainerSlot() + " - " + this.data.ACTIVE_SLOT.get());
    }

    SDFSlot addSDFSlots(Rectangle screenBounds) {
        System.out.println("---SLOTS");
        printInfo();
        SDFSlot inp0 = new SDFSlot(
                dataContainer,
                this,
                screenBounds.left + 1,
                screenBounds.top + 7
        );
        SDFSlot inp1 = new SDFSlot(
                dataContainer,
                this,
                screenBounds.left + 0,
                screenBounds.top + 32

        );

        SDFSlot slot = new SDFSlot(
                dataContainer,
                this,
                screenBounds.left + 12,
                screenBounds.top + 19,
                inp0,
                inp1,
                data.ACTIVE_SLOT.get()
        );
        this.addSlot(slot);
        this.addSlot(inp0);
        this.addSlot(inp1);
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
            int page = container.pageForIndex(idx);
            int pageIdx = container.indexOnPage(idx);
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
