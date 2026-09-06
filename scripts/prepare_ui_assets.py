#!/usr/bin/env python3
"""Prepare the original generated TurtleClient artwork. Requires Pillow >= 10.

Usage: python scripts/prepare_ui_assets.py /path/to/generated/masters
Expected inputs: emblem-master.png, coast-master.png, forest-master.png.
Only compact, stripped runtime PNGs are shipped; the large masters stay outside the jar.
"""
import hashlib
import json
import math
import sys
from pathlib import Path
from PIL import Image, ImageChops, ImageDraw, ImageFont, ImageOps, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/turtle-client"
SOURCE = Path(sys.argv[1])
manifest = []


def save(image, path, colors=None):
    dest = ASSETS / path
    dest.parent.mkdir(parents=True, exist_ok=True)
    if colors:
        # Quantize twice: Pillow applies dithering when a palette is supplied.
        # A single quantize(colors=...) call leaves visible bands in water/sky.
        palette = image.quantize(colors=colors, method=Image.Quantize.MEDIANCUT)
        image = image.quantize(palette=palette, dither=Image.Dither.FLOYDSTEINBERG)
    image.save(dest, optimize=True, compress_level=9)
    # Linear filtering avoids jagged logos and harsh scaling at non-integer GUI scales.
    dest.with_suffix(".png.mcmeta").write_text('{"texture":{"blur":true,"clamp":true}}\n')
    data = dest.read_bytes()
    manifest.append({"path": path, "width": image.width, "height": image.height,
                     "bytes": len(data), "sha256": hashlib.sha256(data).hexdigest()})


# Remove the matte before resizing; otherwise white fringes get baked into small icons.
logo = Image.open(SOURCE / "emblem-master.png").convert("RGBA")
r, g, b, _ = logo.split()
minimum = ImageChops.darker(ImageChops.darker(r, g), b)
mask = minimum.point(lambda value: 0 if value >= 230 else 255)
# Remove the thin contaminated matte edge at master resolution before downsampling.
mask = mask.filter(ImageFilter.MinFilter(11))
logo.putalpha(mask)
logo = logo.crop(mask.getbbox())


