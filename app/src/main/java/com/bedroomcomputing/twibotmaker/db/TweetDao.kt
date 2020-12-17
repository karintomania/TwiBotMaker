package com.bedroomcomputing.twibotmaker.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TweetDao {
    @Query("SELECT * FROM tweet ORDER BY id ASC")
    fun getTweets(): LiveData<List<Tweet>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tweet: Tweet)

    @Query("DELETE FROM tweet WHERE id =:id")
    suspend fun delete(id:Int)
}