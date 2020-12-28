package com.bedroomcomputing.twibotmaker.ui.edit

import androidx.lifecycle.*
import com.bedroomcomputing.twibotmaker.db.Tweet
import com.bedroomcomputing.twibotmaker.db.TweetDao
import com.bedroomcomputing.twibotmaker.db.User
import kotlinx.coroutines.launch

class EditViewModel(
    val tweetDao: TweetDao,
    val tweet: Tweet,
    val user: User
    ) : ViewModel() {

    val tweetContent = MutableLiveData<String>()
    val tweetRestContentLength = Transformations.map(tweetContent){
        TweetContentCounter.countRestCharacter(it).toString()
    }

    val contentError = MutableLiveData<ContentError>()

    enum class ContentError {
        VALID, BLANK, TOO_LONG
    }

    init {
        tweetContent.value = tweet.content
        contentError.value = ContentError.VALID

    }

    fun onClickAdd(){
        tweet.content = tweetContent.value?:""
        tweet.userId = user.userId

        val restCharacter = TweetContentCounter.countRestCharacter(tweet.content)
        if(restCharacter < 0){
            contentError.value = ContentError.TOO_LONG
        }else if(restCharacter == 280){
            contentError.value = ContentError.BLANK
        }else{
            contentError.value = ContentError.VALID
            insert(tweet)
        }
    }

    private fun insert(tweet:Tweet){
        viewModelScope.launch{
            tweetDao.insert(tweet)
        }
    }

}

class EditViewModelFactory(val tweetDao: TweetDao,
                           val tweet: Tweet,
                           val user: User
                           ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditViewModel(tweetDao, tweet, user) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}