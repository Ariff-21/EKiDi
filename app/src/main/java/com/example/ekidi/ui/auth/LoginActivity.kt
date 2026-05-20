package com.example.ekidi.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ekidi.databinding.ActivityLoginBinding
import com.example.ekidi.data.api.RetrofitClient
import com.example.ekidi.data.model.LoginRequest
import com.example.ekidi.ui.home.HomeActivity
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

        binding.btnLogin.setOnClickListener { doLogin() }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun doLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (username.isEmpty()) {
            showError("Username tidak boleh kosong")
            return
        }
        if (password.isEmpty()) {
            showError("Password tidak boleh kosong")
            return
        }

        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.login(
                    LoginRequest(username, password)
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val userData = response.body()!!.data!!
                    sessionManager.saveLoginSession(
                        userId = userData.id,
                        userName = userData.nama,
                        userRole = userData.role,
                        authToken = userData.token,
                        level = userData.level,
                        points = userData.poin
                    )
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    finish()
                } else {
                    showError(response.body()?.message ?: "Login gagal, coba lagi")
                }
            } catch (e: Exception) {
                showError("Gagal terhubung ke server. Periksa koneksi internet.")
            } finally {
                setLoading(false)
            }
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