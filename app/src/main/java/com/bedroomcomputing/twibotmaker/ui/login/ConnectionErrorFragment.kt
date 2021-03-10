package com.bedroomcomputing.twibotmaker.ui.login

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bedroomcomputing.twibotmaker.R

class ConnectionErrorFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.spreadsheet_connectionError)
            .setPositiveButton("OK") { _,_ -> }
            .create()

    companion object {
        const val TAG = "ConnectionErrorFragment"
    }
}