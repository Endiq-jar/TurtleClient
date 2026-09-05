package com.endiq.client.gui

import com.endiq.client.compat.*
import com.endiq.client.cosmetics.CosmeticManager
import com.endiq.client.cosmetics.CosmeticManager.CosmeticType
import com.endiq.client.gui.settings.ModSettingsGui
import com.endiq.client.modules.Module
import com.endiq.client.modules.ModuleManager

class ClickGui : ClientScreen("TurtleClient") {

    // ── Layout ────────────────────────────────────────────────────────
    private val GW    = 580
    private val GH    = 360
    private val TOPH  = 30        // topbar
    private val ROW1H = 22        // module category tabs row
    private val ROW2H = 22        // cosmetics / search row
    private val BBH   = 18        // bottom bar
    private val CW    = 138
    private val CH    = 80
    private val PAD   = 5
    private val COLS  = 4
    private val CCW   = 86
    private val CCH   = 70
    private val CCOLS = 4

    // ── Colours ───────────────────────────────────────────────────────
    private val BG     = 0xF2111111.toInt()
    private val TOPBG  = 0xF2181818.toInt()
    private val TAB1BG = 0xF21C1C1C.toInt()
    private val TAB2BG = 0xF2161616.toInt()
    private val CARD   = 0xF2202020.toInt()
    private val CARDH  = 0xF22A2A2A.toInt()
    private val CARDEQ = 0xF2172D21.toInt()
    private val ON     = 0xFF3D9970.toInt()
    private val OFF    = 0xFF333333.toInt()
    private val RED    = 0xFFE05252.toInt()
    private val WHITE  = 0xFFFFFFFF.toInt()
    private val GRAY   = 0xFF666666.toInt()
    private val LGRAY  = 0xFF999999.toInt()
    private val PURPLE = 0xFF9B59B6.toInt()
    private val PURPLH = 0xFFCC88FF.toInt()

    // ── State ─────────────────────────────────────────────────────────
    private var modTab        = Module.Category.ALL
    private var showCosmetics = false
    private var cosTab        = CosmeticType.CAPE
    private var searchQuery   = ""
    private var searchFocused = false
    private var scroll        = 0
    private var cosScroll     = 0
    private var gx = 0; private var gy = 0

    private val LOGO = identifier("turtle-client", "textures/loading_icon.png")
    private val MOD_TABS = listOf(
        Module.Category.ALL, Module.Category.HUD, Module.Category.HYPIXEL,
        Module.Category.PVP, Module.Category.RENDER, Module.Category.MOVEMENT,
        Module.Category.UTILITY, Module.Category.PERFORMANCE
    )
    private val COS_TYPES = CosmeticType.values().toList()

    // content area starts below topbar + row1 + row2
    private fun contentTop() = gy + TOPH + ROW1H + ROW2H
    private fun contentH()   = GH - TOPH - ROW1H - ROW2H - BBH

    private fun getFiltered() = ModuleManager.getByCategory(modTab)
        .let { if (searchQuery.isEmpty()) it else it.filter { m -> m.name.contains(searchQuery, true) } }

    private fun maxScroll(): Int {
        val rows = (getFiltered().size + COLS - 1) / COLS
        return ((rows * (CH + PAD) + PAD) - contentH()).coerceAtLeast(0)
    }
    private fun cosMaxScroll(): Int {
        val rows = (CosmeticManager.getByType(cosTab).size + CCOLS - 1) / CCOLS
        return ((rows * (CCH + PAD) + PAD) - contentH()).coerceAtLeast(0)
    }

    override fun init() {
        gx = (width - GW) / 2; gy = (height - GH) / 2
        scroll = 0; cosScroll = 0
        CosmeticManager.reload()
    }

    // ═══════════════════════════════════════════════════════════════════
    override fun renderGui(ctx: GuiContext, mx: Int, my: Int, delta: Float) {
        ctx.fill(0, 0, width, height, 0x99000000.toInt())
        ctx.fill(gx, gy, gx + GW, gy + GH, BG)
        drawTopBar(ctx)
        drawRow1(ctx, mx, my)   // module tabs
        drawRow2(ctx, mx, my)   // cosmetics tabs OR search
        if (showCosmetics) drawCosmetics(ctx, mx, my) else drawModGrid(ctx, mx, my)
        drawBottomBar(ctx, mx, my)
        super.renderGui(ctx, mx, my, delta)
    }

