package com.homelab.app.data

import android.content.Context

/** Простое хранилище настроек на SharedPreferences. */
class Prefs(context: Context) {
    private val sp = context.applicationContext
        .getSharedPreferences("homelab", Context.MODE_PRIVATE)

    var qbUrl: String
        get() = sp.getString(KEY_QB_URL, "http://192.168.0.107:8070")!!
        set(v) = sp.edit().putString(KEY_QB_URL, v.trimEnd('/')).apply()

    var qbUser: String
        get() = sp.getString(KEY_QB_USER, "admin")!!
        set(v) = sp.edit().putString(KEY_QB_USER, v).apply()

    var qbPass: String
        get() = sp.getString(KEY_QB_PASS, "")!!
        set(v) = sp.edit().putString(KEY_QB_PASS, v).apply()

    var ntfyUrl: String
        get() = sp.getString(KEY_NTFY_URL, "https://ntfy.bad-cat.duckdns.org")!!
        set(v) = sp.edit().putString(KEY_NTFY_URL, v.trimEnd('/')).apply()

    var ntfyTopic: String
        get() = sp.getString(KEY_NTFY_TOPIC, "homelab")!!
        set(v) = sp.edit().putString(KEY_NTFY_TOPIC, v).apply()

    var ntfyToken: String
        get() = sp.getString(KEY_NTFY_TOKEN, "")!!
        set(v) = sp.edit().putString(KEY_NTFY_TOKEN, v).apply()

    var pushEnabled: Boolean
        get() = sp.getBoolean(KEY_PUSH_ENABLED, false)
        set(v) = sp.edit().putBoolean(KEY_PUSH_ENABLED, v).apply()

    companion object {
        private const val KEY_QB_URL = "qbUrl"
        private const val KEY_QB_USER = "qbUser"
        private const val KEY_QB_PASS = "qbPass"
        private const val KEY_NTFY_URL = "ntfyUrl"
        private const val KEY_NTFY_TOPIC = "ntfyTopic"
        private const val KEY_NTFY_TOKEN = "ntfyToken"
        private const val KEY_PUSH_ENABLED = "pushEnabled"
    }
}
