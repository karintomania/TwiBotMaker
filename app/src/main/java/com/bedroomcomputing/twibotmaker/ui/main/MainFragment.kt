package com.bedroomcomputing.twibotmaker.ui.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bedroomcomputing.twibotmaker.R
import com.bedroomcomputing.twibotmaker.databinding.MainFragmentBinding
import com.bedroomcomputing.twibotmaker.db.Tweet
import com.bedroomcomputing.twibotmaker.db.TweetDatabase
import com.bedroomcomputing.twibotmaker.db.UserDatabase
import com.bedroomcomputing.twibotmaker.ui.edit.EditFragmentDirections
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : Fragment() {

    private lateinit var binding: MainFragmentBinding
    private lateinit var viewModel: MainViewModel

    companion object {
        fun newInstance() = MainFragment()
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

//        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        val tweetDao = TweetDatabase.getDatabase(requireContext()).tweetDao()
        val userDao = UserDatabase.getDatabase(requireContext()).userDao()
        viewModel = MainViewModelFactory(tweetDao, userDao).create(MainViewModel::class.java)

        setSppiner()
        setRecyclerView()


        binding.buttonAdd.setOnClickListener{
            val tweet = Tweet()
            val action = MainFragmentDirections.actionMainFragmentToEditFragment(tweet)
            findNavController().navigate(action)
        }

        binding.buttonStart.setOnClickListener{
            viewModel.onClickStart()
        }

        binding.buttonStop.setOnClickListener{
            viewModel.onClickStop()
        }

        binding.mainViewModel = viewModel


        return binding.root
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
            viewModel.onClickDelete(tweet)
        }

        val editClickListener = TweetListAdapter.EditClickListener { tweet: Tweet ->
            val action = MainFragmentDirections.actionMainFragmentToEditFragment(tweet)
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