    // ── Topbar ────────────────────────────────────────────────────────
    private fun drawTopBar(ctx: GuiContext) {
        ctx.fill(gx, gy, gx + GW, gy + TOPH, TOPBG)
        ctx.fill(gx, gy + TOPH - 1, gx + GW, gy + TOPH, RED)
        val ls = 22; val ly = gy + (TOPH - ls) / 2
        try { ctx.drawTexture(LOGO, gx+4, ly, ls, ls) } catch (_: Exception) {}
        val t = if (showCosmetics) "TurtleClient  |  COSMETICS" else "TurtleClient  |  MOD MENU"
        ctx.drawTextWithShadow(textRenderer, t, gx+4+ls+4, gy+(TOPH-8)/2, WHITE)
        // close
        ctx.fill(gx+GW-16, gy+4, gx+GW-4, gy+TOPH-4, 0xFFAA2222.toInt())
        ctx.drawTextWithShadow(textRenderer, "X", gx+GW-12, gy+(TOPH-8)/2, WHITE)
    }

    // ── Row 1: module category tabs ───────────────────────────────────
    private fun drawRow1(ctx: GuiContext, mx: Int, my: Int) {
        val ry = gy + TOPH
        ctx.fill(gx, ry, gx+GW, ry+ROW1H, TAB1BG)
        ctx.fill(gx, ry+ROW1H-1, gx+GW, ry+ROW1H, 0xFF141414.toInt())
        var tx = gx + 4
        val ty = ry + 3; val th = ROW1H - 6
        for (t in MOD_TABS) {
            val lbl = t.displayName
            val tw  = textRenderer.getWidth(lbl) + 8
            val sel = !showCosmetics && modTab == t
            val hov = mx in tx..(tx+tw) && my in ty..(ty+th)
            when {
                sel  -> { ctx.fill(tx,ty,tx+tw,ty+th,RED);                ctx.drawTextWithShadow(textRenderer,lbl,tx+4,ty+4,WHITE) }
                hov  -> { ctx.fill(tx,ty,tx+tw,ty+th,0xFF2A2A2A.toInt()); ctx.drawTextWithShadow(textRenderer,lbl,tx+4,ty+4,WHITE) }
                else ->   ctx.drawTextWithShadow(textRenderer,lbl,tx+4,ty+4,LGRAY)
            }
            tx += tw + 3
        }
    }

    // ── Row 2: cosmetics tab strip OR search bar ──────────────────────
    private fun drawRow2(ctx: GuiContext, mx: Int, my: Int) {
        val ry = gy + TOPH + ROW1H
        ctx.fill(gx, ry, gx+GW, ry+ROW2H, TAB2BG)
        ctx.fill(gx, ry+ROW2H-1, gx+GW, ry+ROW2H, 0xFF111111.toInt())

        if (showCosmetics) {
            // Cosmetic type tabs
            var tx = gx + 4
            val ty = ry + 3; val th = ROW2H - 6
            for (ct in COS_TYPES) {
                val lbl = "${ct.icon} ${ct.displayName}"
                val tw  = textRenderer.getWidth(lbl) + 8
                val sel = cosTab == ct
                val hov = mx in tx..(tx+tw) && my in ty..(ty+th)
                when {
                    sel  -> { ctx.fill(tx,ty,tx+tw,ty+th,PURPLE);              ctx.drawTextWithShadow(textRenderer,lbl,tx+4,ty+4,WHITE) }
                    hov  -> { ctx.fill(tx,ty,tx+tw,ty+th,0xFF221A2A.toInt());  ctx.drawTextWithShadow(textRenderer,lbl,tx+4,ty+4,PURPLH) }
                    else ->   ctx.drawTextWithShadow(textRenderer,lbl,tx+4,ty+4,LGRAY)
                }
                tx += tw + 3
            }
            // Refresh btn
            val rw = textRenderer.getWidth("↺")+8; val rx = gx+GW-rw-4
            val rhov = mx in rx..(rx+rw) && my in ry..(ry+ROW2H)
            ctx.fill(rx,ry+3,rx+rw,ry+ROW2H-3, if(rhov) 0xFF2A2A2A.toInt() else 0xFF1A1A1A.toInt())
            ctx.drawTextWithShadow(textRenderer,"↺",rx+4,ry+5, if(rhov) WHITE else GRAY)
        } else {
            // Search bar + Cosmetics shortcut button
            ctx.drawTextWithShadow(textRenderer,"⌕",gx+6,ry+5,GRAY)
            val sbX = gx+18
            ctx.fill(sbX,ry+3,gx+GW-70,ry+ROW2H-3,0xFF222222.toInt())
            ctx.fill(sbX,ry+3,sbX+1,ry+ROW2H-3, if(searchFocused) RED else GRAY)
            val disp = if(searchQuery.isEmpty()&&!searchFocused) "Search mods..." else searchQuery + if(searchFocused) "|" else ""
            ctx.drawTextWithShadow(textRenderer,disp,sbX+4,ry+5, if(searchQuery.isEmpty()&&!searchFocused) GRAY else WHITE)
            // Cosmetics button right side of row2
            val cbLbl = "✦ Cosmetics"
            val cbW = textRenderer.getWidth(cbLbl)+10; val cbX = gx+GW-cbW-4
            val cbHov = mx in cbX..(cbX+cbW) && my in ry..(ry+ROW2H)
            ctx.fill(cbX,ry+2,cbX+cbW,ry+ROW2H-2, if(cbHov) 0xFF2A1A3A.toInt() else 0xFF1A1020.toInt())
            ctx.fill(cbX,ry+2,cbX+1,ry+ROW2H-2,PURPLE)
            ctx.drawTextWithShadow(textRenderer,cbLbl,cbX+5,ry+5, if(cbHov) PURPLH else PURPLE)
        }
    }

