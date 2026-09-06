package com.endiq.client.gui.components

/** Pure geometry: keep generated corners crisp instead of stretching whole button images. */
data class Slice(val destination: UiRect, val source: UiRect)
fun nineSlices(rect: UiRect, textureWidth: Int, textureHeight: Int, sourceCorner: Int = 8, corner: Int = 4): List<Slice> {
    if (rect.width <= 0 || rect.height <= 0) return emptyList()
    val dx=minOf(corner,rect.width/2);val dy=minOf(corner,rect.height/2)
    val sx=minOf(sourceCorner,textureWidth/2);val sy=minOf(sourceCorner,textureHeight/2)
    val xs=listOf(rect.x,rect.x+dx,rect.right-dx,rect.right)
    val ys=listOf(rect.y,rect.y+dy,rect.bottom-dy,rect.bottom)
    val us=listOf(0,sx,textureWidth-sx,textureWidth)
    val vs=listOf(0,sy,textureHeight-sy,textureHeight)
    return (0..2).flatMap { y -> (0..2).mapNotNull { x ->
        val destination=UiRect(xs[x],ys[y],xs[x+1]-xs[x],ys[y+1]-ys[y])
        if (destination.width==0 || destination.height==0) null else Slice(destination,UiRect(us[x],vs[y],us[x+1]-us[x],vs[y+1]-vs[y]))
    } }
}
