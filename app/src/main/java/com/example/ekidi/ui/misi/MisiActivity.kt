package com.example.ekidi.ui.misi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ekidi.R
import com.example.ekidi.databinding.ActivityMisiBinding
import com.example.ekidi.ui.home.HomeActivity
import com.example.ekidi.ui.game.GameActivity
import com.example.ekidi.ui.literasi.LiterasiActivity
import com.example.ekidi.ui.profil.ProfilActivity
import com.example.ekidi.utils.SessionManager

class MisiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMisiBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMisiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        sessionManager = SessionManager(this)

        setupUI()
        setupClickListeners()
        setupBottomNav()
    }

    private fun setupUI() {
        val streak = sessionManager.getStreak()
        binding.tvStreak.text = "🔥 $streak Hari"
        binding.tvStreakAngka.text = "$streak"
        binding.tvStreakDesc.text = if (streak == 0)
            "Selesaikan misi hari ini untuk mulai streak!"
        else
            "Keren! Pertahankan streakmu!"
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        // Misi Harian 1 — Baca Materi
        binding.cardMisiHarian1.setOnClickListener {
            startActivity(Intent(this, LiterasiActivity::class.java))
        }

        // Misi Harian 2 — Main Game
        binding.cardMisiHarian2.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }

        // Misi Mingguan
        binding.cardMisiMingguan1.setOnClickListener {
            // Info saja, progress otomatis dari aktivitas user
        }

        // Misi Spesial
        binding.cardMisiSpesial.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_misi
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_literasi -> {
                    startActivity(Intent(this, LiterasiActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_game -> {
                    startActivity(Intent(this, GameActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_misi -> true
                R.id.nav_profil -> {
                    startActivity(Intent(this, ProfilActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}