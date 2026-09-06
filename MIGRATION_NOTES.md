# Multi-version compatibility

The configured build matrix contains eight Minecraft targets. Keep the matrix in
`settings.gradle.kts`, `stonecutter.properties.toml`, and CI in sync.

| Minecraft | Mappings | Java toolchain |
|---|---|---|
| 1.18.2 | Yarn | 17 |
| 1.19.4 | Yarn | 17 |
| 1.20.1 | Yarn | 17 |
| 1.20.6 | Yarn | 21 |
| 1.21.1 | Yarn | 21 |
| 1.21.4 | Yarn — active editor baseline | 21 |
| 1.21.11 | Yarn | 21 |
| 26.2 | Mojang's unobfuscated names | 25 |

Loom Back Compat chooses the appropriate Loom variant. Yarn mappings are supplied
explicitly below 26.1; newer nodes use Mojang's published names. Changing mappings
alone does **not** translate source-level Minecraft API references.

## Building and checking

Build one target, run its checks, and collect its artifacts:

```sh
./gradlew 1.21.4:buildAndCollect
```

Build the entire configured matrix locally:

```sh
./gradlew 1.18.2:buildAndCollect 1.19.4:buildAndCollect \
  1.20.1:buildAndCollect 1.20.6:buildAndCollect \
  1.21.1:buildAndCollect 1.21.4:buildAndCollect \
  1.21.11:buildAndCollect 26.2:buildAndCollect
```

Collected jars are in `build/libs/<minecraft-version>/`. The Gradle wrapper needs
a JDK and network access for the Minecraft, Fabric, and toolchain dependencies.
Gradle selects Java 17, 21, or 25 for each target through its toolchain configuration.
Kotlin 2.1.20 emits JVM 23 bytecode for the Java 25 target; those classes can run on
Java 25 alongside Java sources compiled for 25.

The existing GitHub workflow runs `buildAndCollect` separately for each target.
That task depends on the full `build` lifecycle, including:

- Kotlin and Java compilation, plus remapping where required;
- shared unit tests for GUI scrolling/actions, editable controls, settings, timers,
  notifications, bounded artwork/cosmetics, account storage and fake authentication;
- runtime bytecode probes for version-specific account-service constructors/factories;
- bytecode checks that configured `@Inject` targets exist and their callback
  arguments, return callbacks, and staticness match the target Minecraft API, plus
  `@Redirect` call sites and accessor fields;
- verification that the final jar contains every declared mixin and entrypoint.

Tests intentionally share the root `src/test/kotlin` source set unchanged.
Stonecutter comments in those test files do not select a version: use runtime
bytecode/type inspection for version-specific assertions instead.

These checks do not launch Minecraft or apply Mixins in a running client. Smoke-test
menus, HUDs, name badges, cosmetics, real-account switching, and culling in-game before a release, including with other
rendering mods if those combinations are supported.

## Where compatibility lives

Shared code stays under `src/main/{java,kotlin,resources}` so every version goes
through Stonecutter's source processing. Do not move client classes back to an
unconfigured `src/client` source tree.

The Kotlin adapters in `com.endiq.client.compat` cover:

- **`MinecraftTypes`**: compile-time Yarn/Mojang type aliases, not runtime stubs.
- **`GuiContext`**: MatrixStack, DrawContext, and GuiGraphicsExtractor rendering;
  texture, item, text, and matrix-stack differences.
- **`ClientScreen`**: common GUI callbacks over legacy numeric input arguments and
  newer key/mouse/character records. Character input is passed as a string so
  record-based code points are not narrowed to a single UTF-16 character.
- **`ClientCompat` / `ClientOptions`**: identifiers, text, FPS, chat, options,
  resource packs, inventory, and platform helpers.
- **`SessionBridge`**: disconnected, type-discovered account-service transactions;
  real session/service behavior still requires the manual account checklist.
- **`CosmeticFeature` / `CosmeticTextures`**: player render layers, immutable queued
  loadouts and cached/released custom texture uploads.
- **`GameplayControls`**: render-time FOV/lightmap and input hooks. On native 26.2,
  FOV lives in `Camera.calculateFov` and brightness extraction lives in
  `LightmapRenderStateExtractor.extract`, not the old GameRenderer/LightTexture paths.
- **`BrandingRenderer` / `CullingHooks`**: shared behavior called by version-specific
  Java injection adapters.

Java Mixins keep explicit version branches for their actual target classes and
method signatures. Entity labels have distinct entity-based, render-state, and
queued-rendering APIs. Render-state badge eligibility is captured per entity rather
than inferred from a render-state object as if it were an entity.

Resource processing removes legacy-only mixins from newer jars (and render-state
mixins from older jars), and selects the correct Mixin Java compatibility level.
Only `fabric.mod.json` is template-expanded: expanding a whole mixin configuration
would incorrectly treat the `$` in nested class names as a template variable.

When adding a target, adapt the real API instead of dropping the target, disabling
required injectors, or suppressing compilation failures. The bytecode checks are
intended to catch injection mistakes that otherwise survive Java compilation.
