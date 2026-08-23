# Multi-version migration notes

Turtle Client now targets 9 Minecraft versions via [Stonecutter](https://stonecutter.kikugie.dev/),
spanning two toolchain eras:

| Version   | Mappings         | Notes |
|-----------|------------------|-------|
| 1.17.1    | Yarn (obf.)      | oldest supported, Java 16 |
| 1.18.2    | Yarn (obf.)      | Java 17 required from here |
| 1.19.4    | Yarn (obf.)      | |
| 1.20.1    | Yarn (obf.)      | |
| 1.20.6    | Yarn (obf.)      | Java 21 required from here |
| 1.21.1    | Yarn (obf.)      | |
| 1.21.4    | Yarn (obf.)      | **current single-version baseline** |
| 1.21.11   | Yarn (obf.)      | last version with Yarn mappings at all |
| 26.2      | Mojang (unobf.)  | Java 25 required; no remapping |

`26.1` shipped March 2026 as the first Minecraft release with **no obfuscation at
all**. Mods below it need Yarn/remapping; mods on it and above compile directly
against Mojang's own class/method names. `dev.kikugie.loom-back-compat` +
`loomx.applyMojangMappings()` in `build.gradle.kts` picks the right Loom variant
per node automatically, so you don't need a second build script for that era —
but code that references Minecraft internals by name still needs the right names
for each side of that line.

## How to build

- `./gradlew "1.21.4:build"` — build a single version.
- `./gradlew chiseledBuild` — build every version in the ladder, jars land in
  `build/libs/<mc version>/`.
- Switch which version your editor sees with the **"Set active project to ..."**
  Gradle tasks, or edit `stonecutter active "..."` in `stonecutter.gradle.kts`.

Do this from Termux/CI, not this sandbox — the sandbox can't reach
`maven.fabricmc.net`, Mojang's library servers, or Yarn/Mojmap artifacts, so none
of this has been compiled yet. **Paste me the first build's errors and I'll fix
them version by version.**

## Fixed already

- `TurtleClientClient.kt` — HUD registration is now version-conditioned:
  `HudRenderCallback` below 1.21.8, `HudElementRegistry` (new `hud` package,
  `Matrix3x2fStack`) from 1.21.8 up, which covers both 1.21.11 and 26.2.

## Still needs work — known breaking points in this range

These affect the other ~54 modules and haven't been touched yet. Grep for them
and wrap with `//? if <condition> { ... //?} else { /*...*/ //?}`:

- **`HudRenderer.kt`** — its render method's signature needs to match whichever
  HUD API `TurtleClientClient.kt` calls it with (`DrawContext` pre-1.21.6,
  `GuiGraphicsExtractor`/`Matrix3x2fStack` from 1.21.8). Every HUD module that
  draws directly (armor bar, coordinates, keystrokes, crosshair, etc.) touches
  this.
- **`ColorProviderRegistry` → `BlockColorRegistry`** (26.1+) — only matters if
  any render module tints blocks; check `AnimationsModule.kt`, `NoWeatherModule.kt`.
- **`ItemStack` → `ItemStackTemplate`** (26.1+) — an `ItemStack` can no longer be
  constructed before a world loads; several methods return the new type instead.
  Check `ArmorStatusModule.kt`, `PotionStatusModule.kt`, `AttackIndicatorModule.kt`
  — anything reading inventory/armor contents.
- **Villager trading is fully data-driven from 26.1** — irrelevant unless a
  module reads trade offers.
- **`ResourceLocation`/`Identifier` naming** — Yarn calls it `Identifier`,
  Mojang's own mappings call it `ResourceLocation`. Anywhere you construct one
  (`Identifier.of(...)`) needs a version-conditioned import on the 26.1+ side.
- **Fabric API module renames on 26.1** — e.g. `ItemGroupEvents` became
  `CreativeModeTabEvents`. Full list:
  https://docs.fabricmc.net/develop/porting/26.1/fabric-api

## Fabric API / Yarn build numbers to double-check

`stonecutter.properties.toml` entries marked `# verify` are best-known values for
older releases, not independently confirmed against `fabricmc.net/develop` right
now. If the first sync fails on a specific version with a "could not resolve"
error, that's almost always a stale `deps.fabric_api` or `deps.yarn` string —
fix it there, not in code.
