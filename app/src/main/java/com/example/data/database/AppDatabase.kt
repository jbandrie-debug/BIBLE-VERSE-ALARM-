package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AlarmDao
import com.example.data.dao.FavoriteDao
import com.example.data.dao.UserSettingsDao
import com.example.data.dao.VerseDao
import com.example.data.entity.Alarm
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
        Alarm::class,
        VerseEntity::class,
        FavoriteVerseEntity::class,
        UserSettingsEntity::class
    ],
    version = 3,
    exportSchema = false
)
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
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bible_verse_alarm_db"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed database on creation
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                // Pre-populate Verses
                                database.verseDao().insertVerses(InitialBibleVerses.verses)

                                // Pre-populate Default Settings
                                database.userSettingsDao().saveUserSettings(UserSettingsEntity())

                                // Pre-populate Default Sample Alarm (7:00 AM)
                                database.alarmDao().insertAlarm(
                                    AlarmEntity(
                                        timeHour = 7,
                                        timeMinute = 0,
                                        label = "Morning Inspiration",
                                        isEnabled = true,
                                        repeatDaysBitmask = 62, // Mon - Fri (2+4+8+16+32)
                                        soundMode = SoundMode.VERSE_THEN_ALARM,
                                        verseSelectionType = VerseSelectionType.DAILY_VERSE
                                    )
                                )
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
