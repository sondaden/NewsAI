package com.example.myapplication.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Log
import com.example.myapplication.repository.AuthRepository

class AuthViewModel : ViewModel() {
    private lateinit var authRepository: AuthRepository

    private val _loginState = MutableLiveData<Result<String>?>()
    val loginState: LiveData<Result<String>?> = _loginState

    private val _registerState = MutableLiveData<Result<String>?>()
    val registerState: LiveData<Result<String>?> = _registerState

    private fun initRepository(context: Context) {
        // Initialize repository when context is available
        if (!::authRepository.isInitialized) {
            authRepository = AuthRepository(context)
        }
    }

    fun loginUser(email: String, password: String, context: Context) {
        initRepository(context)
        authRepository.loginUser(email, password) { result ->
            _loginState.postValue(result)
        }
    }

    fun clearLoginState() {
        _loginState.value = null
    }

    fun registerUser(email: String, password: String, displayName: String, avatarUrl: String?) {
        authRepository.registerUser(email, password, displayName, avatarUrl) { result ->
            _registerState.postValue(result)
        }
    }

    fun clearRegisterState() {
        _registerState.value = null
    }

    fun logoutUser(context: Context) {
        initRepository(context)
        authRepository.logoutUser { result ->
            if (result.isSuccess) {
                _loginState.postValue(null)
            } else {
                _loginState.postValue(result)
            }
        }
    }

    fun checkLoginStatus(context: Context) {
        initRepository(context)
        authRepository.checkLoginStatus { result ->
            _loginState.postValue(result)
        }
    }

    fun checkEmailExists(email: String, callback: (Boolean) -> Unit) {
        authRepository.checkEmailExists(email, callback)
    }

    fun resendVerificationEmail(callback: (Boolean) -> Unit) {
        authRepository.resendVerificationEmail(callback)
    }

    fun getDeviceId(context: Context): String {
        initRepository(context)
        return authRepository.getDeviceId()
    }
}