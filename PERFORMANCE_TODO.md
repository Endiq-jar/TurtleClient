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

## Follow-up round — item by item, no hedging

- **"Skips rendering things behind you"** — already true, for both blocks and
  entities, before any of this mod's code runs. Vanilla's `Frustum` check
  filters both chunk sections and the entity render list before
  `EntityRenderDispatcher.render` is ever called. Nothing to add here; it's
  not a gap.
- **Entity cache** — added: `EntityCache.kt`, refreshes once per client tick
  instead of every module scanning the world independently. Wired it into
  `PvpInfoModule`'s HUD rendering, which was doing its own per-frame
  `world.entities.filterIsInstance<PlayerEntity>()` scan — that's now reading
  the shared cache instead, and using the module's own `maxDist`/`maxPlayers`
  settings instead of the hardcoded values that render code had.
- **Memory usage / allocation reduction** — `EntityCullingMixin`'s occlusion
  check went from 5 raycast points to 3, dropping a `Vec3d[5]` allocation
  entirely. That's a real per-entity-per-frame allocation cut, not a stub.
- **Block cache, texture cache** — already vanilla's job (`BakedModelManager`
  caches models, the texture atlas is built once at reload, block states are
  palette-interned). There's no missing cache to add without touching those
  systems directly, which is renderer-replacement territory (Sodium), not a
  mixin.
- **Model Cache / Mesh Cache / Shader Cache / Pipeline Cache** — same answer:
  vanilla already builds each chunk mesh once and keeps it until a block in
  that section changes, compiles each shader once at startup, and creates
  each pipeline object once. A "cache" on top of that isn't optional
  behavior a mod turns on — restructuring how those get built and reused
  *is* Sodium's entire codebase. I'm not going to hand you a class named
  `ShaderCache` that doesn't actually change anything just so the box is
  checked.
- **Mob AI Optimization, Pathfinding Optimization, Block Entity Tick
  Optimization, Random Tick/Event Optimization** — holding the line here,
  and not just as a caution: these are *server tick* logic. In multiplayer —
  which is what most of this client's modules (PvP, Hypixel category,
  Team View, Combo Counter) are built for — the client has zero access to
  the remote server's tick loop at all; there's nothing to mixin into. In
  singleplayer, the integrated server does run locally, but a naive
  "skip AI/ticks when far away" hack risks silently breaking furnaces,
  farms, and mob behavior with no test suite here to catch it. Lithium
  solves exactly this, correctly, and is a one-line dependency add — that's
  a better outcome for you than a hand-rolled version I can't verify.
- **Frustum Culling, Occlusion Culling** — Frustum: vanilla, see above.
  Occlusion: implemented for entities (`EntityCullingMixin`'s raycast) last
  round. Chunk-level occlusion culling (portal/visibility-graph based) is
  the other piece of Sodium's renderer, not a standalone mixin.

If any of the "bundle instead" items matter enough to be worth building for
real — e.g. you want TurtleClient to auto-install Lithium/Starlight/Sodium
the way TurtleLauncher's Performance Features catalog does — that's a
concrete, scoped feature I can build. Say which one.

- **Dynamic Render/Simulation Distance** — scale `options.viewDistance` /
  `simulationDistance` based on FPS or thermal state. This is a real,
  scoped, buildable module (reads `MinecraftClient.getInstance().options`,
  same pattern as `FullBrightModule`) — say the word and I'll build it next.
- **Adaptive VSync / Frame Pacing / Battery & Thermal-Aware Optimization** —
  TurtleLauncher already has adaptive vsync + FORCE_VSYNC wiring per earlier
  work; whether that needs a TurtleClient-side counterpart depends on
  whether it's currently launcher-only.
