package com.locked.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val Context.dataStore by preferencesDataStore(name = "locked_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val PROTECTION_ENABLED = booleanPreferencesKey("protection_enabled")
        val USER_NAME = stringPreferencesKey("user_name")
        val MORNING_ENABLED = booleanPreferencesKey("morning_enabled")
        val MORNING_WINDOW_START = stringPreferencesKey("morning_window_start") // HH:mm
        val MORNING_WINDOW_END = stringPreferencesKey("morning_window_end")     // HH:mm
        val LAST_MORNING_SESSION_DATE = stringPreferencesKey("last_morning_session_date") // yyyy-MM-dd
    }

    val protectionEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.PROTECTION_ENABLED] ?: true }

    suspend fun setProtectionEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PROTECTION_ENABLED] = enabled }
    }

    val userName: Flow<String> =
        context.dataStore.data.map { it[Keys.USER_NAME] ?: "" }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[Keys.USER_NAME] = name }
    }

    val morningEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.MORNING_ENABLED] ?: true }

    suspend fun setMorningEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MORNING_ENABLED] = enabled }
    }

    val morningWindowStart: Flow<LocalTime> =
        context.dataStore.data.map {
            parseTimeOrDefault(it[Keys.MORNING_WINDOW_START], LocalTime.of(4, 0))
        }

    val morningWindowEnd: Flow<LocalTime> =
        context.dataStore.data.map {
            parseTimeOrDefault(it[Keys.MORNING_WINDOW_END], LocalTime.of(11, 0))
        }

    suspend fun setMorningWindow(start: LocalTime, end: LocalTime) {
        context.dataStore.edit {
            it[Keys.MORNING_WINDOW_START] = start.format(DateTimeFormatter.ofPattern("HH:mm"))
            it[Keys.MORNING_WINDOW_END] = end.format(DateTimeFormatter.ofPattern("HH:mm"))
        }
    }

    val lastMorningSessionDate: Flow<String> =
        context.dataStore.data.map { it[Keys.LAST_MORNING_SESSION_DATE] ?: "" }

    suspend fun markMorningSessionDone(date: LocalDate = LocalDate.now()) {
        context.dataStore.edit {
            it[Keys.LAST_MORNING_SESSION_DATE] = date.toString()
        }
    }

    private fun parseTimeOrDefault(value: String?, default: LocalTime): LocalTime {
        if (value.isNullOrBlank()) return default
        return try {
            LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            default
        }
    }
}
