package com.homelab.app.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.homelab.app.MainActivity
import com.homelab.app.R
import com.homelab.app.data.Prefs
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/** Фоновый сервис: держит подписку на ntfy и показывает уведомления. */
class NtfyService : Service() {

    @Volatile private var running = false
    private var worker: Thread? = null
    private val json = Json { ignoreUnknownKeys = true }
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.SECONDS) // долгоживущий поток
        .retryOnConnectionFailure(true)
        .build()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!running) {
            running = true
            createChannels()
            startForeground(FG_ID, foregroundNotification())
            worker = thread(start = true) { loop() }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        running = false
        worker?.interrupt()
        super.onDestroy()
    }

    private fun loop() {
        val prefs = Prefs(this)
        while (running) {
            try {
                val url = "${prefs.ntfyUrl.trimEnd('/')}/${prefs.ntfyTopic}/json"
                val b = Request.Builder().url(url)
                if (prefs.ntfyToken.isNotBlank()) {
                    b.header("Authorization", "Bearer ${prefs.ntfyToken}")
                }
                client.newCall(b.build()).execute().use { resp ->
                    val source = resp.body?.source() ?: return@use
                    while (running && !source.exhausted()) {
                        val line = source.readUtf8Line() ?: break
                        if (line.isNotBlank()) handleLine(line)
                    }
                }
            } catch (_: Exception) {
                // переподключимся ниже
            }
            if (running) {
                try { Thread.sleep(5000) } catch (_: InterruptedException) { break }
            }
        }
    }

    private fun handleLine(line: String) {
        try {
            val obj = json.parseToJsonElement(line).jsonObject
            if (obj["event"]?.jsonPrimitive?.contentOrNull != "message") return
            val msg = obj["message"]?.jsonPrimitive?.contentOrNull ?: return
            val title = obj["title"]?.jsonPrimitive?.contentOrNull ?: "Homelab"
            showNotification(title, msg)
        } catch (_: Exception) {
        }
    }

    private fun showNotification(title: String, text: String) {
        val pi = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val n = NotificationCompat.Builder(this, MSG_CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_notify)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm().notify((System.currentTimeMillis() % 100000).toInt(), n)
    }

    private fun foregroundNotification() =
        NotificationCompat.Builder(this, FG_CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_notify)
            .setContentTitle("Homelab")
            .setContentText("Слушаю уведомления")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    private fun nm() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm().createNotificationChannel(
                NotificationChannel(FG_CHANNEL, "Фоновая подписка", NotificationManager.IMPORTANCE_LOW)
            )
            nm().createNotificationChannel(
                NotificationChannel(MSG_CHANNEL, "Уведомления", NotificationManager.IMPORTANCE_HIGH)
            )
        }
    }

    companion object {
        private const val FG_ID = 1
        private const val FG_CHANNEL = "ntfy_fg"
        private const val MSG_CHANNEL = "ntfy_msg"

        fun start(ctx: Context) {
            val i = Intent(ctx, NtfyService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(i)
            else ctx.startService(i)
        }

        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, NtfyService::class.java))
        }
    }
}
