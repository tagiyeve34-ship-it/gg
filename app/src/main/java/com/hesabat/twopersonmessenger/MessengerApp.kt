package com.hesabat.twopersonmessenger

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

class MessengerApp : Application(), DefaultLifecycleObserver {
    override fun onCreate() {
        super.onCreate()
        // WebRTC is deliberately NOT initialized here. It is started only when a call opens.
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStop(owner: LifecycleOwner) {
        val p = getSharedPreferences("security", 0)
        p.edit()
            .putLong("background_at", System.currentTimeMillis())
            .putInt("background_message_id", p.getInt("last_seen_message_id", 0))
            .putBoolean("was_backgrounded", true)
            .apply()
    }

    override fun onStart(owner: LifecycleOwner) {
        val p = getSharedPreferences("security", 0)
        val delay = p.getLong("auto_clear_ms", 0L)
        val bg = p.getLong("background_at", 0L)
        val edit = p.edit().putBoolean("return_to_documents", p.getBoolean("was_backgrounded", false))
        if (delay > 0L && bg > 0L && System.currentTimeMillis() - bg >= delay) {
            val bgId = p.getInt("background_message_id", 0)
            if (bgId > 0) edit.putInt("hidden_before_id", maxOf(p.getInt("hidden_before_id", 0), bgId))
        }
        // Remove the old time-based cutoff. Server/phone timezone differences could hide new messages.
        edit.remove("cleared_before")
            .putLong("background_at", 0L)
            .putBoolean("was_backgrounded", false)
            .apply()
    }
}
