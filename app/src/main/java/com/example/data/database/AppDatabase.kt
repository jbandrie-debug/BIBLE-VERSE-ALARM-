package com.example.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AlarmDao
import com.example.data.dao.FavoriteDao
import com.example.data.dao.UserSettingsDao
import com.example.data.dao.VerseDao
import com.example.data.entity.AlarmEntity
import com.example.data.entity.FavoriteVerseEntity
import com.example.data.entity.UserSettingsEntity
import com.example.data.entity.VerseEntity
import com.example.data.model.SoundMode
import com.example.data.model.VerseSelectionType
import com.example.util.InitialBibleVerses
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AlarmEntity::class,
        VerseEntity::class,
        FavoriteVerseEntity::class,
        UserSettingsEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao
    abstract fun verseDao(): VerseDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "bible_verse_alarm_db"
                    )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    INSTANCE?.let { database ->
                                        database.verseDao().insertVerses(InitialBibleVerses.verses)
                                        database.userSettingsDao().saveUserSettings(UserSettingsEntity())
                                        database.alarmDao().insertAlarm(
                                            AlarmEntity(
                                                timeHour = 7,
                                                timeMinute = 0,
                                                label = "Morning Inspiration",
                                                isEnabled = true,
                                                repeatDaysBitmask = 62, // Mon - Fri
                                                soundMode = SoundMode.VERSE_THEN_ALARM,
                                                verseSelectionType = VerseSelectionType.DAILY_VERSE
                                            )
                                        )
                                    }
                                } catch (e: Exception) {
                                    Log.e("AppDatabase", "Error seeding database on create", e)
                                }
                            }
                        }
                    })
                    .build()
                    INSTANCE = instance
                }
                instance
            }
        }
    }
}

