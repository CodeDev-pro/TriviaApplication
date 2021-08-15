package com.codedev.triviaapp.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.codedev.triviaapp.R
import com.codedev.triviaapp.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        _binding = FragmentSettingsBinding.bind(view)

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}