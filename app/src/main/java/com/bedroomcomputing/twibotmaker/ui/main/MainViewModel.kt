package com.bedroomcomputing.twibotmaker.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import com.bedroomcomputing.twibotmaker.db.*
import com.bedroomcomputing.twibotmaker.work.TweetWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class MainViewModel(val tweetDao: TweetDao, val userDao: UserDao) : ViewModel() {
//    val tweetDao = TweetDatabase.getDatabase(getApplication()).tweetDao()

    val tweetWorkName = "tweetWork"

    val tweetsList: LiveData<List<Tweet>> = tweetDao.getTweets()
    val tweetSpanIndex = MutableLiveData<Int>()
    val isLoggedIn = MutableLiveData<Boolean>()
    val isRunning = MutableLiveData<Boolean>()
    lateinit var user:User

    init{
        isLoggedIn.value = true
        setUser()
    }

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

        workManager.enqueueUniquePeriodicWork(tweetWorkName,ExistingPeriodicWorkPolicy.KEEP,tweetRequest)

        // update user
        isRunning.value = true
        user.isRunning = true
        user.tweetSpan = tweetSpanIndex.value?:1
        updateUser(user)

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
        workManager.cancelUniqueWork(tweetWorkName)


        // update user
        isRunning.value = false
        user.isRunning = false
        updateUser(user)
    }

    private fun setUser(){
        viewModelScope.launch {
            val users = withContext(Dispatchers.IO) {
                 userDao.getUsers()
            }
            if(users.count() > 0){
                isLoggedIn.value = true
                user = users.get(0)
                isRunning.value = user.isRunning
                tweetSpanIndex.value = user.tweetSpan
            }else{
                isLoggedIn.value = false
            }


            Log.i("Main", "${isLoggedIn.value}")
        }
    }

    fun onClickLogout() {
        viewModelScope.launch {
            userDao.delete(user.id)
        }
        setUser()
    }


    fun updateUser(user:User){
        viewModelScope.launch { userDao.insert(user) }
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