package com.codedev.triviaapp.repositories

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.codedev.triviaapp.utils.TAG_DATASTORE
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import okio.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatastoreRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val Context.datastore by preferencesDataStore(name = "user_datastore")
    val userPreferences = context.datastore.data
        .catch { exception ->
            if(exception is IOException) {
                emit(emptyPreferences())
                Log.d(TAG_DATASTORE, exception.toString())
            } else {
                Log.d(TAG_DATASTORE, exception.toString())
                throw exception
            }
        }
        .map {
            val rememberMe = it[PreferencesKeys.REMEMBER_ME] ?: false
            val userID = it[PreferencesKeys.USER_ID]
            val email = it[PreferencesKeys.USERNAME]
            val username = it[PreferencesKeys.USERNAME]
            mapOf("userID" to userID, "rememberMe" to rememberMe, "email" to email, "username" to username)
        }

    suspend fun saveUserState(rememberMe: Boolean, userID: String, username: String, email: String) {
        Log.d(TAG_DATASTORE, "Saving $userID")
        context.datastore.edit {
            it[PreferencesKeys.REMEMBER_ME] = rememberMe
            it[PreferencesKeys.USER_ID] = userID
            it[PreferencesKeys.USERNAME] = username
            it[PreferencesKeys.EMAIL] = email
        }
        Log.d(TAG_DATASTORE, "Saved $userID")
    }

    private object PreferencesKeys {
        val REMEMBER_ME = booleanPreferencesKey("remember_me")
        val USER_ID = stringPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val EMAIL = stringPreferencesKey("email")
    }
}