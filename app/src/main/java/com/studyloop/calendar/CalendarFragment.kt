package com.studyloop.calendar

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.studyloop.R
import com.studyloop.core.model.ReviewEntity
import com.studyloop.databinding.FragmentCalendarBinding
import com.studyloop.reminder.ReminderViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private val vm: ReminderViewModel by viewModels()
    private var currentMonth = Calendar.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnPrevMonth.setOnClickListener {
            currentMonth.add(Calendar.MONTH, -1)
            loadCalendar()
        }
        binding.btnNextMonth.setOnClickListener {
            currentMonth.add(Calendar.MONTH, 1)
            loadCalendar()
        }
        loadCalendar()
    }

    private fun loadCalendar() {
        viewLifecycleOwner.lifecycleScope.launch {
            val reminders = vm.reminders.first()
            val allReviews = mutableListOf<ReviewEntity>()
            reminders.forEach { r ->
                allReviews.addAll(vm.getReviews(r.id).first())
            }
            buildCalendar(allReviews)
        }
    }

    private fun buildCalendar(reviews: List<ReviewEntity>) {
        val fmt = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.tvMonthYear.text = fmt.format(currentMonth.time).uppercase()

        val grid = binding.gridCalendar
        grid.removeAllViews()

        // Day headers
        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
            grid.addView(TextView(requireContext()).apply {
                text = day; textSize = 10f; gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#9090A8"))
                layoutParams = GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f)
                ).apply { width = 0; height = (32 * resources.displayMetrics.density).toInt() }
            })
        }

        // Build review date map
        val dayFmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val reviewMap = mutableMapOf<String, MutableList<ReviewEntity>>()
        reviews.forEach { r ->
            val key = dayFmt.format(Date(r.scheduledAt))
            reviewMap.getOrPut(key) { mutableListOf() }.add(r)
        }

        val cal = currentMonth.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startDow = cal.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today = dayFmt.format(Date())

        // Empty cells before month start
        repeat(startDow) {
            grid.addView(View(requireContext()).apply {
                layoutParams = GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f)
                ).apply { width = 0; height = (48 * resources.displayMetrics.density).toInt() }
            })
        }

        // Day cells
        for (day in 1..daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val key = dayFmt.format(cal.time)
            val dayReviews = reviewMap[key] ?: emptyList()
            val isToday = key == today

            val cell = FrameLayout(requireContext()).apply {
                layoutParams = GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f)
                ).apply {
                    width = 0
                    height = (52 * resources.displayMetrics.density).toInt()
                    val m = (2 * resources.displayMetrics.density).toInt()
                    setMargins(m, m, m, m)
                }
                background = android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius = 8 * resources.displayMetrics.density
                    setColor(when {
                        isToday -> Color.parseColor("#EDE9FF")
                        dayReviews.any { it.completedAt != null } -> Color.parseColor("#E8F5E9")
                        dayReviews.any { it.scheduledAt < System.currentTimeMillis() && it.completedAt == null } -> Color.parseColor("#FFEBEE")
                        dayReviews.isNotEmpty() -> Color.parseColor("#FFF8E1")
                        else -> Color.TRANSPARENT
                    })
                    if (isToday) setStroke((2 * resources.displayMetrics.density).toInt(), Color.parseColor("#6C63FF"))
                }
            }

            // Day number
            cell.addView(TextView(requireContext()).apply {
                text = day.toString(); textSize = 13f; gravity = Gravity.CENTER
                setTextColor(if (isToday) Color.parseColor("#6C63FF") else Color.parseColor("#1A1A2E"))
                if (isToday) setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            })

            // Review count dot
            if (dayReviews.isNotEmpty()) {
                cell.addView(TextView(requireContext()).apply {
                    text = "${dayReviews.size}"
                    textSize = 8f; gravity = Gravity.CENTER
                    val size = (16 * resources.displayMetrics.density).toInt()
                    layoutParams = FrameLayout.LayoutParams(size, size, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL).apply {
                        bottomMargin = (3 * resources.displayMetrics.density).toInt()
                    }
                    background = android.graphics.drawable.GradientDrawable().apply {
                        shape = android.graphics.drawable.GradientDrawable.OVAL
                        setColor(when {
                            dayReviews.all { it.completedAt != null } -> Color.parseColor("#00C853")
                            dayReviews.any { it.scheduledAt < System.currentTimeMillis() && it.completedAt == null } -> Color.parseColor("#FF4444")
                            else -> Color.parseColor("#FF6B35")
                        })
                    }
                    setTextColor(Color.WHITE)
                })
            }

            // Click to show reviews for that day
            cell.setOnClickListener {
                if (dayReviews.isNotEmpty()) showDayReviews(day, dayReviews)
            }

            grid.addView(cell)
        }

        // Legend
        updateLegend(reviews)
    }

    private fun showDayReviews(day: Int, reviews: List<ReviewEntity>) {
        val msg = reviews.joinToString("\n") { r ->
            val status = when {
                r.completedAt != null -> "✅ Done"
                r.scheduledAt < System.currentTimeMillis() -> "🔴 Missed"
                else -> "🟡 Upcoming"
            }
            "$status — Review #${r.reviewNumber}"
        }
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("📅 Day $day — ${reviews.size} review(s)")
            .setMessage(msg)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun updateLegend(reviews: List<ReviewEntity>) {
        val done    = reviews.count { it.completedAt != null }
        val missed  = reviews.count { it.scheduledAt < System.currentTimeMillis() && it.completedAt == null }
        val upcoming = reviews.count { it.scheduledAt >= System.currentTimeMillis() }
        binding.tvLegend.text = "✅ $done done  •  🔴 $missed missed  •  🟡 $upcoming upcoming"
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
