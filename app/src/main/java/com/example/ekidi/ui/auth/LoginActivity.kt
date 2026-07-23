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
import com.example.ekidi.utils.SoundManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        sessionManager = SessionManager(this)
        soundManager = SoundManager(this)

        // Cek jika sudah login
        if (FirebaseHelper.isLoggedIn()) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        binding.btnLogin.setOnClickListener {
            soundManager.playClick()
            doLogin()
        }
        binding.tvGoToRegister.setOnClickListener {
            soundManager.playClick()
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
                if ((currentUser != null) && !currentUser.isEmailVerified) {
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
                    setLoading(isLoading = false)
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
                        authToken = uid,
                        level = (data["level"] as? Long)?.toInt() ?: 1,
                        points = (data["poin"] as? Long)?.toInt() ?: 0,
                        avatar = data["avatar"] as? String ?: "🐶",
                        totalBintang = (data["totalBintang"] as? Long)?.toInt() ?: 0,
                        totalBadge = (data["totalBadge"] as? Long)?.toInt() ?: 1,
                        totalPembelajaran = (data["totalPembelajaran"] as? Long)?.toInt() ?: 0,
                        streak = (data["streak"] as? Long)?.toInt() ?: 0,
                    )

                    // ✅ Sinkronisasi status misi dari Cloud ke Session
                    val m1 = (data["misi_harian_1_status"] as? Long)?.toInt() ?: 0
                    val m2 = (data["misi_harian_2_status"] as? Long)?.toInt() ?: 0
                    val m3 = (data["misi_harian_3_status"] as? Long)?.toInt() ?: 0
                    val lastReset = data["last_reset_date"] as? String ?: ""

                    sessionManager.setMisiStatus(SessionManager.MISI_HARIAN_1_STATUS, m1)
                    sessionManager.setMisiStatus(SessionManager.MISI_HARIAN_2_STATUS, m2)
                    sessionManager.setMisiStatus(SessionManager.MISI_HARIAN_3_STATUS, m3)
                    sessionManager.saveLastResetDate(lastReset)

                    // ✅ Sinkronisasi Misi Mingguan & Spesial
                    val mwStatus = (data[SessionManager.MISI_MINGGUAN_STATUS] as? Long)?.toInt() ?: 0
                    val mwProgress = (data[SessionManager.MISI_MINGGUAN_PROGRESS] as? Long)?.toInt() ?: 0
                    val msStatus = (data[SessionManager.MISI_SPESIAL_STATUS] as? Long)?.toInt() ?: 0
                    val lastWeekly = (data["lastWeeklyReset"] as? Long) ?: System.currentTimeMillis()

                    sessionManager.setMisiStatus(SessionManager.MISI_MINGGUAN_STATUS, mwStatus)
                    sessionManager.setMisiMingguanProgress(mwProgress)
                    sessionManager.setMisiStatus(SessionManager.MISI_SPESIAL_STATUS, msStatus)
                    sessionManager.saveLastWeeklyReset(lastWeekly)

                    // ✅ Sinkronisasi status badge
                    sessionManager.setBadgeStatus(SessionManager.KEY_BADGE_1, data[SessionManager.KEY_BADGE_1] as? Boolean ?: true)
                    sessionManager.setBadgeStatus(SessionManager.KEY_BADGE_2, data[SessionManager.KEY_BADGE_2] as? Boolean ?: false)
                    sessionManager.setBadgeStatus(SessionManager.KEY_BADGE_3, data[SessionManager.KEY_BADGE_3] as? Boolean ?: false)
                    sessionManager.setBadgeStatus(SessionManager.KEY_BADGE_4, data[SessionManager.KEY_BADGE_4] as? Boolean ?: false)
                    sessionManager.setBadgeStatus(SessionManager.KEY_BADGE_5, data[SessionManager.KEY_BADGE_5] as? Boolean ?: false)
                    sessionManager.setBadgeStatus(SessionManager.KEY_BADGE_6, data[SessionManager.KEY_BADGE_6] as? Boolean ?: false)
                    sessionManager.setBadgeStatus(SessionManager.KEY_BADGE_7, data[SessionManager.KEY_BADGE_7] as? Boolean ?: false)
                    sessionManager.setBadgeStatus(SessionManager.KEY_BADGE_8, data[SessionManager.KEY_BADGE_8] as? Boolean ?: false)
                    sessionManager.setBadgeStatus(SessionManager.KEY_BADGE_9, data[SessionManager.KEY_BADGE_9] as? Boolean ?: false)
                    sessionManager.setBadgeStatus(SessionManager.KEY_BADGE_10, data[SessionManager.KEY_BADGE_10] as? Boolean ?: false)
                    sessionManager.setBadgeStatus(SessionManager.KEY_BADGE_11, data[SessionManager.KEY_BADGE_11] as? Boolean ?: false)
                    sessionManager.setBadgeStatus(SessionManager.KEY_BADGE_12, data[SessionManager.KEY_BADGE_12] as? Boolean ?: false)
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

    override fun onDestroy() {
        super.onDestroy()
        if (::soundManager.isInitialized) {
            soundManager.release()
        }
    }
}
