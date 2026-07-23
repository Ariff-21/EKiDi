package com.example.ekidi.ui.auth

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ekidi.databinding.ActivityRegisterBinding
import kotlinx.coroutines.tasks.await
import com.example.ekidi.utils.FirebaseHelper
import com.example.ekidi.utils.SessionManager
import com.example.ekidi.utils.SoundManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        sessionManager = SessionManager(this)
        soundManager = SoundManager(this)

        binding.btnRegister.setOnClickListener {
            soundManager.playClick()
            doRegister()
        }
        binding.tvGoToLogin.setOnClickListener {
            soundManager.playClick()
            finish()
        }
    }

    private fun doRegister() {
        val nama = binding.etNama.text.toString().trim()
        val email = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        val umurStr = binding.etUmur.text.toString().trim()

        if (nama.isEmpty()) { showError("Nama tidak boleh kosong"); return }
        if (email.isEmpty()) { showError("Email tidak boleh kosong"); return }
        if (!email.contains("@")) { showError("Format email tidak valid"); return }
        if (password.isEmpty()) { showError("Password tidak boleh kosong"); return }
        if (password.length < 6) { showError("Password minimal 6 karakter"); return }
        if (password != confirmPassword) { showError("Konfirmasi password tidak cocok"); return }
        if (umurStr.isEmpty()) { showError("Umur tidak boleh kosong"); return }

        val umur = umurStr.toIntOrNull()

        setLoading(isLoading = true)

        lifecycleScope.launch {
            val result = FirebaseHelper.register(
                email = email,
                password = password,
                nama = nama,
                umur = umur,
            )

            if (result.isSuccess) {
                // ✅ Kirim email verifikasi
                FirebaseHelper.auth.currentUser?.sendEmailVerification()?.await()

                // ✅ Logout dulu, paksa user verifikasi email sebelum login
                FirebaseHelper.logout()

                // Tampilkan dialog info
                runOnUiThread {
                    androidx.appcompat.app.AlertDialog.Builder(this@RegisterActivity)
                        .setTitle("✅ Daftar Berhasil!")
                        .setMessage("Link verifikasi sudah dikirim ke:\n\n$email\n\nSilakan cek email kamu dan klik link verifikasi, lalu login.")
                        .setPositiveButton("OK, Mengerti") { _, _ ->
                            finish() // kembali ke Login
                        }
                        .setCancelable(false)
                        .show()
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Registrasi gagal"
                showError(
                    when {
                        errorMsg.contains("email address is already in use") ->
                            "Email sudah digunakan, coba email lain"
                        errorMsg.contains("badly formatted") ->
                            "Format email tidak valid"
                        else -> "Registrasi gagal, coba lagi"
                    }
                )
            }
            setLoading(isLoading = false)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnRegister.isEnabled = !isLoading
        binding.btnRegister.text = if (isLoading) "Memuat..." else "Daftar Sekarang"
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::soundManager.isInitialized) {
            soundManager.release()
        }
    }
}
