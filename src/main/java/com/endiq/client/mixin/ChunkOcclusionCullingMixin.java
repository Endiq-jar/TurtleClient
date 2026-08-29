package com.endiq.client.mixin;

import com.endiq.client.modules.impl.render.CullingBridge;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;

/**
 * EXPERIMENTAL, NOT COMPILE-VERIFIED -- the riskiest mixin in the culling
 * suite. Read this whole comment before touching it.
 *
 * WHY THIS ONE IS DIFFERENT: EntityCullingMixin / BlockEntityCullingMixin /
 * ParticleCullingMixin all hook a per-object render(Thing, ...) dispatcher
 * method -- clean injection point, cancel one call, one thing disappears.
 * Chunk/terrain rendering has no equivalent: vanilla already builds a
 * per-frame set of "visible sections" each call to WorldRenderer's terrain
 * setup step, via a graph search (SectionOcclusionGraph) that expands from
 * the camera's section through neighboring sections only where a face pair
 * was baked "open" when that section's mesh was built. That IS real
 * occlusion culling, already running, before this mixin does anything. It's
 * conservative, though -- big open caves and complex terrain make the graph
 * search spread further than a straight camera-to-point raycast would
 * allow, so some sections survive vanilla's pass that a raycast would reject.
 *
 * WHAT THIS MIXIN ADDS: a second, supplementary pass. After vanilla finishes
 * building its visible-section collection for the frame, this walks that
 * same collection and drops (mutates in place) any section whose center
 * fails a camera raycast, same technique as EntityCullingMixin. It never
 * ADDS sections back -- purely a trim on top of vanilla's own result.
 *
 * WHY REFLECTION INSTEAD OF A HARDCODED FIELD NAME: this project targets 8
 * Stonecutter version nodes (1.18.2 through 26.2). WorldRenderer's internal
 * field/method names around section visibility have been renamed and
 * restructured multiple times across that span (most drastically around the
 * 1.21.5 RenderPipelines split). A hardcoded wrong field name means a Mixin
 * apply failure -- fatal at launch, per this project's
 * "injectors.defaultRequire": 1. A wrong REFLECTIVE guess just means this
 * pass silently does nothing that frame; vanilla's own culling is completely
 * unaffected either way. Fails safe by construction, at the cost of being
 * slower and uglier than a real hook.
 *
 * The @Inject target method name itself, "setupTerrain", is NOT guarded by
 * reflection -- that name has been stable since the 1.17/1.18 rendering
 * rewrite. If Mixin can't resolve it either, that's a real signal this
 * version's WorldRenderer has been restructured more than expected; check
 * the decompiled class before assuming it's just this method's problem.
 *
 * IF THIS SILENTLY DOES NOTHING: check the log for
 * "[TurtleClient] ChunkOcclusionCullingMixin: visible-section field not
 * found". That means the reflective field search below didn't find a
 * Collection field matching the heuristics (non-empty, element class name
 * containing "Chunk" or "Section"). Decompile WorldRenderer for the target
 * version, find the field that holds the per-frame visible-section list, and
 * either widen the heuristic or hardcode that version's name behind a
 * Stonecutter //? if block.
 */
@Mixin(WorldRenderer.class)
public class ChunkOcclusionCullingMixin {

    private static Field turtleClient$visibleSectionsField;
    private static boolean turtleClient$fieldSearchDone = false;
    private static boolean turtleClient$loggedMissingField = false;

