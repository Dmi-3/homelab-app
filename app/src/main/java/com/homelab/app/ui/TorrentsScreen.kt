package com.homelab.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.homelab.app.data.Prefs
import com.homelab.app.data.QbitClient
import com.homelab.app.data.Torrent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TorrentsScreen(prefs: Prefs) {
    val client = remember { QbitClient(prefs) }
    val scope = rememberCoroutineScope()
    var torrents by remember { mutableStateOf<List<Torrent>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    // автообновление каждые 2 секунды
    LaunchedEffect(Unit) {
        while (true) {
            try {
                if (prefs.qbPass.isBlank()) {
                    error = "Укажи пароль qBittorrent в Настройках"
                } else {
                    torrents = client.torrents()
                    error = null
                }
            } catch (e: Exception) {
                error = "Нет связи с qBittorrent (${prefs.qbUrl})"
            }
            delay(2000)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Торренты") }) },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            error?.let {
                Text(
                    it,
                    Modifier.fillMaxWidth().padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (torrents.isEmpty() && error == null) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Нет торрентов") }
            }
            LazyColumn(Modifier.fillMaxSize()) {
                items(torrents, key = { it.hash }) { t ->
                    TorrentRow(
                        t = t,
                        onToggle = {
                            scope.launch {
                                if (t.state.startsWith("paused") || t.state.startsWith("stopped"))
                                    client.resume(t.hash) else client.pause(t.hash)
                            }
                        },
                        onDelete = { withFiles ->
                            scope.launch {
                                client.delete(t.hash, withFiles)
                                snackbar.showSnackbar("Удалено: ${t.name}")
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showAdd) {
        AddMagnetDialog(
            onDismiss = { showAdd = false },
            onAdd = { magnet ->
                showAdd = false
                scope.launch {
                    client.addMagnet(magnet)
                    snackbar.showSnackbar("Magnet добавлен")
                }
            }
        )
    }
}

@Composable
private fun TorrentRow(t: Torrent, onToggle: () -> Unit, onDelete: (Boolean) -> Unit) {
    var confirm by remember { mutableStateOf(false) }
    val paused = t.state.startsWith("paused") || t.state.startsWith("stopped")
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(t.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { t.progress.toFloat() },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${(t.progress * 100).toInt()}%  ↓${speed(t.dlspeed)}  ↑${speed(t.upspeed)}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onToggle) {
                Icon(
                    if (paused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (paused) "Старт" else "Пауза"
                )
            }
            IconButton(onClick = { confirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить")
            }
        }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Удалить торрент?") },
            text = { Text(t.name) },
            confirmButton = {
                TextButton(onClick = { confirm = false; onDelete(true) }) { Text("С файлами") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { confirm = false; onDelete(false) }) { Text("Только из списка") }
                    TextButton(onClick = { confirm = false }) { Text("Отмена") }
                }
            }
        )
    }
}

@Composable
private fun AddMagnetDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить magnet") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("magnet:?xt=...") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onAdd(text.trim()) }) { Text("Добавить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

private fun speed(bytes: Long): String {
    if (bytes <= 0) return "0"
    val units = listOf("Б/с", "КБ/с", "МБ/с", "ГБ/с")
    var v = bytes.toDouble()
    var i = 0
    while (v >= 1024 && i < units.lastIndex) { v /= 1024; i++ }
    return String.format(Locale.US, "%.1f %s", v, units[i])
}
