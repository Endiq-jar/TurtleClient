#!/usr/bin/env python3
"""Build labelled design references from the shipped PNGs; these are NOT game screenshots."""
from pathlib import Path
import json
from PIL import Image, ImageDraw, ImageFont

ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/turtle-client'
D=ROOT/'docs'
BG='#0E1611';PANEL='#151F19';CARD='#18221D';BORDER='#344239';TEXT='#E7EEE9';MUTED='#A6B5AA';ACCENT='#96CCA9';SUBTLE='#748779'

def font(size):return ImageFont.load_default(size=size)
def text(im,xy,value,size=16,color=TEXT,anchor=None):
    ImageDraw.Draw(im).text(xy,value,font=font(size),fill=color,anchor=anchor)
def pic(im,name,box,tint=None):
    source=Image.open(A/name).convert('RGBA').resize((box[2],box[3]),Image.Resampling.LANCZOS)
    if tint:
        flat=Image.new('RGBA',source.size,tint);flat.putalpha(source.getchannel('A'));source=flat
    im.paste(source,(box[0],box[1]),source)
def rounded(im,box,fill,border=None,radius=10):
    x,y,w,h=box;ImageDraw.Draw(im).rounded_rectangle((x,y,x+w-1,y+h-1),radius,fill,outline=border,width=1)

def skin(im,name,box):
    # Match the runtime nine-slice path instead of stretching borders/corners.
    source=Image.open(A/f'textures/gui/buttons/{name}.png').convert('RGBA')
    x,y,w,h=box;sw,sh=source.size;border=6
    xs=[0,border,sw-border,sw];ys=[0,border,sh-border,sh]
    dx=[x,x+border,x+w-border,x+w];dy=[y,y+border,y+h-border,y+h]
    for row in range(3):
        for col in range(3):
            tile=source.crop((xs[col],ys[row],xs[col+1],ys[row+1]))
            tile=tile.resize((dx[col+1]-dx[col],dy[row+1]-dy[row]),Image.Resampling.LANCZOS)
            im.paste(tile,(dx[col],dy[row]),tile)

