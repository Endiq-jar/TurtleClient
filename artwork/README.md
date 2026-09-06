# Editable Turtle artwork

These are code-authored vector sources for the current interface. They replace the
previous generated masters; every shipped design is reproducible from this tree.
They are not runtime Minecraft resources and are not packed into game jars.

- `branding/`: flat turtle mark and outlined wordmark.
- `icons/`: shared controls/categories, on a 24-unit grid.
- `modules/`: one 24-unit pictogram for every registered built-in module.
- `modules.json`: stable class/key/label catalog; keep it in sync with `ModuleIcons.kt`.
- `buttons/`: text-free, flat nine-slice skins; no baked labels/shadows.
- `backgrounds/`: flat coast/forest compositions.
- `loading/`: contour motif, outlined captions and fixed-cell percentage digits.
- `cosmetics/`: pixel-aligned materials, conventional cape UVs and type/color previews.

Shapes follow the repository license. Outlined DejaVu lettering retains its notice
in `licenses/DejaVu.txt`; no font binary is bundled. This folder describes an
editable vector design workflow, not human authorship or an image-model output.

From the repository root:

```sh
python3 -m pip install -r scripts/artwork-requirements.txt
python3 scripts/prepare_vector_art.py
python3 scripts/prepare_vector_art.py --check
```

Use pinned exporter dependencies for byte-identical PNGs. To add a built-in module,
add its SVG/catalog/key mapping together: the tests inspect real module-registration
bytecode and fail if any built-in falls back to a generic category icon.

See [the visual specification](../docs/ARTWORK.md) for sizes, budgets and runtime
validation limits.
