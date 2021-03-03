package com.bedroomcomputing.twibotmaker.ui.spreadsheet

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bedroomcomputing.twibotmaker.db.Tweet
import com.bedroomcomputing.twibotmaker.db.TweetDao
import com.bedroomcomputing.twibotmaker.db.User
import com.bedroomcomputing.twibotmaker.db.UserDao
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class SpreadsheetViewModel(val tweetDao: TweetDao, val userDao: UserDao, val user: User) : ViewModel() {


//    var spreadsheetId = "1XoRcqhbAkhYB8zh_k4_VTh3_V6TrJLeTz9NfW5mTY_8"
    var spreadsheetId = user.spreadsheetId
    val sheetName = user.userId
    val errorMessage = MutableLiveData<String>()

    val sheetsService = Sheets
        .Builder(
            AndroidHttp.newCompatibleTransport(),
            JacksonFactory.getDefaultInstance(),
            SpreadsheetFragment.credential)
        .setApplicationName("TwiBotMaker")
        .build()

    //------- backup -------

    fun backup(){

        var msg:String? = null
        viewModelScope.launch {
            try{
                withContext(Dispatchers.IO){
                    updateUserUrl()
                    write()
                }
            }catch(e:Exception){
                msg = e.message
                errorMessage.value = "something wrong with url!!"
            }
        }

    }


    suspend fun write(){

        // if sheet does not exist, create a new one
        if(!checkSheetExist(sheetName)){
            createSheet(sheetName)
        }

        val values: List<List<Any>> = createValuesToWrite()
        val body = ValueRange().setValues(values)
        val range = "${sheetName}!A:A"

        // clear the old data
        val clear: ClearValuesResponse =
            sheetsService.spreadsheets()
                .values()
                .clear(spreadsheetId,range, ClearValuesRequest())
                .execute()

        // write data
        val result: UpdateValuesResponse =
            sheetsService.spreadsheets()
                .values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute()

    }

    suspend fun checkSheetExist(sheetName:String): Boolean{
        val spreadSheet = sheetsService.spreadsheets().get(spreadsheetId).execute()
        val sheets = spreadSheet.sheets

        var existsSheet = false
        for(sheet in sheets){
            Log.d("SpreaadsheetViewModel", sheet.properties.title)
           if(sheet.properties.title == sheetName)
               existsSheet = true
        }

        return existsSheet

    }

    suspend fun createSheet(sheetName:String){

        val addSheetRequest = AddSheetRequest()
            .setProperties(SheetProperties().setTitle(sheetName))
        val request = Request().setAddSheet(addSheetRequest)
        val requestBody =
            BatchUpdateSpreadsheetRequest()
                .setRequests(listOf(request))

        Log.i("Spreadsheet", "createSheet")
        sheetsService.spreadsheets()
            .batchUpdate(spreadsheetId,requestBody)
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

    //------- restore -------

    fun restore(){

        Log.d("SpreaadsheetViewModel", "restore")
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                updateUserUrl()
                getValueAndSaveTweet()
            }
        }
    }

    suspend fun getValueAndSaveTweet(){
        val range = "${sheetName}!A:A"
        val result: ValueRange = sheetsService.spreadsheets().values().get(spreadsheetId, range).execute()

        val resultValue = result.getValues() as List<List<String>>
        val tweets = mutableListOf<Tweet>()
        val userId = sheetName

        for(row in resultValue){
            for(value in row){
                Log.d("SpreaadsheetViewModel", value)
                val tweet = Tweet(userId = userId, content = value)
                tweets.add(tweet)
            }
        }

        tweetDao.restore(userId, tweets.toList())


    }


    suspend fun updateUserUrl(){
        if(user.spreadsheetId != spreadsheetId){
            user.spreadsheetId = spreadsheetId
            userDao.insert(user)
        }
    }


}

class SpreadsheetViewModelFactory(val tweetDao: TweetDao, val userDao: UserDao, val user: User) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SpreadsheetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SpreadsheetViewModel(tweetDao, userDao, user) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}