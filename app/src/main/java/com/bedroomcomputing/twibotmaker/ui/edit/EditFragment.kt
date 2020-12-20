package com.bedroomcomputing.twibotmaker.ui.edit

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bedroomcomputing.twibotmaker.R
import com.bedroomcomputing.twibotmaker.databinding.EditFragmentBinding
import com.bedroomcomputing.twibotmaker.db.TweetDatabase

class EditFragment : Fragment() {

    companion object {
        fun newInstance() =
            EditFragment()
    }
    private lateinit var binding: EditFragmentBinding
    private lateinit var viewModel: EditViewModel
    private val args: EditFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate view and obtain an instance of the binding class

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.edit_fragment,
            container,
            false
        )
        val database = TweetDatabase.getDatabase(requireContext()).tweetDao()
        viewModel = EditViewModelFactory(database, args.tweet).create(EditViewModel::class.java)

        binding.buttonAddContent.setOnClickListener{
            viewModel.onClickAdd()
            val action = EditFragmentDirections.actionEditFragmentToMainFragment()
            findNavController().navigate(action)
        }

        viewModel.tweetRestContentLength.observe(viewLifecycleOwner, Observer {
            binding.textViewCountLetter.text = it
        })

        binding.editViewModel = viewModel

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

}