#!/usr/bin/env python3
"""Generate a reproducible pack of distinct TurtleClient launcher icons.

Requires the pinned artwork dependencies:
    python3 -m pip install -r scripts/artwork-requirements.txt

Example:
    python3 scripts/generate_turtle_icon_pack.py --count 2000

The original launcher PNG is included unchanged. Variant artwork is derived from
its editable turtle SVG: each file keeps the TurtleClient turtle mark while using
its own palette, backplate, shell engraving, and decorative treatment.
"""

from __future__ import annotations

import argparse
import colorsys
import hashlib
import json
import math
import random
import shutil
import tempfile
import xml.etree.ElementTree as ET
import zipfile
from io import BytesIO
from pathlib import Path

import resvg_py
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE_SVG = ROOT / "artwork/branding/turtle.svg"
SOURCE_PNG = ROOT / "src/main/resources/assets/turtle-client/icon.png"
LICENSE = ROOT / "LICENSE"
DEFAULT_OUTPUT = ROOT / "turtle-launcher-icons-2000.zip"
SVG_NS = "http://www.w3.org/2000/svg"

# Geometry is intentionally shared with the editable TurtleClient mark. These
# 8 frames and 16 shell engravings, combined with independent palettes and
# background ornaments, create a broad family rather than 2,000 recolors alone.
FRAMES = (
    '<rect x="4.5" y="4.5" width="119" height="119" rx="28"/>',
    '<circle cx="64" cy="64" r="59"/>',
    '<path d="M64 4 116 34v60l-52 30L12 94V34Z"/>',
    '<path d="M64 4 116 23v39c0 29-21 50-52 62C33 112 12 91 12 62V23Z"/>',
    '<path d="M64 4 123 64 64 124 5 64Z"/>',
    '<path d="M29 5h70q24 0 24 24v70q0 24-24 24H29Q5 123 5 99V29Q5 5 29 5Z"/>',
    '<path d="M64 5c34 0 58 22 58 54 0 30-27 58-58 64C33 117 6 89 6 59 6 27 30 5 64 5Z"/>',
    '<path d="M28 5h72l23 23v72l-23 23H28L5 100V28Z"/>',
)


def hsl_hex(hue: float, saturation: float, lightness: float) -> str:
    """Return a rounded CSS color from HSL values (hue in degrees)."""
    red, green, blue = colorsys.hls_to_rgb(
        (hue % 360.0) / 360.0,
        max(0.0, min(1.0, lightness)),
        max(0.0, min(1.0, saturation)),
    )
    return "#{:02X}{:02X}{:02X}".format(round(red * 255), round(green * 255), round(blue * 255))


def color_palette(index: int, salt: int = 0) -> dict[str, str]:
    """Create a stable, high-contrast palette unique to a variant seed."""
    rng = random.Random(0x545552544C45 + index * 104729 + salt * 130363)
    body_hue = rng.uniform(0.0, 360.0)
    body_sat = rng.uniform(0.57, 0.88)
    body_light = rng.uniform(0.50, 0.72)
    background_hue = (body_hue + rng.uniform(138.0, 222.0)) % 360.0
    background2_hue = (background_hue + rng.uniform(-44.0, 44.0)) % 360.0
    accent_hue = (body_hue + rng.uniform(65.0, 295.0)) % 360.0
    return {
        "body": hsl_hex(body_hue, body_sat, body_light),
        "bodyHighlight": hsl_hex(body_hue + rng.uniform(-12.0, 12.0), body_sat * 0.72,
                                  min(0.84, body_light + rng.uniform(0.08, 0.15))),
        "background": hsl_hex(background_hue, rng.uniform(0.22, 0.57), rng.uniform(0.10, 0.21)),
        "background2": hsl_hex(background2_hue, rng.uniform(0.20, 0.60), rng.uniform(0.12, 0.26)),
        "accent": hsl_hex(accent_hue, rng.uniform(0.60, 0.92), rng.uniform(0.58, 0.80)),
        "ink": "#13221B",
    }


