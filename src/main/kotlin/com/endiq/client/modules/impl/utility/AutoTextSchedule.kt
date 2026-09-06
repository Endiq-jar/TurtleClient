package com.endiq.client.modules.impl.utility

/** Monotonic scheduler: toggling repeat live starts/stops it without a rejoin. */
class AutoTextSchedule(private val now:()->Long={System.nanoTime()/1_000_000}) {
    private var deadline:Long?=null
    private var joinPending=false
    fun reset() { deadline=null;joinPending=false }
    fun joined(send:Boolean,delayMillis:Long) {
        reset()
        if(send) { joinPending=true;deadline=now()+delayMillis.coerceAtLeast(0) }
    }
    fun due(joinEnabled:Boolean,repeat:Boolean,repeatMillis:Long):Boolean {
        val time=now();val interval=repeatMillis.coerceAtLeast(5000)
        if((joinPending && !joinEnabled) || (!joinPending && !repeat))reset()
        if(deadline==null && repeat)deadline=time+interval
        val at=deadline ?: return false
        if(time<at)return false
        joinPending=false;deadline=if(repeat)time+interval else null
        return true
    }
}
