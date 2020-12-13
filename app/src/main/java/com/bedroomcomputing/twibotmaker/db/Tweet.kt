package com.bedroomcomputing.twibotmaker.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tweet")
class Tweet(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "content") val content: String
)
