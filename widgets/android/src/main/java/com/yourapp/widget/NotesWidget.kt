package com.yourapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews
import com.yourapp.MainActivity

class NotesWidget : AppWidgetProvider() {
    
    companion object {
        const val WIDGET_PREFS = "widget_prefs"
        const val NOTES_KEY = "widget_notes"
        const val CLICK_ACTION = "com.yourapp.widget.CLICK"
    }
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val prefs = context.getSharedPreferences(WIDGET_PREFS, Context.MODE_PRIVATE)
        val notesJson = prefs.getString(NOTES_KEY, "[]")
        
        val views = RemoteViews(context.packageName, R.layout.widget_notes)
        
        // Создаем Intent для открытия приложения
        val intent = Intent(context, MainActivity::class.java).apply {
            action = CLICK_ACTION
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
        
        // Здесь можно добавить отображение списка заметок
        // Для простоты показываем количество заметок
        views.setTextViewText(R.id.widget_text, "Заметок: $notesJson")
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == CLICK_ACTION) {
            // Запускаем MainActivity с параметрами
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("open_note_id", intent.getStringExtra("note_id"))
            }
            context.startActivity(launchIntent)
        }
    }
}