package com.bedroomcomputing.twibotmaker.ui.edit

import android.app.Application
import androidx.core.content.contentValuesOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bedroomcomputing.twibotmaker.db.Tweet
import com.bedroomcomputing.twibotmaker.db.TweetDatabase
import kotlinx.coroutines.launch

class EditViewModel : AndroidViewModel(Application()) {
    // TODO: Implement the ViewModel

    val tweetDao = TweetDatabase.getDatabase(getApplication()).tweetDao()
    init {
        val tweet = Tweet(id = 0, content = "testだお")
        insert(tweet)
    }


    private fun insert(tweet:Tweet){
        viewModelScope.launch{
            tweetDao.insert(tweet)
        }
    }

}