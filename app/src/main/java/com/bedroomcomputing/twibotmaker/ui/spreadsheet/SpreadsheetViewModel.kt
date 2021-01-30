package com.bedroomcomputing.twibotmaker.ui.spreadsheet

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bedroomcomputing.twibotmaker.db.Tweet
import com.bedroomcomputing.twibotmaker.db.TweetDao
import com.bedroomcomputing.twibotmaker.db.User
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class SpreadsheetViewModel(val tweetDao: TweetDao, val user: User) : ViewModel() {



    fun export(){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                write()
            }
        }
    }

    suspend fun write(){
        val transport = AndroidHttp.newCompatibleTransport();
        val factory = JacksonFactory.getDefaultInstance()
        val sheetsService = Sheets
            .Builder(
                AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                SpreadsheetFragment.credential)
            .setApplicationName("TwiBotMaker")
            .build();

        val values: List<List<Any>> = createValuesToWrite()
        val body = ValueRange().setValues(values)
        val range = "${user.userId}!A1:A1000"

        val clear: ClearValuesResponse =
            sheetsService.spreadsheets()
                .values()
                .clear("1XoRcqhbAkhYB8zh_k4_VTh3_V6TrJLeTz9NfW5mTY_8",range, ClearValuesRequest())
                .execute()
        val result: UpdateValuesResponse =
            sheetsService.spreadsheets()
                .values()
                .update("1XoRcqhbAkhYB8zh_k4_VTh3_V6TrJLeTz9NfW5mTY_8", range, body)
                .setValueInputOption("RAW")
                .execute()

    }

    suspend fun createValuesToWrite(): List<List<Any>>{

        val tweetsList = tweetDao.getTweetsRow()
        Log.i("Spreadsheet", "createValuesToWrite")
        val tweets = mutableListOf<List<Any>>()
        for(tweet in tweetsList){
            Log.i("Spreadsheet", tweet.content)
            val row = Arrays.asList(tweet.content)
            tweets.add(row)
        }

        val values: List<List<Any>> = tweets.toList()

        return values
    }

}

class SpreadsheetViewModelFactory(val tweetDao: TweetDao, val user: User) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SpreadsheetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SpreadsheetViewModel(tweetDao, user) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}