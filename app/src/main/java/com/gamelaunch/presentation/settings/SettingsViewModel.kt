package com.gamelaunch.presentation.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(val notifyDaysAhead: Int = 1)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val notifyKey = intPreferencesKey("notify_days_ahead")

    val uiState: StateFlow<SettingsUiState> = dataStore.data
        .map { prefs -> SettingsUiState(prefs[notifyKey] ?: 1) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setNotifyDaysAhead(days: Int) {
        viewModelScope.launch {
            dataStore.edit { it[notifyKey] = days }
        }
    }
}
