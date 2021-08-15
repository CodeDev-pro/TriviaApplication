package com.codedev.triviaapp.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.codedev.triviaapp.R
import com.codedev.triviaapp.databinding.FragmentTriviaBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TriviaFragment : Fragment(R.layout.fragment_trivia) {

    private var _binding: FragmentTriviaBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        _binding = FragmentTriviaBinding.bind(view)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}