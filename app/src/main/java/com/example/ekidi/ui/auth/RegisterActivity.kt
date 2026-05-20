package com.example.ekidi.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ekidi.databinding.ActivityRegisterBinding
import com.example.ekidi.data.api.RetrofitClient
import com.example.ekidi.data.model.RegisterRequest
import com.example.ekidi.utils.SessionManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sessionManager: SessionManager
    private var selectedRole = "anak"  // default: anak

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        sessionManager = SessionManager(this)

        setupRoleSelector()

        binding.btnRegister.setOnClickListener { doRegister() }

        binding.tvGoToLogin.setOnClickListener {
            finish() // kembali ke LoginActivity
        }
    }

    private fun setupRoleSelector() {
        binding.btnRoleAnak.setOnClickListener { selectRole("anak") }
        binding.btnRoleOrtu.setOnClickListener { selectRole("orang_tua") }
        binding.btnRoleGuru.setOnClickListener { selectRole("guru") }
    }

    private fun selectRole(role: String) {
        selectedRole = role

        // Reset semua ke unselected
        binding.btnRoleAnak.setBackgroundResource(com.example.ekidi.R.drawable.bg_role_unselected)
        binding.btnRoleOrtu.setBackgroundResource(com.example.ekidi.R.drawable.bg_role_unselected)
        binding.btnRoleGuru.setBackgroundResource(com.example.ekidi.R.drawable.bg_role_unselected)

        binding.btnRoleAnak.setTextColor(getColor(com.example.ekidi.R.color.text_secondary))
        binding.btnRoleOrtu.setTextColor(getColor(com.example.ekidi.R.color.text_secondary))
        binding.btnRoleGuru.setTextColor(getColor(com.example.ekidi.R.color.text_secondary))

        // Set yang dipilih
        val selectedView = when (role) {
            "anak" -> binding.btnRoleAnak
            "orang_tua" -> binding.btnRoleOrtu
            else -> binding.btnRoleGuru
        }
        selectedView.setBackgroundResource(com.example.ekidi.R.drawable.bg_role_selected)
        selectedView.setTextColor(getColor(com.example.ekidi.R.color.purple_primary))

        // Tampilkan/sembunyikan field umur (hanya untuk anak)
        val umurVisibility = if (role == "anak") View.VISIBLE else View.GONE
        binding.labelUmur.visibility = umurVisibility
        binding.etUmur.visibility = umurVisibility
        binding.spaceUmur.visibility = umurVisibility
    }

    private fun doRegister() {
        val nama = binding.etNama.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        val umurStr = binding.etUmur.text.toString().trim()

        // Validasi
        if (nama.isEmpty()) { showError("Nama tidak boleh kosong"); return }
        if (username.isEmpty()) { showError("Username tidak boleh kosong"); return }
        if (password.isEmpty()) { showError("Password tidak boleh kosong"); return }
        if (password.length < 6) { showError("Password minimal 6 karakter"); return }
        if (password != confirmPassword) { showError("Konfirmasi password tidak cocok"); return }
        if (selectedRole == "anak" && umurStr.isEmpty()) { showError("Umur tidak boleh kosong"); return }

        val umur = if (selectedRole == "anak") umurStr.toIntOrNull() else null

        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.register(
                    RegisterRequest(
                        nama = nama,
                        username = username,
                        password = password,
                        role = selectedRole,
                        umur = umur
                    )
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
                    // Langsung ke Beranda setelah register
                    val intent = Intent(this@RegisterActivity, com.example.ekidi.ui.home.HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    showError(response.body()?.message ?: "Registrasi gagal, coba lagi")
                }
            } catch (e: Exception) {
                showError("Gagal terhubung ke server. Periksa koneksi internet.")
            } finally {
                setLoading(false)
            }
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