package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        PlaybackHistoryEntity::class,
        EqualizerPresetEntity::class,
        SearchQueryEntity::class,
        UserLikedSongEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class SwarixDatabase : RoomDatabase() {

    abstract fun musicDao(): MusicDao

    companion object {
        @Volatile
        private var INSTANCE: SwarixDatabase? = null

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE playback_history ADD COLUMN durationListenedMs INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE playback_history ADD COLUMN completionPercent REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE playback_history ADD COLUMN isSkipped INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE playback_history ADD COLUMN source TEXT NOT NULL DEFAULT 'DIRECT'")
            }
        }

        fun getDatabase(context: Context): SwarixDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SwarixDatabase::class.java,
                    "swarix_music.db"
                )
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
