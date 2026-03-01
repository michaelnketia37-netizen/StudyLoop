package com.studyloop.reminder

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.studyloop.R
import com.studyloop.core.model.ReminderEntity
import com.studyloop.core.model.ReviewEntity
import com.studyloop.databinding.FragmentReminderBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReminderFragment : Fragment() {

    private var _binding: FragmentReminderBinding? = null
    private val binding get() = _binding!!
    private val vm: ReminderViewModel by viewModels()
    private lateinit var adapter: ReminderAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = FragmentReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ReminderAdapter(
            onItemClick = { reminder ->
                findNavController().navigate(
                    R.id.action_reminder_to_detail,
                    bundleOf("REMINDER_ID" to reminder.id)
                )
            },
            getReviews = { reminderId ->
                viewLifecycleOwner.lifecycleScope.launch {
                    vm.getReviews(reminderId).first()
                }
            },
            vm = vm
        )
        binding.rvReminders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReminders.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            vm.reminders.collect { adapter.submitList(it) }
        }

        binding.fabAddReminder.setOnClickListener { showAddDialog() }
    }

    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reminder_input, null)
        val et = dialogView.findViewById<TextInputEditText>(R.id.et_reminder_content)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("🧠 New Reminder")
            .setView(dialogView)
            .setPositiveButton("Save + Schedule 7 Reviews") { _, _ ->
                val txt = et.text?.toString()?.trim() ?: return@setPositiveButton
                if (txt.isNotEmpty()) vm.addReminder(txt)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

// ── Reminder Adapter ──────────────────────────────────────────────────────
class ReminderAdapter(
    private val onItemClick: (ReminderEntity) -> Unit,
    private val getReviews: (String) -> Unit,
    private val vm: ReminderViewModel
) : RecyclerView.Adapter<ReminderAdapter.VH>() {

    private var items = listOf<ReminderEntity>()
    private val reviewCache = mutableMapOf<String, List<ReviewEntity>>()

    fun submitList(list: List<ReminderEntity>) { items = list; notifyDataSetChanged() }
    fun updateReviews(reminderId: String, reviews: List<ReviewEntity>) {
        reviewCache[reminderId] = reviews; notifyDataSetChanged()
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmoji   = view.findViewById<android.widget.TextView>(R.id.tv_emoji)
        val tvTitle   = view.findViewById<android.widget.TextView>(R.id.tv_title)
        val tvPreview = view.findViewById<android.widget.TextView>(R.id.tv_preview)
        val tvRet     = view.findViewById<android.widget.TextView>(R.id.tv_retention)
        val miniChart = view.findViewById<LineChart>(R.id.mini_chart)
        val llRings   = view.findViewById<android.widget.LinearLayout>(R.id.ll_rings)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_reminder, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val rem = items[position]
        val reviews = reviewCache[rem.id] ?: emptyList()
        holder.tvEmoji.text   = rem.emoji
        holder.tvTitle.text   = rem.title
        holder.tvPreview.text = rem.content
        holder.tvRet.text     = "${vm.retentionPercent(rem, reviews)}% · ${vm.nextReviewCountdown(rem)}"
        holder.itemView.setOnClickListener { onItemClick(rem) }
        setupMiniChart(holder.miniChart, rem, reviews)
        setupRings(holder.llRings, reviews)
        getReviews(rem.id)
    }

    private fun setupMiniChart(chart: LineChart, rem: ReminderEntity, reviews: List<ReviewEntity>) {
        chart.apply {
            setTouchEnabled(false); description.isEnabled = false
            legend.isEnabled = false; setDrawGridBackground(false)
            axisLeft.isEnabled = false; axisRight.isEnabled = false
            xAxis.isEnabled = false; setBackgroundColor(Color.TRANSPARENT)
        }
        val pts = vm.generateCurvePoints(rem, reviews)
        if (pts.isEmpty()) return
        val entries = pts.map { (day, ret) -> Entry(day, ret) }
        val ds = LineDataSet(entries, "").apply {
            color = Color.parseColor("#6C63FF"); lineWidth = 1.5f
            setDrawCircles(false); setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true); fillColor = Color.parseColor("#6C63FF"); fillAlpha = 40
        }
        chart.data = LineData(ds); chart.invalidate()
    }

    private fun setupRings(ll: android.widget.LinearLayout, reviews: List<ReviewEntity>) {
        ll.removeAllViews()
        val days = listOf(1, 3, 7, 14, 30, 60, 120)
        val ctx = ll.context
        reviews.forEachIndexed { i, r ->
            val tv = android.widget.TextView(ctx).apply {
                text = days.getOrNull(i)?.toString() ?: "-"
                textSize = 8f; gravity = android.view.Gravity.CENTER
                val size = (20 * ctx.resources.displayMetrics.density).toInt()
                layoutParams = android.widget.LinearLayout.LayoutParams(size, size).apply {
                    marginEnd = (5 * ctx.resources.displayMetrics.density).toInt()
                }
                val color = when {
                    r.completedAt != null -> Color.parseColor("#43E97B")
                    r.scheduledAt < System.currentTimeMillis() -> Color.parseColor("#FF6584")
                    else -> Color.TRANSPARENT
                }
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(color)
                    if (r.completedAt == null) {
                        setStroke((2 * ctx.resources.displayMetrics.density).toInt(),
                            if (r.scheduledAt < System.currentTimeMillis())
                                Color.parseColor("#FF6584") else Color.parseColor("#F9CA24"))
                    }
                }
                setTextColor(if (r.completedAt != null) Color.BLACK else Color.parseColor("#F9CA24"))
            }
            ll.addView(tv)
        }
    }
}
