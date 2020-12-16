package com.bedroomcomputing.twibotmaker.ui.main

import android.app.Application
import androidx.lifecycle.*
import com.bedroomcomputing.twibotmaker.db.Tweet
import com.bedroomcomputing.twibotmaker.db.TweetDao
import com.bedroomcomputing.twibotmaker.db.TweetDatabase
import kotlinx.coroutines.launch

class MainViewModel(val tweetDao: TweetDao) : ViewModel() {
//    val tweetDao = TweetDatabase.getDatabase(getApplication()).tweetDao()
    val tweetsList: LiveData<List<Tweet>> = tweetDao.getTweets()

    fun onClickDelete(tweet:Tweet){
        delete(tweet)
    }

    private fun delete(tweet:Tweet){
        viewModelScope.launch{
            tweetDao.delete(tweet.id)
        }
    }

}

class MainViewModelFactory(val tweetDao: TweetDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(tweetDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}