def loading():
    # Reference at 640x360 GUI pixels, displayed at 2x (the runtime uses LoadingLayout.fit).
    im=Image.new('RGB',(1280,720),BG)
    pattern=Image.open(A/'textures/gui/loading/contours.png').convert('RGBA').resize((1280,720),Image.Resampling.LANCZOS)
    pattern.putalpha(pattern.getchannel('A').point(lambda a:a//2));im.paste(pattern,(0,0),pattern)
    pic(im,'textures/gui/loading/startup.png',(48,48,200,20),SUBTLE)
    ImageDraw.Draw(im).rectangle((1204,56,1231,59),fill='#263F30')
    pic(im,'textures/gui/loading/edition.png',(544,652,192,20),SUBTLE)
    top=(360-184)//2
    pic(im,'textures/gui/branding/turtle.png',((640-52),2*(top+5),104,104))
    pic(im,'textures/gui/branding/wordmark.png',((640-166),2*(top+73),332,62))
    x=(640-224);y=2*(top+136)
    rounded(im,(x,y,448,6),BORDER,radius=2)
    rounded(im,(x,y,round(448*.68),6),ACCENT,radius=2)
    pic(im,'textures/gui/loading/resources.png',(x,y+28,224,24),MUTED)
    sheet=Image.open(A/'textures/gui/loading/numbers.png').convert('RGBA')
    value='68%';left=x+448-len(value)*16
    for i,c in enumerate(value):
        cell=10 if c=='%' else int(c);tile=sheet.crop((cell*16,0,(cell+1)*16,24))
        im.paste(tile,(left+i*16,y+28),tile)
    return im

def module_card(im,box,key,label,enabled=False):
    x,y,w,h=box
    rounded(im,box,CARD,'#45684F' if enabled else BORDER,9)
    rounded(im,(x+14,y+12,42,42),'#263F30' if enabled else BG,radius=7)
    pic(im,f'textures/gui/modules/{key}.png',(x+20,y+18,30,30),ACCENT if enabled else MUTED)
    pic(im,'textures/gui/icons/settings.png',(x+w-34,y+14,19,19),SUBTLE)
    text(im,(x+14,y+66),label,17)
    text(im,(x+14,y+h-24),'Enabled' if enabled else 'Disabled',12,ACCENT if enabled else SUBTLE)
    rounded(im,(x+w-55,y+h-29,40,19),'#263F30' if enabled else BG,radius=9)
    rounded(im,(x+w-34 if enabled else x+w-51,y+h-26,13,13),ACCENT if enabled else SUBTLE,radius=7)


def main():
    D.mkdir(exist_ok=True)
    im=Image.new('RGB',(1440,1020),'#0A110D')
    text(im,(36,28),'TURTLE CLIENT',18,ACCENT)
    text(im,(36,62),'Loading screen + built-in module icons',33)
    text(im,(1404,38),'VECTOR ARTWORK  /  INTERFACE STUDY',13,SUBTLE,'rt')
    text(im,(36,130),'01  /  RESOURCE LOADING',14,MUTED)
    screen=loading().resize((824,464),Image.Resampling.LANCZOS)
    im.paste(screen,(36,159));rounded(im,(35,158,826,466),None,BORDER,0)
    text(im,(36,639),'Real resource progress · per-reload state · no font bootstrap dependency',14,SUBTLE)
    text(im,(894,130),'02  /  BUILT-IN MODULES',14,MUTED)
    selection=[('fps','FPS Counter',True),('cps','CPS Counter',True),('ping','Ping Display',True),
               ('coordinates','Coordinates',False),('zoom','Zoom',True),('camera','Camera',False)]
    for i,(key,label,enabled) in enumerate(selection):
        module_card(im,(894+(i%2)*263,159+(i//2)*148,246,133),key,label,enabled)
    text(im,(894,620),'52 dedicated pictograms',22)
    text(im,(894,653),'Consistent stroke, optical spacing, clear silhouettes.',14,SUBTLE)
    text(im,(36,704),'03  /  MATTE CONTROLS',14,MUTED)
    for i,(name,label) in enumerate([('normal','Options'),('hover','Hover'),('primary','Play'),('disabled','Unavailable')]):
        box=(36+i*210,737,192,42);skin(im,name,box)
        text(im,(box[0]+box[2]//2,box[1]+13),label,16,BG if name=='primary' else SUBTLE if name=='disabled' else TEXT,'mt')
    text(im,(36,815),'04  /  CLEAN TYPE ICONS',14,MUTED)
    for i,kind in enumerate(('account','search','refresh','folder','delete','settings')):
        pic(im,f'textures/gui/icons/{kind}.png',(43+i*137,849,54,54))
        text(im,(70+i*137,914),kind.title(),13,MUTED,'mt')
    pic(im,'textures/gui/branding/turtle.png',(905,741,89,89))
    pic(im,'textures/gui/branding/wordmark.png',(1018,765,357,67))
    text(im,(894,854),'No glossy bevels. No image-model atlases.',16,MUTED)
    text(im,(894,883),'Editable SVG sources + optimized runtime PNGs.',14,SUBTLE)
    text(im,(36,978),'Design reference built from shipped artwork; not an in-game screenshot. Minecraft still uses its own font in menus.',13,SUBTLE)
    im.save(D/'turtle-interface-preview.png',optimize=True)

    modules=json.loads((A/'module-icons.json').read_text())['modules']
    sheet=Image.new('RGB',(1440,1090),BG)
    text(sheet,(32,30),'TURTLE / MODULE ICON SET',20,ACCENT)
    text(sheet,(32,63),'52 distinct pictograms, drawn on one 24-unit grid.',17,MUTED)
    for i,entry in enumerate(modules):
        x=24+(i%8)*176;y=120+(i//8)*127
        pic(sheet,entry['texture'],(x+56,y,44,44),ACCENT)
        words=entry['label'].split();lines=[];line=''
        for word in words:
            proposed=(line+' '+word).strip()
            if font(14).getlength(proposed)>160 and line:lines.append(line);line=word
            else:line=proposed
        lines.append(line)
        for n,line in enumerate(lines):text(sheet,(x+78,y+59+n*18),line,14,MUTED,'mt')
    text(sheet,(32,1044),'Artwork does not change module availability. Legacy prototypes remain explicitly marked Unavailable.',14,SUBTLE)
    sheet.save(D/'turtle-module-icons.png',optimize=True)

if __name__=='__main__':main()
