package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        PlaybackHistoryEntity::class,
        EqualizerPresetEntity::class,
        SearchQueryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SwarixDatabase : RoomDatabase() {

    abstract fun musicDao(): MusicDao

    companion object {
        @Volatile
        private var INSTANCE: SwarixDatabase? = null

        fun getDatabase(context: Context): SwarixDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SwarixDatabase::class.java,
                    "swarix_music.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
