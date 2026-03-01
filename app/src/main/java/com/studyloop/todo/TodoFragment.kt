package com.studyloop.todo

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.studyloop.R
import com.studyloop.core.model.TodoEntity
import com.studyloop.databinding.FragmentTodoBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TodoFragment : Fragment() {

    private var _binding: FragmentTodoBinding? = null
    private val binding get() = _binding!!
    private val vm: TodoViewModel by viewModels()
    private lateinit var adapter: TodoAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = FragmentTodoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = TodoAdapter(
            onToggle = { vm.toggleTodo(it) },
            onDelete = { vm.deleteTodo(it) }
        )
        binding.rvTodos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTodos.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            vm.todos.collect { todos ->
                adapter.submitList(todos)
                val done  = todos.count { it.isDone }
                val total = todos.size
                val pct   = if (total > 0) (done * 100 / total) else 0
                binding.tvProgressText.text = "$done of $total done"
                binding.tvProgressPct.text  = "$pct%"
                binding.progressBar.progress = pct
            }
        }

        binding.btnAddTodo.setOnClickListener { addTodo() }
        binding.etTodo.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) { addTodo(); true } else false
        }
    }

    private fun addTodo() {
        val text = binding.etTodo.text?.toString()?.trim() ?: return
        if (text.isEmpty()) return
        val colors = listOf("#FF6584", "#F9CA24", "#6C63FF", "#43E97B", "#4FC3F7")
        vm.addTodo(text)
        binding.etTodo.setText("")
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

// ── Todo Adapter ──────────────────────────────────────────────────────────
class TodoAdapter(
    private val onToggle: (TodoEntity) -> Unit,
    private val onDelete: (TodoEntity) -> Unit
) : RecyclerView.Adapter<TodoAdapter.VH>() {

    private var items = listOf<TodoEntity>()
    fun submitList(list: List<TodoEntity>) { items = list; notifyDataSetChanged() }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val cb      = view.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.cb_done)
        val tvTask  = view.findViewById<android.widget.TextView>(R.id.tv_task)
        val dotPri  = view.findViewById<View>(R.id.priority_dot)
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_todo, p, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val todo = items[pos]
        h.cb.isChecked = todo.isDone
        h.tvTask.text  = todo.text
        h.tvTask.alpha = if (todo.isDone) 0.5f else 1f
        h.tvTask.paintFlags = if (todo.isDone)
            h.tvTask.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        else
            h.tvTask.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
        (h.dotPri.background as? android.graphics.drawable.GradientDrawable)
            ?.setColor(Color.parseColor(todo.priorityColor))
        h.cb.setOnCheckedChangeListener(null)
        h.cb.setOnCheckedChangeListener { _, _ -> onToggle(todo) }
        h.itemView.setOnLongClickListener { onDelete(todo); true }
    }
}
