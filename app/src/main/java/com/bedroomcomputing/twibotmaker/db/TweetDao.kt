package com.bedroomcomputing.twibotmaker.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TweetDao {
    @Query("SELECT * FROM tweet")
    fun getTweets(): LiveData<List<Tweet>>

    @Query("SELECT * FROM tweet")
    fun getTweetsRow(): List<Tweet>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTweets(vararg tweets: Tweet)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tweet: Tweet)

    @Query("DELETE FROM tweet WHERE id =:id")
    suspend fun delete(id:Int)
}