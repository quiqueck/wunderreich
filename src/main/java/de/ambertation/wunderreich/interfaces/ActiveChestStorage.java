package de.ambertation.wunderreich.interfaces;

import de.ambertation.wunderreich.blockentities.BoxOfEirBlockEntity;

public interface ActiveChestStorage {
    public void setActiveBoxOfEir(BoxOfEirBlockEntity boxOfEirBlockEntity);
    public boolean isActiveBoxOfEir(BoxOfEirBlockEntity boxOfEirBlockEntity);
    public BoxOfEirBlockEntity getActiveBoxOfEir();
}
