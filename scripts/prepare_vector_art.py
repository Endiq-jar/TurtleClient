#!/usr/bin/env python3
"""Rasterize the editable, code-authored Turtle artwork. No image-model masters needed.

pip install -r scripts/artwork-requirements.txt
python3 scripts/prepare_vector_art.py
python3 scripts/prepare_vector_art.py --check

SVG sources, including outlined lettering, live in artwork/. Nothing in this
script runs in Minecraft. Only small PNGs, filtering metadata and manifests ship.
"""
from pathlib import Path
from io import BytesIO
import argparse
import hashlib
import json
import tempfile

import resvg_py
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "artwork"
ASSETS = ROOT / "src/main/resources/assets/turtle-client"


def produce(destination):
    manifest = []

    def image(source, target, size, blur=True, palette=False):
        svg = (SOURCE / source).read_bytes()
        # Supersample small line icons, then strip all PNG metadata.
        samples = 3 if source.startswith(("icons/", "modules/", "buttons/")) else 1
        png = resvg_py.svg_to_bytes(svg_string=svg.decode("utf-8"), width=size[0] * samples,
                                        height=size[1] * samples, skip_system_fonts=True)
        im = Image.open(BytesIO(png)).convert("RGBA")
        if samples > 1:
            im = im.resize(size, Image.Resampling.LANCZOS)
        # A tiny resampling fringe must not become an opaque legacy font/GUI pixel.
        im.putalpha(im.getchannel("A").point(lambda alpha: 0 if alpha < 4 else alpha))
        if palette:
            im = im.convert("RGB").quantize(colors=128, dither=Image.Dither.NONE)
        out = destination / target
        out.parent.mkdir(parents=True, exist_ok=True)
        im.save(out, optimize=True, compress_level=9)
        out.with_suffix(".png.mcmeta").write_text(json.dumps(
            {"texture": {"blur": blur, "clamp": True}}, separators=(",", ":")) + "\n")
        data = out.read_bytes()
        manifest.append({"path": target, "width": size[0], "height": size[1], "bytes": len(data),
                         "sha256": hashlib.sha256(data).hexdigest(),
                         "source": "artwork/" + source,
                         "sourceSha256": hashlib.sha256(svg).hexdigest()})

    for name, size in (("turtle", 256), ("badge", 64)):
        image("branding/turtle.svg", f"textures/gui/branding/{name}.png", (size, size))
    image("branding/turtle.svg", "icon.png", (128, 128))
    image("branding/wordmark.svg", "textures/gui/branding/wordmark.png", (512, 96))
    for directory in ("icons", "modules"):
        for path in sorted((SOURCE / directory).glob("*.svg")):
            image(f"{directory}/{path.name}", f"textures/gui/{directory}/{path.stem}.png", (32, 32))
    for path in sorted((SOURCE / "buttons").glob("*.svg")):
        image(f"buttons/{path.name}", f"textures/gui/buttons/{path.stem}.png",
              (64, 64) if path.stem == "panel" else (128, 32))
    for name in ("coast", "forest"):
        image(f"backgrounds/{name}.svg", f"textures/gui/backgrounds/{name}.png", (1024, 576), palette=True)
    sizes = {"resources": (224, 24), "finishing": (252, 24), "numbers": (176, 24),
             "edition": (192, 20), "startup": (200, 20), "contours": (512, 288)}
    for name, size in sizes.items():
        image(f"loading/{name}.svg", f"textures/gui/loading/{name}.png", size)
    for path in sorted((SOURCE / "cosmetics").rglob("*.svg")):
        relative = path.relative_to(SOURCE)
        preview = "previews" in path.parts
        size = (128, 64) if path.parent.name == "cape" else (64, 64)
        image(relative.as_posix(), "textures/" + relative.with_suffix(".png").as_posix(), size, blur=preview)

    manifest.sort(key=lambda entry: entry["path"])
    (destination / "ui-assets.json").write_text(json.dumps({"schema": 2, "assets": manifest}, indent=2) + "\n")
    modules = json.loads((SOURCE / "modules.json").read_text())["modules"]
    (destination / "module-icons.json").write_text(json.dumps({"schema": 1, "modules": [
        {**entry, "texture": f'textures/gui/modules/{entry["key"]}.png'} for entry in modules
    ]}, indent=2) + "\n")
    return manifest


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="Rebuild to a temporary directory and compare committed output")
    args = parser.parse_args()
    if args.check:
        with tempfile.TemporaryDirectory(prefix="turtle-art-") as directory:
            tmp = Path(directory)
            manifest = produce(tmp)
            failures = [str(p.relative_to(tmp)) for p in tmp.rglob("*") if p.is_file()
                        and (not (ASSETS / p.relative_to(tmp)).is_file()
                             or p.read_bytes() != (ASSETS / p.relative_to(tmp)).read_bytes())]
            if failures:
                raise SystemExit("Stale/missing artwork:\n" + "\n".join(failures))
            print("SVG sources and all committed runtime output match.")
    else:
        manifest = produce(ASSETS)
    total = sum(entry["bytes"] for entry in manifest)
    rgba = sum(entry["width"] * entry["height"] * 4 for entry in manifest)
    print(f"{len(manifest)} prepared PNGs; {total:,} compressed bytes; {rgba:,} base RGBA bytes.")
    if total >= 250_000:
        raise SystemExit("Artwork exceeds the enforced 250 KB compressed budget.")


if __name__ == "__main__":
    main()
