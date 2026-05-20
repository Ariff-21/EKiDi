package com.example.ekidi.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ekidi.R
import com.example.ekidi.databinding.ActivityHomeBinding
import com.example.ekidi.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.Intent
import com.example.ekidi.ui.game.GameActivity
import com.example.ekidi.ui.literasi.LiterasiActivity
import com.example.ekidi.ui.misi.MisiActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        sessionManager = SessionManager(this)

        setupUI()
        setupClickListeners()
        setupBottomNav()
    }

    private fun setupUI() {
        val nama = sessionManager.getUserName()
        val level = sessionManager.getUserLevel()
        val poin = sessionManager.getUserPoints()

        // Header
        binding.tvGreeting.text = "Halo, $nama!"
        binding.tvLevel.text = "⭐ Level $level"
        binding.tvLevelBadge.text = "Level $level"

        // Progres
        val maxPoin = level * 500
        val progress = ((poin.toFloat() / maxPoin) * 100).toInt()
        binding.tvPoin.text = "$poin poin dari $maxPoin poin"
        binding.progressBelajar.progress = progress

        val levelLabel = when {
            level <= 2 -> "Pemula"
            level <= 4 -> "Berkembang"
            level <= 6 -> "Mahir"
            else -> "Ahli"
        }
        binding.tvLevelLabel.text = levelLabel
    }

    private fun setupClickListeners() {
        // Menu grid
        binding.cardLiterasi.setOnClickListener {
             startActivity(Intent(this, LiterasiActivity::class.java))
        }
        binding.cardGame.setOnClickListener {
             startActivity(Intent(this, GameActivity::class.java))
        }
        binding.cardMisi.setOnClickListener {
             startActivity(Intent(this, MisiActivity::class.java))
        }
        binding.cardPencapaian.setOnClickListener {
            // startActivity(Intent(this, PencapaianActivity::class.java))
        }

        // Rekomendasi
        binding.btnMulaiRekomendasi.setOnClickListener {
            // startActivity(Intent(this, LiterasiActivity::class.java))
        }

        // Settings
        binding.ivSettings.setOnClickListener {
            // startActivity(Intent(this, ProfilActivity::class.java))
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_home

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
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
                R.id.nav_profil -> {
                    // startActivity(Intent(this, ProfilActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}