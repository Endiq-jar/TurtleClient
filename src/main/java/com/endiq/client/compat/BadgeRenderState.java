package com.endiq.client.compat;

/** Per-entity badge eligibility carried from extraction to queued rendering. */
public interface BadgeRenderState {
    boolean turtleClient$hasBadge();
    void turtleClient$setBadge(boolean value);
}
