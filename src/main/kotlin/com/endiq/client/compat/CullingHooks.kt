package com.endiq.client.compat

import com.endiq.TurtleClient
import com.endiq.client.modules.impl.render.CullingBridge
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/** Culling decisions shared by the immediate and render-state renderer hooks. */
object CullingHooks {
    @JvmStatic
    fun shouldCullEntity(entity: Entity): Boolean {
        val client = MinecraftClient.getInstance()
        if (client.world == null || entity === client.player || entity === cameraEntity()) return false
        val isPlayer = entity is PlayerEntity
        val enabled = if (isPlayer) CullingBridge.playerCullingEnabled else CullingBridge.entityCullingEnabled
        if (!enabled) return false
        val camera = cameraPosition()
        val range = if (isPlayer) CullingBridge.playerCullRange else CullingBridge.entityCullRange
        if (distanceSquared(camera, entity.x, entity.y, entity.z) > range * range) return true
        if (!CullingBridge.occlusionCullEnabled) return false

        val box = entity.boundingBox
        return rayHitDistance(camera, Vec3d(box.minX, box.minY, box.minZ), entity) != null &&
            rayHitDistance(camera, Vec3d(box.maxX, box.maxY, box.maxZ), entity) != null &&
            rayHitDistance(camera, box.center, entity) != null
    }

    @JvmStatic
    fun shouldCullBlockEntity(entity: BlockEntity, camera: Vec3d): Boolean {
        if (!CullingBridge.blockEntityCullingEnabled || MinecraftClient.getInstance().world == null) return false
//? if >=26.1 {
/*        val pos = entity.blockPos
*///?} else {
        val pos = entity.pos
//?}
        val range = CullingBridge.blockEntityCullRange
        return distanceSquared(camera, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5) > range * range
    }

    @JvmStatic
    fun shouldCullParticle(x: Double, y: Double, z: Double): Boolean {
        if (!CullingBridge.particleCullingEnabled || MinecraftClient.getInstance().world == null) return false
        val range = CullingBridge.particleCullRange
        return distanceSquared(cameraPosition(), x, y, z) > range * range
    }

    private fun cameraEntity(): Entity? =
//? if >=26.1 {
/*        MinecraftClient.getInstance().gameRenderer.mainCamera().entity()
*///?} else {
        MinecraftClient.getInstance().gameRenderer.camera.focusedEntity
//?}

    private fun cameraPosition(): Vec3d =
//? if >=26.1 {
/*        MinecraftClient.getInstance().gameRenderer.mainCamera().position()
*///?} else if >=1.21.6 {
/*        MinecraftClient.getInstance().gameRenderer.camera.cameraPos
*///?} else {
        MinecraftClient.getInstance().gameRenderer.camera.pos
//?}

    private fun distanceSquared(from: Vec3d, x: Double, y: Double, z: Double): Double {
        val dx = from.x - x
        val dy = from.y - y
        val dz = from.z - z
        return dx * dx + dy * dy + dz * dz
    }

    private fun rayHitDistance(from: Vec3d, to: Vec3d, entity: Entity): Double? {
        val world = MinecraftClient.getInstance().world ?: return null
//? if >=26.1 {
/*        val hit = world.clip(net.minecraft.world.level.ClipContext(from, to,
            net.minecraft.world.level.ClipContext.Block.COLLIDER,
            net.minecraft.world.level.ClipContext.Fluid.NONE, entity))
        if (hit.type == net.minecraft.world.phys.HitResult.Type.MISS) return null
        val pos = hit.location
*///?} else {
        val hit = world.raycast(net.minecraft.world.RaycastContext(from, to,
            net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
            net.minecraft.world.RaycastContext.FluidHandling.NONE, entity))
        if (hit.type == net.minecraft.util.hit.HitResult.Type.MISS) return null
        val pos = hit.pos
//?}
        return distanceSquared(from, pos.x, pos.y, pos.z)
    }

    // Experimental supplementary section pass. Identify sections by their actual
    // type, not "Chunk"/"Section" in a class name (which breaks after remapping).
    private var visibleSectionsField: Field? = null
    private var rendererClass: Class<*>? = null
    private var loggedMissingField = false
    private val wrapperFields = mutableMapOf<Class<*>, Field?>()

    @JvmStatic
    fun cullChunkSections(renderer: Any) {
        if (!CullingBridge.chunkOcclusionCullingEnabled) return
        val client = MinecraftClient.getInstance()
        val player = client.player ?: return
        if (client.world == null) return
        val camera = cameraPosition()
        val sections = findVisibleSections(renderer) ?: return
        val minimum = CullingBridge.chunkOcclusionMinDistance
        val iterator = sections.iterator()
        while (iterator.hasNext()) {
            val center = sectionCenter(iterator.next() ?: continue) ?: continue
            val distance = distanceSquared(camera, center.x, center.y, center.z)
            if (distance < minimum * minimum) continue
            val hitDistance = rayHitDistance(camera, center, player) ?: continue
            if (hitDistance < distance - 4.0) {
                try {
                    iterator.remove()
                } catch (_: UnsupportedOperationException) {
                    return // Some renderer implementations expose immutable snapshots.
                }
            }
        }
    }

    private fun findVisibleSections(renderer: Any): MutableCollection<*>? {
//? if >=26.1 {
/*        return (renderer as net.minecraft.client.renderer.LevelRenderer).visibleSections()
*///?} else {
        if (rendererClass != renderer.javaClass) {
            rendererClass = renderer.javaClass
            visibleSectionsField = null
            loggedMissingField = false
        }
        visibleSectionsField?.let { field ->
            return runCatching { field.get(renderer) as? MutableCollection<*> }.getOrNull()
        }
        for (field in renderer.javaClass.declaredFields) {
            if (Modifier.isStatic(field.modifiers) || !Collection::class.java.isAssignableFrom(field.type)) continue
            val collection = runCatching {
                field.isAccessible = true
                field.get(renderer) as? MutableCollection<*>
            }.getOrNull() ?: continue
            val sample = collection.firstOrNull() ?: continue
            if (sectionCenter(sample) != null) {
                visibleSectionsField = field
                return collection
            }
        }
        // Retry on later frames: an empty first-frame list is not evidence that
        // the field is missing for the rest of the session.
        if (!loggedMissingField) {
            loggedMissingField = true
            TurtleClient.LOGGER.debug("No visible sections available for supplementary chunk culling yet")
        }
        return null
//?}
    }

    private fun sectionCenter(info: Any): Vec3d? {
        val section = if (info is BuiltChunk) info else {
            val field = wrapperFields.getOrPut(info.javaClass) {
                info.javaClass.declaredFields.firstOrNull { BuiltChunk::class.java.isAssignableFrom(it.type) }
                    ?.also { it.isAccessible = true }
            } ?: return null
            runCatching { field.get(info) as? BuiltChunk }.getOrNull() ?: return null
        }
//? if >=26.1 {
/*        val pos = section.renderOrigin
*///?} else {
        val pos = section.origin
//?}
        return Vec3d(pos.x + 8.0, pos.y + 8.0, pos.z + 8.0)
    }
}
