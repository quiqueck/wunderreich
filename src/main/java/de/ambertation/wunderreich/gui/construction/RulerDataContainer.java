package de.ambertation.wunderreich.gui.construction;

import de.ambertation.lib.math.sdf.SDF;
import de.ambertation.lib.math.sdf.shapes.Empty;
import de.ambertation.wunderreich.items.construction.BluePrintData;
import de.ambertation.wunderreich.items.construction.ConstructionData;
import de.ambertation.wunderreich.registries.WunderreichItems;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RulerDataContainer implements Container {
    @Override
    public void clearContent() {

    }

    record SDFMap(SDF sdf, ItemStack wrapper, int idx) {

    }

    static final int MAX_SDF_NODES = 64;

    private final RulerContainerMenu menu;
    private final List<SDFMap> sdfs;

    RulerDataContainer(RulerContainerMenu menu) {
        this.menu = menu;
        sdfs = new ArrayList<>(MAX_SDF_NODES);
    }

    public int nextFree() {
        for (int i = 0; i < sdfs.size(); i++) {
            if (sdfs.get(i) == null) return i;
        }

        sdfs.add(null);
        return sdfs.size() - 1;
    }

    private int addSDF(SDF sdf) {
        final int idx = nextFree();

        ItemStack bp = new ItemStack(WunderreichItems.BLUE_PRINT, 1);
        BluePrintData bData = BluePrintData.getBluePrintData(bp);
        if (bData != null) bData.SDF_DATA.set(sdf);

        SDFMap m = new SDFMap(sdf, bp, idx);
        sdfs.set(idx, m);

        return idx;
    }

    public SDFMap getSDF(int i) {
        for (SDFMap m : sdfs) if (m != null && m.idx == i) return m;
        return null;
    }

    public int addRecursive(SDF s) {
        int idx = _addRecursive(s);
        setChanged();
        return idx;
    }

    private int _addRecursive(SDF s) {
        if (s == null) return -1;
        int idx = addSDF(s);

        for (int i = 0; i < s.getInputSlotCount(); i++) {
            SDF sdf = s.getSlot(i);
            addRecursive(sdf);
        }
        return idx;
    }

    public void removeRecursive(SDFMap s) {
        _removeRecursive(s);
        setChanged();
    }

    private void _removeRecursive(SDFMap s) {
        if (s == null) return;

        sdfs.set(s.idx, null);

        for (int i = 0; i < s.sdf.getInputSlotCount(); i++) {
            SDF sdf = s.sdf.getSlot(i);
            SDFMap m = mapForSDF(sdf);
            removeRecursive(m);
        }
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        SDFMap currentSDF = getSDF(i);
        if (currentSDF != null) {
            removeRecursive(currentSDF);
        }

        BluePrintData bData = BluePrintData.getBluePrintData(itemStack);
        if (bData != null) {
            SDF newSDF = bData.SDF_DATA.get();
            if (currentSDF != null) {
                SDF parent = currentSDF.sdf.getParent();

                if (parent != null) {
                    int oldSlot = parent.inputSlotIndex(currentSDF.sdf);
                    parent.setSlot(oldSlot, newSDF);
                }
            }

            _addRecursive(newSDF);
        }

        setChanged();
    }

    @Override
    public void setChanged() {
        if (menu.rulerStack != null) {
            ConstructionData cd = menu.data;
            if (cd != null) {
                if (sdfs.size() > 0 && sdfs.get(0) != null && sdfs.get(0).sdf != null)
                    cd.SDF_DATA.set(sdfs.get(0).sdf.getRoot());
                else
                    cd.SDF_DATA.set(new Empty());

                System.out.println("new Data:" + cd.SDF_DATA.get());
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public int getContainerSize() {
        return MAX_SDF_NODES;
    }

    @Override
    public boolean isEmpty() {
        if (sdfs.isEmpty()) return true;
        for (SDFMap m : sdfs) {
            if (m != null) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int i) {
        SDFMap m = getSDF(i);
        if (m == null || m.sdf == null || m.sdf instanceof Empty) return ItemStack.EMPTY;
        return m.wrapper;
    }

    @Override
    public ItemStack removeItem(int idx, int count) {
        SDFMap currentSDF = getSDF(idx);
        if (currentSDF != null) {
            removeRecursive(currentSDF);
        }
        return currentSDF.wrapper;
    }

    @Override
    public ItemStack removeItemNoUpdate(int idx) {
        SDFMap currentSDF = getSDF(idx);
        if (currentSDF != null) {
            _removeRecursive(currentSDF);
        }
        return currentSDF.wrapper;
    }

    public SDFMap mapForSDF(SDF sdf) {
        for (SDFMap m : sdfs)
            if (m != null && m.sdf == sdf) return m;

        return null;
    }

    public int slotForSDF(SDF sdf) {
        for (SDFMap m : sdfs)
            if (m != null && m.sdf == sdf) return m.idx;

        return -1;
    }

    public int slotForGrahIndex(int graphIndex) {
        for (SDFMap m : sdfs)
            if (m != null && m.sdf != null && m.sdf.getGraphIndex() == graphIndex) return m.idx;

        return -1;
    }
}
