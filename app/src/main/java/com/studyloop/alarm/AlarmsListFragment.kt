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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.studyloop.R
import com.studyloop.databinding.FragmentAlarmsListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AlarmsListFragment : Fragment() {

    private var _binding: FragmentAlarmsListBinding? = null
    private val binding get() = _binding!!
    private val vm: AlarmViewModel by viewModels()
    private val clockHandler = Handler(Looper.getMainLooper())
    private lateinit var adapter: AlarmAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = FragmentAlarmsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = AlarmAdapter(
            onToggle = { vm.toggleAlarm(it) },
            onDelete = { vm.deleteAlarm(it) }
        )
        binding.rvAlarms.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAlarms.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            vm.alarms.collect { list ->
                adapter.submitList(list)
                binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                binding.rvAlarms.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        binding.fabAddAlarm.setOnClickListener { showAddAlarmDialog() }
        startClock()
    }

    private fun startClock() {
        val fmt  = SimpleDateFormat("HH:mm", Locale.getDefault())
        val fmtD = SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault())
        val tick = object : Runnable {
            override fun run() {
                val now = Date()
                binding.tvClock.text = fmt.format(now)
                binding.tvDate.text  = fmtD.format(now).uppercase()
                clockHandler.postDelayed(this, 1000)
            }
        }
        clockHandler.post(tick)
    }

    private fun showAddAlarmDialog() {
        val cal = Calendar.getInstance()
        TimePickerDialog(requireContext(), { _, hour, min ->
            val dialogView = layoutInflater.inflate(R.layout.dialog_alarm_label, null)
            val etLabel = dialogView.findViewById<TextInputEditText>(R.id.et_alarm_label)
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Alarm Label")
                .setView(dialogView)
                .setPositiveButton("Save") { _, _ ->
                    val label = etLabel.text?.toString()?.trim()?.ifEmpty { "Alarm" } ?: "Alarm"
                    vm.addAlarm(hour, min, label, "")
                }
                .setNegativeButton("Cancel", null)
                .show()
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clockHandler.removeCallbacksAndMessages(null)
        _binding = null
    }
}
