package com.codedev.triviaapp.ui.viewmodels

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import com.codedev.triviaapp.BaseApplication
import com.codedev.triviaapp.data.remote.api.FirebaseFirestore
import com.codedev.triviaapp.models.User
import com.codedev.triviaapp.repositories.DatastoreRepository
import com.codedev.triviaapp.utils.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    app: Application,
    private val firebaseAuth: FirebaseAuth,
    private val datastoreRepository: DatastoreRepository
) : AndroidViewModel(app) {
    private val signUpEventChannel = Channel<LoginEvents>()
    val signUpEventFlow = signUpEventChannel.receiveAsFlow()

    val signUpRememberMeState = MutableStateFlow<Boolean>(false)

    private val _signUpUiState = MutableLiveData<SignUpUiState>(SignUpUiState.InitialState)
    val signUpUiState: LiveData<SignUpUiState> get() = _signUpUiState

    fun initState() = _signUpUiState.postValue(SignUpUiState.InitialState)

    fun signUpViaEmailAndPassword(email: String, password: String, username: String, retypePassword: String) = viewModelScope.launch {
        if(SharedResources.hasInternetConnection(getApplication())){
            _signUpUiState.postValue(SignUpUiState.Loading)
            val signUpInfo = Validator.validateSignUp(email, password, username, retypePassword)
            if(signUpInfo.error) {
                _signUpUiState.postValue(SignUpUiState.Error(message = signUpInfo.message))
            }
            else {
                try {
                    val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                    result.user?.let{
                        val user = User(it.uid, email, username, password)
                        FirebaseFirestore.saveUserInfo(user)
                        Log.d(TAG_SIGNUP, "signUpViaEmailAndPassword: ${user.toString()}")
                        saveToDatastore(userID = user.id ?: "", username, email)
                        _signUpUiState.postValue(SignUpUiState.Success(user = it))
                    }
                }catch (e: Exception) {
                    Log.d(TAG_SIGNUP, "signUpViaEmailAndPassword: $e")
                    _signUpUiState.postValue(SignUpUiState.Error(message = e.toString()))
                }
            }
        }else {
            _signUpUiState.postValue(SignUpUiState.Error("Please turn on your data connection"))
        }
    }

    private suspend fun saveToDatastore(userID: String, username: String, email: String) {
        if (signUpRememberMeState.value) {
            Log.d(TAG_LOGIN, "continueLoginProcess: saving remember me")
            datastoreRepository.saveUserState(true, userID, username = username, email = email)
        }
        else datastoreRepository.saveUserState(false, "", "", "")
    }

    fun signUpViaGoogle() {
        viewModelScope.launch {
            if(SharedResources.hasInternetConnection(getApplication())){
                _signUpUiState.postValue(SignUpUiState.Loading)
                try{
                    val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(CLIENT_ID)
                        .requestEmail()
                        .build()
                    val signInClient = GoogleSignIn.getClient(getApplication<Application>().applicationContext, options)
                    signInClient.signInIntent.also {
                        signUpEventChannel.send(LoginEvents.GoogleSignInIntentReceiver(it))
                    }
                    Log.d(TAG_SIGNUP, "signUpViaGoogle: sent")
                }catch(e: Exception) {
                    Log.d(TAG_SIGNUP, "signUpViaGoogle: $e")
                    _signUpUiState.postValue(SignUpUiState.Error(e.toString()))
                }
            }else {
                signUpEventChannel.send(LoginEvents.NoInternetConnection)
            }
        }
    }

    fun continueSignUpProcess(data: Intent) {
        Log.d(TAG_SIGNUP, "continueSignUpProcess: $data")
        viewModelScope.launch {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).result
            account?.let {
                Log.d(TAG_SIGNUP, "continueSignUpProcess: $it")
                val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
                try {
                    if(!FirebaseFirestore.checkIfUserExists(it)) {
                        val result = firebaseAuth.signInWithCredential(credentials).await()
                        Log.d(TAG_SIGNUP, "continueSignUpProcess: $result")
                        result.user?.let { user ->
                            FirebaseFirestore.saveUserInfo(User(
                                user.uid, user.email.toString(), user.displayName.toString(), SharedResources.randomizeString(user.uid.substring(0, 8)))
                            )
                            Log.d(TAG_SIGNUP, "continueSignUpProcess: ${user.email}")
                            _signUpUiState.postValue(SignUpUiState.Success(user))
                            saveToDatastore(userID = user.uid ?: "", user.displayName ?: "", user.email ?: "")
                        }
                    }else {
                        firebaseAuth.signOut()
                        _signUpUiState.postValue(SignUpUiState.Error(message = "User Already exists"))
                    }

                }catch (e: Exception) {
                    Log.d(TAG_SIGNUP, "continueSignUpProcess: $e")
                    _signUpUiState.postValue(SignUpUiState.Error(e.toString()))
                }
            }
            //Log.d(TAG_SIGNUP, "continueSignUpProcess: Account is null")
        }

    }
}