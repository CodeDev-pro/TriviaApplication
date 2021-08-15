package com.codedev.triviaapp.ui.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.lifecycle.*
import com.codedev.triviaapp.BaseApplication
import com.codedev.triviaapp.data.remote.api.FirebaseFirestore
import com.codedev.triviaapp.repositories.DatastoreRepository
import com.codedev.triviaapp.utils.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    application: Application,
    private val firebaseAuth: FirebaseAuth,
    private val datastoreRepository: DatastoreRepository
) : AndroidViewModel(application) {

    private val loginEventChannel = Channel<LoginEvents>()
    val loginEventsFlow = loginEventChannel.receiveAsFlow()

    val loginRememberMeState = MutableStateFlow<Boolean>(false)

    private val _loginUiState = MutableLiveData<LoginUiState>()
    val loginUiState: LiveData<LoginUiState> get() = _loginUiState

    fun initState() = _loginUiState.postValue(LoginUiState.InitialState)

    fun loginViaEmailAndPassword(email: String, password: String) = viewModelScope.launch {
        Log.d(TAG_LOGIN, "loginViaEmailAndPassword: login clicked")
        if(SharedResources.hasInternetConnection(getApplication())){
            _loginUiState.postValue(LoginUiState.Loading)
            val loginInfo = Validator.validateLogin(email, password)
            Log.d(TAG_LOGIN, "loginViaEmailAndPassword: loginState: ${_loginUiState.toString() + loginUiState.toString()}")

            if(loginInfo.error) {
                Log.d(TAG_LOGIN, "loginViaEmailAndPassword: ${loginInfo.message}")
                _loginUiState.postValue(LoginUiState.Error(message = loginInfo.message))
                Log.d(TAG_LOGIN, "loginViaEmailAndPassword: loginState: ${_loginUiState.toString() + loginUiState.toString()}")
            } else {
                Log.d(TAG_LOGIN, "loginViaEmailAndPassword: success")
                try {
                    val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                    Log.d(TAG_LOGIN, "loginViaEmailAndPassword: $result")
                    result.user?.let {
                        Log.d(TAG_LOGIN, "loginViaEmailAndPassword: $it")
                        _loginUiState.postValue(LoginUiState.Success(user = it))
                        saveToDatastore(it.uid, it.displayName ?: "", it.email ?: "")
                    }
                }catch (e: Exception) {
                    _loginUiState.postValue(LoginUiState.Error(message = e.toString()))
                }
            }

        }else {
            Log.d(TAG_LOGIN, "loginViaEmailAndPassword: no internet connection")
            loginEventChannel.send(LoginEvents.NoInternetConnection)
        }
    }

    fun loginViaGoogle() {
        viewModelScope.launch {
            if(SharedResources.hasInternetConnection(getApplication())){
                _loginUiState.postValue(LoginUiState.Loading)
                try {
                    val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(CLIENT_ID)
                        .requestEmail()
                        .build()
                    val signInClient = GoogleSignIn.getClient(getApplication<Application>().applicationContext, options)
                    signInClient.signInIntent.also {
                        loginEventChannel.send(LoginEvents.GoogleSignInIntentReceiver(it))
                    }
                    Log.d(TAG_LOGIN, "loginViaGoogle: sent")
                }catch (e: Exception) {
                    Log.d(TAG_LOGIN, "loginViaGoogle: $e")
                    _loginUiState.postValue(LoginUiState.Error(e.toString()))
                }
            }else {
                Log.d(TAG_LOGIN, "loginViaGoogle: no internet")
                loginEventChannel.send(LoginEvents.NoInternetConnection)
            }
        }
    }

    private suspend fun saveToDatastore(userID: String, username: String, email: String) {
        if (loginRememberMeState.value) {
            Log.d(TAG_LOGIN, "continueLoginProcess: saving remember me")
            datastoreRepository.saveUserState(true, userID, username = username, email = email)
        }
        else datastoreRepository.saveUserState(false, "", "", "")
    }

    fun continueLoginProcess(data: Intent) {
        Log.d(TAG_LOGIN, "loginViaGoogle: $data")
        viewModelScope.launch {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data).result
                Log.d(TAG_LOGIN, "continueLoginProcess: $account")
                account?.let {
                    val credentials = GoogleAuthProvider.getCredential(it.idToken, null)
                    Log.d(TAG_LOGIN, "continueLoginProcess: $credentials")
                    if(!FirebaseFirestore.checkIfUserExists(it)){
                        val error = SharedResources.errorStringFormatter(listOf("User does not exist in our database, please sign up to Continue"))
                        Log.d(TAG_LOGIN, "continueLoginProcess: user does not exist")
                        _loginUiState.postValue(LoginUiState.Error(error))
                    } else {
                        Log.d(TAG_LOGIN, "continueLoginProcess: user exists")
                        val task = firebaseAuth.signInWithCredential(credentials).await()
                        Log.d(TAG_LOGIN, "continueLoginProcess: ${task.toString()}")
                        task.user?.let { user ->
                            _loginUiState.postValue(LoginUiState.Success(user = user))
                            saveToDatastore(user.uid, it.displayName ?: "", it.email ?: "")
                        }
                    }
                }
            }catch (e: Exception) {
                Log.d(TAG_LOGIN, "continueLoginProcess: $e")
                _loginUiState.postValue(LoginUiState.Error(e.toString()))
            }
        }

    }

}

sealed class LoginEvents {
    data class GoogleSignInIntentReceiver(val intent: Intent) : LoginEvents()
    object NoInternetConnection : LoginEvents()
    data class DisplayError(val errors: String): LoginEvents()
}