def emblem(size):
    result = Image.new("RGBA", (size, size))
    resized = ImageOps.contain(logo, (round(size * .88), round(size * .88)), Image.Resampling.LANCZOS)
    result.alpha_composite(resized, ((size - resized.width) // 2, (size - resized.height) // 2))
    return result


save(emblem(256), "textures/gui/branding/turtle.png")
save(emblem(64), "textures/gui/branding/badge.png")
save(emblem(128), "icon.png")

# Typeset, not AI-generated lettering. Pillow's bundled Aileron font is used only
# during image production; no font binary or runtime font renderer is bundled.
wordmark = Image.new("RGBA", (512, 96))
draw = ImageDraw.Draw(wordmark)
font = ImageFont.load_default(size=65)
left = (512 - draw.textlength("TURTLE CLIENT", font=font)) / 2
bbox = draw.textbbox((0, 0), "TURTLE CLIENT", font=font, stroke_width=1)
y = (96 - (bbox[3] - bbox[1])) / 2 - bbox[1]
draw.text((left, y), "TURTLE", font=font, fill="#F0F7F4", stroke_width=1)
left += draw.textlength("TURTLE ", font=font)
draw.text((left, y), "CLIENT", font=font, fill="#86E8BC", stroke_width=1)
save(wordmark, "textures/gui/branding/wordmark.png")

for scene in ("coast", "forest"):
    image = ImageOps.fit(Image.open(SOURCE / f"{scene}-master.png").convert("RGB"), (1024, 576), Image.Resampling.LANCZOS)
    save(image, f"textures/gui/backgrounds/{scene}.png", colors=256)


# A consistent 32px monochrome icon family, antialiased at build time and tinted
# by the GUI. No emoji/font-dependent missing glyphs and no giant screenshot buttons.
def make_icon(name):
    scale = 4
    image = Image.new("RGBA", (32 * scale, 32 * scale))
    d = ImageDraw.Draw(image)
    white = (255, 255, 255, 255)
    def points(p): return [(x * scale, y * scale) for x, y in p]
    def line(p, width=2): d.line(points(p), fill=white, width=round(width * scale), joint="curve")
    def box(p, radius=2): d.rounded_rectangle(tuple(v * scale for v in p), radius=radius * scale, outline=white, width=2 * scale)
    def ellipse(p): d.ellipse(tuple(v * scale for v in p), outline=white, width=2 * scale)
    if name == "close": line([(9,9),(23,23)]); line([(23,9),(9,23)])
    elif name == "search": ellipse((5,5,21,21)); line([(19,19),(27,27)], 2.5)
    elif name == "settings":
        teeth=[]
        for i in range(48):
            angle=i*math.tau/48; radius=12 if i%6 in (1,2,3,4) else 9
            teeth.append((16+math.cos(angle)*radius,16+math.sin(angle)*radius))
        line(teeth+[teeth[0]], 2); ellipse((12,12,20,20))
    elif name == "grid":
        for x,y in [(5,5),(19,5),(5,19),(19,19)]: box((x,y,x+8,y+8),1.5)
    elif name == "hud": box((4,6,28,24)); line([(12,28),(20,28)]); line([(16,24),(16,28)]); line([(8,18),(8,12),(14,12)])
    elif name == "pvp": line([(7,25),(25,7),(25,13)]); line([(25,7),(19,7)]); line([(6,18),(14,26)]); line([(7,7),(25,25)]); line([(6,7),(12,7)]); line([(18,26),(26,18)])
    elif name == "render": ellipse((3,9,29,23)); ellipse((12,12,20,20))
    elif name == "movement": line([(5,7),(14,16),(5,25)],2.5); line([(17,7),(26,16),(17,25)],2.5)
    elif name == "utility": line([(5,27),(19,13)],3); line([(19,13),(16,8),(18,4),(19,9),(24,12),(28,10),(26,16),(21,18),(19,13)])
    elif name == "hypixel": line([(16,3),(28,10),(28,23),(16,30),(4,23),(4,10),(16,3)]); line([(4,10),(16,17),(28,10)]); line([(16,17),(16,30)])
    elif name == "performance": box((5,19,9,27),1); box((14,12,18,27),1); box((23,5,27,27),1)
    elif name == "cape": line([(9,5),(23,5),(28,27),(16,24),(4,27),(9,5)]); line([(12,6),(13,20)]); line([(20,6),(19,20)])
    elif name == "hat": box((8,8,24,23),2); line([(3,24),(29,24)],2.5); line([(8,18),(24,18)])
    elif name == "wings": line([(15,26),(4,16),(4,5),(16,16),(28,5),(28,16),(17,26)]); line([(5,12),(13,20)]); line([(27,12),(19,20)])
    elif name == "mask": box((4,9,28,23),6); ellipse((8,13,13,17)); ellipse((19,13,24,17))
    elif name == "suit": line([(10,5),(5,10),(3,19),(9,20),(9,28),(23,28),(23,20),(29,19),(27,10),(22,5),(16,11),(10,5)]); line([(16,11),(16,25)])
    elif name == "pet":
        for p in [(4,8,9,14),(11,4,16,11),(19,5,24,12),(25,10,29,16)]: ellipse(p)
        box((10,17,25,28),6)
    elif name == "folder": line([(4,11),(4,7),(13,7),(16,11),(28,11),(26,26),(5,26),(4,11)]); line([(4,13),(28,13)])
    elif name == "camera": box((3,9,29,26),3); line([(10,9),(12,5),(21,5),(23,9)]); ellipse((11,13,21,23))
    elif name == "refresh": line([(26,11),(21,5),(12,5),(5,11),(5,20)]); line([(6,21),(12,27),(21,27),(27,21),(27,14)]); line([(20,11),(26,11),(26,5)]); line([(12,21),(6,21),(6,27)])
    return image.resize((32,32), Image.Resampling.LANCZOS)


names = ("close", "search", "settings", "grid", "hud", "pvp", "render", "movement", "utility", "hypixel", "performance",
         "cape", "hat", "wings", "mask", "suit", "pet", "folder", "camera", "refresh")
for name in names: save(make_icon(name), f"textures/gui/icons/{name}.png")

manifest.sort(key=lambda item: item["path"])
(ASSETS / "ui-assets.json").write_text(json.dumps({"schema":1, "assets":manifest}, indent=2) + "\n")
print(f"Prepared {len(manifest)} PNGs: {sum(a['bytes'] for a in manifest):,} bytes; "
      f"RGBA pixel budget: {sum(a['width']*a['height']*4 for a in manifest):,} bytes")
