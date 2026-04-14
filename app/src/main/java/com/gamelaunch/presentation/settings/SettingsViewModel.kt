package com.gamelaunch.presentation.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelaunch.domain.model.Platform
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val notifyDaysAhead: Int = 1,
    val favoritePlatforms: Set<Platform> = emptySet(),
    val language: String = "es"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = dataStore.data
        .map { prefs ->
            SettingsUiState(
                notifyDaysAhead    = prefs[NOTIFY_KEY] ?: 1,
                favoritePlatforms  = prefs[FAVORITE_PLATFORMS_KEY]
                    ?.mapNotNull { name -> runCatching { Platform.valueOf(name) }.getOrNull() }
                    ?.toSet()
                    ?: emptySet(),
                language           = prefs[LANGUAGE_KEY] ?: "es"
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setNotifyDaysAhead(days: Int) {
        viewModelScope.launch { dataStore.edit { it[NOTIFY_KEY] = days } }
    }

    fun toggleFavoritePlatform(platform: Platform) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val current = prefs[FAVORITE_PLATFORMS_KEY] ?: emptySet()
                prefs[FAVORITE_PLATFORMS_KEY] = if (platform.name in current)
                    current - platform.name
                else
                    current + platform.name
            }
        }
    }

    fun setLanguage(lang: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            dataStore.edit { it[LANGUAGE_KEY] = lang }
            context.getSharedPreferences(LANG_CACHE_PREFS, Context.MODE_PRIVATE)
                .edit().putString(LANG_CACHE_KEY, lang).apply()
            dataStore.data.first { prefs -> prefs[LANGUAGE_KEY] == lang }
            onComplete()
        }
    }

    companion object {
        val NOTIFY_KEY              = intPreferencesKey("notify_days_ahead")
        val FAVORITE_PLATFORMS_KEY  = stringSetPreferencesKey("favorite_platforms")
        val LANGUAGE_KEY            = stringPreferencesKey("language")
        const val LANG_CACHE_PREFS  = "lang_cache"
        const val LANG_CACHE_KEY    = "language"
    }
}
