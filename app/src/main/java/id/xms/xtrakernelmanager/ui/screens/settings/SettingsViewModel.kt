package id.xms.xtrakernelmanager.ui.screens.settings

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.xms.xtrakernelmanager.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "settings")

data class SettingsUiState(
    val themeMode: Int = Constants.THEME_MODE_SYSTEM,
    val themeStyle: Int = Constants.THEME_GLASS,
    val dynamicColor: Boolean = true,
    val applyOnBoot: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val context: Context
        get() = getApplication<Application>().applicationContext

    private val themeModeKey = intPreferencesKey(Constants.PREF_THEME_MODE)
    private val themeStyleKey = intPreferencesKey(Constants.PREF_THEME_STYLE)

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val preferences = context.dataStore.data.first()
            _uiState.value = _uiState.value.copy(
                themeMode = preferences[themeModeKey] ?: Constants.THEME_MODE_SYSTEM,
                themeStyle = preferences[themeStyleKey] ?: Constants.THEME_GLASS
            )
        }
    }

    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[themeModeKey] = mode
            }
            _uiState.value = _uiState.value.copy(themeMode = mode)
        }
    }

    fun setThemeStyle(style: Int) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[themeStyleKey] = style
            }
            _uiState.value = _uiState.value.copy(themeStyle = style)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(dynamicColor = enabled)
    }

    fun setApplyOnBoot(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(applyOnBoot = enabled)
    }
}
