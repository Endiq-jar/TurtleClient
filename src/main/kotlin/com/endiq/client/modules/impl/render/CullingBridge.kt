package com.endiq.client.modules.impl.render

// Plain field mirror of CullingModule's settings, kept in sync every client
// tick. Exists so the hot render-path mixins (EntityCullingMixin,
// BlockEntityCullingMixin, ParticleCullingMixin) can do a cheap static field
// read instead of a ModuleManager.getByName() lookup per entity/particle.
object CullingBridge {
    @JvmStatic var entityCullingEnabled = false
    @JvmStatic var entityCullRange = 64.0
    @JvmStatic var occlusionCullEnabled = true

    @JvmStatic var blockEntityCullingEnabled = false
    @JvmStatic var blockEntityCullRange = 48.0

    @JvmStatic var particleCullingEnabled = false
    @JvmStatic var particleCullRange = 32.0

    // Separate from generic entity culling: other players are usually the
    // thing you most want to *keep* visible at range (PvP/SMP awareness), so
    // this gets its own toggle + a longer default range rather than reusing
    // entityCullRange. Occlusion behavior (behind terrain) still follows
    // occlusionCullEnabled -- no separate raycast setting for players.
    @JvmStatic var playerCullingEnabled = false
    @JvmStatic var playerCullRange = 96.0

    // Sky/cloud are flat full-screen-ish draws with no distance concept --
    // these are on/off only, not range-based.
    @JvmStatic var skyCullingEnabled = false
    @JvmStatic var cloudCullingEnabled = false
}
