package com.studyloop.alarm

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.studyloop.R

class StopwatchFragment : Fragment() {

    private var isRunning = false
    private var elapsedMs = 0L
    private var startTime = 0L
    private val laps = mutableListOf<Long>()
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var tvTime: TextView
    private lateinit var btnStartStop: Button
    private lateinit var btnLap: Button
    private lateinit var btnReset: Button
    private lateinit var lvLaps: ListView
    private lateinit var lapAdapter: ArrayAdapter<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_stopwatch, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tvTime       = view.findViewById(R.id.tv_stopwatch_time)
        btnStartStop = view.findViewById(R.id.btn_start_stop)
        btnLap       = view.findViewById(R.id.btn_lap)
        btnReset     = view.findViewById(R.id.btn_reset)
        lvLaps       = view.findViewById(R.id.lv_laps)

        lapAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, mutableListOf())
        lvLaps.adapter = lapAdapter

        btnStartStop.setOnClickListener { toggleStartStop() }
        btnLap.setOnClickListener { recordLap() }
        btnReset.setOnClickListener { reset() }
        updateDisplay()
    }

    private fun toggleStartStop() {
        if (isRunning) {
            elapsedMs += System.currentTimeMillis() - startTime
            isRunning = false
            handler.removeCallbacksAndMessages(null)
            btnStartStop.text = "▶ Start"
            btnStartStop.backgroundTintList =
                android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#00C853"))
        } else {
            startTime = System.currentTimeMillis()
            isRunning = true
            btnStartStop.text = "⏸ Pause"
            btnStartStop.backgroundTintList =
                android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FF6B35"))
            tick()
        }
    }

    private val ticker = object : Runnable {
        override fun run() {
            updateDisplay()
            handler.postDelayed(this, 16)
        }
    }

    private fun tick() { handler.post(ticker) }

    private fun updateDisplay() {
        val total = if (isRunning) elapsedMs + (System.currentTimeMillis() - startTime) else elapsedMs
        tvTime.text = formatTime(total)
    }

    private fun recordLap() {
        if (!isRunning && elapsedMs == 0L) return
        val total = if (isRunning) elapsedMs + (System.currentTimeMillis() - startTime) else elapsedMs
        laps.add(0, total)
        val prev = if (laps.size > 1) laps[1] else 0L
        val lapTime = total - prev
        lapAdapter.insert("Lap ${laps.size}    ${formatTime(lapTime)}    ${formatTime(total)}", 0)
        lapAdapter.notifyDataSetChanged()
    }

    private fun reset() {
        handler.removeCallbacksAndMessages(null)
        isRunning = false; elapsedMs = 0L; laps.clear()
        lapAdapter.clear(); lapAdapter.notifyDataSetChanged()
        btnStartStop.text = "▶ Start"
        btnStartStop.backgroundTintList =
            android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#00C853"))
        updateDisplay()
    }

    private fun formatTime(ms: Long): String {
        val h   = ms / 3_600_000
        val m   = (ms % 3_600_000) / 60_000
        val s   = (ms % 60_000) / 1_000
        val cs  = (ms % 1_000) / 10
        return if (h > 0) "%02d:%02d:%02d.%02d".format(h, m, s, cs)
        else "%02d:%02d.%02d".format(m, s, cs)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }
}
