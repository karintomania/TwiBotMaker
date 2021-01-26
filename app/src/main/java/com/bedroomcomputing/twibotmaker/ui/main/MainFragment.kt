package com.bedroomcomputing.twibotmaker.ui.main

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkManager
import com.bedroomcomputing.twibotmaker.R
import com.bedroomcomputing.twibotmaker.databinding.MainFragmentBinding
import com.bedroomcomputing.twibotmaker.db.Tweet
import com.bedroomcomputing.twibotmaker.db.TweetDatabase
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.squareup.picasso.Picasso


class MainFragment : Fragment() {

    private lateinit var binding: MainFragmentBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var mInterstitialAd: InterstitialAd

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ads
        mInterstitialAd = InterstitialAd(requireContext())
        mInterstitialAd.adUnitId = "ca-app-pub-2164726777115826/4221468386"

        mInterstitialAd.loadAd(AdRequest.Builder().build())

        // option
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Inflate view and obtain an instance of the binding class
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.main_fragment,
            container,
            false
        )

        val tweetDao = TweetDatabase.getDatabase(requireContext()).tweetDao()
        val userDao = TweetDatabase.getDatabase(requireContext()).userDao()
        val workManager = WorkManager.getInstance(requireActivity())
        viewModel = MainViewModelFactory(tweetDao, userDao, workManager).create(MainViewModel::class.java)

        setSppiner()
        setRecyclerView()

        viewModel.isLoggedIn.observe(viewLifecycleOwner, Observer {
            if(!it){
                val action = MainFragmentDirections.actionMainFragmentToLoginFragment()
                findNavController().navigate(action)
            }
        })

        viewModel.botInfo.observe(viewLifecycleOwner, Observer {
            binding.textViewBotName.text = it
        })
        viewModel.tweetSpanIndex.observe(viewLifecycleOwner, Observer {
            binding.spinner.setSelection(it)
        })
        viewModel.iconUrl.observe(viewLifecycleOwner, Observer {
            setUserIcon(it)
        })

        viewModel.isRunning.observe(viewLifecycleOwner, Observer {
            // when the bot is running, hide start btn and spinner
            binding.buttonStart.isEnabled = !it
            binding.spinner.isEnabled = !it

            // when the bot is running, show stop btn
            binding.buttonStop.isEnabled = it
        })



        // move to edit fragment
        binding.buttonAdd.setOnClickListener{
            val tweet = Tweet()
            val action = MainFragmentDirections.actionMainFragmentToEditFragment(tweet,viewModel.user)
            findNavController().navigate(action)
        }

        // つぶやき数が少ない場合、ダイアログ表示
        binding.buttonStart.setOnClickListener{
            if(viewModel.tweetsList.value?.count()!! < 20){
                val alert = AlertDialog.Builder(requireContext())
                alert.setTitle(R.string.insufficient_tweet_title)
                alert.setMessage(R.string.insufficient_tweet)
                alert.setPositiveButton(R.string.insufficient_tweet_okButton,
                    DialogInterface.OnClickListener { dialog, which -> //Yesボタンが押された時の処理
                        viewModel.onClickStart()
                    })
                alert.setNegativeButton(R.string.insufficient_tweet_cancelButton,
                    DialogInterface.OnClickListener { dialog, which -> //Noボタンが押された時の処理
                        return@OnClickListener
                    })
                alert.show()
            }else{
                mInterstitialAd.show()
                viewModel.onClickStart()
            }

        }

        binding.buttonStop.setOnClickListener{
            viewModel.onClickStop()
        }

        binding.mainViewModel = viewModel


        return binding.root
    }

    private fun setUserIcon(iconUrl: String) {
        val url = iconUrl.toString().replace("http", "https")
        val iv = binding.imageViewIcon
        val picasso = Picasso.get()
        picasso.load(url)
            .placeholder(R.drawable.default_icon)
            .error(R.drawable.default_icon)
            .into(iv)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                viewModel.onClickLogout()
                true
            }
            R.id.menu_spreadsheet -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setSppiner(){

        val spinnerSelectedListner = SpinnerSelectedListenr{
            viewModel.tweetSpanIndex.value = it
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.span_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            binding.spinner.adapter = adapter
            binding.spinner.onItemSelectedListener = spinnerSelectedListner
        }

    }

    private fun setRecyclerView(){

        val recyclerView = binding.recyclerview

        val deleteClickListener = TweetListAdapter.DeleteClickListener { tweet: Tweet ->
            val shortenContent = if(tweet.content.length > 20) tweet.content.substring(0,20)+"..." else tweet.content
            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle(R.string.delete_tweet_title)
            alert.setMessage("${shortenContent}")
            alert.setPositiveButton(R.string.delete_tweet_ok,
                DialogInterface.OnClickListener { dialog, which -> //Yesボタンが押された時の処理
                    viewModel.onClickDelete(tweet)
                })
            alert.setNegativeButton(R.string.delete_tweet_cancel,
                DialogInterface.OnClickListener { dialog, which -> //Noボタンが押された時の処理
                    return@OnClickListener
                })
            alert.show()
        }

        val editClickListener = TweetListAdapter.EditClickListener { tweet: Tweet ->
            val action = MainFragmentDirections.actionMainFragmentToEditFragment(tweet, viewModel.user)
            findNavController().navigate(action)

        }

        val adapter = TweetListAdapter(editClickListener, deleteClickListener)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.tweetsList.observe(viewLifecycleOwner, Observer {
            it.let{adapter.submitList(it)}
        })

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

}

class SpinnerSelectedListenr(val onItemSelected: (position:Int) -> Unit): AdapterView.OnItemSelectedListener{
    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        onItemSelected(p2)
    }
}