def shell_engraving(kind: int, accent: str) -> str:
    """Subtle vector detail clipped to the shell interior."""
    strokes = (
        '<circle cx="12" cy="12.15" r="1.35"/>',
        '<path d="m12 9.45 2.55 2.65L12 14.75l-2.55-2.65Z"/>',
        '<path d="M9.25 10.45 12 9l2.75 1.45M9.1 13.05 12 14.7l2.9-1.65"/>',
        '<path d="M9.2 11.15q2.8-3.15 5.6 0M9.2 13.05q2.8 3.15 5.6 0"/>',
        '<circle cx="10.1" cy="11" r="0.55"/><circle cx="13.9" cy="11" r="0.55"/><circle cx="12" cy="13.55" r="0.55"/>',
        '<path d="M12 9.25v5.5M9.4 10.6l5.2 2.8M14.6 10.6l-5.2 2.8"/>',
        '<path d="M9.6 13.45q2.4-4.8 4.8 0M10.1 14.05h3.8"/>',
        '<path d="m12 9.45.55 1.9 1.9.55-1.9.55-.55 1.9-.55-1.9-1.9-.55 1.9-.55Z"/>',
        '<path d="M9.2 10.35h5.6M9.2 12h5.6M9.2 13.65h5.6"/>',
        '<path d="m10 9.6 4 4.8m0-4.8-4 4.8"/>',
        '<path d="M10 10.05h4v3.9h-4z"/>',
        '<path d="M9.2 11.1q2.8-2.8 5.6 0-2.8 2.8-5.6 0Zm2.8-1.4v2.8"/>',
        '<path d="M9.4 9.8 12 11.1l2.6-1.3M9.4 14.2 12 12.9l2.6 1.3"/>',
        '<path d="M10 9.6q4 2.4 0 4.8m4-4.8q-4 2.4 0 4.8"/>',
        '<path d="M9.45 12h5.1M12 9.45v5.1"/><circle cx="12" cy="12" r="2.5"/>',
        '<path d="m12 9.1 2.7 1.55v2.7L12 14.9l-2.7-1.55v-2.7Z"/>',
    )
    return (
        '<g clip-path="url(#shellClip)" fill="none" stroke="{accent}" '
        'stroke-width="0.43" stroke-linecap="round" stroke-linejoin="round" opacity="0.74">'
        '{detail}</g>'
    ).format(accent=accent, detail=strokes[kind % len(strokes)])


def sparkle(x: float, y: float, radius: float) -> str:
    return (
        f'<path d="M{x:.2f} {y-radius:.2f} '
        f'Q{x+radius*.18:.2f} {y-radius*.18:.2f} {x+radius:.2f} {y:.2f} '
        f'Q{x+radius*.18:.2f} {y+radius*.18:.2f} {x:.2f} {y+radius:.2f} '
        f'Q{x-radius*.18:.2f} {y+radius*.18:.2f} {x-radius:.2f} {y:.2f} '
        f'Q{x-radius*.18:.2f} {y-radius*.18:.2f} {x:.2f} {y-radius:.2f}Z"/>'
    )


