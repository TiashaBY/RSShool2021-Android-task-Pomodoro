package com.example.pomodorotimer.utils

fun Long.displayTime(): String {
    if (this <= 0L) {
        return START_TIME
    }

    val msToVisualize = this + 999 //to show how many seconds really passed, so for the last one it will be 1 and 0 at the 'real' end
    val h = msToVisualize / 1000 / 3600
    val m = msToVisualize / 1000 % 3600 / 60
    val s = msToVisualize / 1000 % 60

    return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}"
}

private fun displaySlot(count: Long): String {
    return if (count / 10L > 0) {
        "$count"
    } else {
        "0$count"
    }
}