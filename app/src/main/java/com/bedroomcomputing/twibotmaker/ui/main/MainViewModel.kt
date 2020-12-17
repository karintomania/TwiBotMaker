package com.bedroomcomputing.twibotmaker.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import com.bedroomcomputing.twibotmaker.db.Tweet
import com.bedroomcomputing.twibotmaker.db.TweetDao
import com.bedroomcomputing.twibotmaker.db.TweetDatabase
import com.bedroomcomputing.twibotmaker.db.UserDao
import com.bedroomcomputing.twibotmaker.work.TweetWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainViewModel(val tweetDao: TweetDao, val userDao: UserDao) : ViewModel() {
//    val tweetDao = TweetDatabase.getDatabase(getApplication()).tweetDao()
    val tweetsList: LiveData<List<Tweet>> = tweetDao.getTweets()
    val tweetSpanIndex = MutableLiveData<Int>()
    val isLoggedIn = MutableLiveData<Boolean>()


    fun onClickDelete(tweet:Tweet){
        delete(tweet)
    }

    private fun delete(tweet:Tweet){
        viewModelScope.launch{
            tweetDao.delete(tweet.id)
        }
    }

    fun onClickStart(){
        val workManager = WorkManager.getInstance()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val tweetRequest = PeriodicWorkRequestBuilder<TweetWorker>(getSpanHour(),TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()


        workManager.enqueueUniquePeriodicWork("tweetWork",ExistingPeriodicWorkPolicy.KEEP,tweetRequest)

    }

    private fun getSpanHour():Long{
        val spanHour = when(tweetSpanIndex.value){
            0 -> 1L
            1 -> 3L
            2 -> 6L
            else -> 1L
        }
        return spanHour
    }

    fun onClickStop(){
        Log.i("stop", getSpanHour().toString())
        val workManager = WorkManager.getInstance()
        workManager.cancelUniqueWork("tweetWork")
    }

}

class MainViewModelFactory(val tweetDao: TweetDao, val userDao: UserDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(tweetDao, userDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}