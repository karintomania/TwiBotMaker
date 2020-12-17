package com.bedroomcomputing.twibotmaker.work

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bedroomcomputing.twibotmaker.db.TweetDatabase

class TweetWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    val tag = "TweetWorker"
    val context = ctx
    val datasource = TweetDatabase.getDatabase(context).tweetDao()

    override fun doWork(): Result {

        Log.i(tag,"doWork()")
        return Result.success()
    }
}
