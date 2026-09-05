package com.endiq.client.modules.impl.render

import com.endiq.client.compat.*
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

/**
 * Real memory/CPU optimization, not a cosmetic one: PvpInfoModule, TeamCirclesModule
 * etc. each need "entities near me" every frame. Without this, that's N modules x
 * up to 60 world scans/sec, each allocating its own filtered list. This refreshes
 * ONCE per client tick (20/sec, not 60+) and every module reads the same three
 * lists -- one scan, one set of allocations, shared.
 *
 * NOT COMPILE-VERIFIED (see MIGRATION_NOTES.md) -- `ClientWorld.getEntities()`
 * is remembered, not confirmed against a built jar.
 */
object EntityCache {
    var all: List<Entity> = emptyList()
        private set
    var living: List<LivingEntity> = emptyList()
        private set
    var players: List<PlayerEntity> = emptyList()
        private set

    init {
        ClientTickEvents.END_CLIENT_TICK.register { refresh() }
    }

    private fun refresh() {
        val mc = MinecraftClient.getInstance()
        val world = mc.world ?: run { all = emptyList(); living = emptyList(); players = emptyList(); return }
        val self = mc.player

        val snapshot = ArrayList<Entity>(64)
        for (e in world.entities) {
            if (e === self) continue
            snapshot.add(e)
        }
        all = snapshot
        living = snapshot.filterIsInstance<LivingEntity>()
        players = snapshot.filterIsInstance<PlayerEntity>()
    }

    /** Players within [range] blocks of the client player. Uses the cached list, no world query. */
    fun playersWithin(range: Double): List<PlayerEntity> {
        val self = MinecraftClient.getInstance().player ?: return emptyList()
        val rangeSq = range * range
        return players.filter { self.squaredDistanceTo(it) <= rangeSq }
    }
}
