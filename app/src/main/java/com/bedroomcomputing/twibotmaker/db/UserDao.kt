package com.bedroomcomputing.twibotmaker.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Query("SELECT * FROM user ORDER BY id ASC")
    fun getUsers(): List<User>

    @Query("SELECT * FROM user WHERE is_active = 1 ORDER BY id ASC")
    fun getActiveUsers(): LiveData<List<User>>

    @Query("SELECT * FROM user WHERE id =:id")
    fun getUser(id: Int): LiveData<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("DELETE FROM user WHERE id =:id")
    suspend fun delete(id: Int)
}