    // ── Cosmetics content ─────────────────────────────────────────────
    private fun drawCosmetics(ctx: GuiContext, mx: Int, my: Int) {
        val ct = contentTop(); val ch = contentH()
        val GRID_W = 368; val PREV_X = gx+GRID_W; val PREV_W = GW-GRID_W
        ctx.fill(PREV_X,ct,PREV_X+1,ct+ch,0xFF1A1A1A.toInt())

        // Left: card grid
        val items = CosmeticManager.getByType(cosTab)
        ctx.enableScissor(gx, ct, PREV_X, ct+ch)
        if (items.isEmpty()) {
            val m1 = "No ${cosTab.displayName} found"
            val m2 = "Add .png to  custom_cosmetics/${cosTab.folderName}/"
            ctx.drawTextWithShadow(textRenderer,m1,gx+GRID_W/2-textRenderer.getWidth(m1)/2,ct+40,LGRAY)
            ctx.drawTextWithShadow(textRenderer,m2,gx+GRID_W/2-textRenderer.getWidth(m2)/2,ct+54,GRAY)
        } else {
            items.forEachIndexed { i, entry ->
                val col=i%CCOLS; val row=i/CCOLS
                val cx=gx+PAD+col*(CCW+PAD); val cy=ct+PAD+row*(CCH+PAD)-cosScroll
                if (cy+CCH<ct||cy>ct+ch) return@forEachIndexed
                val isEq=CosmeticManager.isEquipped(entry); val hov=mx in cx..(cx+CCW)&&my in cy..(cy+CCH)
                ctx.fill(cx,cy,cx+CCW,cy+CCH, when{isEq->CARDEQ;hov->CARDH;else->CARD})
                if (isEq) ctx.fill(cx,cy,cx+2,cy+CCH,ON)
                val ic=entry.type.icon
                ctx.fill(cx+CCW/2-11,cy+7,cx+CCW/2+11,cy+24,0xFF2A2A2A.toInt())
                ctx.drawTextWithShadow(textRenderer,ic,cx+CCW/2-textRenderer.getWidth(ic)/2,cy+13, if(isEq) ON else LGRAY)
                val nm=if(textRenderer.getWidth(entry.name)>CCW-6) entry.name.take(8)+"…" else entry.name
                ctx.drawTextWithShadow(textRenderer,nm,cx+CCW/2-textRenderer.getWidth(nm)/2,cy+30,WHITE)
                val bY=cy+CCH-16;val bX=cx+4;val bW=CCW-8
                ctx.fill(bX,bY,bX+bW,bY+12, if(isEq) ON else OFF)
                val bl=if(isEq)"Equipped ✓" else "Equip"
                ctx.drawTextWithShadow(textRenderer,bl,bX+bW/2-textRenderer.getWidth(bl)/2,bY+2, if(isEq) WHITE else LGRAY)
            }
        }
        ctx.disableScissor()
        // Scrollbar
        val cms=cosMaxScroll()
        if(cms>0){val tH=(ch.toFloat()/(ch+cms)*ch).toInt().coerceAtLeast(14);val tY=ct+(cosScroll.toFloat()/cms*(ch-tH)).toInt();ctx.fill(PREV_X-5,ct,PREV_X-1,ct+ch,0xFF1A1A1A.toInt());ctx.fill(PREV_X-5,tY,PREV_X-1,tY+tH,0xFF777777.toInt())}

        // Right: player preview
        val px=PREV_X+2; val pw=PREV_W-3
        ctx.fill(px,ct,px+pw,ct+ch,0xF2161616.toInt())
        ctx.drawTextWithShadow(textRenderer,"Preview",px+pw/2-textRenderer.getWidth("Preview")/2,ct+4,LGRAY)

        val cx2=px+pw/2; val bTop=ct+20
        val hW=16;val hH=16;val boW=12;val boH=20;val aW=4;val aH=16;val lW=5;val lH=18
        val hX=cx2-hW/2;val hY=bTop
        val boX=cx2-boW/2;val boY=hY+hH+1
        val laX=boX-aW-1;val raX=boX+boW+1;val aY=boY
        val llX=boX;val rlX=boX+boW-lW;val lY=boY+boH+1

        // Cape behind
        CosmeticManager.getEquipped(CosmeticType.CAPE)?.let{
            val cX=cx2-9;val cY=hY+hH-2
            ctx.fill(cX,cY,cX+18,cY+boH+14,0xFFAA2222.toInt())
            ctx.fill(cX+1,cY+1,cX+17,cY+boH+13,0xFF882222.toInt())
            for(s in 0..2)ctx.fill(cX+3+s*5,cY+4,cX+4+s*5,cY+boH+8,0x33FFFFFF.toInt())
        }
        // Wings behind
        CosmeticManager.getEquipped(CosmeticType.WINGS)?.let{
            ctx.fill(boX-20,aY,boX-1,aY+22,0xFF665522.toInt())
            ctx.fill(boX+boW+1,aY,boX+boW+20,aY+22,0xFF665522.toInt())
            ctx.fill(boX-19,aY+1,boX-2,aY+21,0xFF997733.toInt())
            ctx.fill(boX+boW+2,aY+1,boX+boW+19,aY+21,0xFF997733.toInt())
        }
        // Head
        ctx.fill(hX,hY,hX+hW,hY+hH,0xFF8B6040.toInt())
        ctx.fill(hX,hY,hX+hW,hY+4,0xFF2A1A0A.toInt())
        // Hat
        CosmeticManager.getEquipped(CosmeticType.HAT)?.let{
            ctx.fill(hX-2,hY-1,hX+hW+2,hY,0xFF222222.toInt())
            ctx.fill(hX,hY-8,hX+hW,hY-1,0xFF222222.toInt())
            ctx.fill(hX,hY-2,hX+hW,hY-1,0xFFCC2222.toInt())
        }
        // Body
        val suit=CosmeticManager.getEquipped(CosmeticType.SUIT)
        val bc=if(suit!=null) 0xFF224488.toInt() else 0xFF4466AA.toInt()
        ctx.fill(boX,boY,boX+boW,boY+boH,bc)
        suit?.let{ctx.fill(boX,boY,boX+1,boY+boH,0xFF4488CC.toInt());ctx.fill(boX+boW-1,boY,boX+boW,boY+boH,0xFF4488CC.toInt())}
        // Arms
        ctx.fill(laX,aY,laX+aW,aY+aH,bc);ctx.fill(raX,aY,raX+aW,aY+aH,bc)
        ctx.fill(laX,aY+aH-3,laX+aW,aY+aH,0xFF9A7050.toInt());ctx.fill(raX,aY+aH-3,raX+aW,aY+aH,0xFF9A7050.toInt())
        // Pet
        CosmeticManager.getEquipped(CosmeticType.PET)?.let{
            ctx.fill(laX-6,aY-3,laX-6+8,aY+6,0xFF55AA55.toInt())
            ctx.fill(laX-5,aY-6,laX+1,aY-2,0xFF55AA55.toInt())
            ctx.fill(laX-4,aY-5,laX-3,aY-4,0xFF000000.toInt())
            ctx.fill(laX-1,aY-5,laX,aY-4,0xFF000000.toInt())
        }
        // Legs
        ctx.fill(llX,lY,llX+lW,lY+lH,0xFF334477.toInt());ctx.fill(rlX,lY,rlX+lW,lY+lH,0xFF334477.toInt())
        ctx.fill(llX,lY+lH-2,llX+lW,lY+lH,0xFF111111.toInt());ctx.fill(rlX,lY+lH-2,rlX+lW,lY+lH,0xFF111111.toInt())

        // Equipped list
        val sList=CosmeticManager.equipped.values.filterNotNull()
        var ey=lY+lH+6
        if(sList.isEmpty()){ctx.drawTextWithShadow(textRenderer,"Nothing equipped",px+pw/2-textRenderer.getWidth("Nothing equipped")/2,ey,GRAY)}
        else{for(e in sList){val s="${e.type.icon} ${e.name.take(10)}";ctx.drawTextWithShadow(textRenderer,s,px+pw/2-textRenderer.getWidth(s)/2,ey,ON);ey+=9}}

        // Remove All
        val raL="✕ Remove All";val raW=textRenderer.getWidth(raL)+10;val raX2=px+pw/2-raW/2;val raY2=ct+ch-15
        val raHov=mx in raX2..(raX2+raW)&&my in raY2..(raY2+12)
        ctx.fill(raX2,raY2,raX2+raW,raY2+12, if(raHov) 0xFF4A1A1A.toInt() else 0xFF2A1010.toInt())
        ctx.fill(raX2,raY2,raX2+1,raY2+12,RED)
        ctx.drawTextWithShadow(textRenderer,raL,raX2+5,raY2+2,RED)
    }

