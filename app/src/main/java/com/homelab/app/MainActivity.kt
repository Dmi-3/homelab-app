package com.homelab.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.homelab.app.data.Prefs
import com.homelab.app.push.NtfyService
import com.homelab.app.ui.SettingsScreen
import com.homelab.app.ui.TorrentsScreen

class MainActivity : ComponentActivity() {

    private val notifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = Prefs(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (prefs.pushEnabled) NtfyService.start(this)

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                AppScaffold(prefs)
            }
        }
    }

    @Composable
    private fun AppScaffold(prefs: Prefs) {
        val nav = rememberNavController()
        val backStack by nav.currentBackStackEntryAsState()
        val current = backStack?.destination?.route ?: "torrents"

        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = current == "torrents",
                        onClick = { nav.navigate("torrents") { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Download, contentDescription = null) },
                        label = { Text("Торренты") }
                    )
                    NavigationBarItem(
                        selected = current == "settings",
                        onClick = { nav.navigate("settings") { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Настройки") }
                    )
                }
            }
        ) { pad ->
            NavHost(nav, startDestination = "torrents", modifier = Modifier.padding(pad)) {
                composable("torrents") { TorrentsScreen(prefs) }
                composable("settings") {
                    SettingsScreen(prefs) { enabled ->
                        if (enabled) NtfyService.start(this@MainActivity)
                        else NtfyService.stop(this@MainActivity)
                    }
                }
            }
        }
    }
}
