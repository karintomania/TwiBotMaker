package com.bedroomcomputing.twibotmaker.ui.spreadsheet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import com.bedroomcomputing.twibotmaker.R
import com.bedroomcomputing.twibotmaker.databinding.SpreadsheetFragmentBinding
import com.bedroomcomputing.twibotmaker.db.TweetDatabase
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


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val tweetDao = TweetDatabase.getDatabase(requireContext()).tweetDao()
        val userDao = TweetDatabase.getDatabase(requireContext()).userDao()
        viewModel = SpreadsheetViewModelFactory(tweetDao, userDao, args.user).create(SpreadsheetViewModel::class.java)


        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.spreadsheet_fragment,
            container,
            false
        )

        // もし前回起動時にサインインしていたら、サインイン不要
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        if(account != null){
            credential?.setSelectedAccount(account.account)
            // hide signin view
            binding.spreadsheetSigninLayout.visibility = View.GONE
        }else{
            binding.spreadsheetMainLayout.visibility = View.GONE
        }

        // URLがセットされていたら表示
        if(viewModel.user.spreadsheetId != ""){
            // set spreadsheet URL
            binding.editUrl.setText(generateUrlFromSpreadsheetId(viewModel.user.spreadsheetId))
        }

        // show error message
        viewModel.isError.observe(viewLifecycleOwner, Observer{
            if(it){
                showErrorToast("something wrong")
                viewModel.isError.value = false
            }
        })

        // show success message
        viewModel.isSuccess.observe(viewLifecycleOwner, Observer{
            if(it){
                showErrorToast("Success!")
                viewModel.isSuccess.value = false
            }
        })

        // sign in button on click
        binding.buttonSignin.setOnClickListener {
            signIn()
        }

        // sign out button on click
        binding.buttonSignout.setOnClickListener {
            mGoogleSignInClient.signOut().addOnCompleteListener {
                binding.spreadsheetSigninLayout.visibility = View.VISIBLE
                binding.spreadsheetMainLayout.visibility = View.GONE

            }
        }

        binding.buttonBackup.setOnClickListener {
            // overwrite spreadsheet id
            viewModel.spreadsheetId = extractSpreadsheetIdFromUrl(binding.editUrl.text.toString())
            // backup
            viewModel.backup()
        }

        binding.buttonRestore.setOnClickListener {
            // overwrite spreadsheet id
            viewModel.spreadsheetId = extractSpreadsheetIdFromUrl(binding.editUrl.text.toString())
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

            credential?.setSelectedAccount(account?.account)
            //change visibility of layouts
            binding.spreadsheetSigninLayout.visibility = View.GONE
            binding.spreadsheetMainLayout.visibility = View.VISIBLE

        } catch (e: ApiException) {
            Log.w("Spreadsheet", "signInResult:failed code=" + e.statusCode)

        }
    }

    private fun showErrorToast(msg:String){
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    fun extractSpreadsheetIdFromUrl(url:String):String{
        val regex = Regex("""spreadsheets\/d\/([^\/]+)""")

        val match = regex.find(url)

        val id = match?.groups?.get(1)?.value

        return id?:""
    }

    fun generateUrlFromSpreadsheetId(spreadsheetId:String):String{
        return "https://docs.google.com/spreadsheets/d/${spreadsheetId}/edit"
    }


}