    // ── Mod grid ──────────────────────────────────────────────────────
    private fun drawModGrid(ctx: GuiContext, mx: Int, my: Int) {
        val ct=contentTop();val ch=contentH()
        ctx.enableScissor(gx,ct,gx+GW,ct+ch)
        getFiltered().forEachIndexed { i, mod ->
            val col=i%COLS;val row=i/COLS
            val cx=gx+PAD+col*(CW+PAD);val cy=ct+PAD+row*(CH+PAD)-scroll
            if(cy+CH<ct||cy>ct+ch)return@forEachIndexed
            val hov=mx in cx..(cx+CW)&&my in cy..(cy+CH)
            ctx.fill(cx,cy,cx+CW,cy+CH, if(hov) CARDH else CARD)
            if(mod.enabled)ctx.fill(cx,cy,cx+2,cy+CH,ON)
            if(mod.isNew){ctx.fill(cx+3,cy+3,cx+26,cy+12,ON);ctx.drawTextWithShadow(textRenderer,"NEW",cx+4,cy+4,WHITE)}
            val icon=getIcon(mod.name)
            ctx.fill(cx+CW/2-12,cy+14,cx+CW/2+12,cy+34,0xFF2A2A2A.toInt())
            ctx.drawTextWithShadow(textRenderer,icon,cx+CW/2-textRenderer.getWidth(icon)/2,cy+20, if(mod.enabled) ON else LGRAY)
            val nm=if(textRenderer.getWidth(mod.name)>CW-8)mod.name.take(11)+"..." else mod.name
            ctx.drawTextWithShadow(textRenderer,nm,cx+CW/2-textRenderer.getWidth(nm)/2,cy+40,WHITE)
            val bY=cy+CH-18;val bX=cx+4;val bW=CW-24
            ctx.fill(bX,bY,bX+bW,bY+13, if(mod.enabled) ON else OFF)
            val lb=if(mod.enabled)"Enabled" else "Disabled"
            ctx.drawTextWithShadow(textRenderer,lb,bX+bW/2-textRenderer.getWidth(lb)/2,bY+3, if(mod.enabled) WHITE else LGRAY)
            ctx.fill(cx+CW-17,bY,cx+CW-4,bY+13,0xFF2A2A2A.toInt())
            ctx.drawTextWithShadow(textRenderer,"⚙",cx+CW-14,bY+2,GRAY)
        }
        ctx.disableScissor()
        val ms=maxScroll()
        if(ms>0){val tH=(contentH().toFloat()/(contentH()+ms)*contentH()).toInt().coerceAtLeast(20);val tY=ct+(scroll.toFloat()/ms*(contentH()-tH)).toInt();ctx.fill(gx+GW-8,ct,gx+GW,ct+contentH(),0xFF1A1A1A.toInt());ctx.fill(gx+GW-8,tY,gx+GW,tY+tH,0xFF888888.toInt())}
    }

