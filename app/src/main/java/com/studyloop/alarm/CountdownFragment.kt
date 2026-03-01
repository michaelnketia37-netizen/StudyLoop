package com.studyloop.alarm

import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.studyloop.R

class CountdownFragment : Fragment() {

    private var timer: CountDownTimer? = null
    private var isRunning = false
    private var totalMs = 0L
    private var remainingMs = 0L

    private lateinit var tvCountdown: TextView
    private lateinit var npHours: NumberPicker
    private lateinit var npMinutes: NumberPicker
    private lateinit var npSeconds: NumberPicker
    private lateinit var btnStartStop: Button
    private lateinit var btnReset: Button
    private lateinit var layoutPicker: LinearLayout
    private lateinit var layoutRunning: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_countdown, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tvCountdown   = view.findViewById(R.id.tv_countdown_time)
        npHours       = view.findViewById(R.id.np_hours)
        npMinutes     = view.findViewById(R.id.np_minutes)
        npSeconds     = view.findViewById(R.id.np_seconds)
        btnStartStop  = view.findViewById(R.id.btn_countdown_start)
        btnReset      = view.findViewById(R.id.btn_countdown_reset)
        layoutPicker  = view.findViewById(R.id.layout_picker)
        layoutRunning = view.findViewById(R.id.layout_running)

        // Setup number pickers
        npHours.minValue = 0; npHours.maxValue = 23
        npMinutes.minValue = 0; npMinutes.maxValue = 59
        npSeconds.minValue = 0; npSeconds.maxValue = 59

        // Quick preset buttons
        val presets = listOf(
            Pair("5 min", 5 * 60_000L),
            Pair("10 min", 10 * 60_000L),
            Pair("25 min\n🍅 Pomodoro", 25 * 60_000L),
            Pair("30 min", 30 * 60_000L),
            Pair("1 hour", 60 * 60_000L)
        )
        val llPresets = view.findViewById<LinearLayout>(R.id.ll_presets)
        presets.forEach { (label, ms) ->
            val btn = Button(requireContext()).apply {
                text = label; textSize = 11f; isAllCaps = false
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = (8 * resources.displayMetrics.density).toInt() }
                backgroundTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#F0F0FF"))
                setTextColor(android.graphics.Color.parseColor("#6C63FF"))
                setOnClickListener { startWithMs(ms) }
            }
            llPresets.addView(btn)
        }

        btnStartStop.setOnClickListener {
            if (isRunning) pauseTimer() else startTimer()
        }
        btnReset.setOnClickListener { resetTimer() }
    }

    private fun startWithMs(ms: Long) {
        totalMs = ms; remainingMs = ms
        val h = ms / 3_600_000
        val m = (ms % 3_600_000) / 60_000
        val s = (ms % 60_000) / 1_000
        npHours.value = h.toInt()
        npMinutes.value = m.toInt()
        npSeconds.value = s.toInt()
        startTimer()
    }

    private fun startTimer() {
        if (remainingMs == 0L) {
            val h = npHours.value; val m = npMinutes.value; val s = npSeconds.value
            totalMs = (h * 3600 + m * 60 + s) * 1000L
            remainingMs = totalMs
        }
        if (remainingMs <= 0) return
        layoutPicker.visibility = View.GONE
        layoutRunning.visibility = View.VISIBLE
        isRunning = true
        btnStartStop.text = "⏸ Pause"

        timer = object : CountDownTimer(remainingMs, 100) {
            override fun onTick(ms: Long) {
                remainingMs = ms
                tvCountdown.text = formatTime(ms)
                // Progress color changes as time runs out
                val pct = ms.toFloat() / totalMs
                val color = when {
                    pct > 0.5f -> android.graphics.Color.parseColor("#6C63FF")
                    pct > 0.2f -> android.graphics.Color.parseColor("#FF6B35")
                    else       -> android.graphics.Color.parseColor("#FF4444")
                }
                tvCountdown.setTextColor(color)
            }
            override fun onFinish() {
                remainingMs = 0
                tvCountdown.text = "00:00.00"
                tvCountdown.setTextColor(android.graphics.Color.parseColor("#00C853"))
                isRunning = false
                btnStartStop.text = "▶ Start"
                showFinishedToast()
            }
        }.start()
    }

    private fun pauseTimer() {
        timer?.cancel()
        isRunning = false
        btnStartStop.text = "▶ Resume"
    }

    private fun resetTimer() {
        timer?.cancel()
        isRunning = false; remainingMs = 0L; totalMs = 0L
        layoutPicker.visibility = View.VISIBLE
        layoutRunning.visibility = View.GONE
        btnStartStop.text = "▶ Start"
        tvCountdown.text = "00:00.00"
        tvCountdown.setTextColor(android.graphics.Color.parseColor("#6C63FF"))
    }

    private fun showFinishedToast() {
        Toast.makeText(requireContext(), "⏰ Timer finished!", Toast.LENGTH_LONG).show()
    }

    private fun formatTime(ms: Long): String {
        val h  = ms / 3_600_000
        val m  = (ms % 3_600_000) / 60_000
        val s  = (ms % 60_000) / 1_000
        val cs = (ms % 1_000) / 10
        return if (h > 0) "%02d:%02d:%02d".format(h, m, s)
        else "%02d:%02d.%02d".format(m, s, cs)
    }

    override fun onDestroyView() { super.onDestroyView(); timer?.cancel() }
}
