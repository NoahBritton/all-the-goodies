#!/usr/bin/env python3
"""
Generate a Minecraft cuboid ("crate") item model JSON without Blockbench.

A CS:GO-style crate is just a rectangular prism. This writes a self-contained
Java item model (elements + block-style display transforms) so the cache renders
as a real 3D box in the inventory/hand/world. Tweak --w/--h/--d to reshape it from
anywhere; open the result in Blockbench later to add detail (lid, latches, bevels).

  py -3.11 tools/gen_crate_model.py                 # default CS:GO-ish crate
  py -3.11 tools/gen_crate_model.py --w 14 --h 7 --d 10   # wider, flatter
  py -3.11 tools/gen_crate_model.py --w 9 --h 9 --d 9     # cube

Dimensions are in model units (16 = one full block). The box is centered.
"""
from __future__ import annotations
import argparse, json, os

# Standard vanilla "block" display transforms — make the item render as a 3D box.
DISPLAY = {
    "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.375, 0.375, 0.375]},
    "thirdperson_lefthand":  {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.375, 0.375, 0.375]},
    "firstperson_righthand": {"rotation": [0, 45, 0],  "translation": [0, 0, 0],   "scale": [0.40, 0.40, 0.40]},
    "firstperson_lefthand":  {"rotation": [0, 225, 0], "translation": [0, 0, 0],   "scale": [0.40, 0.40, 0.40]},
    "ground": {"rotation": [0, 0, 0],   "translation": [0, 3, 0], "scale": [0.25, 0.25, 0.25]},
    "gui":    {"rotation": [30, 225, 0],"translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
    "head":   {"rotation": [0, 0, 0],   "translation": [0, 0, 0], "scale": [1, 1, 1]},
    "fixed":  {"rotation": [0, 0, 0],   "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]},
}


def build(w: float, h: float, d: float, tex: str) -> dict:
    # center the box in the 16^3 model space
    fx, fy, fz = (16 - w) / 2, (16 - h) / 2, (16 - d) / 2
    tx, ty, tz = fx + w, fy + h, fz + d
    # per-face UVs sized to the face so the texture isn't stretched (uses a 16x16 sheet)
    face = lambda u0, v0, u1, v1: {"uv": [u0, v0, u1, v1], "texture": "#0"}
    faces = {
        "north": face(0, 0, w, h), "south": face(0, 0, w, h),
        "east":  face(0, 0, d, h), "west":  face(0, 0, d, h),
        "up":    face(0, 0, w, d), "down":  face(0, 0, w, d),
    }
    return {
        "comment": f"Generated crate (w x h x d = {w} x {h} x {d}). Tweak via tools/gen_crate_model.py "
                   f"or ask Claude to reshape. Open in Blockbench to add detail; keep texture id '{tex}'.",
        "gui_light": "side",
        "textures": {"0": tex, "particle": tex},
        "elements": [{"from": [fx, fy, fz], "to": [tx, ty, tz], "faces": faces}],
        "display": DISPLAY,
    }


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--w", type=float, default=13.0, help="width  (X) in model units, <=16")
    ap.add_argument("--h", type=float, default=9.0,  help="height (Y)")
    ap.add_argument("--d", type=float, default=10.0, help="depth  (Z)")
    ap.add_argument("--tex", default="allthegoodies:item/atm_cache", help="texture id")
    ap.add_argument("--out", default=os.path.join(os.path.dirname(__file__), "..", "mod", "src", "main",
                    "resources", "assets", "allthegoodies", "models", "item", "atm_cache.json"))
    a = ap.parse_args()
    for v in (a.w, a.h, a.d):
        if not (0 < v <= 16):
            raise SystemExit(f"dimensions must be in (0,16]; got {v}")
    model = build(a.w, a.h, a.d, a.tex)
    out = os.path.abspath(a.out)
    os.makedirs(os.path.dirname(out), exist_ok=True)
    with open(out, "w", encoding="utf-8") as f:
        json.dump(model, f, indent=2)
    print(f"wrote {out}\n  crate {a.w} x {a.h} x {a.d} (W x H x D), texture {a.tex}")


if __name__ == "__main__":
    main()
