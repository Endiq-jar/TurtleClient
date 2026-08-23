# 🐢 TurtleClient — Fabric, multi-version

A custom Minecraft client mod built with Fabric + Kotlin, targeting 1.17.1
through 26.2 via [Stonecutter](https://stonecutter.kikugie.dev/). See
`MIGRATION_NOTES.md` for the version ladder, toolchain split at 26.1, and
known API differences that still need per-module fixes.

Quick reference:

```
./gradlew "1.21.4:build"       # build one version
./gradlew chiseledBuild        # build every version
```

## Features

### HUD (top-left + array list)
- FPS Display
- CPS Display
- Coordinates (XYZ)
- Keystrokes (WASD + Jump)
- Armor Status with durability %
- Potion Status
- **Array List** top-right with accent bar (like Lunar)

### Movement
- Auto Sprint
- Speed (potion effect)
- NoFall

### Combat
- KillAura (range configurable)
- Reach

### Render
- Fullbright
- NoWeather
- TimeChanger

### Utility
- Freecam

## ClickGUI
Press **Right Shift** in-game to open the GUI.
- Left click a module to toggle it
- Left click a panel header to collapse it
- Middle-click drag a panel to move it

---

## Building on Termux

```bash
# 1. Copy the project to your Termux home
cp -r /sdcard/Download/turtle-client ~/

# 2. Run the build script
cd ~/turtle-client
bash build-termux.sh
```

First build downloads ~500MB (Minecraft + Fabric). Takes 5–15 min.

## Building on PC (IntelliJ)

1. Open the folder in IntelliJ IDEA
2. Let Gradle sync
3. Run the **Minecraft Client** config to test
4. `./gradlew build` → JAR in `build/libs/`

## Install

Drop `turtle-client-1.0.0.jar` into `.minecraft/mods/` alongside:
- `fabric-api-*.jar`
- `fabric-language-kotlin-*.jar`
