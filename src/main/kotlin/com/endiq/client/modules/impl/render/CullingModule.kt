package com.endiq.client.modules.impl.render

import com.endiq.client.modules.Module
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

// Distance + occlusion culling for entities (including other players),
// block entities (chests, signs, etc.), particles, sky, and clouds. The
// actual skip-render logic lives in the mixins (EntityCullingMixin /
// BlockEntityCullingMixin / ParticleCullingMixin / SkyCloudCullingMixin) --
// this module is just the settings UI + the bridge sync.
//
// "Leaves" and "font" culling from the todo list aren't handled here: leaves
// are a vanilla graphics-quality setting (Fast/Fancy), not something a mod
// culls per-entity, and font rendering optimization is a GPU batching change
// (see MIGRATION_NOTES.md -- ImmediatelyFast already solves this, bundling
// it is far safer than a hand-rolled font renderer rewrite).
//
// Full block/chunk occlusion culling (skipping whole hidden chunk sections)
// is deliberately NOT handled here either -- that's a chunk visibility-graph
// rewrite that Sodium already does far better than a hand-rolled version
// could; this module only touches per-object rendering (entities/particles/
// block entities/sky/clouds), same scope as before, just wider coverage.
class CullingModule : Module(
    "Culling",
    "Skips rendering entities, players, block entities, particles, sky, and clouds that are far away, hidden, or not worth drawing",
    Category.RENDER
) {
    private val optEntity = bool("Entity Culling", "Hide entities blocked by terrain", default = true)
    private val optEntityRange = slider("Entity Range", "Max render distance for occluded entities", default = 64f, min = 16f, max = 128f, suffix = "b")
    private val optOcclusion = bool("Occlusion Raycast", "Actually raycast to check line of sight (costs a little CPU); off = distance-only", default = true)

    private val optPlayer = bool("Player Culling", "Distance-cull other players in multiplayer (occlusion still applies if enabled above)", default = false)
    private val optPlayerRange = slider("Player Range", "Max render distance for other players", default = 96f, min = 32f, max = 256f, suffix = "b")

    private val optBlockEntity = bool("Block Entity Culling", "Distance-cull chest/sign/etc. detail rendering", default = true)
    private val optBlockEntityRange = slider("Block Entity Range", default = 48f, min = 16f, max = 128f, suffix = "b")

    private val optParticles = bool("Particle Culling", "Skip spawning particles beyond range", default = true)
    private val optParticleRange = slider("Particle Range", default = 32f, min = 8f, max = 96f, suffix = "b")

    private val optSky = bool("Sky Culling", "Skip rendering the sky (sun/moon/stars/horizon)", default = false)
    private val optClouds = bool("Cloud Culling", "Skip rendering clouds", default = false)

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

        CullingBridge.playerCullingEnabled = enabled && optPlayer.value
        CullingBridge.playerCullRange = optPlayerRange.value.toDouble()

        CullingBridge.blockEntityCullingEnabled = enabled && optBlockEntity.value
        CullingBridge.blockEntityCullRange = optBlockEntityRange.value.toDouble()

        CullingBridge.particleCullingEnabled = enabled && optParticles.value
        CullingBridge.particleCullRange = optParticleRange.value.toDouble()

        CullingBridge.skyCullingEnabled = enabled && optSky.value
        CullingBridge.cloudCullingEnabled = enabled && optClouds.value
    }
}
