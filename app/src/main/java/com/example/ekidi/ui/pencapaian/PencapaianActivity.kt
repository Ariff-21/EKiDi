package com.example.ekidi.ui.pencapaian

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ekidi.R
import com.example.ekidi.databinding.ActivityPencapaianBinding
import com.example.ekidi.ui.home.HomeActivity
import com.example.ekidi.ui.game.GameActivity
import com.example.ekidi.ui.literasi.LiterasiActivity
import com.example.ekidi.ui.misi.MisiActivity
import com.example.ekidi.ui.profil.ProfilActivity
import com.example.ekidi.utils.SessionManager

class PencapaianActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPencapaianBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPencapaianBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        sessionManager = SessionManager(this)

        setupUI()
        setupClickListeners()
        setupBottomNav()
    }

    private fun setupUI() {
        val level = sessionManager.getUserLevel()
        val poin = sessionManager.getUserPoints()
        val maxPoin = level * 500
        val progress = ((poin.toFloat() / maxPoin) * 100).toInt()

        binding.tvLevelSaatIni.text = "Level $level"
        binding.tvPoinSaatIni.text = "$poin poin"
        binding.tvPoinTarget.text = "Target: $maxPoin poin"
        binding.progressLevel.progress = progress

        // Stats (sementara 0, nanti dari API)
        binding.tvTotalBintang.text = "0"
        binding.tvTotalBadge.text = "1"
        binding.tvTotalPembelajaran.text = "0"
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_home
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
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
                R.id.nav_misi -> {
                    startActivity(Intent(this, MisiActivity::class.java))
                    finish()
                    true
                }
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