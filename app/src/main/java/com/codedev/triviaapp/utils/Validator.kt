package com.codedev.triviaapp.utils

import android.util.Patterns
import java.util.regex.Pattern

object Validator {
    private val regexPassword = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,20}\$"

    fun validateSignUp(email: String, password: String, username: String, retypePassword: String) : ErrorNotifier {
        val usernameInfo = validateUsername(username)
        val emailInfo = validateEmail(email)
        val passwordInfo = validatePassword(password)
        val retypePasswordInfo = validateRetypePassword(retypePassword, password)

        val errors = mutableListOf<String>()

        if (usernameInfo.error){
            errors.add(usernameInfo.message)
        }
        if(emailInfo.error) {
            errors.add(emailInfo.message)
        }
        if (passwordInfo.error) {
            errors.add(passwordInfo.message)
        }
        if(retypePasswordInfo.error) {
            errors.add(retypePasswordInfo.message)
        }

        if(usernameInfo.error || passwordInfo.error || retypePasswordInfo.error || emailInfo.error) {
            val error = SharedResources.errorStringFormatter(errors)
            return ErrorNotifier(true, error)
        }
        return ErrorNotifier(false, "")
    }

    private fun validateRetypePassword(retypePassword: String, password: String) : ErrorNotifier {
        if(password == retypePassword) return ErrorNotifier(false, "")
        return ErrorNotifier(true, "Password does not match")
    }

    private fun validateUsername(username: String) : ErrorNotifier {
        if(username.trim() != "") {
            return ErrorNotifier(false, "")
        }
        return ErrorNotifier(true, "Please Input a valid username")
    }

    private fun validateEmail(email: String) : ErrorNotifier{
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            return ErrorNotifier(false, "")
        }
        return ErrorNotifier(true, "Please Input a correct email")
    }

    private fun validatePassword(password: String): ErrorNotifier{
        if (Pattern.compile(regexPassword).matcher(password).matches()){
            return ErrorNotifier(false, "")
        }
        return ErrorNotifier(true, "Password must be at least 8 digits long and Contain an Uppercase and a number")
    }

    fun validateLogin(email: String, password: String): ErrorNotifier {
        val emailInfo = validateEmail(email)
        val passwordInfo = validatePassword(password)

        val errors = mutableListOf<String>()
        if(emailInfo.error) {
            errors.add(emailInfo.message)
        }
        if (passwordInfo.error) {
            errors.add(passwordInfo.message)
        }

        if(passwordInfo.error || emailInfo.error) {
            val error = SharedResources.errorStringFormatter(errors)
            return ErrorNotifier(true, error)
        }
        return ErrorNotifier(false, "")
    }
}

data class ErrorNotifier(
    val error: Boolean,
    val message: String
)