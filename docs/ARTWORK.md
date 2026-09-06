# TurtleClient visual refresh

![TurtleClient artwork preview](turtle-ui-preview.jpg)

## Original identity

The turtle emblem and both landscape masters were generated specifically for
TurtleClient. No Lunar Client logos, branded panorama screenshots, screenshot-sized
button images, or modified Mojang/title atlases remain in the menu resources.
The vanilla title atlas overrides were removed; the client renders its own brand
through its existing loading/menu hooks instead.

- **Turtle emblem:** mint, jade, and deep-green shell; transparent PNGs at 256px
  (menu/loading), 128px (mod icon), and 64px (in-world name badge).
- **Wordmark:** clean typeset lettering, 512 × 96, with transparency. It is typeset
  during preparation using Pillow's bundled Aileron font, not generated lettering;
  no additional font binary or runtime font renderer is bundled.
- **Coast / Forest:** two original 1024 × 576 backgrounds, with no baked branding
  or interface. The renderer uses aspect-preserving cover/crop, not stretched
  square cubemap faces. Only one scene is drawn per frame.
- **Icons:** a coherent set of antialiased 32px monochrome glyphs, tinted by the UI.
  Controls no longer depend on unsupported emoji or large image-backed buttons.

## Initial UI-only optimization

`scripts/prepare_ui_assets.py` requires Python and Pillow >= 10. Pass a directory
containing `emblem-master.png`, `coast-master.png`, and `forest-master.png`. Large
masters are intentionally kept outside the repository and are not included in jars.
The runtime PNGs are the deliverables and are checked into the repository.

The script strips metadata, removes the emblem's background/matte fringe, resizes
with Lanczos filtering, uses a dithered 256-color palette for the backgrounds, and
writes maximum-compression PNGs. Minecraft texture metadata enables linear
filtering and edge clamping. The manifest at
`assets/turtle-client/ui-assets.json` records dimensions, sizes, and SHA-256 hashes.

Compared with the previous bundled PNG set:

- Compressed PNG data is approximately **two-thirds smaller**.
- Total decoded RGBA pixel storage falls from **20.3 MiB to 5.1 MiB** (about **75%
  less**). This is an image-pixel budget, not a claim about total Minecraft VRAM.
- Each background is capped at 400 KB; all new UI PNGs together stay below 900 KB.

`UiAssetsTest` checks image readability, hashes, dimensions, transparency, required
paths, filtering metadata, and these budgets in every Minecraft build target.

## Interface and input

`TurtleTheme` supplies consistent colors, rounded panels, typography fitting, and
shared artwork. Buttons use small generated nine-slice skins; sliders and toggles use lightweight
primitives. Images are cached by Minecraft's texture manager; no file decoding
occurs in render loops.

`UiGrid` reflows columns to the GUI-scaled window. `ScrollState` retains fractional
wheel movement, clamps to the actual content height, and supports dragging and
keyboard paging. Modules, cosmetics, and settings use the same scroll model.
Rendering and hit testing share geometry and the same scroll offset, so clipped
rows cannot be activated through headers or footers. Returning from settings keeps
the module list's position. The settings footer/keybind controls remain fixed.
Favorited modules remain reachable through the title menu's Favorites shortcut.
Typing in a screen no longer fires module hotkeys; held keys must be released
before they can toggle modules again after closing the screen.

The initial UI refresh only exposed registry/equip state. The extension below now
adds six actual in-world mesh types; use third-person view to inspect them. The
menu does not present a box-drawn fake player as a working 3D preview.

The included artwork preview is a design/asset preview, not an in-game screenshot.

## Generated control/cosmetic extension

The original logo and landscapes are retained. Three further generated masters
(icon atlas, blank button-state atlas, and Jade/Aurora/Ember material/crest atlas)
were sliced with `scripts/prepare_controls_cosmetics.py`.

Run the original preparation script **first**, then:

```sh
python3 scripts/prepare_controls_cosmetics.py /path/to/v2-masters
```

Expected master names: `icon-atlas-master.png`, `button-atlas-master.png`,
`cosmetic-material-master.png`. Masters are not runtime resources. The second
script replaces the earlier procedural icons; running only the first script
would revert them. Unwanted generated lettering was removed from the monitor
icon. Buttons contain no baked-in text and are rendered in nine slices. Runtime
icons are 32x32, button strips 128x32, material tiles 64x64 and capes 128x64.

The combined manifest now covers **81 PNGs / 953,966 compressed bytes**, including
18 model textures and 18 thumbnails. Including the retained 1×1 white primitive
texture, the complete runtime PNG set is **82 files / 954,035 bytes**, or
**6,168,580 bytes of base RGBA pixels** (about 5.88 MiB). World materials use nearest sampling;
interface assets use linear filtering. The manifest contains actual dimensions,
byte counts and SHA-256 hashes; tests enforce transparency where appropriate and
a 1.2 MB compressed budget. These are file/decode budgets, not measured VRAM/FPS.
The new [artwork sheet](turtle-controls-preview.jpg) is not a game screenshot.
