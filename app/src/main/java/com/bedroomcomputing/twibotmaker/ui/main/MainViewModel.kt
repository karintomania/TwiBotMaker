package com.bedroomcomputing.twibotmaker.ui.main

import android.app.Application
import androidx.lifecycle.*
import com.bedroomcomputing.twibotmaker.db.Tweet
import com.bedroomcomputing.twibotmaker.db.TweetDao
import com.bedroomcomputing.twibotmaker.db.TweetDatabase
import kotlinx.coroutines.launch

class MainViewModel(val tweetDao: TweetDao) : ViewModel() {
    // TODO: Implement the ViewModel
//    val tweetDao = TweetDatabase.getDatabase(getApplication()).tweetDao()
    val tweetsList: LiveData<List<Tweet>> = tweetDao.getTweets()


}

class WordViewModelFactory(val tweetDao: TweetDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(tweetDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}