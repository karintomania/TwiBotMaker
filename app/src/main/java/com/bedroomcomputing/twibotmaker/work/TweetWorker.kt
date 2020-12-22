package com.bedroomcomputing.twibotmaker.work

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bedroomcomputing.twibotmaker.TwitterConst
import com.bedroomcomputing.twibotmaker.db.Tweet
import com.bedroomcomputing.twibotmaker.db.TweetDatabase
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class TweetWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    val tag = "TweetWorker"
    val context = ctx
    val datasource = TweetDatabase.getDatabase(context).tweetDao()
    val userDao = TweetDatabase.getDatabase(context).userDao()

    override fun doWork(): Result {

        Log.i(tag, "process start")
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

        var content = getRandomTweetContent()
        if(content != "") {
            var doRetry = true
            var countRetry = 0

            while(doRetry && countRetry < 5){
                Log.i(tag ,"Content = ${content}")
                try{
                    twitter.updateStatus(content)
                    doRetry = false
                    Log.i(tag ,"Tweet Success!!")
                }catch(te: TwitterException){
                    Log.i(tag, te.message.toString())
                    Log.i(tag, te.errorCode.toString())
                    // if the post is same as recent post, the request will be denied. then, change get another post
                    content = getRandomTweetContent()
                    countRetry ++
                }
            }
        }
        return Result.success()
    }

    private fun getRandomTweetContent(): String {
//        val timestamp = SimpleDateFormat("HH:mm:ss").format(Date())
        val tweets = datasource.getTweetsRow()

        if(tweets.count() == 0){
            return ""
        }else{
            val maxIndex = (tweets.count()-1)
            val rnd = (0..maxIndex).random()
//            return tweets[rnd].content + "\n" + timestamp
            return tweets[rnd].content
        }


    }
}
