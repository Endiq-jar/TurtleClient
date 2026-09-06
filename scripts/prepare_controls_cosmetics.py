#!/usr/bin/env python3
"""Slice original generated v2 masters into runtime art. Pillow >= 10.
Run after prepare_ui_assets.py, with the v2 master directory as argument.
"""
from pathlib import Path
import hashlib, json, sys
from PIL import Image, ImageChops, ImageDraw, ImageOps, ImageEnhance
ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/turtle-client'
S=Path(sys.argv[1])
manifest_path=A/'ui-assets.json'
manifest={a['path']:a for a in json.loads(manifest_path.read_text())['assets']}
def save(im,path,blur=True):
    p=A/path;p.parent.mkdir(parents=True,exist_ok=True)
    im.save(p,optimize=True,compress_level=9)
    p.with_suffix('.png.mcmeta').write_text(json.dumps({'texture':{'blur':blur,'clamp':True}},separators=(',',':'))+'\n')
    data=p.read_bytes()
    manifest[path]={'path':path,'width':im.width,'height':im.height,'bytes':len(data),'sha256':hashlib.sha256(data).hexdigest()}
names='grid hud pvp render movement utility hypixel performance cape hat wings mask suit pet folder camera refresh close search settings account add back check logout delete copy external play pause'.split()
sheet=Image.open(S/'icon-atlas-master.png').convert('RGB')
for i,name in enumerate(names):
    x,y=i%6,i//6
    cell=sheet.crop((round(x*sheet.width/6)+7,round(y*sheet.height/5)+7,round((x+1)*sheet.width/6)-7,round((y+1)*sheet.height/5)-7))
    mask=cell.convert('L').point(lambda v: round(max(0,min(1,(v-70)/175))*255))
    if name=='hud':
        # The generated monitor contained lettering; remove it for a language-neutral glyph.
        d=ImageDraw.Draw(mask);w,h=mask.size
        d.rectangle((w*.21,h*.25,w*.81,h*.52),fill=0)
        d.rectangle((w*.27,h*.30,w*.46,h*.34),fill=255)
        d.rectangle((w*.27,h*.42,w*.66,h*.46),fill=255)
    box=mask.getbbox();assert box,name
    mask=ImageOps.contain(mask.crop(box),(26,26),Image.Resampling.LANCZOS)
    canvas=Image.new('RGBA',(32,32),(255,255,255,0))
    glyph=Image.new('RGBA',mask.size,(255,255,255,255));glyph.putalpha(mask)
    canvas.alpha_composite(glyph,((32-mask.width)//2,(32-mask.height)//2))
    save(canvas,f'textures/gui/icons/{name}.png')
sheet=Image.open(S/'button-atlas-master.png').convert('RGB')
buttons=['normal','hover','primary','primary_hover','disabled','danger','danger_hover','selected']
for i,name in enumerate(buttons):
    x,y=i%4,i//4
    cell=sheet.crop((round(x*sheet.width/4)+4,round(y*sheet.height/2)+4,round((x+1)*sheet.width/4)-4,round((y+1)*sheet.height/2)-4))
    mask=cell.convert('L').point(lambda v:255 if v>18 else 0)
    box=mask.getbbox();assert box,name
    im=cell.crop(box).resize((128,32),Image.Resampling.LANCZOS)
    if name=='hover':im=ImageEnhance.Brightness(im).enhance(.56)
    if name=='disabled':im=ImageEnhance.Brightness(im).enhance(.40)
    im=im.convert('RGBA');alpha=Image.new('L',(512,128));ImageDraw.Draw(alpha).rounded_rectangle((1,1,510,126),radius=24,fill=255)
    im.putalpha(alpha.resize((128,32),Image.Resampling.LANCZOS))
    save(im,f'textures/gui/buttons/{name}.png')
# A reusable 9-slice panel using the original generated material, not a screenshot.
button=Image.open(A/'textures/gui/buttons/normal.png').convert('RGBA')
panel=Image.new('RGBA',(64,64),(16,28,32,255))
panel.alpha_composite(button.crop((0,0,8,8)),(0,0));panel.alpha_composite(button.crop((120,0,128,8)),(56,0))
panel.alpha_composite(button.crop((0,24,8,32)),(0,56));panel.alpha_composite(button.crop((120,24,128,32)),(56,56))
d=ImageDraw.Draw(panel);d.line((8,0,55,0),fill='#304D48');d.line((8,63,55,63),fill='#233C38');d.line((0,8,0,55),fill='#304D48');d.line((63,8,63,55),fill='#233C38')
a=Image.new('L',(256,256));ImageDraw.Draw(a).rounded_rectangle((0,0,255,255),radius=24,fill=255);panel.putalpha(a.resize((64,64),Image.Resampling.LANCZOS))
save(panel,'textures/gui/buttons/panel.png')
# Vanilla-layout capes plus compact all-face materials for the other meshes.
sheet=Image.open(S/'cosmetic-material-master.png').convert('RGB')
catalog=[]
for col,style in enumerate(['jade','aurora','ember']):
    x0,x1=round(col*sheet.width/3),round((col+1)*sheet.width/3)
    material=sheet.crop((x0,0,x1,sheet.height//2)).resize((64,64),Image.Resampling.NEAREST).quantize(colors=32).convert('RGBA')
    crest=sheet.crop((x0,sheet.height//2,x1,sheet.height)).resize((40,64),Image.Resampling.NEAREST)
    for kind in ['cape','hat','wings','mask','suit','pet']:
        path=f'textures/cosmetics/{kind}/{style}.png'
        if kind=='cape':
            texture=Image.new('RGBA',(256,128))
            front=crest.convert('RGBA');texture.alpha_composite(front,(4,4));texture.alpha_composite(front,(48,4))
            # Sides/top/bottom in the standard 64x32 cape layout, at 4x resolution.
            edge=material.resize((4,64),Image.Resampling.NEAREST)
            texture.alpha_composite(edge,(0,4));texture.alpha_composite(edge,(44,4))
            texture.alpha_composite(material.resize((40,4),Image.Resampling.NEAREST),(4,0))
            texture.alpha_composite(material.resize((40,4),Image.Resampling.NEAREST),(44,0))
            texture=texture.resize((128,64),Image.Resampling.NEAREST)
        else:texture=material.copy()
        save(texture,path,False)
        # Catalog thumbnails use the actual generated materials and a clear type silhouette.
        preview=Image.new('RGBA',(64,64));pattern=material.resize((48,48),Image.Resampling.NEAREST)
        glyph=Image.open(A/f'textures/gui/icons/{kind}.png').getchannel('A').resize((48,48),Image.Resampling.LANCZOS)
        pattern.putalpha(glyph);preview.alpha_composite(pattern,(8,8))
        # Bright edge keeps dark colorways recognizable in a small card.
        save(preview,f'textures/cosmetics/previews/{kind}_{style}.png')
        catalog.append({'id':f'builtin:{kind}:{style}','type':kind.upper(),'name':f'{style.title()} {"Shell Suit" if kind=="suit" else "Hatchling" if kind=="pet" else kind.title()}','texture':path,'preview':f'textures/cosmetics/previews/{kind}_{style}.png'})
(A/'cosmetics.json').write_text(json.dumps({'schema':1,'items':catalog},indent=2)+'\n')
manifest_path.write_text(json.dumps({'schema':1,'assets':list(manifest.values())},indent=2)+'\n')
print(f'{len(manifest)} runtime PNGs, {sum(a["bytes"] for a in manifest.values()):,} compressed bytes')
