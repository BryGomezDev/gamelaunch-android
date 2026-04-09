package com.gamelaunch.presentation.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelaunch.domain.model.Region
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val notifyDaysAhead: Int = 1,
    val preferredRegion: Region = Region.WORLDWIDE,
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
                notifyDaysAhead = prefs[NOTIFY_KEY] ?: 1,
                preferredRegion = Region.fromId(prefs[REGION_KEY] ?: Region.WORLDWIDE.igdbId),
                language = prefs[LANGUAGE_KEY] ?: "es"
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setNotifyDaysAhead(days: Int) {
        viewModelScope.launch { dataStore.edit { it[NOTIFY_KEY] = days } }
    }

    fun setPreferredRegion(region: Region) {
        viewModelScope.launch { dataStore.edit { it[REGION_KEY] = region.igdbId } }
    }

    fun setLanguage(lang: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            dataStore.edit { it[LANGUAGE_KEY] = lang }
            // Sync cache for attachBaseContext (synchronous read on main thread)
            context.getSharedPreferences(LANG_CACHE_PREFS, Context.MODE_PRIVATE)
                .edit().putString(LANG_CACHE_KEY, lang).apply()
            // Wait for DataStore to confirm before recreating the Activity
            dataStore.data.first { prefs -> prefs[LANGUAGE_KEY] == lang }
            onComplete()
        }
    }

    companion object {
        val NOTIFY_KEY    = intPreferencesKey("notify_days_ahead")
        val REGION_KEY    = intPreferencesKey("preferred_region")
        val LANGUAGE_KEY  = stringPreferencesKey("language")
        const val LANG_CACHE_PREFS = "lang_cache"
        const val LANG_CACHE_KEY   = "language"
    }
}
