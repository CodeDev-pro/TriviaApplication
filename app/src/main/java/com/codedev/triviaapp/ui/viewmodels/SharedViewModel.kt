package com.codedev.triviaapp.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codedev.triviaapp.data.remote.api.FirebaseFirestore
import com.codedev.triviaapp.models.User
import com.codedev.triviaapp.repositories.DatastoreRepository
import com.codedev.triviaapp.utils.SHARED_VIEW_MODEL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val datastoreRepository: DatastoreRepository
) : ViewModel() {

    private val _userChannel: Channel<SharedEvents> = Channel()
    val userChannelFlow = _userChannel.receiveAsFlow()

    fun checkIfUserIsLoggedIn() = viewModelScope.launch {
        try {
            datastoreRepository.userPreferences.collect {
                val userID = it["userID"] as String?
                val rememberMe = it["rememberMe"] as Boolean? ?: false
                Log.d(SHARED_VIEW_MODEL, "checkIfUserIsLoggedIn: $userID")

                if(rememberMe && userID != null) {
                    Log.d(SHARED_VIEW_MODEL, "checkIfUserIsLoggedIn: $userID is not null")
                    _userChannel.send(SharedEvents.UserLoggedIn(userID))
                }else {
                    Log.d(SHARED_VIEW_MODEL, "checkIfUserIsLoggedIn: user does not exist $userID")
                    _userChannel.send(SharedEvents.UserNotLoggedIn)
                }
            }
        }catch (e: Exception) {
            Log.d("SHARED VIEW MODEL", "checkIfUserIsLoggedIn: $e")
            _userChannel.send(SharedEvents.UserNotLoggedIn)
        }
    }
}

sealed class SharedEvents {
    object UserNotLoggedIn : SharedEvents()
    data class UserLoggedIn(val userID: String) : SharedEvents()
}