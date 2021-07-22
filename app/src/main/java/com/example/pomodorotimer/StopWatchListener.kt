package com.example.pomodorotimer

interface StopWatchListener {
    fun start(id: Int)
    fun pause(id: Int)
    fun finish(id: Int)
    fun delete(id: Int)
}