package com.endiq.client.gui.components

/** Every visible action has an ID and callback; disabled actions explain why. */
class UiAction(val id: String, val label: String, val enabled: () -> Boolean = { true },
               val disabledReason: String = "Not available here", private val execute: () -> Unit) {
    fun invoke(): Boolean {
        if (!enabled()) return false
        execute();return true
    }
}
class ActionButtons(private val feedback: (String) -> Unit = {}) {
    data class Entry(val bounds: UiRect,val action: UiAction,val primary: Boolean = false,val danger: Boolean = false)
    val entries=mutableListOf<Entry>()
    fun clear()=entries.clear()
    fun add(bounds:UiRect,action:UiAction,primary:Boolean=false,danger:Boolean=false) {
        require(entries.none { it.action.id==action.id }) { "Duplicate button action: ${action.id}" }
        entries+=Entry(bounds,action,primary,danger)
    }
    fun click(mx:Double,my:Double,button:Int):Boolean {
        if(button!=0)return false
        val entry=entries.firstOrNull { it.bounds.contains(mx,my) }?:return false
        if(!entry.action.enabled()) feedback(entry.action.disabledReason)
        else runCatching { entry.action.invoke() }.onFailure { feedback("Could not ${entry.action.label.lowercase()}. Please try again.") }
        return true
    }
}
