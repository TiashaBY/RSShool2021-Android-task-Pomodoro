package com.example.pomodorotimer.ui

import android.graphics.Color
import android.os.CountDownTimer
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.example.pomodorotimer.R
import com.example.pomodorotimer.StopWatchListener
import com.example.pomodorotimer.databinding.TimerItemBinding
import com.example.pomodorotimer.models.Stopwatch
import com.example.pomodorotimer.utils.START_TIME
import com.example.pomodorotimer.utils.UNIT_MS
import com.example.pomodorotimer.utils.displayTime

class StopwatchViewHolder(
    private val binding: TimerItemBinding,
    private val listener: StopWatchListener
): RecyclerView.ViewHolder(binding.root) {

    private var timer: CountDownTimer? = null
    private var vectorDot : AnimatedVectorDrawableCompat? = null

    init {
        vectorDot = AnimatedVectorDrawableCompat.create(itemView.context, R.drawable.anim_dot)
        binding.dot.setImageDrawable(vectorDot)
    }

    fun bind(stopwatch: Stopwatch) {
        initializeDefaults(stopwatch)
        val viewContext = itemView.context
            if (stopwatch.isRunning && stopwatch.currentMsec >= 0L) {
                binding.startStopButton.text = viewContext.getString(R.string.stop_button_label)
                runTimer(stopwatch)
            } else {
                binding.startStopButton.text = viewContext.getString(R.string.start_button_label)

                if (stopwatch.currentMsec == -1L) {
                    binding.startStopButton.visibility = INVISIBLE
                    itemView.setBackgroundColor(Color.rgb(220, 220, 220))
                    Log.d("run", "${stopwatch.id} finished")
                }
                stopTimer()
            }
        initButtonsListeners(stopwatch)
    }

    private fun initializeDefaults(stopwatch: Stopwatch) {
        binding.stopwatchTimer.text = stopwatch.currentMsec.displayTime()
        binding.progressBar.updateProgress(stopwatch.getProgress())
        binding.startStopButton.visibility = VISIBLE
        itemView.setBackgroundColor(Color.WHITE)
    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {
        binding.startStopButton.setOnClickListener {
            if (stopwatch.isRunning) {
                listener.pause(stopwatch.id)
            } else {
                listener.start(stopwatch.id)
            }
        }
        binding.deleteButton.setOnClickListener { listener.delete(stopwatch.id) }
    }

    private fun runTimer(stopwatch: Stopwatch) {
        binding.dot.visibility = VISIBLE
        vectorDot?.start()
        timer?.cancel()
        timer = getCountDownTimer(stopwatch)
        timer?.start()
    }

    private fun stopTimer() {
        vectorDot?.stop()
        binding.dot.visibility = INVISIBLE
        timer?.cancel()
    }

    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
        return object : CountDownTimer(stopwatch.currentMsec, UNIT_MS) {
            override fun onTick(millisUntilFinished: Long) {
                stopwatch.currentMsec = millisUntilFinished
                binding.stopwatchTimer.text = stopwatch.currentMsec.displayTime()
                binding.progressBar.updateProgress(stopwatch.getProgress())
            }
            override fun onFinish() {
                binding.stopwatchTimer.text = START_TIME
                stopwatch.isRunning = false
                stopwatch.currentMsec = -1L
                this.cancel()
                listener.finish(stopwatch.id)
            }
        }
    }
}
