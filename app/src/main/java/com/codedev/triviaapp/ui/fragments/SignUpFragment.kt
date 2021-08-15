package com.codedev.triviaapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.CompoundButton
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.codedev.triviaapp.R
import com.codedev.triviaapp.databinding.FragmentSignUpBinding
import com.codedev.triviaapp.ui.HomeActivity
import com.codedev.triviaapp.ui.viewmodels.LoginEvents
import com.codedev.triviaapp.ui.viewmodels.SignUpViewModel
import com.codedev.triviaapp.utils.LoginUiState
import com.codedev.triviaapp.utils.SharedResources
import com.codedev.triviaapp.utils.SignUpUiState
import com.codedev.triviaapp.utils.TAG_SIGNUP
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

const val REQUEST_SIGN_UP = 2

@AndroidEntryPoint
class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    private val viewModel: SignUpViewModel by viewModels()
    private var _binding: FragmentSignUpBinding? = null
    private val binding
        get() = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentSignUpBinding.bind(view)

        binding.ohIHaveAnAccount.setOnClickListener {
            val action = SignUpFragmentDirections.actionSignUpFragmentToLoginFragment()
            findNavController().navigate(action)
        }

        binding.rememberMeRadioButton.setOnCheckedChangeListener { _, check ->
            viewModel.signUpRememberMeState.value = check
        }



        binding.signUpButton.setOnClickListener {
            viewModel.signUpViaEmailAndPassword(
                binding.signUpEmailInput.text.toString(),
                binding.signUpPasswordInput.text.toString(),
                binding.signUpUsernameInput.text.toString(),
                binding.signUpRetypePasswordInput.text.toString()
            )
        }
        binding.googleButton.setOnClickListener { viewModel.signUpViaGoogle() }

        lifecycleScope.launchWhenStarted {
            viewModel.signUpEventFlow.collect { event ->
                when(event) {
                    is LoginEvents.GoogleSignInIntentReceiver -> {
                        startActivityForResult(event.intent, REQUEST_SIGN_UP)
                    }
                    is LoginEvents.NoInternetConnection -> {
                        val action = SignUpFragmentDirections.actionSignUpFragmentToErrorSheetFragment(
                            SharedResources.errorStringFormatter(listOf("Please turn on your data connection")))
                        findNavController().navigate(action)
                    }
                    is LoginEvents.DisplayError -> {
                        val action = SignUpFragmentDirections.actionSignUpFragmentToErrorSheetFragment(
                            SharedResources.errorStringFormatter(listOf(event.errors)))
                        findNavController().navigate(action)
                    }
                }
            }
        }

        viewModel.signUpUiState.observe(viewLifecycleOwner) { event ->
            when(event){

                is SignUpUiState.Loading -> {
                    loadingState()
                }
                is SignUpUiState.Error -> {
                    errorState(event)
                }
                is SignUpUiState.Success -> {
                    successState()
                }
                is SignUpUiState.InitialState -> {
                    binding.loadingScreen.visibility = View.GONE
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_SIGN_UP) {
            Log.d(TAG_SIGNUP, "continueSignUpProcess: $data")
            data?.let {
                viewModel.continueSignUpProcess(data)
            }
            //Log.d(TAG_SIGNUP, "continueSignUpProcess: data is null")
        }
    }

    private fun successState() {
        binding.loadingScreen.startAnimation(AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.fade_out
        ))
        binding.loadingScreen.visibility = View.GONE
        val intent = Intent(requireContext(), HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }.also {
            startActivity(it)
        }

    }

    private fun errorState(event: SignUpUiState.Error) {
        binding.loadingScreen.startAnimation(AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.fade_out
        ))
        binding.loadingScreen.visibility = View.GONE
        val action = SignUpFragmentDirections.actionSignUpFragmentToErrorSheetFragment(event.message)
        findNavController().navigate(action)
    }

    private fun loadingState() {
        binding.loadingScreen.visibility = View.VISIBLE
        binding.loadingScreen.startAnimation(AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.fade_in
        ))
    }

    override fun onStop() {
        super.onStop()
        viewModel.initState()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}