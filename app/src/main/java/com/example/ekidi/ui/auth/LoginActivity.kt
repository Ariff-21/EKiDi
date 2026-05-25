package com.example.ekidi.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ekidi.databinding.ActivityLoginBinding
import kotlinx.coroutines.tasks.await
import com.example.ekidi.ui.home.HomeActivity
import com.example.ekidi.utils.FirebaseHelper
import com.example.ekidi.utils.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        sessionManager = SessionManager(this)

        // Cek jika sudah login
        if (FirebaseHelper.isLoggedIn()) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        binding.btnLogin.setOnClickListener { doLogin() }
        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun doLogin() {
        val email = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) { showError("Email tidak boleh kosong"); return }
        if (password.isEmpty()) { showError("Password tidak boleh kosong"); return }
        if (password.length < 6) { showError("Password minimal 6 karakter"); return }

        setLoading(isLoading = true)

        lifecycleScope.launch {
            val result = FirebaseHelper.login(email, password)

            if (result.isSuccess) {
                val currentUser = FirebaseHelper.auth.currentUser

                // ✅ Cek apakah email sudah diverifikasi
                if (currentUser != null && !currentUser.isEmailVerified) {
                    // Email belum diverifikasi
                    FirebaseHelper.logout()
                    runOnUiThread {
                        androidx.appcompat.app.AlertDialog.Builder(this@LoginActivity)
                            .setTitle("⚠️ Email Belum Diverifikasi")
                            .setMessage("Silakan cek email kamu dan klik link verifikasi terlebih dahulu.\n\nTidak menerima email?")
                            .setPositiveButton("Kirim Ulang") { _, _ ->
                                lifecycleScope.launch {
                                    // Login lagi sementara untuk kirim ulang
                                    FirebaseHelper.login(email, password)
                                    FirebaseHelper.auth.currentUser
                                        ?.sendEmailVerification()
                                        ?.await()
                                    FirebaseHelper.logout()
                                    showError("Email verifikasi sudah dikirim ulang!")
                                }
                            }
                            .setNegativeButton("Tutup", null)
                            .show()
                    }
                    setLoading(false)
                    return@launch
                }

                // ✅ Email sudah diverifikasi, lanjut ke Beranda
                val uid = result.getOrNull()!!
                val userData = FirebaseHelper.getUserData(uid)

                if (userData.isSuccess) {
                    val data = userData.getOrNull()!!
                    sessionManager.saveLoginSession(
                        userId = uid.hashCode(),
                        userName = data["nama"] as? String ?: "Pengguna",
                        userRole = data["role"] as? String ?: "anak",
                        authToken = uid,
                        level = (data["level"] as? Long)?.toInt() ?: 1,
                        points = (data["poin"] as? Long)?.toInt() ?: 0,
                    )
                }
                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                finish()

            } else {
                showError("Email atau password salah")
            }
            setLoading(isLoading = false)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.btnLogin.text = if (isLoading) "Memuat..." else "Masuk"
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }
}