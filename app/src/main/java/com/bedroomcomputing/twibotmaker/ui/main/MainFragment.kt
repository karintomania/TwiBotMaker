package com.bedroomcomputing.twibotmaker.ui.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.bedroomcomputing.twibotmaker.db.TweetDatabase
import com.bedroomcomputing.twibotmaker.ui.edit.EditFragmentDirections

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
        viewModel = MainViewModelFactory(TweetDatabase.getDatabase(requireContext()).tweetDao()).create(MainViewModel::class.java)

        setSppiner()
        setRecyclerView()


        binding.buttonAdd.setOnClickListener{
            val action = MainFragmentDirections.actionMainFragmentToEditFragment(1)
            findNavController().navigate(action)
        }

        binding.mainViewModel = viewModel


        return binding.root
    }

    private fun setSppiner(){
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.span_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            binding.spinner.adapter = adapter
        }
    }

    private fun setRecyclerView(){

        val recyclerView = binding.recyclerview
        val adapter = TweetListAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.tweetsList.observe(viewLifecycleOwner, Observer {
            it.let{adapter.submitList(it)}
        })

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // TODO: Use the ViewModel
    }

}