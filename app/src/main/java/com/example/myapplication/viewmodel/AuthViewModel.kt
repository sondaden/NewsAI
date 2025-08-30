package com.example.myapplication.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.myapplication.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private lateinit var authRepository: AuthRepository
    private var contextHolder: Context? = null
    private val TAG = "AuthViewModel"

    private val _loginState = MutableLiveData<Result<String>?>()
    val loginState: LiveData<Result<String>?> = _loginState

    private val _registerState = MutableLiveData<Result<String>?>()
    val registerState: LiveData<Result<String>?> = _registerState

    private val _resetPasswordState = MutableLiveData<Result<String>?>()
    val resetPasswordState: LiveData<Result<String>?> = _resetPasswordState

    private var userViewModel: UserViewModel? = null

    fun setUserViewModel(userViewModel: UserViewModel) {
        Log.d(TAG, "Liên kết UserViewModel với AuthViewModel")
        this.userViewModel = userViewModel
    }

    private fun initRepository(context: Context) {
        if (!::authRepository.isInitialized) {
            Log.d(TAG, "Khởi tạo AuthRepository với context")
            authRepository = AuthRepository(context)
            contextHolder = context
        }
    }

    fun loginUser(email: String, password: String, context: Context) {
        Log.d(TAG, "Gọi loginUser với email: $email")
        initRepository(context)
        authRepository.loginUser(email, password) { result ->
            Log.d(TAG, "Kết quả đăng nhập: $result")
            _loginState.postValue(result)
            result.onSuccess {
                Log.d(TAG, "Đăng nhập thành công, trì hoãn 5 giây trước khi gọi fetchUserData để đồng bộ")
                viewModelScope.launch {
                    delay(5000)
                    userViewModel?.fetchUserData()
                }
            }
            result.onFailure { error ->
                Log.e(TAG, "Đăng nhập thất bại: ${error.message}", error)
            }
        }
    }

    fun clearLoginState() {
        Log.d(TAG, "Xóa trạng thái đăng nhập trong AuthViewModel")
        _loginState.value = null
    }

    fun registerUser(email: String, password: String, displayName: String, avatarUrl: String?, context: Context) {
        Log.d(TAG, "Gọi registerUser với email: $email, displayName: $displayName")
        initRepository(context)
        authRepository.registerUser(email, password, displayName, avatarUrl) { result ->
            Log.d(TAG, "Kết quả đăng ký: $result")
            _registerState.postValue(result)
        }
    }

    fun clearRegisterState() {
        Log.d(TAG, "Xóa trạng thái đăng ký trong AuthViewModel")
        _registerState.value = null
    }

    fun sendPasswordResetEmail(email: String, context: Context) {
        Log.d(TAG, "Gọi sendPasswordResetEmail với email: $email")
        initRepository(context)

        // Kiểm tra email trống
        if (email.isBlank()) {
            Log.d(TAG, "Email trống")
            _resetPasswordState.postValue(Result.failure(Exception("Email không được để trống!")))
            return
        }

        // Kiểm tra định dạng email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.d(TAG, "Định dạng email không hợp lệ: $email")
            _resetPasswordState.postValue(Result.failure(Exception("Định dạng email không hợp lệ!")))
            return
        }

        // Kiểm tra email đã đăng ký trong Firestore
        authRepository.checkEmailExists(email) { exists ->
            if (!exists) {
                Log.d(TAG, "Email chưa đăng ký tài khoản: $email")
                _resetPasswordState.postValue(Result.failure(Exception("Email chưa đăng ký tài khoản!")))
            } else {
                // Gửi yêu cầu đặt lại mật khẩu
                authRepository.sendPasswordResetEmail(email) { result ->
                    Log.d(TAG, "Kết quả gửi email đặt lại mật khẩu: $result")
                    _resetPasswordState.postValue(result)
                }
            }
        }
    }

    fun clearResetPasswordState() {
        Log.d(TAG, "Xóa trạng thái đặt lại mật khẩu trong AuthViewModel")
        _resetPasswordState.value = null
    }

    fun logoutUser(context: Context, onResult: (Result<String>) -> Unit, onLogoutSuccess: () -> Unit = {}) {
        Log.d(TAG, "Gọi logoutUser")
        initRepository(context)
        authRepository.logoutUser { result ->
            Log.d(TAG, "Kết quả đăng xuất: $result")
            if (result.isSuccess) {
                Log.d(TAG, "Đăng xuất thành công, cập nhật loginState và gọi onLogoutSuccess")
                _loginState.postValue(null)
                onLogoutSuccess()
            } else {
                Log.e(TAG, "Đăng xuất thất bại: ${result.exceptionOrNull()?.message}")
                _loginState.postValue(result)
            }
            onResult(result)
        }
    }

    fun checkLoginStatus(context: Context) {
        Log.d(TAG, "Gọi checkLoginStatus")
        initRepository(context)
        authRepository.checkLoginStatus { result ->
            Log.d(TAG, "Kết quả kiểm tra trạng thái đăng nhập: $result")
            _loginState.postValue(result)
        }
    }

    fun checkEmailExists(email: String, context: Context, callback: (Boolean) -> Unit) {
        Log.d(TAG, "Gọi checkEmailExists với email: $email")
        initRepository(context)
        authRepository.checkEmailExists(email, callback)
    }

    fun resendVerificationEmail(callback: (Boolean) -> Unit) {
        Log.d(TAG, "Gọi resendVerificationEmail")
        if (::authRepository.isInitialized) {
            authRepository.resendVerificationEmail(callback)
        } else {
            Log.e(TAG, "authRepository chưa được khởi tạo")
            callback(false)
        }
    }

    fun getDeviceId(context: Context): String {
        Log.d(TAG, "Gọi getDeviceId")
        initRepository(context)
        val deviceId = authRepository.getDeviceId()
        Log.d(TAG, "Device ID trả về: $deviceId")
        return deviceId
    }
}