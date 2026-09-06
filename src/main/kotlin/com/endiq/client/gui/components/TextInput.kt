package com.endiq.client.gui.components

/** Small, testable single-line editor with real cursor/selection/paste behavior. */
class TextInput(private val limit: Int = 96) {
    var text=""
        private set
    var cursor=0
        private set
    private var anchor=0
    val selection get()=minOf(anchor,cursor)..maxOf(anchor,cursor)
    fun set(value:String) { text=value.filterNot { it.isISOControl() }.let { it.substring(0,it.offsetByCodePoints(0,minOf(limit,it.codePointCount(0,it.length)))) };cursor=text.length;anchor=cursor }
    fun place(index:Int,select:Boolean=false) {
        cursor=index.coerceIn(0,text.length)
        if(cursor>0 && cursor<text.length && text[cursor].isLowSurrogate() && text[cursor-1].isHighSurrogate())cursor--
        if(!select)anchor=cursor
    }
    fun selectAll() { anchor=0;cursor=text.length }
    fun selectedText()=text.substring(selection.first,selection.last)
    fun insert(value:String) {
        val left=text.substring(0,selection.first);val right=text.substring(selection.last)
        val clean=value.filterNot { it.isISOControl() }
        val available=(limit-(left+right).codePointCount(0,(left+right).length)).coerceAtLeast(0)
        val clipped=clean.substring(0,clean.offsetByCodePoints(0,minOf(available,clean.codePointCount(0,clean.length))))
        text=left+clipped+right;cursor=left.length+clipped.length;anchor=cursor
    }
    fun backspace() { if(anchor!=cursor)insert("") else if(cursor>0) { anchor=text.offsetByCodePoints(cursor,-1);insert("") } }
    fun delete() { if(anchor!=cursor)insert("") else if(cursor<text.length) { anchor=text.offsetByCodePoints(cursor,1);insert("") } }
    fun move(direction:Int,select:Boolean=false) {
        cursor=if(direction<0 && cursor>0)text.offsetByCodePoints(cursor,-1) else if(direction>0 && cursor<text.length)text.offsetByCodePoints(cursor,1) else cursor
        if(!select)anchor=cursor
    }
    fun home(select:Boolean=false) { cursor=0;if(!select)anchor=cursor }
    fun end(select:Boolean=false) { cursor=text.length;if(!select)anchor=cursor }
}
