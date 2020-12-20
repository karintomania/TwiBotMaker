package com.bedroomcomputing.twibotmaker.work

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bedroomcomputing.twibotmaker.TwitterConst
import com.bedroomcomputing.twibotmaker.db.Tweet
import com.bedroomcomputing.twibotmaker.db.TweetDatabase
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder

class TweetWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    val tag = "TweetWorker"
    val context = ctx
    val datasource = TweetDatabase.getDatabase(context).tweetDao()
    val userDao = TweetDatabase.getDatabase(context).userDao()

    override fun doWork(): Result {

        val user = userDao.getUsers().get(0)
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
        val twitter = factory.instance

        val content = getRandomTweetContent()
        if(content != "") {
            twitter.updateStatus(content)
        }
        Log.i("TweetWorker", "TweetContent = ${content}")
        return Result.success()
    }

    private fun getRandomTweetContent(): String {
        val tweets = datasource.getTweetsRow()

        if(tweets.count() == 0){
            return ""
        }else{
            val rnd = (0..tweets.count()).random()
            return tweets[rnd].content
        }
    }
}
