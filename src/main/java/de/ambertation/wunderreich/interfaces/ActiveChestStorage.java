package de.ambertation.wunderreich.interfaces;

import de.ambertation.wunderreich.blockentities.BoxOfEirBlockEntity;

public interface ActiveChestStorage {
    public boolean isActiveBoxOfEir(BoxOfEirBlockEntity boxOfEirBlockEntity);

    public BoxOfEirBlockEntity getActiveBoxOfEir();

    public void setActiveBoxOfEir(BoxOfEirBlockEntity boxOfEirBlockEntity);
}
