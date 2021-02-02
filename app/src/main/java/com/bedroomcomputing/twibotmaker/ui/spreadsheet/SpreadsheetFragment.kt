package com.bedroomcomputing.twibotmaker.ui.spreadsheet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.work.WorkManager
import com.bedroomcomputing.twibotmaker.R
import com.bedroomcomputing.twibotmaker.databinding.SpreadsheetFragmentBinding
import com.bedroomcomputing.twibotmaker.db.TweetDao
import com.bedroomcomputing.twibotmaker.db.TweetDatabase
import com.bedroomcomputing.twibotmaker.ui.edit.EditFragmentArgs
import com.bedroomcomputing.twibotmaker.ui.main.MainViewModel
import com.bedroomcomputing.twibotmaker.ui.main.MainViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import java.util.*

class SpreadsheetFragment : Fragment() {

    companion object {
        fun newInstance() = SpreadsheetFragment()
        lateinit var  credential: GoogleAccountCredential
    }

    private lateinit var viewModel: SpreadsheetViewModel
    private lateinit var binding: SpreadsheetFragmentBinding
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private val args: SpreadsheetFragmentArgs by navArgs()


    // サインイン用intentを識別するためのID。0であることに意味はない
    val RC_SIGN_IN = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // サインインのオプションを設定。Emailの取得とspreadsheetのアクセスを要求する
        val gso= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope("https://www.googleapis.com/auth/spreadsheets"))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        credential = GoogleAccountCredential.usingOAuth2(requireContext(), Collections.singleton("https://www.googleapis.com/auth/spreadsheets"))

        // もし前回起動時にサインインしていたら、サインイン不要
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        account?.let{
            Log.i("Spreadsheet", "${account.displayName}")
            credential?.setSelectedAccount(account.account)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val tweetDao = TweetDatabase.getDatabase(requireContext()).tweetDao()
        viewModel = SpreadsheetViewModelFactory(tweetDao, args.user).create(SpreadsheetViewModel::class.java)


        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.spreadsheet_fragment,
            container,
            false
        )

        // sign in button
        binding.buttonSignin.setOnClickListener {
            signIn()
        }

        binding.buttonBackup.setOnClickListener {
            viewModel.backup()
        }

        binding.buttonRestore.setOnClickListener {
            viewModel.restore()
        }
        return binding.root
    }


    private fun signIn() {
        // サインイン用のインテントを呼び出す。onActivityResultに戻ってくる
        val signInIntent: Intent = mGoogleSignInClient.getSignInIntent()
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // サインイン完了時の処理
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            Log.i("Spreadsheet", "${account?.displayName}")
            credential?.setSelectedAccount(account?.account)

        } catch (e: ApiException) {
            Log.w("Spreadsheet", "signInResult:failed code=" + e.statusCode)

        }
    }

}

