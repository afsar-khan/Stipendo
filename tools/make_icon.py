"""Stipendo launcher icon v2: a graduation cap whose tassel is a gold coin with a square hole
(the classic Chinese cash coin), on a deep jade tile. One spec -> Android vector drawables
(adaptive icon, themed icon, notification icon) AND bitmaps for older phones, so they always match."""
import math, numpy as np
from PIL import Image, ImageDraw
from pathlib import Path

OUT = Path("res")
for d in ["drawable","mipmap-mdpi","mipmap-hdpi","mipmap-xhdpi","mipmap-xxhdpi","mipmap-xxxhdpi","mipmap-anydpi-v26"]:
    (OUT/d).mkdir(parents=True, exist_ok=True)

# ---------------- design spec (108 x 108 canvas, safe zone = circle of radius 33 around 54,54) ----------------
BG0, BG1 = "#1F9179", "#0A4237"        # jade tile
IV0, IV1 = "#FFFCF3", "#EDE5D1"         # cap top
BASE0, BASE1 = "#E2DAC6", "#BFB59B"     # cap band (darker, in shade)
GOLD0, GOLD1 = "#FBDA72", "#D8992A"     # coin
DARK = "#0B4A3E"                        # hole / button
SHADOW = "#031F19"
TOP, RIGHT, BOTTOM, LEFT = (54, 34), (78, 45), (54, 56), (30, 45)
DIAMOND = [TOP, RIGHT, BOTTOM, LEFT]
BAND_X0, BAND_X1, BAND_Y_TOP, BAND_Y_BOT, BAND_CTRL = 39, 69, 49.1, 62, 73
BUTTON, BUTTON_R = (54, 45), 2.6
COIN, COIN_R, HOLE = (78, 66), 5.4, 3.4
CORD_W = 2.0

def a(al): return f"{int(round(al*255)):02X}"
def circle_path(cx, cy, r): return f"M{cx-r},{cy} a{r},{r} 0 1,0 {2*r},0 a{r},{r} 0 1,0 {-2*r},0z"
def diamond_path(dx=0, dy=0): return "M" + " L".join(f"{x+dx},{y+dy}" for x, y in DIAMOND) + " Z"
def band_path(dx=0, dy=0):
    return (f"M{BAND_X0+dx},{BAND_Y_TOP+dy} V{BAND_Y_BOT+dy} Q54{'' if not dx else ''},{BAND_CTRL+dy} {BAND_X1+dx},{BAND_Y_BOT+dy} "
            f"V{BAND_Y_TOP+dy} L{54+dx},{BOTTOM[1]+dy} Z").replace("Q54,", f"Q{54+dx},")
hole_x, hole_y = COIN[0]-HOLE/2, COIN[1]-HOLE/2
hole_path = f"M{hole_x},{hole_y} h{HOLE} v{HOLE} h{-HOLE} z"
cord_path = f"M{BUTTON[0]},{BUTTON[1]} H{RIGHT[0]} V{COIN[1]-COIN_R+0.6}"
shadow_offsets = [(0, 1.4, .10), (0, 2.8, .09), (0, 4.2, .07)]

# safe-zone check
pts = [TOP, RIGHT, BOTTOM, LEFT, (BAND_X0, BAND_Y_BOT), (BAND_X1, BAND_Y_BOT), (54, 68.5)]
far = max(math.hypot(x-54, y-54) for x, y in pts)
far = max(far, math.hypot(COIN[0]-54, COIN[1]-54) + COIN_R)
print(f"furthest point from centre: {far:.1f} (limit 33)")
assert far <= 33.5

# ---------------- Android vector XML ----------------
NS = 'xmlns:android="http://schemas.android.com/apk/res/android" xmlns:aapt="http://schemas.android.com/aapt"'
def vec(body, w=108, h=108, vw=108, vh=108, dp=108):
    return (f'<?xml version="1.0" encoding="utf-8"?>\n<vector {NS}\n    android:width="{dp}dp"\n    android:height="{dp*h//w}dp"\n'
            f'    android:viewportWidth="{vw}"\n    android:viewportHeight="{vh}">\n{body}</vector>\n')
