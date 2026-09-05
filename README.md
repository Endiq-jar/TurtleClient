# 🐢 TurtleClient — Fabric, multi-version

A custom Minecraft client mod built with Fabric + Kotlin for eight Minecraft
targets, from 1.18.2 through 26.2, via [Stonecutter](https://stonecutter.kikugie.dev/).
See [MIGRATION_NOTES.md](MIGRATION_NOTES.md) for the exact version matrix,
Java toolchains, compatibility adapters, and validation checks.

Quick reference:

```
./gradlew 1.21.4:buildAndCollect  # build, check, and collect one version
./gradlew 1.21.4:check            # run tests and packaged-jar verification
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
4. `./gradlew 1.21.4:buildAndCollect` → JARs in `build/libs/1.21.4/`

## Install

Drop the non-sources TurtleClient jar for your Minecraft version into `.minecraft/mods/` alongside:
- `fabric-api-*.jar`
- `fabric-language-kotlin-*.jar`
