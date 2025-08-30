package com.example.myapplication.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class AuthRepository(private val context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val TAG = "AuthRepository"
    private var lastLoginTime: Long = 0

    // Hàm gửi email đặt lại mật khẩu
    fun sendPasswordResetEmail(email: String, onResult: (Result<String>) -> Unit) {
        Log.d(TAG, "Bắt đầu gửi email đặt lại mật khẩu cho: $email")
        if (email.isBlank()) {
            Log.w(TAG, "Email trống")
            onResult(Result.failure(Exception("Email không được để trống!")))
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Gửi email đặt lại mật khẩu thành công cho: $email")
                    onResult(Result.success("Email đặt lại mật khẩu đã được gửi! Vui lòng kiểm tra hộp thư."))
                } else {
                    Log.e(TAG, "Gửi email đặt lại mật khẩu thất bại: ${task.exception?.message}", task.exception)
                    val errorMessage = when {
                        task.exception?.message?.contains("no user") == true -> "Email không tồn tại!"
                        task.exception?.message?.contains("invalid") == true -> "Email không hợp lệ!"
                        else -> "Không thể gửi email đặt lại mật khẩu. Vui lòng thử lại!"
                    }
                    onResult(Result.failure(Exception(errorMessage)))
                }
            }
    }

    fun loginUser(email: String, password: String, onResult: (Result<String>) -> Unit) {
        Log.d(TAG, "Bắt đầu đăng nhập với email: $email")
        if (email.isBlank() || password.isBlank()) {
            Log.w(TAG, "Email hoặc mật khẩu trống")
            onResult(Result.failure(Exception("Email và mật khẩu không được để trống!")))
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Đăng nhập thành công với Firebase Auth")
                    lastLoginTime = System.currentTimeMillis()
                    val user = auth.currentUser
                    if (user != null) {
                        Log.d(TAG, "Người dùng hiện tại: UID=${user.uid}, Email=${user.email}, EmailVerified=${user.isEmailVerified}")
                        if (!user.isEmailVerified) {
                            Log.w(TAG, "Tài khoản chưa xác minh email")
                            onResult(Result.failure(Exception("Tài khoản chưa xác minh email! Vui lòng kiểm tra email.")))
                        } else {
                            val deviceId = getDeviceId()
                            Log.d(TAG, "Device ID: $deviceId")
                            val userRef = firestore.collection("users").document(user.uid)

                            userRef.get().addOnSuccessListener { document ->
                                if (document.exists()) {
                                    Log.d(TAG, "Lấy tài liệu người dùng thành công: ${document.data}")
                                    val existingDevices = (document.get("deviceIds") as? List<*>)?.mapNotNull { it as? String }?.toMutableList() ?: mutableListOf()
                                    if (!existingDevices.contains(deviceId)) {
                                        existingDevices.add(deviceId)
                                        Log.d(TAG, "Thêm device ID mới: $deviceId")
                                    }

                                    userRef.update(
                                        "isLoggedIn", true,
                                        "deviceIds", existingDevices,
                                        "isEmailVerified", true
                                    ).addOnSuccessListener {
                                        Log.d(TAG, "Cập nhật trạng thái đăng nhập trên Firestore thành công")
                                        saveLoginState(true)
                                        Log.d(TAG, "Đã lưu trạng thái đăng nhập: isLoggedIn=true")
                                        onResult(Result.success("Đăng nhập thành công!"))
                                    }.addOnFailureListener {
                                        Log.e(TAG, "Lỗi khi cập nhật trạng thái đăng nhập trên Firestore: ${it.message}", it)
                                        onResult(Result.failure(Exception("Lỗi khi cập nhật trạng thái đăng nhập: ${it.message}")))
                                    }
                                } else {
                                    Log.w(TAG, "Tài liệu người dùng không tồn tại cho UID: ${user.uid}")
                                    onResult(Result.failure(Exception("Không tìm thấy thông tin người dùng")))
                                }
                            }.addOnFailureListener {
                                Log.e(TAG, "Lỗi khi lấy tài liệu người dùng: ${it.message}", it)
                                onResult(Result.failure(Exception("Lỗi khi lấy thông tin người dùng: ${it.message}")))
                            }
                        }
                    } else {
                        Log.w(TAG, "Không tìm thấy người dùng sau khi đăng nhập")
                        onResult(Result.failure(Exception("Không tìm thấy người dùng")))
                    }
                } else {
                    Log.e(TAG, "Đăng nhập thất bại: ${task.exception?.message}", task.exception)
                    onResult(Result.failure(Exception("Sai email hoặc mật khẩu!")))
                }
            }
    }

    fun registerUser(email: String, password: String, displayName: String, avatarUrl: String?, onResult: (Result<String>) -> Unit) {
        Log.d(TAG, "Bắt đầu đăng ký với email: $email, displayName: $displayName")
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Đăng ký thành công với Firebase Auth")
                    val user = auth.currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { emailTask ->
                        if (emailTask.isSuccessful) {
                            Log.d(TAG, "Gửi email xác minh thành công")
                            val userId = user.uid
                            val userData = hashMapOf(
                                "uid" to userId,
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
                            firestore.collection("users").document(userId)
                                .set(userData)
                                .addOnSuccessListener {
                                    Log.d(TAG, "Tạo dữ liệu người dùng trên Firestore thành công")
                                    val preferences = hashMapOf(
                                        "categories" to emptyList<String>(),
                                        "isDarkTheme" to false,
                                        "updatedAt" to System.currentTimeMillis()
                                    )
                                    firestore.collection("users").document(userId)
                                        .collection("preferences").document("userPrefs")
                                        .set(preferences)
                                        .addOnSuccessListener {
                                            Log.d(TAG, "Tạo dữ liệu sở thích người dùng thành công")
                                            onResult(Result.success("Vui lòng kiểm tra email để xác nhận!"))
                                        }
                                        .addOnFailureListener {
                                            Log.e(TAG, "Lỗi khi tạo dữ liệu sở thích người dùng: ${it.message}", it)
                                            onResult(Result.failure(Exception("Lỗi khi tạo dữ liệu sở thích người dùng: ${it.message}")))
                                        }
                                }
                                .addOnFailureListener {
                                    Log.e(TAG, "Lỗi khi tạo dữ liệu người dùng: ${it.message}", it)
                                    onResult(Result.failure(Exception("Lỗi khi tạo dữ liệu người dùng: ${it.message}")))
                                }
                        } else {
                            Log.e(TAG, "Không thể gửi email xác nhận: ${emailTask.exception?.message}", emailTask.exception)
                            onResult(Result.failure(Exception("Không thể gửi email xác nhận: ${emailTask.exception?.message}")))
                        }
                    }
                } else {
                    Log.e(TAG, "Đăng ký thất bại: ${task.exception?.message}", task.exception)
                    onResult(Result.failure(Exception(task.exception?.message ?: "Đăng ký thất bại!")))
                }
            }
    }

    fun logoutUser(onResult: (Result<String>) -> Unit) {
        Log.d(TAG, "Bắt đầu đăng xuất")
        val user = auth.currentUser
        if (user != null) {
            Log.d(TAG, "Người dùng hiện tại: UID=${user.uid}, Email=${user.email}")
            val deviceId = getDeviceId()
            Log.d(TAG, "Device ID: $deviceId")
            val userRef = firestore.collection("users").document(user.uid)

            userRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d(TAG, "Lấy tài liệu người dùng thành công: ${document.data}")
                    val existingDevices = (document.get("deviceIds") as? List<*>)?.mapNotNull { it as? String }?.toMutableList() ?: mutableListOf()
                    existingDevices.remove(deviceId)
                    Log.d(TAG, "Xóa device ID: $deviceId, danh sách thiết bị còn lại: $existingDevices")

                    userRef.update(
                        "isLoggedIn", existingDevices.isNotEmpty(),
                        "deviceIds", existingDevices
                    ).addOnSuccessListener {
                        Log.d(TAG, "Cập nhật trạng thái đăng xuất trên Firestore thành công")
                        auth.signOut()
                        Log.d(TAG, "Đã gọi auth.signOut(), user hiện tại: ${auth.currentUser?.uid}")
                        saveLoginState(false)
                        Log.d(TAG, "Đã lưu trạng thái đăng nhập: isLoggedIn=false")
                        onResult(Result.success("Đăng xuất thành công"))
                    }.addOnFailureListener {
                        Log.e(TAG, "Lỗi khi cập nhật trạng thái đăng xuất trên Firestore: ${it.message}", it)
                        auth.signOut()
                        Log.d(TAG, "Đã gọi auth.signOut() (lỗi cập nhật Firestore), user hiện tại: ${auth.currentUser?.uid}")
                        saveLoginState(false)
                        Log.d(TAG, "Đã lưu trạng thái đăng nhập: isLoggedIn=false")
                        onResult(Result.success("Đăng xuất thành công"))
                    }
                } else {
                    Log.w(TAG, "Tài liệu người dùng không tồn tại cho UID: ${user.uid}")
                    auth.signOut()
                    Log.d(TAG, "Đã gọi auth.signOut() (không có tài liệu), user hiện tại: ${auth.currentUser?.uid}")
                    saveLoginState(false)
                    Log.d(TAG, "Đã lưu trạng thái đăng nhập: isLoggedIn=false")
                    onResult(Result.success("Đăng xuất thành công"))
                }
            }.addOnFailureListener {
                Log.e(TAG, "Lỗi khi lấy tài liệu người dùng: ${it.message}", it)
                auth.signOut()
                Log.d(TAG, "Đã gọi auth.signOut() (lỗi lấy tài liệu), user hiện tại: ${auth.currentUser?.uid}")
                saveLoginState(false)
                Log.d(TAG, "Đã lưu trạng thái đăng nhập: isLoggedIn=false")
                onResult(Result.success("Đăng xuất thành công"))
            }
        } else {
            Log.d(TAG, "Không có người dùng hiện tại, xóa trạng thái đăng nhập")
            saveLoginState(false)
            Log.d(TAG, "Đã lưu trạng thái đăng nhập: isLoggedIn=false")
            onResult(Result.success("Đăng xuất thành công"))
        }
    }

    fun checkLoginStatus(onResult: (Result<String>?) -> Unit) {
        Log.d(TAG, "Kiểm tra trạng thái đăng nhập")
        try {
            val isLoggedInPref = isUserLoggedIn()
            Log.d(TAG, "Trạng thái đăng nhập từ SharedPreferences: isLoggedIn=$isLoggedInPref")

            val user = auth.currentUser
            Log.d(TAG, "Người dùng hiện tại từ Firebase Auth: ${user?.uid}, Email=${user?.email}")

            // Tránh xóa trạng thái đăng nhập nếu vừa đăng nhập thành công
            val timeSinceLastLogin = System.currentTimeMillis() - lastLoginTime
            if (user == null && timeSinceLastLogin < 5000) { // Cho phép 5 giây để đồng bộ
                Log.d(TAG, "Vừa đăng nhập thành công ($timeSinceLastLogin ms trước), bỏ qua kiểm tra để tránh xóa trạng thái")
                onResult(Result.success("Đã đăng nhập (Offline)"))
                return
            }

            if (user == null) {
                Log.d(TAG, "Không có người dùng hiện tại, cập nhật SharedPreferences: isLoggedIn=false")
                saveLoginState(false)
                onResult(null)
                return
            }

            // Nếu SharedPreferences cho rằng người dùng đã đăng nhập, kiểm tra Firestore
            if (isLoggedInPref) {
                Log.d(TAG, "Người dùng đã đăng nhập (theo SharedPreferences), kiểm tra Firestore")
                val deviceId = getDeviceId()
                Log.d(TAG, "Device ID: $deviceId")
                firestore.collection("users").document(user.uid).get()
                    .addOnSuccessListener { document ->
                        try {
                            if (document.exists()) {
                                Log.d(TAG, "Lấy tài liệu người dùng: ${document.data}")
                                val storedDeviceIds = (document.get("deviceIds") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                                val isLoggedInFirestore = document.getBoolean("isLoggedIn") == true
                                Log.d(TAG, "Trạng thái đăng nhập từ Firestore: isLoggedIn=$isLoggedInFirestore, storedDeviceIds=$storedDeviceIds")

                                if (isLoggedInFirestore && storedDeviceIds.contains(deviceId)) {
                                    saveLoginState(true)
                                    Log.d(TAG, "Người dùng đã đăng nhập (Online), cập nhật SharedPreferences: isLoggedIn=true")
                                    onResult(Result.success("Đã đăng nhập (Online)"))
                                } else {
                                    Log.w(TAG, "Firestore cho biết người dùng không còn đăng nhập, cập nhật SharedPreferences: isLoggedIn=false")
                                    saveLoginState(false)
                                    onResult(null)
                                }
                            } else {
                                Log.w(TAG, "Tài liệu người dùng không tồn tại cho UID: ${user.uid}")
                                saveLoginState(false)
                                onResult(null)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Lỗi khi xử lý tài liệu người dùng: ${e.message}", e)
                            onResult(null)
                        }
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "Lỗi khi lấy tài liệu người dùng: ${it.message}", it)
                        onResult(null)
                    }
            } else {
                Log.d(TAG, "Người dùng không đăng nhập theo SharedPreferences")
                onResult(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi kiểm tra trạng thái đăng nhập: ${e.message}", e)
            onResult(null)
        }
    }

    fun checkEmailExists(email: String, callback: (Boolean) -> Unit) {
        Log.d(TAG, "Kiểm tra email tồn tại: $email")
        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { result ->
                val exists = !result.isEmpty
                Log.d(TAG, "Kết quả kiểm tra email: $email, exists=$exists")
                callback(exists)
            }
            .addOnFailureListener {
                Log.e(TAG, "Lỗi khi kiểm tra email tồn tại: ${it.message}", it)
                callback(false)
            }
    }

    fun resendVerificationEmail(callback: (Boolean) -> Unit) {
        Log.d(TAG, "Gửi lại email xác minh")
        val user = auth.currentUser
        if (user != null) {
            Log.d(TAG, "Người dùng hiện tại: UID=${user.uid}, Email=${user.email}")
            user.sendEmailVerification()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Gửi email xác minh thành công")
                    } else {
                        Log.e(TAG, "Lỗi khi gửi email xác minh: ${task.exception?.message}", task.exception)
                    }
                    callback(task.isSuccessful)
                }
        } else {
            Log.w(TAG, "Không có người dùng hiện tại để gửi email xác minh")
            callback(false)
        }
    }

    private fun saveLoginState(isLoggedIn: Boolean) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit {
            putBoolean("isLoggedIn", isLoggedIn)
        }
    }

    private fun isUserLoggedIn(): Boolean {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    fun getDeviceId(): String {
        val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        var deviceId = sharedPreferences.getString("device_id", null)

        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            sharedPreferences.edit { putString("device_id", deviceId) }
            Log.d(TAG, "Tạo device ID mới: $deviceId")
        }
        return deviceId
    }
}