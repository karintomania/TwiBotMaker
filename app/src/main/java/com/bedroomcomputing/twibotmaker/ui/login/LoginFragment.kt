package com.bedroomcomputing.twibotmaker.ui.login

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.net.Uri
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bedroomcomputing.twibotmaker.R
import com.bedroomcomputing.twibotmaker.TwitterConst
import com.bedroomcomputing.twibotmaker.databinding.LoginFragmentBinding
import com.bedroomcomputing.twibotmaker.db.TweetDatabase
import com.bedroomcomputing.twibotmaker.db.User
import com.bedroomcomputing.twibotmaker.ui.main.MainViewModel
import com.bedroomcomputing.twibotmaker.ui.main.MainViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import twitter4j.conf.ConfigurationBuilder

class LoginFragment : Fragment() {

    companion object {
        fun newInstance() =
            LoginFragment()
    }

    private lateinit var binding: LoginFragmentBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.login_fragment,
            container,
            false
        )
        val userDao = TweetDatabase.getDatabase(requireContext()).userDao()
        viewModel = LoginViewModelFactory(userDao).create(LoginViewModel::class.java)


        binding.loginViewModel = viewModel
        binding.buttonLogin.setOnClickListener{
            getRequestToken()
        }

        viewModel.user.observe(viewLifecycleOwner, Observer {
            Log.i("LoginFragment", "user chnaged")
            it?.let{
                Log.i("LoginFragment", "move")
                val action = LoginFragmentDirections.actionLoginFragmentToMainFragment()
                findNavController().navigate(action)
            }
        })


        return binding.root
    }

    // Twitter Log in.
    lateinit var twitter: Twitter

    private fun getRequestToken() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            val builder = ConfigurationBuilder()
                .setDebugEnabled(true)
                .setOAuthConsumerKey(TwitterConst.CONSUMER_KEY)
                .setOAuthConsumerSecret(TwitterConst.CONSUMER_SECRET)
                .setIncludeEmailEnabled(false)
            val config = builder.build()
            val factory = TwitterFactory(config)
            twitter = factory.instance
            try {
                val requestToken = twitter.oAuthRequestToken
                withContext(Dispatchers.Main) {
                    setupTwitterWebviewDialog(requestToken.authorizationURL)
                }
            } catch (e: IllegalStateException) {
                Log.e("ERROR: ", e.toString())
            }
        }
    }


    lateinit var twitterDialog: Dialog

    // Show twitter login page in a dialog
    @SuppressLint("SetJavaScriptEnabled")
    fun setupTwitterWebviewDialog(url: String) {
        twitterDialog = Dialog(requireContext())
        val webView = WebView(requireContext())
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
        webView.webViewClient = TwitterWebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(url)
        twitterDialog.setContentView(webView)
        twitterDialog.show()
    }

    // A client to know about WebView navigations
    // For API 21 and above
    inner class TwitterWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            if (request?.url.toString().startsWith(TwitterConst.CALLBACK_URL)) {
                Log.d("Authorization URL: ", request?.url.toString())
                handleUrl(request?.url.toString())

                // Close the dialog after getting the oauth_verifier
                if (request?.url.toString().contains(TwitterConst.CALLBACK_URL)) {
                    twitterDialog.dismiss()
                }
                return true
            }
            return false
        }

        // Get the oauth_verifier
        private fun handleUrl(url: String) {
            val uri = Uri.parse(url)
            val oauthVerifier = uri.getQueryParameter("oauth_verifier") ?: ""
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                val token = withContext(Dispatchers.IO) {
                    twitter.getOAuthAccessToken(oauthVerifier)
                }
                storeUserToken(token)
            }
        }

        suspend fun storeUserToken(accessToken: AccessToken) {
            val usr = withContext(Dispatchers.IO) { twitter.verifyCredentials() }

            val userId = usr.screenName
            val name = usr.name
            val token = accessToken.token
            val tokenSecret = accessToken.tokenSecret

            Log.i("storeUserToken", token)
            Log.i("storeUserToken", tokenSecret)

            val user = User(userId = userId, name = name, token = token, tokenSecret = tokenSecret)
            viewModel.user.value = user
            viewModel.saveUser(user)
        }

    }
}