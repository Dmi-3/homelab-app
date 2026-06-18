package com.homelab.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
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
import com.homelab.app.data.Service
import com.homelab.app.push.NtfyService
import com.homelab.app.ui.ServicesScreen
import com.homelab.app.ui.SettingsScreen
import com.homelab.app.ui.TorrentsScreen
import com.homelab.app.ui.WebViewScreen
import com.homelab.app.ui.theme.HomelabTheme

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
            HomelabTheme {
                AppScaffold(prefs)
            }
        }
    }

    private data class Tab(val route: String, val label: String, val icon: @Composable () -> Unit)

    @Composable
    private fun AppScaffold(prefs: Prefs) {
        val nav = rememberNavController()
        val backStack by nav.currentBackStackEntryAsState()
        val current = backStack?.destination?.route ?: "services"
        var selected by remember { mutableStateOf<Service?>(null) }

        val tabs = listOf(
            Tab("services", "Сервисы") { Icon(Icons.Default.Apps, null) },
            Tab("torrents", "Торренты") { Icon(Icons.Default.Download, null) },
            Tab("settings", "Настройки") { Icon(Icons.Default.Settings, null) },
        )
        val showBars = current != "web"

        Scaffold(
            bottomBar = {
                if (showBars) {
                    NavigationBar {
                        tabs.forEach { tab ->
                            NavigationBarItem(
                                selected = current == tab.route,
                                onClick = {
                                    nav.navigate(tab.route) {
                                        launchSingleTop = true
                                        restoreState = true
                                        popUpTo("services") { saveState = true }
                                    }
                                },
                                icon = tab.icon,
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }
            }
        ) { pad ->
            NavHost(
                navController = nav,
                startDestination = "services",
                modifier = Modifier.padding(pad),
                enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 12 } },
                exitTransition = { fadeOut(tween(180)) },
                popEnterTransition = { fadeIn(tween(220)) },
                popExitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(220)) { it / 12 } },
            ) {
                composable("services") {
                    ServicesScreen(onOpen = { svc ->
                        selected = svc
                        nav.navigate("web")
                    })
                }
                composable("torrents") { TorrentsScreen(prefs) }
                composable("settings") {
                    SettingsScreen(prefs) { enabled ->
                        if (enabled) NtfyService.start(this@MainActivity)
                        else NtfyService.stop(this@MainActivity)
                    }
                }
                composable(
                    "web",
                    enterTransition = {
                        slideInHorizontally(tween(260)) { it } + fadeIn(tween(260))
                    },
                    popExitTransition = {
                        slideOutHorizontally(tween(260)) { it } + fadeOut(tween(260))
                    },
                ) {
                    val svc = selected
                    if (svc == null) {
                        nav.popBackStack()
                    } else {
                        WebViewScreen(svc.name, svc.url) { nav.popBackStack() }
                    }
                }
            }
        }
    }
}
