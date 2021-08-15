package com.codedev.triviaapp.data.remote.api

import android.util.Log
import com.codedev.triviaapp.models.User
import com.codedev.triviaapp.utils.TAG_DATASTORE
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.CoroutineContext

object FirebaseFirestore {

    private val usersCollection = Firebase.firestore.collection("users")

    suspend fun saveUserInfo(user: User) {
        user.id?.let {
            usersCollection.document(it).set(user).await()
            Log.d(TAG_DATASTORE, "checkIfUserExists: saved successfully")
        }
    }

    suspend fun getUserInfo(userID: String) : User? {
        try {
            val documentSnapshot = usersCollection.document(userID).get().await()

            if (documentSnapshot.exists()) {
                Log.d(TAG_DATASTORE, "checkIfUserExists: $documentSnapshot")
                val user = User(
                    id = documentSnapshot["id"] as String? ?: userID,
                    displayName = documentSnapshot["displayName"] as String? ?: "1",
                    email = documentSnapshot["email"].toString() as String? ?: "1",
                    password = documentSnapshot["password"] as String? ?: "1"
                )
                Log.d(TAG_DATASTORE, "checkIfUserExists: $user")
                user.let {
                    return it
                }
            } else {
                Log.d(TAG_DATASTORE, "checkIfUserExists: user does not exists")
                throw Exception("User not found")
            }
        }catch (e: Exception) {
            Log.d(TAG_DATASTORE, "checkIfUserExists: $e")
            throw e
        }
        return null
    }

    suspend fun checkIfUserExists(account: GoogleSignInAccount) : Boolean {
        try {
            val querySnapshot = usersCollection.get().await()

            if(querySnapshot.documents.isNotEmpty()) {
                for(document in querySnapshot.documents) {
                    Log.d(TAG_DATASTORE, "checkIfUserExists: $document")
                    if(document.exists()) {
                        val user = User(
                            id = document["id"] as String? ?: "1",
                            displayName = document["displayName"] as String? ?: "1",
                            email = document["email"].toString() as String? ?: "1",
                            password = document["password"] as String? ?: "1"
                        )
                        Log.d(TAG_DATASTORE, "checkIfUserExists: ${user.toString()}")
                        if(user.email == account.email) {
                            Log.d(TAG_DATASTORE, "checkIfUserExists: ${user.email}")
                            return true
                        }
                    }
                    //Log.d(TAG_DATASTORE, "checkIfUserExists: does not exists")
                    //throw Exception("Document does not exist")
                }
            }else {
                throw Exception("Database is empty")
            }
            Log.d(TAG_DATASTORE, "checkIfUserExists: docs is empty")
            return false
        }catch(e: Exception){
            Log.d(TAG_DATASTORE, "checkIfUserExists: $e")
            return false
        }
    }
}