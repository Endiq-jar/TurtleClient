package com.endiq.client.modules.impl.performance
import com.endiq.client.modules.Module
/**
 * FHASIX parity entry. In FHASIX itself this only shows in the menu
 * ("are not available" / placeholder screens) - it is NOT implemented
 * there or here. ModulePresentation marks it Unavailable, so it can
 * never be toggled on and presents no fake working controls.
 */
class HeatSaverModule : Module("Heat Saver", "FHASIX performance: reduce device heat on mobile / low-end PCs", Category.PERFORMANCE)
