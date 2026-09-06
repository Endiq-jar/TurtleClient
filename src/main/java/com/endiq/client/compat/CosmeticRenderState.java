package com.endiq.client.compat;

import com.endiq.client.cosmetics.CosmeticManager;

/** Snapshot travels with render state; deferred GPU callbacks never read live player state. */
public interface CosmeticRenderState {
    CosmeticManager.Loadout turtleClient$getCosmetics();
    void turtleClient$setCosmetics(CosmeticManager.Loadout value);
}
