package com.bedroomcomputing.twibotmaker.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TweetDao {
    @Query("SELECT * FROM tweet")
    fun getTweets(): LiveData<List<Tweet>>

    @Query("SELECT * FROM tweet")
    fun getTweetsRow(): List<Tweet>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTweets(vararg tweets: Tweet)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tweets: List<Tweet>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tweet: Tweet)

    @Query("DELETE FROM tweet WHERE id =:id")
    suspend fun delete(id:Int)

    @Query("DELETE FROM tweet WHERE user_id =:userId")
    suspend fun deleteByUser(userId:String)

    @Transaction
    suspend fun restore(userId:String, tweets:List<Tweet>){
        deleteByUser(userId)
        insertAll(tweets)
    }
}