def grad(c0, c1, x0, y0, x1, y1):
    return (f'        <aapt:attr name="android:fillColor">\n            <gradient android:startX="{x0}" android:startY="{y0}" android:endX="{x1}" android:endY="{y1}" android:type="linear">\n'
            f'                <item android:offset="0" android:color="#FF{c0[1:]}"/>\n                <item android:offset="1" android:color="#FF{c1[1:]}"/>\n            </gradient>\n        </aapt:attr>\n')

(OUT/"drawable/ic_launcher_background.xml").write_text(vec(
    '    <path android:pathData="M0,0h108v108h-108z">\n' + grad(BG0, BG1, 0, 0, 108, 108) + '    </path>\n'
    f'    <path android:pathData="{circle_path(38,34,52)}" android:fillColor="#14FFFFFF"/>\n'))

fg = ""
for dx, dy, al in shadow_offsets:
    fg += f'    <path android:pathData="{band_path(dx,dy)}" android:fillColor="#{a(al)}{SHADOW[1:]}"/>\n'
    fg += f'    <path android:pathData="{diamond_path(dx,dy)}" android:fillColor="#{a(al)}{SHADOW[1:]}"/>\n'
fg += f'    <path android:pathData="{band_path()}">\n' + grad(BASE0, BASE1, 54, 50, 54, 70) + '    </path>\n'
fg += f'    <path android:pathData="{diamond_path()}">\n' + grad(IV0, IV1, 54, 34, 54, 56) + '    </path>\n'
fg += f'    <path android:pathData="M{LEFT[0]+1.2},{LEFT[1]} L{TOP[0]},{TOP[1]+0.6} L{RIGHT[0]-1.2},{RIGHT[1]}" android:strokeColor="#99FFFFFF" android:strokeWidth="0.9" android:strokeLineJoin="round" android:strokeLineCap="round"/>\n'
fg += f'    <path android:pathData="{cord_path}" android:strokeColor="#FF{GOLD1[1:]}" android:strokeWidth="{CORD_W}" android:strokeLineCap="round" android:strokeLineJoin="round"/>\n'
fg += f'    <path android:pathData="{circle_path(*BUTTON, BUTTON_R)}">\n' + grad(GOLD0, GOLD1, BUTTON[0]-2, BUTTON[1]-2, BUTTON[0]+2, BUTTON[1]+2) + '    </path>\n'
fg += f'    <path android:pathData="{circle_path(COIN[0], COIN[1]+1.2, COIN_R)}" android:fillColor="#4D{SHADOW[1:]}"/>\n'
fg += f'    <path android:pathData="{circle_path(*COIN, COIN_R)}">\n' + grad(GOLD0, GOLD1, COIN[0]-COIN_R, COIN[1]-COIN_R, COIN[0]+COIN_R, COIN[1]+COIN_R) + '    </path>\n'
fg += f'    <path android:pathData="{circle_path(*COIN, COIN_R-1.1)}" android:strokeColor="#66FFFFFF" android:strokeWidth="0.6"/>\n'
fg += f'    <path android:pathData="{hole_path}" android:fillColor="#FF{DARK[1:]}"/>\n'
(OUT/"drawable/ic_launcher_foreground.xml").write_text(vec(fg))

mono = (f'    <path android:pathData="{diamond_path()}" android:fillColor="#FF000000"/>\n'
        f'    <path android:pathData="{band_path()}" android:fillColor="#FF000000"/>\n'
        f'    <path android:pathData="{cord_path}" android:strokeColor="#FF000000" android:strokeWidth="{CORD_W}" android:strokeLineCap="round"/>\n'
        f'    <path android:pathData="{circle_path(*COIN, COIN_R)} {hole_path}" android:fillColor="#FF000000" android:fillType="evenOdd"/>\n')
(OUT/"drawable/ic_launcher_monochrome.xml").write_text(vec(mono))

# notification icon: same silhouette cropped to its own box (viewport 56 x 56, shown at 24dp)
OFF = 26
def shift(p): return p
stat = (f'    <group android:translateX="{-OFF}" android:translateY="{-OFF+1}">\n'
        f'        <path android:pathData="{diamond_path()}" android:fillColor="#FFFFFFFF"/>\n'
        f'        <path android:pathData="{band_path()}" android:fillColor="#FFFFFFFF"/>\n'
        f'        <path android:pathData="{cord_path}" android:strokeColor="#FFFFFFFF" android:strokeWidth="{CORD_W+0.6}" android:strokeLineCap="round"/>\n'
        f'        <path android:pathData="{circle_path(*COIN, COIN_R+0.6)} {hole_path}" android:fillColor="#FFFFFFFF" android:fillType="evenOdd"/>\n'
        f'    </group>\n')
