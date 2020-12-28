package com.bedroomcomputing.twibotmaker.db

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "tweet")
class Tweet(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "user_id") var userId: String = "",
    @ColumnInfo(name = "content") var content: String = ""
): Parcelable