    // ── Bottom bar ────────────────────────────────────────────────────
    private fun drawBottomBar(ctx: GuiContext, mx: Int, my: Int) {
        val bbY=gy+GH-BBH
        ctx.fill(gx,bbY,gx+GW,gy+GH,0xF20D0D0D.toInt())
        ctx.fill(gx,bbY,gx+GW,bbY+1,0xFF1A1A1A.toInt())
        val hint=if(showCosmetics)"Click: equip/unequip  |  ↺: reload  |  RShift: close"
                 else             "Left: toggle  |  Right: settings  |  RShift: close"
        ctx.drawTextWithShadow(textRenderer,hint,gx+6,bbY+5,GRAY)
        val ver="v1.0  MC${gameVersion()}";val vW=textRenderer.getWidth(ver)+10
        ctx.fill(gx+GW-vW-2,bbY+2,gx+GW-2,gy+GH-2,0xFF1A1A1A.toInt())
        ctx.fill(gx+GW-vW-2,bbY+2,gx+GW-vW-1,gy+GH-2,RED)
        ctx.drawTextWithShadow(textRenderer,ver,gx+GW-vW+3,bbY+5,LGRAY)
    }

    // ═══════════════════════════════════════════════════════════════════
    // INPUT
    // ═══════════════════════════════════════════════════════════════════
    override fun onMouseClicked(mx: Double, my: Double, btn: Int): Boolean {
        val imx=mx.toInt();val imy=my.toInt()
        // Close
        if(imx in (gx+GW-16)..(gx+GW-4)&&imy in (gy+4)..(gy+TOPH-4)){MinecraftClient.getInstance().setScreen(null);return true}
        // Row 1: module tabs
        val r1y=gy+TOPH;val r1b=r1y+ROW1H
        if(imy in r1y..r1b){
            var tx=gx+4
            for(t in MOD_TABS){val tw=textRenderer.getWidth(t.displayName)+8;if(imx in tx..(tx+tw)){showCosmetics=false;modTab=t;scroll=0;searchQuery="";return true};tx+=tw+3}
        }
        // Row 2
        val r2y=gy+TOPH+ROW1H;val r2b=r2y+ROW2H
        if(imy in r2y..r2b){
            if(showCosmetics){
                // cos type tabs
                var tx=gx+4
                for(ct in COS_TYPES){val tw=textRenderer.getWidth("${ct.icon} ${ct.displayName}")+8;if(imx in tx..(tx+tw)){cosTab=ct;cosScroll=0;return true};tx+=tw+3}
                // refresh
                val rw=textRenderer.getWidth("↺")+8;val rx=gx+GW-rw-4
                if(imx in rx..(rx+rw)){CosmeticManager.reload();return true}
            } else {
                // search focus
                val sbX=gx+18
                if(imx in sbX..(gx+GW-70)&&imy in r2y..r2b){searchFocused=true;return true} else searchFocused=false
                // cosmetics button
                val cbLbl="✦ Cosmetics";val cbW=textRenderer.getWidth(cbLbl)+10;val cbX=gx+GW-cbW-4
                if(imx in cbX..(cbX+cbW)){showCosmetics=true;cosScroll=0;CosmeticManager.reload();return true}
            }
        }
        // Cosmetics content clicks
        if(showCosmetics){
            val ct=contentTop();val ch=contentH();val GRID_W=368;val PREV_X=gx+GRID_W;val PREV_W=GW-GRID_W
            val px=PREV_X+2;val pw=PREV_W-3
            // Remove All
            val raL="✕ Remove All";val raW=textRenderer.getWidth(raL)+10;val raX2=px+pw/2-raW/2;val raY2=ct+ch-15
            if(imx in raX2..(raX2+raW)&&imy in raY2..(raY2+12)){COS_TYPES.forEach{CosmeticManager.unequip(it)};return true}
            // Cards
            if(imx<PREV_X&&imy>=ct&&imy<=ct+ch){
                CosmeticManager.getByType(cosTab).forEachIndexed{i,entry->
                    val col=i%CCOLS;val row=i/CCOLS
                    val cx=gx+PAD+col*(CCW+PAD);val cy=ct+PAD+row*(CCH+PAD)-cosScroll
                    if(imx in cx..(cx+CCW)&&imy in cy..(cy+CCH)){if(CosmeticManager.isEquipped(entry))CosmeticManager.unequip(entry.type) else CosmeticManager.equip(entry);return true}
                }
            }
            return super.onMouseClicked(mx,my,btn)
        }
        // Mod cards
        val ct=contentTop();val ch=contentH()
        if(imy>=ct&&imy<=ct+ch){
            getFiltered().forEachIndexed{i,mod->
                val col=i%COLS;val row=i/COLS
                val cx=gx+PAD+col*(CW+PAD);val cy=ct+PAD+row*(CH+PAD)-scroll
                if(imx !in cx..(cx+CW)||imy !in cy..(cy+CH))return@forEachIndexed
                when(btn){0->{mod.toggle();return true};1->{MinecraftClient.getInstance().setScreen(ModSettingsGui(mod,this));return true}}
            }
        }
        return super.onMouseClicked(mx,my,btn)
    }

