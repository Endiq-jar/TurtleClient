package com.endiq.client.compat;

import com.endiq.client.capes.CapeManager;

/** Snapshot travels with render state; deferred GPU callbacks never read live player state. */
public interface CapeRenderState {
    CapeManager.Outfit turtleClient$getCape();
    void turtleClient$setCape(CapeManager.Outfit value);
}
