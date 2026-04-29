package com.gamelaunch

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.gamelaunch.presentation.navigation.AppNavGraph
import com.gamelaunch.presentation.settings.SettingsViewModel.Companion.LANG_CACHE_KEY
import com.gamelaunch.presentation.settings.SettingsViewModel.Companion.LANG_CACHE_PREFS
import com.gamelaunch.ui.theme.Background
import com.gamelaunch.ui.theme.GameLaunchTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameLaunchTheme {
                MainScaffold()
            }
        }
    }

    override fun attachBaseContext(base: Context) {
        val lang = base.getSharedPreferences(LANG_CACHE_PREFS, Context.MODE_PRIVATE)
            .getString(LANG_CACHE_KEY, "es") ?: "es"
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(base.createConfigurationContext(config))
    }
}

@Composable
private fun MainScaffold() {
    val navController = rememberNavController()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        AppNavGraph(navController = navController)
    }
}