    override fun onMouseScrolled(mx: Double, my: Double, h: Double, v: Double): Boolean {
        if(showCosmetics){if(mx.toInt()<gx+368){val ms=cosMaxScroll();if(ms>0)cosScroll=(cosScroll-(v*18).toInt()).coerceIn(0,ms)};return true}
        val ms=maxScroll();if(ms>0)scroll=(scroll-(v*20).toInt()).coerceIn(0,ms);return true
    }

    override fun onKeyPressed(kc: Int, sc: Int, mods: Int): Boolean {
        if(!showCosmetics&&searchFocused){
            when(kc){org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE->{if(searchQuery.isNotEmpty()){searchQuery=searchQuery.dropLast(1);scroll=0};return true};org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE->{searchFocused=false;return true}}
            return true
        }
        return super.onKeyPressed(kc,sc,mods)
    }
    override fun onCharTyped(c: String): Boolean {if(!showCosmetics&&searchFocused){searchQuery+=c;scroll=0;return true};return super.onCharTyped(c)}

    private fun getIcon(n:String)=when(n){"FPS"->"FPS";"CPS"->"CPS";"Coordinates"->"XYZ";"Keystrokes"->"KEY";"Armor Status"->"ARM";"Armor Bar"->"BAR";"Attack Indicator"->"ATK";"Autohide HUD"->"HUD";"Block Indicator"->"BLK";"Block Overlay"->"OVR";"Boss Bar"->"BSS";"Crosshair"->" + ";"Hotbar"->"HOT";"Nametags"->"TAG";"Pack Display"->"PKG";"Ping"->"PNG";"Potion Status"->"POT";"Scoreboard"->"SCR";"Server Address"->"SRV";"Reach Display"->"RCH";"Speed HUD"->"SPD";"Memory HUD"->"MEM";"Clock HUD"->"CLK";"Direction HUD"->"DIR";"Hit Color"->"HIT";"PvP Info"->"PVP";"Team Circles"->"CRL";"Toggle Sprint"->"SPR";"Sprint"->">>>";"Freecam"->"CAM";"Animations"->"ANI";"Motion Blur"->"BLR";"NoWeather"->"SUN";"TimeChanger"->"TME";"Zoom"->"ZOM";"Auto Text"->"TXT";"Camera"->"PIC";"Chat"->"MSG";"Nick Hider"->"NCK";"Popup Events"->"POP";"Timers"->"TMR";"Waypoints"->"WPT";"UHC Overlay"->"UHC";"Hypixel Addons"->"HYP";"Skyblock Addons"->"SKY";"Tab Stat"->"TAB";"Net Graph"->"NET";"Combo Counter"->"CMB";"FOV Changer"->"FOV";"Full Bright"->"BRT";"Team View"->"TM";else->"MOD"}
}
