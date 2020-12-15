package com.bedroomcomputing.twibotmaker.ui.edit

import android.app.Application
import androidx.core.content.contentValuesOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bedroomcomputing.twibotmaker.db.Tweet
import com.bedroomcomputing.twibotmaker.db.TweetDao
import kotlinx.coroutines.launch

class EditViewModel(val tweetDao: TweetDao) : ViewModel() {
    // TODO: Implement the ViewModel

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

class EditViewModelFactory(val tweetDao: TweetDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditViewModel(tweetDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}