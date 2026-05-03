package com.funnyenglish.core.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.funnyenglish.core.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

class ThemeRepositoryImpl(private val context: Context) : ThemeRepository {

    private val darkModeKey = booleanPreferencesKey("dark_mode")

    override val isDarkMode: Flow<Boolean> = context.themeDataStore.data
        .map { preferences ->
            preferences[darkModeKey] ?: false
        }

    override suspend fun setDarkMode(enabled: Boolean) {
        context.themeDataStore.edit { preferences ->
            preferences[darkModeKey] = enabled
        }
    }
}
