package com.studyloop.reminder

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.studyloop.databinding.FragmentReminderDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReminderDetailFragment : Fragment() {

    private var _binding: FragmentReminderDetailBinding? = null
    private val binding get() = _binding!!
    private val vm: ReminderViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = FragmentReminderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val reminderId = arguments?.getString("REMINDER_ID") ?: return
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        viewLifecycleOwner.lifecycleScope.launch {
            val remindersFlow = vm.reminders
            val reviewsFlow   = vm.getReviews(reminderId)

            remindersFlow.combine(reviewsFlow) { list, reviews ->
                Pair(list.find { it.id == reminderId }, reviews)
            }.collect { (reminder, reviews) ->
                reminder ?: return@collect
                binding.toolbar.title = reminder.title
                binding.tvContent.text = reminder.content
                binding.tvRetentionVal.text = "${vm.retentionPercent(reminder, reviews)}%"
                binding.tvNextReview.text   = vm.nextReviewCountdown(reminder)
                setupChart(binding.forgettingChart, reminder, reviews)
                setupTimeline(reminder, reviews)
            }
        }

        binding.btnReviewed.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val reminder = vm.reminders.value.find { it.id == reminderId } ?: return@launch
                val reviews  = vm.getReviews(reminderId).value ?: emptyList()
                vm.markReviewed(reminder, reviews)
            }
        }
    }

    private fun setupChart(chart: LineChart, rem: com.studyloop.core.model.ReminderEntity, reviews: List<com.studyloop.core.model.ReviewEntity>) {
        val pts = vm.generateCurvePoints(rem, reviews)
        if (pts.isEmpty()) return

        // Decay curve
        val entries = pts.map { (d, r) -> Entry(d, r) }
        val decay = LineDataSet(entries, "Retention %").apply {
            color = Color.parseColor("#6C63FF"); lineWidth = 2.5f
            setDrawCircles(false); setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true); fillColor = Color.parseColor("#6C63FF"); fillAlpha = 40
        }

        // Review ring markers
        val reviewDays = SpacedRepetitionEngine.REVIEW_DAYS
        val ringEntries = reviewDays.mapIndexed { i, day ->
            val ret = pts.find { it.first == day.toFloat() }?.second ?: 0f
            Entry(day.toFloat(), ret)
        }
        val ringColors = reviews.map { r ->
            when {
                r.completedAt != null -> Color.parseColor("#43E97B")
                r.scheduledAt < System.currentTimeMillis() -> Color.parseColor("#FF6584")
                else -> Color.parseColor("#F9CA24")
            }
        }
        val rings = LineDataSet(ringEntries, "Reviews").apply {
            setDrawCircles(true); circleRadius = 7f
            setDrawValues(false); setDrawLines(false); color = Color.TRANSPARENT
            circleColors = ringColors
        }

        chart.apply {
            data = LineData(decay, rings)
            setBackgroundColor(Color.parseColor("#13131A"))
            setGridBackgroundColor(Color.TRANSPARENT)
            description.isEnabled = false
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.parseColor("#9090A8"); gridColor = Color.parseColor("#2A2A3F")
                setLabelCount(7, true)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(v: Float) = "d${v.toInt()}"
                }
            }
            axisLeft.apply {
                textColor = Color.parseColor("#9090A8"); gridColor = Color.parseColor("#2A2A3F")
                axisMinimum = 0f; axisMaximum = 100f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(v: Float) = "${v.toInt()}%"
                }
            }
            axisRight.isEnabled = false
            legend.textColor = Color.parseColor("#9090A8")
            invalidate()
        }
    }

    private fun setupTimeline(rem: com.studyloop.core.model.ReminderEntity, reviews: List<com.studyloop.core.model.ReviewEntity>) {
        binding.llTimeline.removeAllViews()
        val days = SpacedRepetitionEngine.REVIEW_DAYS
        val ctx = requireContext()
        reviews.forEachIndexed { i, r ->
            val col = android.widget.LinearLayout(ctx).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                val sz = (56 * ctx.resources.displayMetrics.density).toInt()
                layoutParams = android.widget.LinearLayout.LayoutParams(sz, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            val ring = android.widget.TextView(ctx).apply {
                val sz = (28 * ctx.resources.displayMetrics.density).toInt()
                layoutParams = android.widget.LinearLayout.LayoutParams(sz, sz)
                gravity = android.view.Gravity.CENTER; textSize = 9f
                text = days.getOrNull(i)?.toString() ?: ""
                val done = r.completedAt != null
                val missed = !done && r.scheduledAt < System.currentTimeMillis()
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(when { done -> Color.parseColor("#43E97B"); missed -> Color.parseColor("#33FF6584"); else -> Color.TRANSPARENT })
                    if (!done) setStroke((2 * ctx.resources.displayMetrics.density).toInt(),
                        if (missed) Color.parseColor("#FF6584") else Color.parseColor("#F9CA24"))
                }
                setTextColor(if (done) Color.BLACK else Color.parseColor("#F9CA24"))
            }
            val label = android.widget.TextView(ctx).apply {
                text = "Day ${days.getOrNull(i)}"; textSize = 8f
                gravity = android.view.Gravity.CENTER
                setTextColor(Color.parseColor("#5A5A72"))
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = (4 * ctx.resources.displayMetrics.density).toInt() }
            }
            col.addView(ring); col.addView(label)
            binding.llTimeline.addView(col)
        }
    }

    // workaround: collect Flow<List<T>> as value
    private val <T> kotlinx.coroutines.flow.Flow<List<T>>.value: List<T>?
        get() = null  // will be collected via lifecycleScope

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
