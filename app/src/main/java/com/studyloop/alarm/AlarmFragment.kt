package com.studyloop.alarm

import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.studyloop.R
import com.studyloop.core.model.AlarmEntity
import com.studyloop.databinding.FragmentAlarmBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AlarmFragment : Fragment() {

    private var _binding: FragmentAlarmBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = FragmentAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tabs = listOf("Alarms", "Stopwatch", "Countdown")

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 3
            override fun createFragment(position: Int) = when (position) {
                0 -> AlarmsListFragment()
                1 -> StopwatchFragment()
                else -> CountdownFragment()
            }
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabs[position]
        }.attach()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

// ── Alarm Adapter ─────────────────────────────────────────────────────────
class AlarmAdapter(
    private val onToggle: (AlarmEntity) -> Unit,
    private val onDelete: (AlarmEntity) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.VH>() {

    private var items = listOf<AlarmEntity>()

    fun submitList(list: List<AlarmEntity>) { items = list; notifyDataSetChanged() }

    inner class VH(val view: View) : RecyclerView.ViewHolder(view) {
        val tvTime      = view.findViewById<android.widget.TextView>(R.id.tv_time)
        val tvLabel     = view.findViewById<android.widget.TextView>(R.id.tv_label)
        val tvCountdown = view.findViewById<android.widget.TextView>(R.id.tv_countdown)
        val swToggle    = view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.sw_toggle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_alarm, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val alarm = items[position]
        val h = alarm.hour; val m = alarm.minute
        val ampm = if (h < 12) "AM" else "PM"
        val h12  = when { h == 0 -> 12; h > 12 -> h - 12; else -> h }
        holder.tvTime.text      = "%02d:%02d %s".format(h12, m, ampm)
        holder.tvLabel.text     = alarm.label + if (alarm.repeatDays.isNotEmpty()) " · ${alarm.repeatDays}" else " · Once"
        holder.tvCountdown.text = countdownText(alarm)
        holder.swToggle.isChecked = alarm.isActive
        holder.swToggle.setOnCheckedChangeListener(null)
        holder.swToggle.setOnCheckedChangeListener { _, _ -> onToggle(alarm) }
        holder.view.setOnLongClickListener { onDelete(alarm); true }
    }

    private fun countdownText(a: AlarmEntity): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, a.hour)
            set(Calendar.MINUTE, a.minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        val diff = cal.timeInMillis - System.currentTimeMillis()
        val h = diff / 3_600_000; val m = (diff % 3_600_000) / 60_000
        return "in ${h}h ${m}m"
    }
}
