package com.endiq.client.modules.impl.render

import com.endiq.client.modules.Module
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

// Distance + occlusion culling for entities, block entities (chests, signs,
// etc.) and particles. The actual skip-render logic lives in the mixins
// (EntityCullingMixin / BlockEntityCullingMixin / ParticleCullingMixin) --
// this module is just the settings UI + the bridge sync.
//
// "Leaves" and "font" culling from the todo list aren't handled here: leaves
// are a vanilla graphics-quality setting (Fast/Fancy), not something a mod
// culls per-entity, and font rendering optimization is a GPU batching change
// (see MIGRATION_NOTES.md -- ImmediatelyFast already solves this, bundling
// it is far safer than a hand-rolled font renderer rewrite).
class CullingModule : Module(
    "Culling",
    "Skips rendering entities, block entities, and particles that are far away or hidden behind terrain",
    Category.RENDER
) {
    private val optEntity = bool("Entity Culling", "Hide entities blocked by terrain", default = true)
    private val optEntityRange = slider("Entity Range", "Max render distance for occluded entities", default = 64f, min = 16f, max = 128f, suffix = "b")
    private val optOcclusion = bool("Occlusion Raycast", "Actually raycast to check line of sight (costs a little CPU); off = distance-only", default = true)

    private val optBlockEntity = bool("Block Entity Culling", "Distance-cull chest/sign/etc. detail rendering", default = true)
    private val optBlockEntityRange = slider("Block Entity Range", default = 48f, min = 16f, max = 128f, suffix = "b")

    private val optParticles = bool("Particle Culling", "Skip spawning particles beyond range", default = true)
    private val optParticleRange = slider("Particle Range", default = 32f, min = 8f, max = 96f, suffix = "b")

    init {
        // Registered once (Module instances are constructed once by
        // ModuleManager.init()), always runs, gated internally by `enabled`
        // so toggling the module doesn't need to add/remove listeners.
        ClientTickEvents.END_CLIENT_TICK.register { sync() }
    }

    override fun onEnable() { sync() }
    override fun onDisable() { sync() }

    private fun sync() {
        CullingBridge.entityCullingEnabled = enabled && optEntity.value
        CullingBridge.entityCullRange = optEntityRange.value.toDouble()
        CullingBridge.occlusionCullEnabled = optOcclusion.value

        CullingBridge.blockEntityCullingEnabled = enabled && optBlockEntity.value
        CullingBridge.blockEntityCullRange = optBlockEntityRange.value.toDouble()

        CullingBridge.particleCullingEnabled = enabled && optParticles.value
        CullingBridge.particleCullRange = optParticleRange.value.toDouble()
    }
}