def background_motif(kind: int, accent: str, index: int) -> str:
    """Generate quiet corner/halo details that sit behind the turtle mark."""
    rng = random.Random(0x4D4F544946 + index * 8191)
    if kind == 0:
        shapes = "".join(sparkle(x, y, r) for x, y, r in (
            (19, 24, 3.1), (109, 26, 2.4), (19, 101, 2.5), (108, 102, 3.0)))
        return f'<g fill="{accent}" opacity="0.58">{shapes}</g>'
    if kind == 1:
        return (f'<g fill="none" stroke="{accent}" stroke-width="1.4" opacity="0.36">'
                '<circle cx="64" cy="64" r="49" stroke-dasharray="2 5"/>'
                '<circle cx="64" cy="64" r="55" stroke-dasharray="13 12"/></g>')
    if kind == 2:
        dots = "".join(
            f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{rng.uniform(1.0, 2.2):.1f}"/>'
            for x, y in ((18, 43), (110, 43), (16, 80), (112, 81), (38, 14), (90, 114)))
        return f'<g fill="{accent}" opacity="0.58">{dots}</g>'
    if kind == 3:
        return (f'<g fill="none" stroke="{accent}" stroke-width="1.6" '
                'stroke-linecap="round" opacity="0.30">'
                '<path d="M15 43a52 52 0 0 1 24-28M113 84a52 52 0 0 1-24 29"/>'
                '<path d="M16 84a52 52 0 0 0 20 25M112 43a52 52 0 0 0-19-27"/></g>')
    if kind == 4:
        return (f'<g fill="none" stroke="{accent}" stroke-width="1.5" '
                'stroke-linecap="round" opacity="0.34">'
                '<path d="M12 31q10-8 20 0t20 0M76 98q10-8 20 0t20 0"/>'
                '<path d="M14 37q8-6 16 0t16 0M82 104q8-6 16 0t16 0"/></g>')
    if kind == 5:
        rays = []
        for angle in range(0, 360, 30):
            a = math.radians(angle + index % 30)
            x1, y1 = 64 + 48 * math.cos(a), 64 + 48 * math.sin(a)
            x2, y2 = 64 + 56 * math.cos(a), 64 + 56 * math.sin(a)
            rays.append(f'<path d="M{x1:.1f} {y1:.1f}L{x2:.1f} {y2:.1f}"/>')
        return (f'<g fill="none" stroke="{accent}" stroke-width="1.7" '
                f'stroke-linecap="round" opacity="0.5">{"".join(rays)}</g>')
    if kind == 6:
        squares = "".join(
            f'<rect x="{x}" y="{y}" width="{size}" height="{size}" rx="1"/>'
            for x, y, size in ((14, 17, 4), (23, 12, 2), (110, 17, 4), (103, 11, 2),
                               (13, 105, 3), (21, 111, 2), (109, 105, 3), (101, 111, 2)))
        return f'<g fill="{accent}" opacity="0.50">{squares}</g>'
    if kind == 7:
        return (f'<g fill="none" stroke="{accent}" stroke-width="1.3" opacity="0.32">'
                '<ellipse cx="64" cy="64" rx="51" ry="22" transform="rotate(-34 64 64)"/>'
                '<ellipse cx="64" cy="64" rx="51" ry="22" transform="rotate(34 64 64)"/></g>')
    if kind == 8:
        marks = "".join(sparkle(x, y, 2.2) for x, y in (
            (16, 61), (112, 64), (34, 15), (94, 113), (29, 108), (101, 20)))
        return f'<g fill="{accent}" opacity="0.52">{marks}</g>'
    if kind == 9:
        return (f'<g fill="none" stroke="{accent}" stroke-width="1.4" '
                'stroke-linecap="round" opacity="0.34">'
                '<path d="M17 20h11v7h7M111 20h-11v7h-7M17 108h11v-7h7M111 108h-11v-7h-7"/>'
                '<circle cx="17" cy="20" r="2" fill="{accent}"/>'
                '<circle cx="111" cy="20" r="2" fill="{accent}"/>'
                '<circle cx="17" cy="108" r="2" fill="{accent}"/>'
                '<circle cx="111" cy="108" r="2" fill="{accent}"/></g>').format(accent=accent)
    if kind == 10:
        return (f'<g fill="none" stroke="{accent}" stroke-width="1.2" opacity="0.33">'
                '<path d="M64 10v10M64 108v10M10 64h10M108 64h10"/>'
                '<path d="M26 26l7 7M95 95l7 7M102 26l-7 7M33 95l-7 7"/></g>')
    # Small rotating leaf-like arcs; position/order is seeded per variant.
    angle = rng.randrange(0, 360)
    return (f'<g transform="rotate({angle} 64 64)" fill="none" stroke="{accent}" '
            'stroke-width="1.4" stroke-linecap="round" opacity="0.42">'
            '<path d="M15 64q9-9 18 0-9 9-18 0ZM95 64q9-9 18 0-9 9-18 0Z"/>'
            '<path d="M24 64h9M95 64h9"/></g>')


