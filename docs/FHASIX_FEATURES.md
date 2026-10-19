# FHASIX feature parity

Correction: the reference client is **FHASIX Client** (videos
`x44cDCbn_SY` and `v2vZzUoygPc`, GitHub `cryonixlauncherteam/fhasix-client`
and the `fhasixuitheme` source in `souravkeda-web/Fhasix-client-`), not
FastClient. The earlier FastClient mapping was wrong and has been removed.

## Source evidence used

- Video 2 transcript/description: "mod menu with 100 settings",
  performance settings, a heat-reduction setting for mobile, a custom UI
  with a switch back to normal Minecraft UI, "One click server joiner
  with the best ping", cosmetics, FPS Boost & Performance Mods,
  Multiple Built-in Mods & Settings, Custom Cosmetics & UI.
- `fhasixuitheme` source: title screen (Singleplayer, Multiplayer,
  Partners, Cosmetics, Store, Quit Game, "Switch to FHASIX", Account
  pill, 6 icon buttons), Cosmetics/Store categories **Capes, Wings,
  Banners, Emotes, Hats, Glasses, Masks, Shoes, Particles, Characters**,
  two real capes (First Optix (Collab), Red Charms - unlocked in the
  Store with a 4-digit code), custom loading image, global custom font.
  Every non-Capes category in FHASIX itself renders
  `"<name> are not available"` (ComingSoonScreen) and Partners/Store/
  Account/icon buttons were placeholders ("do anything on click yet").

## What was added here

### Only shows in the mod menu - not implemented (mirrored honestly)

These FHASIX entries appear as cards in TurtleClient's mod menu, are
labelled **"Not implemented: this FHASIX feature only shows in the mod
menu."**, have no settings, and can never be toggled on - exactly like
FHASIX's own "are not available" screens, and never presented as
working features:

Wings, Banners, Emotes, Hats, Glasses, Masks, Shoes, Particles,
Characters, Best Ping Joiner, UI Switch, Heat Saver.

### Already working in TurtleClient (FHASIX equivalents)

FPS/CPS/Ping/Coordinates/Speed/Memory/Clock/Keystrokes/Armor/Potion
HUD, Zoom, Full Bright, Crosshair, Toggle Sprint, downloadable Capes
(equip in the Capes view), account switcher, HUD editor, three themes,
and the sidebar **FPS Boost** preset (Culling + Dynamic Render
Distance + Adaptive VSync).

### Kept from the previous change, reframed

Day Counter, Health Display and Tool Warning are real, working HUD
modules in the same family as FHASIX's built-in mods; they stay, with
no FastClient attribution.

## Deliberately not copied

FHASIX's bundled cape textures, logo, background, font and store code
system (TurtleClient downloads capes instead of bundling them, and a
hardcoded unlock code is not a feature), its title-screen replacement
(TurtleClient has its own), Pojav/mobile packaging (TurtleClient is a
Fabric mod jar), and any "cracked" account handling (see
`docs/ACCOUNTS.md`). Vendor FPS/heat claims are not repeated as facts.
