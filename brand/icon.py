#!/usr/bin/env python3
"""Generates the Jade Client icon: a faceted jade gemstone with a soft green
glow, flat vector style. Outputs 512x512 PNG and SVG.

Usage: python3 brand/icon.py
Requires: Pillow (pip install pillow)
"""
import math
import os

from PIL import Image, ImageDraw

SIZE = 512
OUT_DIR = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "jade-client")

# Jade palette
DARK = (14, 17, 22, 255)          # background
JADE = (0, 200, 150, 255)         # main accent
JADE_LIGHT = (127, 220, 190, 255) # facet highlight
JADE_DARK = (15, 185, 138, 255)   # facet shadow
GLOW = (0, 200, 150, 60)


def gem_points(cx, cy, rx, ry, facets=6, rot=math.pi / 6):
    pts = []
    for i in range(facets):
        a = rot + i * 2 * math.pi / facets
        pts.append((cx + rx * math.cos(a), cy + ry * math.sin(a)))
    return pts


def render_png():
    img = Image.new("RGBA", (SIZE, SIZE), DARK)
    d = ImageDraw.Draw(img)

    cx, cy = SIZE / 2, SIZE / 2 + 12
    r = SIZE * 0.34

    # Soft glow: concentric translucent gem outlines
    for i in range(24, 0, -1):
        gr = r + i * 4
        alpha = int(GLOW[3] * (1 - i / 24) ** 2)
        pts = gem_points(cx, cy, gr, gr * 0.9)
        d.polygon(pts, fill=(0, 200, 150, alpha))

    # Main gem body
    body = gem_points(cx, cy, r, r * 0.9)
    d.polygon(body, fill=JADE_DARK, outline=JADE, width=6)

    # Facets: inner hexagon rotated, connected to corners
    inner = gem_points(cx, cy, r * 0.52, r * 0.47, rot=-math.pi / 6)
    d.polygon(inner, fill=JADE)
    for i in range(6):
        d.line([body[i], inner[i]], fill=JADE_LIGHT, width=4)
    d.line([inner[i] for i in [0, 1, 2, 3, 4, 5, 0]], fill=JADE_LIGHT, width=3)

    # Top shine
    shine = gem_points(cx - r * 0.2, cy - r * 0.28, r * 0.18, r * 0.1, rot=math.pi / 5)
    d.polygon(shine, fill=(240, 255, 250, 200))

    os.makedirs(OUT_DIR, exist_ok=True)
    img.save(os.path.join(OUT_DIR, "icon.png"))
    print(f"wrote {os.path.join(OUT_DIR, 'icon.png')}")


def render_svg():
    cx, cy, r = 256, 268, 150
    lines = [
        '<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 512 512">',
        '<rect width="512" height="512" fill="#0E1116"/>',
    ]
    # glow
    lines.append(f'<polygon points="{pts_attr(gem_points(cx, cy, r + 40, (r + 40) * 0.9))}" '
                 f'fill="#00C896" opacity="0.15"/>')
    # body
    body = gem_points(cx, cy, r, r * 0.9)
    lines.append(f'<polygon points="{pts_attr(body)}" fill="#0FB98A" stroke="#00C896" stroke-width="6"/>')
    inner = gem_points(cx, cy, r * 0.52, r * 0.47, rot=-math.pi / 6)
    lines.append(f'<polygon points="{pts_attr(inner)}" fill="#00C896"/>')
    for i in range(6):
        lines.append(f'<line x1="{body[i][0]:.0f}" y1="{body[i][1]:.0f}" '
                     f'x2="{inner[i][0]:.0f}" y2="{inner[i][1]:.0f}" stroke="#7FDCBE" stroke-width="4"/>')
    lines.append(f'<polygon points="{pts_attr(inner)}" fill="none" stroke="#7FDCBE" stroke-width="3"/>')
    lines.append('</svg>')

    with open(os.path.join(os.path.dirname(__file__), "icon.svg"), "w") as f:
        f.write("\n".join(lines))
    print("wrote brand/icon.svg")


def pts_attr(pts):
    return " ".join(f"{x:.0f},{y:.0f}" for x, y in pts)


if __name__ == "__main__":
    render_png()
    render_svg()
