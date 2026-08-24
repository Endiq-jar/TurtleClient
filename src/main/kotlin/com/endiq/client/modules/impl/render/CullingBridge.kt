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
}
