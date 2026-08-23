# Performance todo — what's implemented vs. what to bundle

`todo.txt` listed ~100 optimization targets. A few of those are genuinely
first-party client features and are now implemented (see below). The rest —
chunk meshing, lighting engine rewrites, redstone/hopper tick optimization,
network compression, GPU batching — are each the entire scope of an existing,
maintained, widely-used Fabric mod. Hand-rewriting a chunk mesher or lighting
engine from scratch inside TurtleClient, without the ability to compile-test
or run a regression suite here, would be far more likely to corrupt worlds or
tank FPS than to help. The correct move — and the one TurtleLauncher's own
Performance Features catalog already takes — is to bundle the proven mod.

## Implemented in this pass (CullingModule + 3 mixins)

- **Entity Culling** — distance cutoff + occlusion raycast (`EntityCullingMixin`)
- **Block Entity Culling** — distance cutoff for chests/signs/banners/etc.
  (`BlockEntityCullingMixin`)
- **Particle Culling** — distance cutoff on spawn (`ParticleCullingMixin`)

**Not real per-mod-implementable items** (clarifying, not dodging):
- *Frustum Culling* — vanilla Minecraft already does this for every chunk;
  there's nothing to add.
- *Leaves Culling* — vanilla's Fast/Fancy graphics toggle already controls
  this; it isn't a separate system to build.
- *Font culling* — text rendering isn't culled per-glyph in any client;
  what actually costs FPS here is unbatched GUI draw calls, which is a
  renderer-level change (see ImmediatelyFast below), not a culling problem.

## Recommend bundling instead of reimplementing

| todo.txt items | Existing mod that already solves it |
|---|---|
| Occlusion Culling (chunks), Draw Call Reduction, Instanced Rendering, GPU Buffer Pooling, Vertex Buffer Optimization, Shader/Pipeline Cache, VRAM Optimization | **Sodium** (+ Indium for Fabric Rendering API compat) |
| Entity/Mob AI Optimization, Pathfinding, Redstone, Hopper, Fluid/Block/Random Tick Optimization, Chunk Loading/Meshing Optimization | **Lithium** |
| Lighting Optimization, Light Update/Sky Light/Block Light Optimization | **Starlight** |
| Async Chunk Loading/Saving, Parallel Chunk Processing, Chunk Generation Optimization | **C2ME** |
| Memory Allocation, RAM Optimization, GC Reduction, Object Pooling | **FerriteCore** |
| Fast Startup, Fast Resource Reload, Resource Pack Optimization, Model/Mesh Cache | **ModernFix** |
| Font/GUI batching, Animated Texture Optimization | **ImmediatelyFast** |
| Network Packet/Compression Optimization | **Krypton** |

All of these are already in TurtleLauncher's Performance Features Modrinth
catalog per earlier sessions — if TurtleClient should auto-suggest or bundle
them too, that's a small addition to whichever mod-list UI reads that
catalog, not new engine code.

## Genuinely missing, worth building later (settings-level, not engine-level)

- **Dynamic Render/Simulation Distance** — scale `options.viewDistance` /
  `simulationDistance` based on FPS or thermal state. This is a real,
  scoped, buildable module (reads `MinecraftClient.getInstance().options`,
  same pattern as `FullBrightModule`) — say the word and I'll build it next.
- **Adaptive VSync / Frame Pacing / Battery & Thermal-Aware Optimization** —
  TurtleLauncher already has adaptive vsync + FORCE_VSYNC wiring per earlier
  work; whether that needs a TurtleClient-side counterpart depends on
  whether it's currently launcher-only.
