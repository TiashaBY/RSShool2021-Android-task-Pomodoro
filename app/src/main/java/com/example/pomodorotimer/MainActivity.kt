package com.example.pomodorotimer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodorotimer.databinding.ActivityMainBinding
import com.example.pomodorotimer.models.Stopwatch
import com.example.pomodorotimer.ui.StopwatchAdapter
import com.example.pomodorotimer.utils.*


class MainActivity : AppCompatActivity(), StopWatchListener, LifecycleObserver {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = checkNotNull(_binding)

    private var stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0
    private val stopwatchAdapter = StopwatchAdapter(this)

    private var isInForeground = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        savedInstanceState?.getBundle(SAVED_BUNDLE)?.let {
            stopwatches = it.get(STOPWATCHES) as MutableList<Stopwatch>
            nextId = it.getInt(INDEX)
            stopwatchAdapter.submitList(stopwatches)
        }

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = stopwatchAdapter

        }

        (binding.minutes as EditText).filters = arrayOf(MinMaxFilter("0", "480"))
        (binding.seconds as EditText).filters = arrayOf(MinMaxFilter("0", "60"))

        binding.add.setOnClickListener {
            getTimerMs()?.let {
                stopwatches.add(Stopwatch(nextId++, it, false))
                stopwatchAdapter.submitList(stopwatches.toList())
                stopwatchAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun getTimerMs() : Long? {
        val min = (binding.minutes  as EditText).text.toString()
        val sec = (binding.seconds as EditText).text.toString()
        return if (min.isEmpty() && sec.isEmpty()) {
            Toast.makeText(applicationContext, getString(R.string.no_time_error), Toast.LENGTH_SHORT).show()
            null
        } else {
            try {
                var minutes = 0
                if (min.isNotEmpty()) minutes = min.toInt()

                var seconds = 0
                if (sec.isNotEmpty()) seconds = sec.toInt()

                if (minutes + seconds == 0) {
                    showToast(getString(R.string.no_time_error))
                    null
                } else {
                    (((minutes * 60) + seconds) * 1000).toLong()
                }
            } catch (ex: NumberFormatException) {
                showToast(getString(R.string.incorrect_number))
                null
            }
        }
    }

    private fun showToast(msg: String) {
        if (isInForeground) {
            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun start(id: Int) {
        stopwatches.filter { it.isRunning && it.id !=id }.map { pause(it.id) }
        modifyStopwatch(id, null, true)
    }

    override fun pause(id: Int) {
        modifyStopwatch(id, null, false)
    }

    override fun finish(id: Int) {
        val index = stopwatches.indexOfFirst { it.id == id }
        modifyStopwatch(id, stopwatches[index].fullTimerMs, false)
        showToast(getString(R.string.timer_done))
    }

    override fun delete(id: Int) {
        val index = stopwatches.indexOfFirst { it.id == id }
        stopwatches.removeAt(index)
        stopwatchAdapter.submitList(stopwatches.toList())
        stopwatchAdapter.notifyDataSetChanged()
    }

    private fun modifyStopwatch(id: Int, ms: Long?, isRunning: Boolean) {
        stopwatches.find { it.id == id }.apply {
            ms?.let { this?.currentMsec = ms }
            this?.isRunning = isRunning
        }
        stopwatchAdapter.submitList(stopwatches.toList())
        stopwatchAdapter.notifyDataSetChanged()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        Log.d("app", "app is backgrounded")
        if (stopwatches.isNotEmpty()) {
            val runningStopwatch = stopwatches.firstOrNull { it.isRunning }
            runningStopwatch?.let {
                val startIntent = Intent(this, ForegroundService::class.java)
                startIntent.putExtra(COMMAND_ID, COMMAND_START)
                startIntent.putExtra(STARTED_TIMER_TIME_MS, it.currentMsec)
                startService(startIntent)
            }
        }
        isInForeground = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        Log.d("app", "app is foregrounded")
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
        isInForeground = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBundle(SAVED_BUNDLE, bundleOf(STOPWATCHES to stopwatches, INDEX to nextId) )
        super.onSaveInstanceState(outState)
    }
}


