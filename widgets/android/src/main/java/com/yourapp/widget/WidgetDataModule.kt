package com.yourapp.widget

import android.content.Context
import android.content.SharedPreferences
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import org.json.JSONArray
import org.json.JSONObject

class WidgetDataModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    
    override fun getName(): String = "WidgetDataModule"
    
    private fun getWidgetPrefs(): SharedPreferences {
        return reactApplicationContext.getSharedPreferences(
            NotesWidget.WIDGET_PREFS, 
            Context.MODE_PRIVATE
        )
    }
    
    @ReactMethod
    fun updateWidgetNotes(notesArray: ReadableArray) {
        val notesJson = JSONArray()
        for (i in 0 until notesArray.size()) {
            val note = notesArray.getMap(i)
            val noteObj = JSONObject()
            noteObj.put("id", note.getString("id"))
            noteObj.put("title", note.getString("title"))
            noteObj.put("content", note.getString("content"))
            notesJson.put(noteObj)
        }
        
        getWidgetPrefs().edit()
            .putString(NotesWidget.NOTES_KEY, notesJson.toString())
            .apply()
        
        // Принудительное обновление виджета
        val intent = android.content.Intent(reactApplicationContext, NotesWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        reactApplicationContext.sendBroadcast(intent)
    }
}