package com.example.ekidi.ui.auth

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ekidi.databinding.ActivityRegisterBinding
import kotlinx.coroutines.tasks.await
import com.example.ekidi.ui.home.HomeActivity
import com.example.ekidi.utils.FirebaseHelper
import com.example.ekidi.utils.SessionManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sessionManager: SessionManager
    private var selectedRole = "anak"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        sessionManager = SessionManager(this)

        setupRoleSelector()
        binding.btnRegister.setOnClickListener { doRegister() }
        binding.tvGoToLogin.setOnClickListener { finish() }
    }

    private fun setupRoleSelector() {
        binding.btnRoleAnak.setOnClickListener { selectRole("anak") }
        binding.btnRoleOrtu.setOnClickListener { selectRole("orang_tua") }
        binding.btnRoleGuru.setOnClickListener { selectRole("guru") }
    }

    private fun selectRole(role: String) {
        selectedRole = role

        binding.btnRoleAnak.setBackgroundResource(com.example.ekidi.R.drawable.bg_role_unselected)
        binding.btnRoleOrtu.setBackgroundResource(com.example.ekidi.R.drawable.bg_role_unselected)
        binding.btnRoleGuru.setBackgroundResource(com.example.ekidi.R.drawable.bg_role_unselected)
        binding.btnRoleAnak.setTextColor(getColor(com.example.ekidi.R.color.text_secondary))
        binding.btnRoleOrtu.setTextColor(getColor(com.example.ekidi.R.color.text_secondary))
        binding.btnRoleGuru.setTextColor(getColor(com.example.ekidi.R.color.text_secondary))

        val selectedView = when (role) {
            "anak" -> binding.btnRoleAnak
            "orang_tua" -> binding.btnRoleOrtu
            else -> binding.btnRoleGuru
        }
        selectedView.setBackgroundResource(com.example.ekidi.R.drawable.bg_role_selected)
        selectedView.setTextColor(getColor(com.example.ekidi.R.color.purple_primary))

        val umurVisibility = if (role == "anak") View.VISIBLE else View.GONE
        binding.labelUmur.visibility = umurVisibility
        binding.etUmur.visibility = umurVisibility
        binding.spaceUmur.visibility = umurVisibility
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
        if (selectedRole == "anak" && umurStr.isEmpty()) { showError("Umur tidak boleh kosong"); return }

        val umur = if (selectedRole == "anak") umurStr.toIntOrNull() else null

        setLoading(isLoading = true)

        lifecycleScope.launch {
            val result = FirebaseHelper.register(
                email = email,
                password = password,
                nama = nama,
                role = selectedRole,
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
}