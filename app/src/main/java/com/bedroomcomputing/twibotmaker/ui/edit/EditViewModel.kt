package com.bedroomcomputing.twibotmaker.ui.edit

import android.app.Application
import androidx.core.content.contentValuesOf
import androidx.lifecycle.*
import com.bedroomcomputing.twibotmaker.db.Tweet
import com.bedroomcomputing.twibotmaker.db.TweetDao
import kotlinx.coroutines.launch

class EditViewModel(
    val tweetDao: TweetDao,
    val tweet: Tweet
    ) : ViewModel() {

    val tweetContent = MutableLiveData<String>()

    init {
        tweetContent.value = tweet.content
    }

    fun onClickAdd(){
        tweet.content = tweetContent.value?:""
        insert(tweet)
    }

    private fun insert(tweet:Tweet){
        viewModelScope.launch{
            tweetDao.insert(tweet)
        }
    }

}

class EditViewModelFactory(val tweetDao: TweetDao, val tweet: Tweet) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditViewModel(tweetDao, tweet) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}