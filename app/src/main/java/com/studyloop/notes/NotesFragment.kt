package com.studyloop.notes

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.studyloop.R
import com.studyloop.core.model.NoteEntity
import com.studyloop.databinding.FragmentNotesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!
    private val vm: NotesViewModel by viewModels()
    private lateinit var adapter: NoteAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = NoteAdapter { vm.deleteNote(it) }
        binding.rvNotes.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.rvNotes.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            vm.notes.collect { adapter.submitList(it) }
        }

        binding.fabAddNote.setOnClickListener { showAddDialog() }
    }

    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_note_input, null)
        val etTitle   = dialogView.findViewById<TextInputEditText>(R.id.et_note_title)
        val etContent = dialogView.findViewById<TextInputEditText>(R.id.et_note_content)
        val colors = listOf("#FFF3CD", "#D1ECF1", "#F8D7DA", "#D4EDDA", "#E2D9F3")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("📝 New Note")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title   = etTitle.text?.toString()?.trim() ?: ""
                val content = etContent.text?.toString()?.trim() ?: ""
                if (content.isNotEmpty()) {
                    vm.addNote(
                        title.ifEmpty { content.split(" ").take(3).joinToString(" ") },
                        content,
                        colors.random()
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

// ── Note Adapter ──────────────────────────────────────────────────────────
class NoteAdapter(
    private val onLongClick: (NoteEntity) -> Unit
) : RecyclerView.Adapter<NoteAdapter.VH>() {

    private var items = listOf<NoteEntity>()
    fun submitList(list: List<NoteEntity>) { items = list; notifyDataSetChanged() }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val card    = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_note)
        val tvTitle = view.findViewById<android.widget.TextView>(R.id.tv_note_title)
        val tvBody  = view.findViewById<android.widget.TextView>(R.id.tv_note_content)
        val tvDate  = view.findViewById<android.widget.TextView>(R.id.tv_note_date)
        val dotDue  = view.findViewById<View>(R.id.dot_due)
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_note, p, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val n = items[pos]
        h.card.setCardBackgroundColor(Color.parseColor(n.colorHex))
        h.tvTitle.text = n.title
        h.tvBody.text  = n.content
        val days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - n.savedAt)
        h.tvDate.text  = if (days == 0L) "Today" else "${days}d ago"
        (h.dotDue.background as? android.graphics.drawable.GradientDrawable)?.setColor(
            if (n.isDueForReview) Color.RED else Color.parseColor("#27AE60")
        )
        h.itemView.setOnLongClickListener { onLongClick(n); true }
    }
}
