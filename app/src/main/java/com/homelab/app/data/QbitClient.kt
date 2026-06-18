package com.homelab.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

@Serializable
data class Torrent(
    val hash: String = "",
    val name: String = "",
    val progress: Double = 0.0,
    val dlspeed: Long = 0,
    val upspeed: Long = 0,
    val size: Long = 0,
    val state: String = "",
    val eta: Long = 0,
)

/** Клиент qBittorrent WebUI API (v2). Хранит SID-cookie между вызовами. */
class QbitClient(private val prefs: Prefs) {

    private val cookies = mutableMapOf<String, List<Cookie>>()

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .cookieJar(object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, list: List<Cookie>) {
                cookies[url.host] = list
            }
            override fun loadForRequest(url: HttpUrl): List<Cookie> =
                cookies[url.host] ?: emptyList()
        })
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    private fun base() = prefs.qbUrl.trimEnd('/')

    /** Логин: возвращает true при успехе ("Ok."). */
    suspend fun login(): Boolean = withContext(Dispatchers.IO) {
        val body = FormBody.Builder()
            .add("username", prefs.qbUser)
            .add("password", prefs.qbPass)
            .build()
        val req = Request.Builder()
            .url("${base()}/api/v2/auth/login")
            .header("Referer", base())
            .post(body)
            .build()
        client.newCall(req).execute().use { resp ->
            resp.isSuccessful && (resp.body?.string()?.trim() == "Ok.")
        }
    }

    suspend fun torrents(): List<Torrent> = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("${base()}/api/v2/torrents/info?sort=added_on&reverse=true")
            .build()
        client.newCall(req).execute().use { resp ->
            if (resp.code == 403) { // сессия истекла — перелогиниться один раз
                if (login()) return@withContext torrents()
                return@withContext emptyList()
            }
            val text = resp.body?.string() ?: return@withContext emptyList()
            json.decodeFromString<List<Torrent>>(text)
        }
    }

    /** Пауза: пробует v5 (/stop), при 404 — v4 (/pause). */
    suspend fun pause(hash: String) = action(hash, "stop", "pause")

    /** Старт: пробует v5 (/start), при 404 — v4 (/resume). */
    suspend fun resume(hash: String) = action(hash, "start", "resume")

    private suspend fun action(hash: String, v5: String, v4: String) = withContext(Dispatchers.IO) {
        if (post("/api/v2/torrents/$v5", "hashes", hash) == 404) {
            post("/api/v2/torrents/$v4", "hashes", hash)
        }
        Unit
    }

    suspend fun delete(hash: String, deleteFiles: Boolean) = withContext(Dispatchers.IO) {
        val body = FormBody.Builder()
            .add("hashes", hash)
            .add("deleteFiles", deleteFiles.toString())
            .build()
        execForm("/api/v2/torrents/delete", body)
        Unit
    }

    suspend fun addMagnet(url: String) = withContext(Dispatchers.IO) {
        val body = FormBody.Builder().add("urls", url).build()
        execForm("/api/v2/torrents/add", body)
        Unit
    }

    private fun post(path: String, key: String, value: String): Int {
        val body = FormBody.Builder().add(key, value).build()
        return execForm(path, body)
    }

    private fun execForm(path: String, body: FormBody): Int {
        val req = Request.Builder()
            .url("${base()}$path")
            .header("Referer", base())
            .post(body)
            .build()
        return client.newCall(req).execute().use { it.code }
    }
}
