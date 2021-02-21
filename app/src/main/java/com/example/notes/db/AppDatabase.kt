package com.example.notes.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(entities = [User::class, Reminder::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun reminderDao(): ReminderDao

    object DatabaseBuilder {

        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Since we didn't alter the table, there's nothing else to do here.
                database.execSQL("CREATE TABLE IF NOT EXISTS `User` (`uid` INTEGER, `username` TEXT, `password` TEXT, PRIMARY KEY(`uid`))")
                database.execSQL("CREATE TABLE IF NOT EXISTS `reminders` (`rid` INTEGER, `message` TEXT NOT NULL, `location_x` TEXT NOT NULL, `location_y` TEXT NOT NULL, `reminder_time` TEXT NOT NULL, `creation_time` INTEGER NOT NULL, `creator_id` TEXT NOT NULL,  `reminder_seen` TEXT NOT NULL, PRIMARY KEY(`rid`))")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = buildRoomDB(context)
                }
            }
            return INSTANCE!!
        }

        private fun buildRoomDB(context: Context) =
            Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "user-database"
            )
                .addMigrations(MIGRATION_1_2)
                .build()

    }
}