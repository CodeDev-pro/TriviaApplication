package com.codedev.triviaapp.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.codedev.triviaapp.R
import com.codedev.triviaapp.databinding.FragmentStartUpPageBinding
import com.codedev.triviaapp.ui.viewmodels.SharedEvents
import com.codedev.triviaapp.ui.viewmodels.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class StartUpPageFragment : Fragment(R.layout.fragment_start_up_page) {

    private var _binding: FragmentStartUpPageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SharedViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentStartUpPageBinding.bind(view)


        lifecycleScope.launchWhenStarted {
            viewModel.checkIfUserIsLoggedIn()
            viewModel.userChannelFlow.collect {
                when(it) {
                    is SharedEvents.UserNotLoggedIn -> {
                        delay(3000)
                        val action = StartUpPageFragmentDirections.actionStartUpPageFragmentToLoginFragment()
                        findNavController().navigate(action)
                    }
                    is SharedEvents.UserLoggedIn -> {
                        delay(3000)
                        val action = StartUpPageFragmentDirections.actionStartUpPageFragmentToLandingFragment()
                        findNavController().navigate(action)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}