    @Inject(method = "setupTerrain", at = @At("RETURN"))
    private void turtleClient$cullChunkSections(CallbackInfo ci) {
        if (!CullingBridge.getChunkOcclusionCullingEnabled()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;
        Camera camera = mc.gameRenderer.getCamera();
        if (camera == null) return;
        Vec3d camPos = camera.getPos();

        Collection<?> visible = turtleClient$findVisibleSections((WorldRenderer) (Object) this);
        if (visible == null) return;

        double minDist = CullingBridge.getChunkOcclusionMinDistance();
        Iterator<?> it = visible.iterator();
        while (it.hasNext()) {
            Object sectionInfo = it.next();
            Vec3d center = turtleClient$sectionCenter(sectionInfo);
            if (center == null) continue;

            double distSq = camPos.squaredDistanceTo(center);
            if (distSq < minDist * minDist) continue; // never touch sections right around the camera

            RaycastContext rc = new RaycastContext(
                    camPos, center,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    mc.player
            );
            HitResult hit = mc.world.raycast(rc);
            // Blocked only if the ray stopped meaningfully short of the
            // section center -- "hit the section's own front face" shouldn't
            // count, only "hit something well before it".
            boolean blocked = hit != null
                    && hit.getType() != HitResult.Type.MISS
                    && hit.getPos().squaredDistanceTo(camPos) < distSq - 4.0;
            if (blocked) {
                try {
                    it.remove();
                } catch (UnsupportedOperationException immutableCollection) {
                    // Found the right field but it's an immutable snapshot --
                    // can't mutate it here. Bail out for this frame rather
                    // than half-apply and leave state inconsistent.
                    return;
                }
            }
        }
    }

    /** Reflection lookup by type + content heuristic, cached after the first attempt. */
    private static Collection<?> turtleClient$findVisibleSections(WorldRenderer renderer) {
        if (turtleClient$fieldSearchDone) {
            if (turtleClient$visibleSectionsField == null) return null;
            try {
                Object val = turtleClient$visibleSectionsField.get(renderer);
                return val instanceof Collection ? (Collection<?>) val : null;
            } catch (IllegalAccessException e) {
                return null;
            }
        }
        turtleClient$fieldSearchDone = true;

        for (Field f : WorldRenderer.class.getDeclaredFields()) {
            if (!Collection.class.isAssignableFrom(f.getType())) continue;
            f.setAccessible(true);
            try {
                Object val = f.get(renderer);
                if (!(val instanceof Collection<?> coll) || coll.isEmpty()) continue;
                String sampleClassName = coll.iterator().next().getClass().getSimpleName();
                if (sampleClassName.contains("Chunk") || sampleClassName.contains("Section")) {
                    turtleClient$visibleSectionsField = f;
                    return coll;
                }
            } catch (IllegalAccessException ignored) {
                // inaccessible field, skip
            }
        }

        if (!turtleClient$loggedMissingField) {
            turtleClient$loggedMissingField = true;
            System.err.println("[TurtleClient] ChunkOcclusionCullingMixin: visible-section field not found, "
                    + "chunk occlusion pass disabled this session (vanilla culling unaffected)");
        }
        return null;
    }

    /**
     * Pulls a world-space center point out of whatever the visible-section
     * element type turns out to be, by trying the common shapes rather than
     * one hardcoded field/method name: unwrap a likely wrapper field
     * ("chunk"/"section"/"builtChunk"), then look for a no-arg getOrigin()
     * returning BlockPos, which is what ChunkBuilder.BuiltChunk exposes in
     * every Yarn mapping generation this project has seen so far.
     */
    private static Vec3d turtleClient$sectionCenter(Object sectionInfo) {
        Object target = sectionInfo;
        for (String wrapperField : new String[]{"chunk", "section", "builtChunk"}) {
            try {
                Field f = target.getClass().getDeclaredField(wrapperField);
                f.setAccessible(true);
                Object inner = f.get(target);
                if (inner != null) {
                    target = inner;
                    break;
                }
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
                // try the next shape
            }
        }

        try {
            Method getOrigin = target.getClass().getMethod("getOrigin");
            Object origin = getOrigin.invoke(target);
            if (origin instanceof BlockPos pos) {
                return new Vec3d(pos.getX() + 8.0, pos.getY() + 8.0, pos.getZ() + 8.0);
            }
        } catch (Exception ignored) {
            // no getOrigin() on this shape -- give up for this element,
            // caller just skips it (fail safe, not fail loud)
        }
        return null;
    }
}
