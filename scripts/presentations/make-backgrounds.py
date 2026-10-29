"""Renders the deck backgrounds (black to electric-blue gradient) into ./assets.

Run: python scripts/presentations/make-backgrounds.py   (needs numpy and Pillow)
"""
import os
import numpy as np
from PIL import Image

W, H = 1920, 1080
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'assets')
os.makedirs(OUT, exist_ok=True)

BLUE = np.array([38, 58, 255], dtype=float)
VIOLET = np.array([96, 44, 235], dtype=float)
BLACK = np.array([5, 6, 12], dtype=float)


def glow(h, w, cx, cy, r, color, power=1.6):
    y, x = np.mgrid[0:h, 0:w]
    d = np.sqrt(((x - cx * w) / (r * w)) ** 2 + ((y - cy * h) / (r * w)) ** 2)
    a = np.clip(1 - d, 0, 1) ** power
    return a[..., None] * color


def gradient(h, w):
    img = np.tile(BLACK, (h, w, 1))
    img += glow(h, w, 0.92, 1.0, 0.85, BLUE, 1.5)
    img += glow(h, w, 1.0, 0.1, 0.5, VIOLET, 1.8)
    img += glow(h, w, 0.55, 1.15, 0.45, BLUE * 0.5, 1.8)
    return np.clip(img, 0, 255)


def save(arr, name):
    Image.fromarray(arr.astype('uint8')).save(os.path.join(OUT, name))


full = gradient(H, W)
save(full, 'dark.png')

# White upper part with a gradient band below (content slides)
split_y = int(H * 3.05 / 5.625)
band = gradient(H - split_y, W)
split = np.full((H, W, 3), 255.0)
split[split_y:] = band
save(split, 'split.png')
print('backgrounds written to', OUT)
