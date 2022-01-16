package de.ambertation.wunderreich.interfaces;

import de.ambertation.wunderreich.blockentities.WunderKisteBlockEntity;

public interface ActiveChestStorage {
    boolean isActiveWunderKiste(WunderKisteBlockEntity wunderKisteBlockEntity);

    WunderKisteBlockEntity getActiveWunderKiste();

    void setActiveWunderKiste(WunderKisteBlockEntity wunderKisteBlockEntity);
}
