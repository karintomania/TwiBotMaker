package com.bedroomcomputing.twibotmaker.ui.edit

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TweetContentCounter() {

    companion object {

        fun countRestCharacter(str: String): Int {
            return 280 - count(str)
        }

        fun count(str: String): Int {

            val strUrlReplaced = replaceUrl(str)

            val chars = strUrlReplaced.toCharArray()
            var length: Int = 0

            for (char in chars) {
                if (char.toString().toByteArray().size == 1) {
                    length++
                } else {
                    length += 2
                }
            }

            return length
        }

        fun replaceUrl(str: String): String {
            val regex = Regex("""https?://[\w!?/+\-_~;.,*&@#$%()\[\]']+""")
            val replacement = "abcdefghijklmnopqrstuv" // 22 characters
            val replaced = regex.replace(str, replacement)
            return replaced
        }
    }

}