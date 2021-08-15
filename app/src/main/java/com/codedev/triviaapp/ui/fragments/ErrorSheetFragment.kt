package com.codedev.triviaapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.codedev.triviaapp.R
import com.codedev.triviaapp.databinding.FragmentErrorSheetBinding
import com.codedev.triviaapp.utils.TAG_BOTTOM
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ErrorSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentErrorSheetBinding? = null
    private val binding get() =  _binding!!
    private val args: ErrorSheetFragmentArgs by navArgs<ErrorSheetFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d(TAG_BOTTOM, "onCreateView: bottom sheet")
        Log.d(TAG_BOTTOM, "onCreateView: ${args.errors}")

        return inflater.inflate(R.layout.fragment_error_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentErrorSheetBinding.bind(view)

        Log.d(TAG_BOTTOM, "onViewCreated: bottom sheet")
        Log.d(TAG_BOTTOM, "onViewCreated: ${args.errors}")

        binding.buttonCancel.setOnClickListener { onClickListener() }
        binding.buttonOk.setOnClickListener { onClickListener() }
        binding.cancelButton.setOnClickListener { onClickListener() }
        binding.errorText.text = args.errors

    }

    private fun onClickListener() {
        dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}