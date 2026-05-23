package com.example.ekidi.ui.profil

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ekidi.R
import com.example.ekidi.databinding.ActivityProfilBinding
import com.example.ekidi.ui.auth.LoginActivity
import com.example.ekidi.ui.game.GameActivity
import com.example.ekidi.ui.literasi.LiterasiActivity
import com.example.ekidi.ui.misi.MisiActivity
import com.example.ekidi.utils.FirebaseHelper
import com.example.ekidi.utils.SessionManager
import kotlinx.coroutines.launch

class ProfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfilBinding
    private lateinit var sessionManager: SessionManager
    private var avatarDipilih = "🐶"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        sessionManager = SessionManager(this)

        setupUI()
        setupClickListeners()
        setupBottomNav()
    }

    private fun setupUI() {
        val nama = sessionManager.getUserName()
        val role = sessionManager.getUserRole()
        val level = sessionManager.getUserLevel()
        val poin = sessionManager.getUserPoints()

        binding.tvNamaProfil.text = nama
        binding.tvUsername.text = nama
        binding.tvLevelProfil.text = "⭐ Level $level"
        binding.tvPoinProfil.text = "$poin Poin"
        binding.tvLevelInfo.text = "Level $level"
        binding.tvTotalPoin.text = "$poin poin"

        val roleLabel = when (role) {
            "anak" -> "👦 Anak"
            "orang_tua" -> "👨 Orang Tua"
            "guru" -> "👩‍🏫 Guru"
            else -> "👦 Anak"
        }
        binding.tvPeran.text = roleLabel
        binding.tvRoleBadge.text = roleLabel
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        // Ganti Avatar
        binding.btnGantiAvatar.setOnClickListener {
            val isVisible = binding.cardPilihAvatar.visibility == View.VISIBLE
            binding.cardPilihAvatar.visibility = if (isVisible) View.GONE else View.VISIBLE
        }

        // Pilihan avatar
        val avatarMap = mapOf(
            binding.avatar1 to "🐶",
            binding.avatar2 to "🐱",
            binding.avatar3 to "🐻",
            binding.avatar4 to "🦊",
            binding.avatar5 to "🐼"
        )

        avatarMap.forEach { (view, emoji) ->
            view.setOnClickListener {
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
            AlertDialog.Builder(this)
                .setTitle("EKiDi")
                .setMessage("EKiDi — Edukasi Literasi Digital\nVersi 1.0.0\n\nAplikasi pembelajaran literasi digital untuk anak usia 5–8 tahun yang menyenangkan dan interaktif.")
                .setPositiveButton("Tutup", null)
                .show()
        }

        // Logout
        binding.menuLogout.setOnClickListener {
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
                R.id.nav_home -> { finish(); true }
                R.id.nav_literasi -> {
                    startActivity(Intent(this, LiterasiActivity::class.java))
                    true
                }
                R.id.nav_game -> {
                    startActivity(Intent(this, GameActivity::class.java))
                    true
                }
                R.id.nav_misi -> {
                    startActivity(Intent(this, MisiActivity::class.java))
                    true
                }
                R.id.nav_profil -> true
                else -> false
            }
        }
    }
}