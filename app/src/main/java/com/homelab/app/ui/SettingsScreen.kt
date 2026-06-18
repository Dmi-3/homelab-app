package com.homelab.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.homelab.app.data.Prefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    prefs: Prefs,
    onPushChange: (Boolean) -> Unit,
) {
    var qbUrl by remember { mutableStateOf(prefs.qbUrl) }
    var qbUser by remember { mutableStateOf(prefs.qbUser) }
    var qbPass by remember { mutableStateOf(prefs.qbPass) }
    var ntfyUrl by remember { mutableStateOf(prefs.ntfyUrl) }
    var ntfyTopic by remember { mutableStateOf(prefs.ntfyTopic) }
    var ntfyToken by remember { mutableStateOf(prefs.ntfyToken) }
    var push by remember { mutableStateOf(prefs.pushEnabled) }
    val snackbar = remember { SnackbarHostState() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Настройки") }) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("qBittorrent", style = MaterialTheme.typography.titleMedium)
            field("Адрес", qbUrl) { qbUrl = it }
            field("Логин", qbUser) { qbUser = it }
            field("Пароль", qbPass, password = true) { qbPass = it }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text("Уведомления (ntfy)", style = MaterialTheme.typography.titleMedium)
            field("Адрес ntfy", ntfyUrl) { ntfyUrl = it }
            field("Тема", ntfyTopic) { ntfyTopic = it }
            field("Токен (Bearer)", ntfyToken, password = true) { ntfyToken = it }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Пуш-уведомления", Modifier.weight(1f))
                Switch(checked = push, onCheckedChange = {
                    push = it
                    prefs.pushEnabled = it
                    onPushChange(it)
                })
            }

            Button(
                onClick = {
                    prefs.qbUrl = qbUrl
                    prefs.qbUser = qbUser
                    prefs.qbPass = qbPass
                    prefs.ntfyUrl = ntfyUrl
                    prefs.ntfyTopic = ntfyTopic
                    prefs.ntfyToken = ntfyToken
                    if (push) onPushChange(true) // переподписаться с новыми настройками
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Сохранить") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun field(
    label: String,
    value: String,
    password: Boolean = false,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier.fillMaxWidth()
    )
}
