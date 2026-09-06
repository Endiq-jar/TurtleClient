TURTLE CLIENT COSMETICS
======================
Open Cosmetics from the title menu or Right Shift menu. Click a card to equip;
click it again to remove it. Press F5 in a world to see your outfit.

Included: Jade, Aurora and Ember variants of all six types (18 cosmetics):
cape, hat, wings, mask, shell suit and turtle hatchling pet.
These are client-side textured player render layers, not server-distributed
cosmetics or real pet entities. Other players do not see them automatically.
Hats/masks follow the head; the suit follows body/limb poses. Wings/capes and
the pet have lightweight motion. Invisible/spectator players are skipped;
capes/wings are suppressed during elytra flight and pets while sleeping.

Custom PNGs go in your Minecraft instance directory:
  custom_cosmetics/capes/
  custom_cosmetics/hats/
  custom_cosmetics/wings/
  custom_cosmetics/masks/
  custom_cosmetics/suits/
  custom_cosmetics/pets/

Use the folder button to open the selected type's directory and Reload after
adding files. Reload, folder and unequip-all also remain available in compact
windows. Selections persist in config/turtle-client/cosmetics.json.

PNG limits: 16-512 pixels in each dimension, at most 2 MiB per file. Capes use
the standard 64x32 (2:1) Minecraft cape layout, including scaled versions.
Other types use a tile/material PNG on the included low-poly mesh; they are
not custom model/skin importers. Up to 64 PNG candidates per category are
scanned, with a combined custom decoded-pixel budget of 16 MiB. Invalid or
over-budget PNGs are skipped with a message. Textures are cached and removed
textures are released. No per-frame disk reads or texture decoding.

Compilation/geometry tests do not replace checking a real third-person game
scene, slim/classic skins, armor, elytra and resource/shader-pack combinations.
