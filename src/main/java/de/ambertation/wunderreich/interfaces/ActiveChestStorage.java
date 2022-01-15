package de.ambertation.wunderreich.interfaces;

import de.ambertation.wunderreich.blockentities.WunderKisteBlockEntity;

public interface ActiveChestStorage {
    public boolean isActiveWunderKiste(WunderKisteBlockEntity wunderKisteBlockEntity);

    public WunderKisteBlockEntity getActiveWunderKiste();

    public void setActiveWunderKiste(WunderKisteBlockEntity wunderKisteBlockEntity);
}
