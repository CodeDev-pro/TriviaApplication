package com.codedev.triviaapp.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.codedev.triviaapp.BaseApplication
import com.codedev.triviaapp.ui.HomeActivity
import com.google.firebase.firestore.auth.User

object SharedResources {

    fun navigateActivity(context: Context, activity: Activity) {
        Intent(context, activity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }.also {
            context.startActivity(it)
        }
    }

    fun randomizeString(string: String) : String {
        val subs = string.map { it }
        var i = 0
        val builder = StringBuilder()
        while (i<subs.size) {
            builder.append(subs.random())
            i++
        }
        return builder.toString()
    }

    fun hasInternetConnection(app: Application): Boolean {
        val connectivityManager =
            app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    else -> false
                }
            }
        }
        return false
    }

    fun errorStringFormatter(errors: List<String>) : String {
        val stringBuilder = StringBuilder()
        errors.forEach {
            stringBuilder.append("● $it\n")
        }
        return stringBuilder.toString()
    }
}