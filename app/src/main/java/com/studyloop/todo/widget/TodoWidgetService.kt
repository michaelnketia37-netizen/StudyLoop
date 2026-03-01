package com.studyloop.todo.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.studyloop.R
import com.studyloop.core.database.AppDatabase
import com.studyloop.core.model.TodoEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class TodoWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent) = TodoViewsFactory(applicationContext)
}

class TodoViewsFactory(private val ctx: Context) : RemoteViewsService.RemoteViewsFactory {
    private var todos = listOf<TodoEntity>()

    override fun onCreate() { loadData() }
    override fun onDataSetChanged() { loadData() }
    override fun onDestroy() {}
    override fun getCount() = todos.size
    override fun getViewTypeCount() = 1
    override fun hasStableIds() = true
    override fun getItemId(pos: Int) = pos.toLong()

    private fun loadData() {
        runBlocking {
            todos = AppDatabase.getInstance(ctx).todoDao().getAll().first()
                .filter { !it.isDone }.take(8)
        }
    }

    override fun getView(pos: Int, rv: RemoteViews?, parent: android.view.ViewGroup?): RemoteViews {
        val todo = todos.getOrNull(pos) ?: return RemoteViews(ctx.packageName, R.layout.widget_item_todo)
        return RemoteViews(ctx.packageName, R.layout.widget_item_todo).apply {
            setTextViewText(R.id.widget_todo_text, todo.text)
        }
    }

    override fun getLoadingView(): RemoteViews? = null
}
