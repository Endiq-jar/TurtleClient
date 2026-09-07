# 🐢 TurtleClient — Fabric, multi-version

## Controls, capes and accounts

- **New vector visual identity:** matte controls, a flat turtle mark and **52 distinct built-in module icons**. Editable SVG sources are included.
- **Remade startup/resource loading:** real percentage, per-reload time-based smoothing, responsive placement and font-independent loading captions. Vanilla completion/error handling is retained.
- **Capes from the internet:** your Mojang profile cape, your LabyMod cape and NameMC's public cape index are downloaded on demand and cached — no capes are bundled. Equip one in **Capes** and press **F5** in a world. See [cape sources and limits](CAPES_README.txt).
- **Minimal title screen:** a flat backdrop and a bare text menu. The old scene switcher and its two backdrop images are gone.
- **Account switcher:** click the name at the top right of the title menu. Offline profiles and the launcher account are available; official Microsoft device sign-in requires your own approved public application ID. Tokens are memory-only. See [setup and security](docs/ACCOUNTS.md).
- Repaired HUD lookups, CPS input, live FOV/zoom/brightness, timer/utility actions, crosshair customization, RGBA editing and saved preferences.
- **Not every legacy prototype is implemented.** Unavailable modules and basic fixed-layout overlays are explicitly identified instead of presenting fake working controls. See the [button audit and manual test checklist](docs/BUTTON_AUDIT.md).

![Vector loading and module design reference, not an in-game screenshot](docs/turtle-interface-preview.png)

[All 52 module icons](docs/turtle-module-icons.png) · [SVG sources and regeneration](docs/ARTWORK.md)


A custom Minecraft client mod built with Fabric + Kotlin for eight Minecraft
targets, from 1.18.2 through 26.2, via [Stonecutter](https://stonecutter.kikugie.dev/).
See [MIGRATION_NOTES.md](MIGRATION_NOTES.md) for the exact version matrix,
Java toolchains, compatibility adapters, and validation checks.

Quick reference:

```
./gradlew 1.21.4:buildAndCollect  # build, check, and collect one version
./gradlew 1.21.4:check            # run tests and packaged-jar verification
```

## Features and current scope

- **HUD:** FPS, CPS, ping/history, coordinates, speed, memory, clock, direction,
  server/resource-pack labels, crosshair, attack cooldown, attack-distance readout
  and combo counter. Nearby-player and scoreboard-team tables have layout/filter controls.
- **Basic fixed-layout overlays:** keystrokes, armor, potion status and UHC readouts.
- **Visual controls:** FOV, hold-to-zoom with scroll adjustment, full-bright lightmap,
  and the existing culling/adaptive view-distance/VSync controls.
- **Utility:** timer start/pause/reset, clean PNG screenshots, session waypoints,
  opt-in Auto Text, popup notifications and forward auto-sprint.
- **Capes/accounts:** the downloaded cape list and the account modes described above.

The old README advertised KillAura, NoFall, a movement-speed potion, working
weather/time controls and freecam. Those are **not implemented features in this
checkout**. Reach Display reports attack distance; it does not extend attack reach.
Twelve incomplete legacy prototypes are marked **Unavailable**. See the
[control audit](docs/BUTTON_AUDIT.md) for the precise limits and manual checks.

## ClickGUI
Press **Right Shift** in-game to open the GUI.
- Left click a module to toggle it
- Right-click a module (or click its gear) to open settings
- Use the mouse wheel or drag the scrollbar in modules, capes, and settings
- Page Up / Page Down and Home / End also scroll
- Search or choose a category to filter modules; Escape returns to the previous screen

---

## Building on Termux

```bash
# 1. Copy the project to your Termux home
cp -r /sdcard/Download/turtle-client ~/

# 2. Run the build script
cd ~/turtle-client
bash build-termux.sh
```

The Termux helper is legacy and has not been validated for the full matrix.
Use compatible JDKs (17/21/25) and the target-specific Gradle commands above;
first builds require substantial dependency/toolchain downloads.

## Building on PC (IntelliJ)

1. Open the folder in IntelliJ IDEA
2. Let Gradle sync
3. Run the **Minecraft Client** config to test
4. `./gradlew 1.21.4:buildAndCollect` → JARs in `build/libs/1.21.4/`

## Install

Drop the non-sources TurtleClient jar for your Minecraft version into `.minecraft/mods/` alongside:
- `fabric-api-*.jar`
- `fabric-language-kotlin-*.jar`

## Artwork

The menu uses editable, code-authored Turtle vector branding over a single photographic
night-sky panorama backdrop (slowly panned and veiled so text stays readable).
Vector PNG data totals about 178 KB; the panorama ships separately and is credited in
docs/ARTWORK.md. No image-model masters or runtime SVG renderer are required.
See [docs/ARTWORK.md](docs/ARTWORK.md) for texture budgets and generation details.
