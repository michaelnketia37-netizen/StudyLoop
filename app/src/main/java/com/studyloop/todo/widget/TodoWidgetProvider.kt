package com.studyloop.todo.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.studyloop.MainActivity
import com.studyloop.R

class TodoWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(ctx: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { appWidgetId ->
            val rv = RemoteViews(ctx.packageName, R.layout.widget_todo).apply {
                setRemoteAdapter(R.id.widget_list,
                    Intent(ctx, TodoWidgetService::class.java))
                val clickIntent = PendingIntent.getActivity(
                    ctx, 0,
                    Intent(ctx, MainActivity::class.java).apply { putExtra("TAB", "todo") },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widget_title, clickIntent)
            }
            manager.updateAppWidget(appWidgetId, rv)
        }
    }
}
