package com.endiq.client.modules.impl.utility
import com.endiq.client.modules.Module
/**
 * FHASIX parity entry. In FHASIX itself this only shows in the menu
 * ("are not available" / placeholder screens) - it is NOT implemented
 * there or here. ModulePresentation marks it Unavailable, so it can
 * never be toggled on and presents no fake working controls.
 */
class BestPingJoinerModule : Module("Best Ping Joiner", "FHASIX: one-click join of the lowest-ping server", Category.UTILITY)
