TURTLE CLIENT CAPES
===================
Open Capes from the title menu or the Right Shift menu, then press the download
button. Nothing is bundled: the client fetches

  - your own cape from your Mojang profile (session server),
  - your LabyMod cape        (https://dl.labymod.net/capes/<uuid>),
  - NameMC's public index    (https://namemc.com/capes), whose textures come from
                             https://texture.namemc.com/<aa>/<bb>/<hash>.png

Click a cape to equip it, click it again to remove it, and press F5 in a world to
see it. Capes are a client-side render layer on your own player: other players do
not see them, and no packets are sent. Invisible/spectator players are skipped and
the cape is hidden while gliding with elytra.

Cache (in your Minecraft instance directory):
  turtle-client/capes/index.json     what was downloaded, with display names
  turtle-client/capes/mojang/        your profile cape
  turtle-client/capes/labymod/       your LabyMod cape
  turtle-client/capes/namemc/        the NameMC catalogue
  turtle-client/capes/local/         PNGs you add yourself (optional)

Because the cache is on disk, the list still works with no connection after the
first download. A refresh only re-downloads what is missing: NameMC textures are
content-addressed, so a cached file is reused. The selection is stored in
config/turtle-client/capes.json.

Limits: PNG only, 2 MiB per file, header-validated without decoding, and capes
must use the standard 2:1 Minecraft cape layout between 16x8 and 1024x512 pixels.
At most 64 capes are queued from the index and at most 32 local files are read.
Downloads run on a background thread; textures are uploaded on the client thread.
If NameMC blocks the request the list still shows your own and cached capes, and
the status line says so instead of failing silently.

Compilation and parsing tests do not replace checking a real third-person scene,
slim/classic skins, armor, elytra, or resource and shader packs.
