package com.bedroomcomputing.twibotmaker.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import com.bedroomcomputing.twibotmaker.TwitterConst
import com.bedroomcomputing.twibotmaker.db.*
import com.bedroomcomputing.twibotmaker.work.TweetWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import java.util.concurrent.TimeUnit

class MainViewModel(val tweetDao: TweetDao, val userDao: UserDao, val workManager: WorkManager) : ViewModel() {
//    val tweetDao = TweetDatabase.getDatabase(getApplication()).tweetDao()

    val tweetWorkName = "tweetWork"

    val tweetsList: LiveData<List<Tweet>> = tweetDao.getTweets()
    val tweetSpanIndex = MutableLiveData<Int>()
    val isLoggedIn = MutableLiveData<Boolean>()
    val isRunning = MutableLiveData<Boolean>()
    val botInfo = MutableLiveData<String>()
    lateinit var user:User
    lateinit var twitter: Twitter
    val iconUrl = MutableLiveData<String>()

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
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val spanHour = getSpanHour()
        val tweetRequest = PeriodicWorkRequestBuilder<TweetWorker>(spanHour,TimeUnit.HOURS, 30L, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(tweetWorkName,ExistingPeriodicWorkPolicy.KEEP,tweetRequest)

        // update user
        isRunning.value = true
        user.isRunning = true
        user.tweetSpan = tweetSpanIndex.value?:0
        updateUser(user)

    }

    private fun getSpanHour():Long{
        val spanHour = when(tweetSpanIndex.value){
            0 -> 1L
            1 -> 2L
            2 -> 3L
            else -> 1L
        }
        return spanHour
    }

    fun onClickStop(){
        Log.i("stop", getSpanHour().toString())
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
                botInfo.value = "${user.name}\n@${user.userId}"

                // get icon image
                createTwitterInstance()
                iconUrl.value = withContext(Dispatchers.IO){twitter.showUser(twitter.id).profileImageURL}
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

    private fun createTwitterInstance(){
        val userToken = user.token
        val userTokenSecret = user.tokenSecret
        val builder = ConfigurationBuilder()
            .setDebugEnabled(true)
            .setOAuthConsumerKey(TwitterConst.CONSUMER_KEY)
            .setOAuthConsumerSecret(TwitterConst.CONSUMER_SECRET)
            .setOAuthAccessToken(userToken)
            .setOAuthAccessTokenSecret(userTokenSecret)
            .setIncludeEmailEnabled(false)

        val config = builder.build()
        val factory = TwitterFactory(config)
        twitter = factory.instance

    }

}

class MainViewModelFactory(val tweetDao: TweetDao, val userDao: UserDao, val workManager: WorkManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(tweetDao, userDao, workManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}