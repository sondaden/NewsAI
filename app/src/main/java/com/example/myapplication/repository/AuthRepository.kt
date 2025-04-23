package com.example.myapplication.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

/**
 * Repository quản lý xác thực người dùng với Firebase Auth và Firestore.
 */
class AuthRepository(private val context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val prefs: SharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    /**
     * Đăng nhập người dùng bằng email và mật khẩu.
     */
    fun loginUser(email: String, password: String, onResult: (Result<String>) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onResult(Result.failure(Exception("Email và mật khẩu không được để trống!")))
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        if (!user.isEmailVerified) {
                            onResult(Result.failure(Exception("Tài khoản chưa xác minh email! Vui lòng kiểm tra email.")))
                        } else {
                            updateLoginState(user.uid, true, onResult)
                        }
                    } else {
                        onResult(Result.failure(Exception("Không tìm thấy người dùng!")))
                    }
                } else {
                    onResult(Result.failure(Exception("Sai email hoặc mật khẩu!")))
                }
            }
    }

    /**
     * Đăng ký người dùng mới.
     */
    fun registerUser(email: String, password: String, displayName: String, avatarUrl: String?, onResult: (Result<String>) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { emailTask ->
                        if (emailTask.isSuccessful) {
                            saveUserData(user.uid, email, displayName, avatarUrl, onResult)
                        } else {
                            onResult(Result.failure(Exception("Không thể gửi email xác nhận!")))
                        }
                    }
                } else {
                    onResult(Result.failure(Exception(task.exception?.message ?: "Đăng ký thất bại!")))
                }
            }
    }

    /**
     * Đăng xuất người dùng.
     */
    fun logoutUser(onResult: (Result<String>) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            updateLoginState(user.uid, false, onResult) { auth.signOut() }
        } else {
            saveLoginState(false)
            onResult(Result.success("Đăng xuất thành công"))
        }
    }

    /**
     * Kiểm tra trạng thái đăng nhập.
     * @return Result.success nếu đã đăng nhập, Result.failure nếu không.
     */
    fun checkLoginStatus(onResult: (Result<String>) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            saveLoginState(false)
            onResult(Result.failure(Exception("Chưa đăng nhập")))
            return
        }

        firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                val storedDeviceIds = (document.get("deviceIds") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                val isLoggedIn = document.getBoolean("isLoggedIn") == true
                val deviceId = getDeviceId()

                if (isLoggedIn && storedDeviceIds.contains(deviceId)) {
                    saveLoginState(true)
                    onResult(Result.success("Đã đăng nhập (Online)"))
                } else {
                    saveLoginState(false)
                    onResult(Result.failure(Exception("Phiên đăng nhập không hợp lệ")))
                }
            }
            .addOnFailureListener {
                onResult(Result.failure(it))
            }
    }

    /**
     * Kiểm tra email đã tồn tại trong Firestore chưa.
     */
    fun checkEmailExists(email: String, callback: (Boolean) -> Unit) {
        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { result -> callback(!result.isEmpty) }
            .addOnFailureListener { callback(false) }
    }

    /**
     * Gửi lại email xác thực.
     */
    fun resendVerificationEmail(callback: (Boolean) -> Unit) {
        auth.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener { task -> callback(task.isSuccessful) }
            ?: callback(false)
    }

    /**
     * Lấy hoặc tạo ID thiết bị duy nhất.
     */
    fun getDeviceId(): String {
        var deviceId = prefs.getString("device_id", null)
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            prefs.edit { putString("device_id", deviceId) }
        }
        return deviceId
    }

    // Lưu trạng thái đăng nhập vào SharedPreferences
    private fun saveLoginState(isLoggedIn: Boolean) {
        prefs.edit { putBoolean("isLoggedIn", isLoggedIn) }
    }

    // Cập nhật trạng thái đăng nhập trên Firestore
    private fun updateLoginState(uid: String, isLoggedIn: Boolean, onResult: (Result<String>) -> Unit, onSuccess: (() -> Unit)? = null) {
        val deviceId = getDeviceId()
        val userRef = firestore.collection("users").document(uid)

        userRef.get().addOnSuccessListener { document ->
            val existingDevices = (document.get("deviceIds") as? List<*>)?.mapNotNull { it as? String }?.toMutableList() ?: mutableListOf()
            if (isLoggedIn && !existingDevices.contains(deviceId)) existingDevices.add(deviceId)
            else if (!isLoggedIn) existingDevices.remove(deviceId)

            userRef.update(
                "isLoggedIn", isLoggedIn || existingDevices.isNotEmpty(),
                "deviceIds", existingDevices
            ).addOnSuccessListener {
                saveLoginState(isLoggedIn)
                onSuccess?.invoke()
                onResult(Result.success(if (isLoggedIn) "Đăng nhập thành công" else "Đăng xuất thành công"))
            }.addOnFailureListener {
                onResult(Result.failure(Exception("Lỗi khi cập nhật trạng thái đăng nhập!")))
            }
        }.addOnFailureListener {
            onResult(Result.failure(Exception("Lỗi khi truy xuất thông tin người dùng!")))
        }
    }

    // Lưu dữ liệu người dùng mới vào Firestore
    private fun saveUserData(uid: String, email: String, displayName: String, avatarUrl: String?, onResult: (Result<String>) -> Unit) {
        val userData = hashMapOf(
            "uid" to uid,
            "email" to email,
            "displayName" to displayName,
            "avatarUrl" to avatarUrl,
            "isEmailVerified" to false,
            "isLoggedIn" to false,
            "createdAt" to System.currentTimeMillis(),
            "provider" to "email",
            "favorites" to emptyList<String>(),
            "savedArticles" to emptyList<String>(),
            "followedSources" to emptyList<String>(),
            "preferredTopics" to emptyList<String>()
        )
        firestore.collection("users").document(uid)
            .set(userData)
            .addOnSuccessListener { onResult(Result.success("Vui lòng kiểm tra email để xác nhận!")) }
            .addOnFailureListener { onResult(Result.failure(Exception("Lỗi khi tạo dữ liệu người dùng!"))) }
    }
}