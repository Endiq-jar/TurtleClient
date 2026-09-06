package com.endiq.client.gui.components

import com.endiq.client.compat.*
import kotlin.math.sqrt

/** Small reusable primitives; no screenshot-sized button textures or per-frame image decoding. */
object TurtleTheme {
    val BACKGROUND = 0xFF0E1611.toInt()
    val PANEL = 0xFA151F19.toInt()
    val CARD = 0xFF18221D.toInt()
    val HOVER = 0xFF213128.toInt()
    val BORDER = 0xFF344239.toInt()
    val ACCENT = 0xFF96CCA9.toInt()
    val ACTIVE = 0xFF263F30.toInt()
    val TEXT = 0xFFE7EEE9.toInt()
    val MUTED = 0xFFA6B5AA.toInt()
    val SUBTLE = 0xFF748779.toInt()
    val DANGER = 0xFFE49B8C.toInt()
    val LOGO = identifier("turtle-client", "textures/gui/branding/turtle.png")
    val WORDMARK = identifier("turtle-client", "textures/gui/branding/wordmark.png")
    private val icons = mutableMapOf<String, Identifier>()
    fun icon(name: String) = icons.getOrPut(name) { identifier("turtle-client", "textures/gui/icons/$name.png") }

    private val moduleTextures = mutableMapOf<Class<*>, Identifier>()
    fun moduleIcon(module: com.endiq.client.modules.Module): Identifier = moduleTextures.getOrPut(module.javaClass) {
        identifier("turtle-client", ModuleIcons.path(module.javaClass.simpleName, module.category.name.lowercase(java.util.Locale.ROOT)))
    }

    fun rounded(ctx: GuiContext, rect: UiRect, color: Int, radius: Int = 5) {
        if (rect.width <= 0 || rect.height <= 0) return
        val r = minOf(radius, rect.width / 2, rect.height / 2).coerceAtLeast(0)
        if (r == 0) { ctx.fill(rect.x, rect.y, rect.right, rect.bottom, color); return }
        ctx.fill(rect.x, rect.y + r, rect.right, rect.bottom - r, color)
        for (row in 0 until r) {
            val dy = r - row - 0.5
            val cut = (r - sqrt(r * r - dy * dy)).toInt()
            ctx.fill(rect.x + cut, rect.y + row, rect.right - cut, rect.y + row + 1, color)
            ctx.fill(rect.x + cut, rect.bottom - row - 1, rect.right - cut, rect.bottom - row, color)
        }
    }

    fun skin(ctx: GuiContext, rect: UiRect, material: String, color: Int = -1) {
        val h=if(material=="panel")64 else 32
        val w=if(material=="panel")64 else 128
        val texture=icons.getOrPut("button:$material") { identifier("turtle-client","textures/gui/buttons/$material.png") }
        for (slice in nineSlices(rect,w,h)) {
            val d=slice.destination;val source=slice.source
            ctx.drawTextureRegion(texture,d.x,d.y,d.width,d.height,source.x.toFloat(),source.y.toFloat(),source.width,source.height,w,h,color)
        }
    }

    fun panel(ctx: GuiContext, rect: UiRect, color: Int = PANEL, border: Int = BORDER, radius: Int = 7) {
        if (color==PANEL && border==BORDER) { skin(ctx,rect,"panel");return }
        rounded(ctx, rect, border, radius)
        rounded(ctx, rect.inset(1), color, (radius - 1).coerceAtLeast(0))
    }

    fun fit(font: TextRenderer, text: String, width: Int): String {
        if (width <= 0) return ""
        if (font.getWidth(text) <= width) return text
        val suffix = "..."
        if (font.getWidth(suffix) > width) return ""
        var end = text.length
        while (end > 0 && font.getWidth(text.substring(0, end) + suffix) > width) end--
        return text.substring(0, end) + suffix
    }

    fun label(ctx: GuiContext, font: TextRenderer, text: String, x: Int, y: Int, color: Int = TEXT, maxWidth: Int = Int.MAX_VALUE) {
        ctx.drawText(font, fit(font, text, maxWidth), x, y, color, false)
    }

    fun button(ctx: GuiContext, font: TextRenderer, rect: UiRect, text: String, hovered: Boolean, primary: Boolean = false,
               enabled: Boolean = true, danger: Boolean = false) {
        val material=when {
            !enabled -> "disabled"
            danger -> if(hovered) "danger_hover" else "danger"
            primary -> if(hovered) "primary_hover" else "primary"
            hovered -> "hover"
            else -> "normal"
        }
        skin(ctx,rect,material)
        val value = fit(font, text, rect.width - 12)
        label(ctx, font, value, rect.x + (rect.width - font.getWidth(value)) / 2, rect.y + (rect.height - 8) / 2,
            if(!enabled) SUBTLE else if(primary || (danger && hovered))BACKGROUND else TEXT)
    }

    fun actions(ctx:GuiContext,font:TextRenderer,buttons:ActionButtons,mx:Int,my:Int) {
        buttons.entries.forEach { entry -> button(ctx,font,entry.bounds,entry.action.label,entry.bounds.contains(mx.toDouble(),my.toDouble()),
            entry.primary,entry.action.enabled(),entry.danger) }
    }

    fun tooltip(ctx:GuiContext,font:TextRenderer,text:String,mx:Int,my:Int,screenWidth:Int,screenHeight:Int) {
        val value=fit(font,text,screenWidth-24);val w=font.getWidth(value)+12
        val x=(mx+8).coerceIn(2,(screenWidth-w-2).coerceAtLeast(2))
        val y=(my+16).coerceIn(2,(screenHeight-23).coerceAtLeast(2))
        panel(ctx,UiRect(x,y,w,20),BACKGROUND,BORDER,4);label(ctx,font,value,x+6,y+6,TEXT)
    }

    /** Bounds for [textButton]: 5 pixels of padding, 14 pixels tall, centred on [centerX]. */
    fun textButtonBounds(textWidth: Int, centerX: Int, y: Int) =
        UiRect(centerX - textWidth / 2 - 5, y - 3, textWidth + 10, 14)

    /**
     * Bare text button: no skin, no panel, no shadow — just text and a hover rule.
     * [rect] comes from [textButtonBounds] so hit testing and drawing always agree.
     */
    fun textButton(ctx: GuiContext, font: TextRenderer, rect: UiRect, text: String, hovered: Boolean, enabled: Boolean = true) {
        val color = if (!enabled) SUBTLE else if (hovered) ACCENT else TEXT
        label(ctx, font, text, rect.x + (rect.width - font.getWidth(text)) / 2, rect.y + 3, color)
        if (hovered && enabled) ctx.fill(rect.x + 5, rect.bottom - 2, rect.right - 5, rect.bottom - 1, color)
    }

    fun scrollbar(ctx: GuiContext, state: ScrollState, track: UiRect, mx: Int, my: Int) {
        if (state.maximum <= 0) return
        rounded(ctx, track, CARD, 3)
        rounded(ctx, state.thumb(track), if (state.dragging || track.contains(mx.toDouble(), my.toDouble())) ACCENT else SUBTLE, 3)
    }
}

fun coverBounds(width: Int, height: Int, textureWidth: Int, textureHeight: Int): UiRect {
    val scale = maxOf(width.toDouble() / textureWidth, height.toDouble() / textureHeight)
    val w = kotlin.math.ceil(textureWidth * scale).toInt()
    val h = kotlin.math.ceil(textureHeight * scale).toInt()
    return UiRect((width - w) / 2, (height - h) / 2, w, h)
}