(OUT/"drawable/ic_stat_stipendo.xml").write_text(vec(stat, w=56, h=56, vw=56, vh=56, dp=24))

adaptive = ('<?xml version="1.0" encoding="utf-8"?>\n<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
            '    <background android:drawable="@drawable/ic_launcher_background" />\n'
            '    <foreground android:drawable="@drawable/ic_launcher_foreground" />\n'
            '    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />\n</adaptive-icon>\n')
(OUT/"mipmap-anydpi-v26/ic_launcher.xml").write_text(adaptive)
(OUT/"mipmap-anydpi-v26/ic_launcher_round.xml").write_text(adaptive)

# ---------------- bitmaps ----------------
S = 8; W = 108*S
def rgb(h): h=h.lstrip("#"); return tuple(int(h[i:i+2],16) for i in (0,2,4))
def lin(w, h, c0, c1, x0, y0, x1, y1):
    ys, xs = np.mgrid[0:h, 0:w]
    dx, dy = x1-x0, y1-y0
    t = np.clip(((xs-x0)*dx + (ys-y0)*dy) / (dx*dx + dy*dy), 0, 1)[..., None]
    arr = (np.array(rgb(c0))*(1-t) + np.array(rgb(c1))*t).astype(np.uint8)
    return Image.fromarray(arr, "RGB").convert("RGBA")
def mask_of(draw_fn):
    m = Image.new("L", (W, W), 0); draw_fn(ImageDraw.Draw(m)); return m
def paint(img, mask, c0, c1, x0, y0, x1, y1, alpha=1.0):
    fill = lin(W, W, c0, c1, x0*S, y0*S, x1*S, y1*S)
    if alpha < 1: mask = mask.point(lambda v: int(v*alpha))
    fill.putalpha(mask); return Image.alpha_composite(img, fill)
def sc(pts): return [(x*S, y*S) for x, y in pts]
def bez(p0, p1, p2, n=40):
    return [((1-t)**2*p0[0]+2*(1-t)*t*p1[0]+t*t*p2[0], (1-t)**2*p0[1]+2*(1-t)*t*p1[1]+t*t*p2[1]) for t in [i/n for i in range(n+1)]]
def band_poly(dx=0, dy=0):
    pts = [(BAND_X0+dx, BAND_Y_TOP+dy), (BAND_X0+dx, BAND_Y_BOT+dy)] + bez((BAND_X0+dx, BAND_Y_BOT+dy), (54+dx, BAND_CTRL+dy), (BAND_X1+dx, BAND_Y_BOT+dy)) \
          + [(BAND_X1+dx, BAND_Y_TOP+dy), (54+dx, BOTTOM[1]+dy)]
    return sc(pts)
def dia_poly(dx=0, dy=0): return sc([(x+dx, y+dy) for x, y in DIAMOND])
def ell(d, c, r, fill=255): d.ellipse([(c[0]-r)*S, (c[1]-r)*S, (c[0]+r)*S, (c[1]+r)*S], fill=fill)

img = lin(W, W, BG0, BG1, 0, 0, W, W)
halo = mask_of(lambda d: ell(d, (38, 34), 52)); img = paint(img, halo, "#FFFFFF", "#FFFFFF", 0, 0, 1, 1, alpha=0.08)
for dx, dy, al in shadow_offsets:
    img = paint(img, mask_of(lambda d: d.polygon(band_poly(dx, dy), fill=255)), SHADOW, SHADOW, 0, 0, 1, 1, alpha=al)
    img = paint(img, mask_of(lambda d: d.polygon(dia_poly(dx, dy), fill=255)), SHADOW, SHADOW, 0, 0, 1, 1, alpha=al)
