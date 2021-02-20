package com.bedroomcomputing.twibotmaker.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = arrayOf(Tweet::class, User::class), version = 5, exportSchema = false)
abstract class TweetDatabase : RoomDatabase() {

    abstract fun tweetDao(): TweetDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: TweetDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE user ADD COLUMN spreadsheet_id TEXT  NOT NULL default \"\"")
            }
        }

        fun getDatabase(context: Context): TweetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TweetDatabase::class.java,
                    "tweet_database"
                )
                .addMigrations(MIGRATION_4_5)
                .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
