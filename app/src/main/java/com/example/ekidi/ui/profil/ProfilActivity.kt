package com.example.ekidi.ui.profil

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.ekidi.R
import com.example.ekidi.databinding.ActivityProfilBinding
import com.example.ekidi.ui.home.HomeActivity
import com.example.ekidi.ui.auth.LoginActivity
import com.example.ekidi.ui.game.GameActivity
import com.example.ekidi.ui.literasi.LiterasiActivity
import com.example.ekidi.ui.misi.MisiActivity
import com.example.ekidi.utils.FirebaseHelper
import com.example.ekidi.utils.SessionManager
import com.example.ekidi.utils.SoundManager
import kotlinx.coroutines.launch

class ProfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfilBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var soundManager: SoundManager
    private var avatarDipilih = "🐶"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        sessionManager = SessionManager(this)
        soundManager = SoundManager(this)

        setupUI()
        setupClickListeners()
        setupBottomNav()
    }

    private fun setupUI() {
        val nama = sessionManager.getUserName()
        val level = sessionManager.getUserLevel()
        val poin = sessionManager.getUserPoints()

        binding.tvAvatar.text = sessionManager.getUserAvatar()
        binding.tvNamaProfil.text = nama
        binding.tvUsername.text = nama
        binding.tvLevelProfil.text = getString(R.string.level_star_format, level)
        binding.tvPoinProfil.text = getString(R.string.skor_format, poin)
        binding.tvLevelInfo.text = getString(R.string.level_format, level)
        binding.tvTotalPoin.text = getString(R.string.skor_format, poin)
        
        binding.tvStreakProfil.text = getString(R.string.streak_format, sessionManager.getStreak())
        binding.tvBadgeProfil.text = sessionManager.getTotalBadge().toString()
        binding.tvMateriProfil.text = sessionManager.getTotalPembelajaran().toString()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            soundManager.playClick()
            finish()
        }

        binding.btnGantiAvatar.setOnClickListener {
            soundManager.playClick()
            binding.cardPilihAvatar.isVisible = !binding.cardPilihAvatar.isVisible
            if (binding.cardPilihAvatar.isVisible) {
                binding.root.post {
                    val scroll = findViewById<androidx.core.widget.NestedScrollView>(R.id.profilScroll)
                    scroll?.smoothScrollTo(0, binding.cardPilihAvatar.top)
                }
            }
        }

        // Pilihan avatar
        val avatarMap = mapOf(
            binding.avatar1 to "🐶",
            binding.avatar2 to "🐱",
            binding.avatar3 to "🐻",
            binding.avatar4 to "🦊",
            binding.avatar5 to "🐼",
            binding.avatar6 to "🐷",
            binding.avatar7 to "🐸",
            binding.avatar8 to "🦁",
        )

        avatarMap.forEach { (view, emoji) ->
            view.setOnClickListener {
                soundManager.playClick()
                avatarDipilih = emoji
                binding.tvAvatar.text = emoji
                avatarMap.keys.forEach { v ->
                    v.setBackgroundResource(R.drawable.bg_role_unselected)
                }
                view.setBackgroundResource(R.drawable.bg_role_selected)
                sessionManager.updateAvatar(emoji)

                // ✅ Update avatar ke Firestore juga
                val uid = FirebaseHelper.getCurrentUid()
                if (uid != null) {
                    lifecycleScope.launch {
                        FirebaseHelper.updateAvatar(uid, emoji)
                    }
                }

                binding.cardPilihAvatar.visibility = View.GONE
            }
        }

        // Tentang Aplikasi
        binding.menuTentang.setOnClickListener {
            soundManager.playClick()
            AlertDialog.Builder(this)
                .setTitle("EKiDi")
                .setMessage("EKiDi — Edukasi Literasi Digital\nVersi 1.0.0\n\nAplikasi pembelajaran literasi digital untuk anak usia 5–8 tahun yang menyenangkan dan interaktif.")
                .setPositiveButton("Tutup", null)
                .show()
        }

        // Logout
        binding.menuLogout.setOnClickListener {
            soundManager.playClick()
            AlertDialog.Builder(this)
                .setTitle("Keluar")
                .setMessage("Apakah kamu yakin ingin keluar?")
                .setPositiveButton("Keluar") { _, _ ->
                    // ✅ Logout dari Firebase + Session
                    FirebaseHelper.logout()
                    sessionManager.logout()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_profil
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    true
                }
                R.id.nav_literasi -> {
                    val intent = Intent(this, LiterasiActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_game -> {
                    val intent = Intent(this, GameActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_misi -> {
                    val intent = Intent(this, MisiActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profil -> true
                else -> false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::soundManager.isInitialized) {
            soundManager.release()
        }
    }
}