def source_paths() -> tuple[str, str, str]:
    root = ET.fromstring(SOURCE_SVG.read_text(encoding="utf-8"))
    paths = [element.attrib["d"] for element in root if element.tag.endswith("path")]
    if len(paths) != 3:
        raise RuntimeError(f"Expected the three paths in {SOURCE_SVG}, found {len(paths)}")
    return paths[0], paths[1], paths[2]


def make_svg(index: int, paths: tuple[str, str, str], salt: int = 0) -> tuple[str, dict[str, str], dict[str, int]]:
    palette = color_palette(index, salt)
    rng = random.Random(0x5348454C4C + index * 65537 + salt * 4099)
    frame_id = (index + salt) % len(FRAMES)
    shell_id = (index * 7 + index // 9 + salt) % 16
    motif_id = (index * 5 + index // 13 + salt) % 11
    body_path, shell_path, line_path = paths

    frame = FRAMES[frame_id]
    inset = 9.2 + rng.uniform(-0.45, 0.45)
    scale = (128.0 - inset * 2.0) / 24.0
    shell_pattern = shell_engraving(shell_id, palette["accent"])
    motif = background_motif(motif_id, palette["accent"], index + salt * 101)
    # Two understated border treatments cycle independently of the backplate.
    border_dash = "" if index % 3 == 0 else f' stroke-dasharray="{5 + index % 7} {3 + (index // 7) % 5}"'
    secondary_ring = ""
    if index % 4 == 0:
        secondary_ring = (
            f'<circle cx="64" cy="64" r="52" fill="none" stroke="{palette["accent"]}" '
            'stroke-width="0.75" opacity="0.34"/>'
        )
    elif index % 4 == 1:
        secondary_ring = (
            f'<circle cx="64" cy="64" r="54" fill="none" stroke="{palette["accent"]}" '
            'stroke-width="0.65" stroke-dasharray="1.5 4.5" opacity="0.42"/>'
        )

    svg = f'''<svg xmlns="{SVG_NS}" width="{256}" height="{256}" viewBox="0 0 128 128">
<defs>
  <linearGradient id="backdrop" x1="0" y1="0" x2="1" y2="1">
    <stop offset="0" stop-color="{palette['background']}"/>
    <stop offset="1" stop-color="{palette['background2']}"/>
  </linearGradient>
  <linearGradient id="turtle" x1="0" y1="0" x2="1" y2="1">
    <stop offset="0" stop-color="{palette['bodyHighlight']}"/>
    <stop offset="1" stop-color="{palette['body']}"/>
  </linearGradient>
  <linearGradient id="gloss" x1="0" y1="0" x2="0" y2="1">
    <stop offset="0" stop-color="#FFFFFF" stop-opacity="0.13"/>
    <stop offset="0.52" stop-color="#FFFFFF" stop-opacity="0"/>
  </linearGradient>
  <clipPath id="shellClip"><path d="M12 8.1 16 10.3v4.1l-4 2.3-4-2.3v-4.1Z"/></clipPath>
</defs>
<g>
  <g fill="url(#backdrop)" stroke="{palette['accent']}" stroke-width="1.6" stroke-opacity="0.72"{border_dash}>
    {frame}
  </g>
  <g fill="url(#gloss)" stroke="none" opacity="0.9">{frame}</g>
  {secondary_ring}
  {motif}
  <g transform="translate({inset:.3f} {inset:.3f}) scale({scale:.5f})">
    <path d="{body_path}" fill="url(#turtle)"/>
    <path d="{shell_path}" fill="url(#turtle)"/>
    {shell_pattern}
    <path d="{line_path}" fill="none" stroke="{palette['ink']}" stroke-width="1.25" stroke-linejoin="round"/>
  </g>
</g>
</svg>'''
    return svg, palette, {"frame": frame_id, "shell": shell_id, "motif": motif_id}


def render_png(svg: str, size: int) -> tuple[bytes, bytes]:
    rendered = resvg_py.svg_to_bytes(svg_string=svg, width=size, height=size, skip_system_fonts=True)
    image = Image.open(BytesIO(rendered)).convert("RGBA")
    pixels = image.tobytes()
    output = BytesIO()
    image.save(output, format="PNG", optimize=True, compress_level=9)
    return output.getvalue(), pixels


def write_gallery(destination: Path, count: int, size: int) -> None:
    cards = [
        '<article><img loading="lazy" src="original/turtle-client-icon-original.png" alt="Original TurtleClient icon">'
        '<div>Original</div></article>'
    ]
    cards.extend(
        f'<article><img loading="lazy" src="png/variant-{number:04d}.png" alt="Turtle icon variant {number}">'
        f'<div>Variant {number:04d}</div></article>'
        for number in range(1, count + 1)
    )
    page = '''<!doctype html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>TurtleClient icon variants</title><style>
:root{color-scheme:dark}body{margin:0;padding:24px;background:#101715;color:#e6eee8;font:14px system-ui,sans-serif}
h1{font-size:24px;margin:0 0 6px}.note{color:#aebeb5;margin:0 0 20px}.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(126px,1fr));gap:12px}
article{background:#1a2420;border:1px solid #2b3a32;border-radius:12px;padding:8px;text-align:center}img{display:block;width:100%;aspect-ratio:1;object-fit:contain;border-radius:8px;background:#0d1210}article div{padding:7px 0 1px;color:#cbd8d0;font-size:12px}
</style></head><body><h1>TurtleClient launcher icons</h1>
<p class="note">Original icon plus {count} distinct variants. PNGs are {size}×{size}; matching editable SVGs are in the <code>svg/</code> folder. Open this page from the extracted ZIP.</p>
<div class="grid">{cards}</div></body></html>
'''
    page = (page.replace("{count}", str(count)).replace("{size}", str(size))
            .replace("{cards}", "\n".join(cards)))
    (destination / "gallery.html").write_text(page, encoding="utf-8")


def create_pack(output: Path, count: int, size: int) -> tuple[int, int]:
    paths = source_paths()
    output.parent.mkdir(parents=True, exist_ok=True)
    png_hashes: set[str] = set()
    pixel_hashes: set[str] = set()
    records: list[dict[str, object]] = []

    with tempfile.TemporaryDirectory(prefix="turtle-icon-pack-") as temporary:
        stage = Path(temporary) / "turtle-launcher-icons"
        (stage / "png").mkdir(parents=True)
        (stage / "svg").mkdir(parents=True)
        (stage / "original").mkdir(parents=True)
        shutil.copyfile(SOURCE_PNG, stage / "original/turtle-client-icon-original.png")
        shutil.copyfile(SOURCE_SVG, stage / "original/turtle.svg")
        shutil.copyfile(LICENSE, stage / "LICENSE.txt")

        for number in range(1, count + 1):
            variant_index = number - 1
            salt = 0
            while True:
                svg, palette, styles = make_svg(variant_index, paths, salt)
                png, pixel_bytes = render_png(svg, size)
                file_sha = hashlib.sha256(png).hexdigest()
                pixel_sha = hashlib.sha256(pixel_bytes).hexdigest()
                if file_sha not in png_hashes and pixel_sha not in pixel_hashes:
                    break
                salt += 1
                if salt > 100:
                    raise RuntimeError(f"Could not produce unique pixels for variant {number}")

            png_hashes.add(file_sha)
            pixel_hashes.add(pixel_sha)
            filename = f"variant-{number:04d}"
            (stage / "png" / f"{filename}.png").write_bytes(png)
            (stage / "svg" / f"{filename}.svg").write_text(svg + "\n", encoding="utf-8")
            records.append({
                "id": number,
                "png": f"png/{filename}.png",
                "svg": f"svg/{filename}.svg",
                "sha256": file_sha,
                "pixelSha256": pixel_sha,
                "palette": palette,
                "style": styles,
            })
            if number % 250 == 0 or number == count:
                print(f"Rendered {number:,}/{count:,} distinct variants")

        original_bytes = (stage / "original/turtle-client-icon-original.png").read_bytes()
        original_image = Image.open(BytesIO(original_bytes)).convert("RGBA")
        original_sha = hashlib.sha256(original_bytes).hexdigest()
        original_pixel_sha = hashlib.sha256(original_image.tobytes()).hexdigest()
        if original_sha in png_hashes or original_pixel_sha in pixel_hashes:
            raise RuntimeError("A variant unexpectedly duplicates the original icon")

        manifest = {
            "name": "TurtleClient launcher icon variants",
            "variantCount": count,
            "originalIncluded": True,
            "variantCanvas": {"width": size, "height": size, "format": "PNG + editable SVG"},
            "original": {
                "png": "original/turtle-client-icon-original.png",
                "svg": "original/turtle.svg",
                "pngSha256": original_sha,
                "pixelSha256": original_pixel_sha,
                "dimensions": list(original_image.size),
            },
            "uniqueness": {
                "pngFileHashesUnique": len(png_hashes) == count,
                "decodedPixelHashesUnique": len(pixel_hashes) == count,
                "variantPngHashes": len(png_hashes),
                "variantDecodedPixelHashes": len(pixel_hashes),
            },
            "variants": records,
        }
        (stage / "manifest.json").write_text(
            json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8")
        write_gallery(stage, count, size)
        (stage / "README.txt").write_text(
            "TurtleClient launcher icon pack\n"
            "================================\n\n"
            f"Contents: {count:,} new, individually rendered icon variants plus the original launcher icon.\n"
            f"Variant PNG canvas: {size} x {size} pixels; a matching editable SVG is included for each variant.\n\n"
            "The variants preserve the TurtleClient turtle mark from the included original SVG and\n"
            "vary the palette, backplate, shell engraving and background ornament. The manifest\n"
            "records per-file SHA-256 hashes and confirms every variant has unique decoded pixels.\n"
            "Open gallery.html after extracting the ZIP to browse the set offline.\n\n"
            "The original PNG is copied unchanged from the TurtleClient repository. The included\n"
            "LICENSE.txt is the repository's CC0 1.0 license.\n",
            encoding="utf-8",
        )

        temporary_zip = output.with_suffix(output.suffix + ".tmp")
        try:
            with zipfile.ZipFile(temporary_zip, "w", compression=zipfile.ZIP_DEFLATED,
                                 compresslevel=9, strict_timestamps=False) as archive:
                for file_path in sorted(stage.rglob("*")):
                    if file_path.is_file():
                        archive.write(file_path, file_path.relative_to(stage.parent))
            temporary_zip.replace(output)
        finally:
            temporary_zip.unlink(missing_ok=True)

    return len(png_hashes), output.stat().st_size


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--count", type=int, default=2000,
                        help="number of new variants to create (default: 2000)")
    parser.add_argument("--size", type=int, default=256,
                        help="PNG side length in pixels (default: 256)")
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT,
                        help=f"ZIP destination (default: {DEFAULT_OUTPUT.relative_to(ROOT)})")
    args = parser.parse_args()
    if args.count < 1:
        parser.error("--count must be at least 1")
    if args.size < 64 or args.size > 2048:
        parser.error("--size must be between 64 and 2048")
    output = args.output if args.output.is_absolute() else ROOT / args.output
    unique_count, zip_bytes = create_pack(output, args.count, args.size)
    print(f"Created {output}")
    print(f"{unique_count:,} variants + original; all {unique_count:,} variant pixel hashes are unique")
    print(f"ZIP size: {zip_bytes:,} bytes")


if __name__ == "__main__":
    main()