img = paint(img, mask_of(lambda d: d.polygon(band_poly(), fill=255)), BASE0, BASE1, 54, 50, 54, 70)
img = paint(img, mask_of(lambda d: d.polygon(dia_poly(), fill=255)), IV0, IV1, 54, 34, 54, 56)
layer = Image.new("RGBA", (W, W), (0,0,0,0)); d = ImageDraw.Draw(layer)
d.line(sc([(LEFT[0]+1.2, LEFT[1]), (TOP[0], TOP[1]+0.6), (RIGHT[0]-1.2, RIGHT[1])]), fill=(255,255,255,153), width=int(0.9*S), joint="curve")
img = Image.alpha_composite(img, layer)
cord = mask_of(lambda d: (d.line(sc([BUTTON, (RIGHT[0], BUTTON[1]), (RIGHT[0], COIN[1]-COIN_R+0.6)]), fill=255, width=int(CORD_W*S), joint="curve"),
                          ell(d, BUTTON, CORD_W/2), ell(d, (RIGHT[0], COIN[1]-COIN_R+0.6), CORD_W/2)))
img = paint(img, cord, GOLD1, GOLD1, 0, 0, 1, 1)
img = paint(img, mask_of(lambda d: ell(d, BUTTON, BUTTON_R)), GOLD0, GOLD1, BUTTON[0]-2, BUTTON[1]-2, BUTTON[0]+2, BUTTON[1]+2)
img = paint(img, mask_of(lambda d: ell(d, (COIN[0], COIN[1]+1.2), COIN_R)), SHADOW, SHADOW, 0, 0, 1, 1, alpha=0.30)
img = paint(img, mask_of(lambda d: ell(d, COIN, COIN_R)), GOLD0, GOLD1, COIN[0]-COIN_R, COIN[1]-COIN_R, COIN[0]+COIN_R, COIN[1]+COIN_R)
layer = Image.new("RGBA", (W, W), (0,0,0,0)); d = ImageDraw.Draw(layer)
r = COIN_R-1.1; d.ellipse([(COIN[0]-r)*S,(COIN[1]-r)*S,(COIN[0]+r)*S,(COIN[1]+r)*S], outline=(255,255,255,102), width=int(0.6*S))
d.rectangle([hole_x*S, hole_y*S, (hole_x+HOLE)*S, (hole_y+HOLE)*S], fill=rgb(DARK)+(255,))
img = Image.alpha_composite(img, layer)
full = img.convert("RGB")
full.resize((512,512), Image.LANCZOS).save("preview_full.png")

def legacy(size, round_shape):
    crop = full.crop((18*S, 18*S, 90*S, 90*S)).resize((size*4, size*4), Image.LANCZOS)
    m = Image.new("L", crop.size, 0); dm = ImageDraw.Draw(m)
    if round_shape: dm.ellipse([0,0,crop.size[0]-1,crop.size[1]-1], fill=255)
    else: dm.rounded_rectangle([0,0,crop.size[0]-1,crop.size[1]-1], radius=int(crop.size[0]*.22), fill=255)
    out = crop.convert("RGBA"); out.putalpha(m); return out.resize((size,size), Image.LANCZOS)
for name, size in {"mdpi":48,"hdpi":72,"xhdpi":96,"xxhdpi":144,"xxxhdpi":192}.items():
    legacy(size, False).save(OUT/f"mipmap-{name}/ic_launcher.webp", "WEBP", quality=95)
    legacy(size, True).save(OUT/f"mipmap-{name}/ic_launcher_round.webp", "WEBP", quality=95)

# preview: big squircle + circle + small sizes on a light and a dark wallpaper
sheet = Image.new("RGB", (1180, 640), (236,238,242))
sq, ci = legacy(420, False), legacy(420, True)
sheet.paste(sq.convert("RGB"), (20, 20), sq.split()[3]); sheet.paste(ci.convert("RGB"), (470, 20), ci.split()[3])
dark = Image.new("RGB", (260, 420), (28,30,36)); sheet.paste(dark, (900, 20))
x = 920
for s in (96, 72, 48):
    ic = legacy(s, False); sheet.paste(ic.convert("RGB"), (x, 60 + (96-s)//2), ic.split()[3]); x += s + 22
x = 920
for s in (96, 72, 48):
    ic = legacy(s, True); sheet.paste(ic.convert("RGB"), (x, 220 + (96-s)//2), ic.split()[3]); x += s + 22
sheet.save("preview_launcher.png")
print("ok")
