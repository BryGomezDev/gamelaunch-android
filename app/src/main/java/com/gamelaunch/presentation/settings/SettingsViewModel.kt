package com.gamelaunch.presentation.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelaunch.domain.model.Region
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val dataStore: DataStore<Preferences>
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

    fun setLanguage(lang: String) {
        viewModelScope.launch { dataStore.edit { it[LANGUAGE_KEY] = lang } }
    }

    companion object {
        val NOTIFY_KEY    = intPreferencesKey("notify_days_ahead")
        val REGION_KEY    = intPreferencesKey("preferred_region")
        val LANGUAGE_KEY  = stringPreferencesKey("language")
    }
}
