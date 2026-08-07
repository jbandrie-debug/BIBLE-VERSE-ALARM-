package com.example.data.repository

import com.example.data.dao.UserSettingsDao
import com.example.data.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val settingsDao: UserSettingsDao) {

    val userSettings: Flow<UserSettingsEntity?> = settingsDao.getUserSettings()

    suspend fun getSettingsDirect(): UserSettingsEntity {
        return settingsDao.getUserSettingsDirect() ?: UserSettingsEntity().also {
            settingsDao.saveUserSettings(it)
        }
    }

    suspend fun updateSettings(settings: UserSettingsEntity) {
        settingsDao.saveUserSettings(settings)
    }
}
