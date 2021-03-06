package com.bedroomcomputing.twibotmaker.db

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "user")
class User (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "user_id") val userId: String = "",
    @ColumnInfo(name = "name") val name: String = "",
    @ColumnInfo(name = "token") val token: String = "",
    @ColumnInfo(name = "token_secret") val tokenSecret: String = "",
    @ColumnInfo(name = "tweet_span") var tweetSpan: Int = 1,
    @ColumnInfo(name = "is_running") var isRunning: Boolean = false,
    @ColumnInfo(name = "is_active") var isActive: Boolean = true,
    @ColumnInfo(name = "spreadsheet_id") var spreadsheetId: String = ""
): Parcelable
