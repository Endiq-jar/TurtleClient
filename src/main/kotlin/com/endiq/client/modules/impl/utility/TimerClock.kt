package com.endiq.client.modules.impl.utility

/** Monotonic stopwatch/countdown. Pausing does not discard elapsed time. */
class TimerClock(private val now:()->Long={System.nanoTime()/1_000_000}) {
    var running=false
        private set
    var started=false
        private set
    var duration=60000L
        private set
    private var accumulated=0L
    private var since=0L
    fun start(seconds:Int) {
        if(running)return
        if(!started) { duration=seconds.coerceIn(1,3600)*1000L;accumulated=0 }
        started=true;running=true;since=now()
    }
    fun elapsed()=accumulated+if(running)(now()-since).coerceAtLeast(0) else 0
    fun remaining()=(duration-elapsed()).coerceAtLeast(0)
    fun pause() { accumulated=elapsed();running=false }
    fun reset() { accumulated=0;running=false;started=false }
}
