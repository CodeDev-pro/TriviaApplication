package com.codedev.triviaapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.codedev.triviaapp.R
import com.codedev.triviaapp.databinding.FragmentLoginBinding
import com.codedev.triviaapp.ui.HomeActivity
import com.codedev.triviaapp.ui.viewmodels.LoginEvents
import com.codedev.triviaapp.ui.viewmodels.LoginViewModel
import com.codedev.triviaapp.utils.LoginUiState
import com.codedev.triviaapp.utils.SharedResources
import com.codedev.triviaapp.utils.TAG_LOGIN
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

const val REQUEST_CODE_LOGIN = 1

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private val viewModel: LoginViewModel by viewModels()
    private var _binding: FragmentLoginBinding? = null
    private val binding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentLoginBinding.bind(view)

        binding.dontHaveAnAccount.setOnClickListener{
            val action = LoginFragmentDirections.actionLoginFragmentToSignUpFragment()
            findNavController().navigate(action)
        }

        binding.signUpButton.setOnClickListener {
            viewModel.loginViaEmailAndPassword(
                binding.loginEmailEditText.text.toString(),
                binding.loginPasswordEditText.text.toString()
            )
        }

        binding.rememberMeRadioButton.setOnCheckedChangeListener { _, b ->
            Log.d(TAG_LOGIN, "radio button clicked")
            viewModel.loginRememberMeState.value = b
        }

        binding.googleButton.setOnClickListener { viewModel.loginViaGoogle() }

        binding.loginEmailEditText.addTextChangedListener {

        }

        binding.loginPasswordEditText.addTextChangedListener {

        }

        viewModel.loginUiState.observe(viewLifecycleOwner) { event ->
            when(event){
                is LoginUiState.Loading -> {
                    binding.loadingScreen.visibility = View.VISIBLE
                    binding.loadingScreen.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            R.anim.fade_in
                        )
                    )
                }
                is LoginUiState.Error -> {
                    binding.loadingScreen.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            R.anim.fade_out
                        )
                    )
                    binding.loadingScreen.visibility = View.GONE
                    Log.d(TAG_LOGIN, "onViewCreated: error ${event.message}")
                    displayError(event.message)
                    Log.d(TAG_LOGIN, "onViewCreated: error ${event.message} success")
                }
                is LoginUiState.Success -> {
                    binding.loadingScreen.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            R.anim.fade_out
                        )
                    )
                    Log.d(TAG_LOGIN, "onViewCreated: success ${event.user}")
                    binding.loadingScreen.visibility = View.GONE
                    SharedResources.navigateActivity(requireContext(), HomeActivity())
                }
                is LoginUiState.InitialState -> {
                    binding.loadingScreen.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.loginEventsFlow.collect { event ->
                when(event) {
                    is LoginEvents.GoogleSignInIntentReceiver -> {
                        startActivityForResult(event.intent, REQUEST_CODE_LOGIN)
                    }
                    is LoginEvents.NoInternetConnection -> {
                        displayError("Please turn on your data connection")
                    }
                    is LoginEvents.DisplayError -> {
                        displayError(event.errors)
                    }
                }
            }

        }
    }

    private fun displayError(error: String) {
        val message = SharedResources.errorStringFormatter(listOf(error))
        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToErrorSheetFragment(message))
    }

    override fun onStop() {
        super.onStop()
        viewModel.initState()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_LOGIN) {
            data?.let {
                viewModel.continueLoginProcess(data)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}