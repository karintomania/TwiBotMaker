package com.bedroomcomputing.twibotmaker.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Tweet::class, User::class), version = 3, exportSchema = false)
abstract class TweetDatabase : RoomDatabase() {

    abstract fun tweetDao(): TweetDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: TweetDatabase? = null

        fun getDatabase(context: Context): TweetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TweetDatabase::class.java,
                    "tweet_database"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
