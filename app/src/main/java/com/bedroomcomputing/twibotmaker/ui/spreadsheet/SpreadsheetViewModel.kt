package com.bedroomcomputing.twibotmaker.ui.spreadsheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.AppendValuesResponse
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class SpreadsheetViewModel : ViewModel() {

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
        val sheetsService = Sheets.Builder(AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), SpreadsheetFragment.credential).build();

        val values: List<List<Any>> = Arrays.asList(
            Arrays.asList("A","B") // Additional rows ...
        )
        val body = ValueRange().setValues(values)

        val result: AppendValuesResponse =
            sheetsService.spreadsheets().values().append("1XoRcqhbAkhYB8zh_k4_VTh3_V6TrJLeTz9NfW5mTY_8", "Sheet1!A1:B2", body)
                .setValueInputOption("RAW")
                .execute()

    }

}