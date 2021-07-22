package com.example.pomodorotimer

import android.os.Parcel
import android.os.Parcelable

data class Stopwatch(val id: Int, var currentMsec: Long, var isRunning: Boolean = false) : Parcelable {
    private var fullTimerMs: Long = 0

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte()
    ) {
        fullTimerMs = parcel.readLong()
    }

    init {
        fullTimerMs = currentMsec
    }

    fun getProgress(): Int {
        return (currentMsec * 100 / fullTimerMs).toInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeLong(currentMsec)
        parcel.writeByte(if (isRunning) 1 else 0)
        parcel.writeLong(fullTimerMs)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Stopwatch> {
        override fun createFromParcel(parcel: Parcel): Stopwatch {
            return Stopwatch(parcel)
        }

        override fun newArray(size: Int): Array<Stopwatch?> {
            return arrayOfNulls(size)
        }
    }
}
