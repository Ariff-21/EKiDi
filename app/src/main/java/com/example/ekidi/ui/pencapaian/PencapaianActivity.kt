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
        
        // Hitung progress bar dalam level saat ini
        val poinAwal = com.example.ekidi.utils.FirebaseHelper.poinAwalLevel(level)
        val poinTarget = com.example.ekidi.utils.FirebaseHelper.poinUntukLevelBerikutnya(level)
        val poinDiLevel = poin - poinAwal
        val totalPoinDiLevel = poinTarget - poinAwal
        val progress = if (totalPoinDiLevel > 0) {
            ((poinDiLevel.toFloat() / totalPoinDiLevel) * 100).toInt()
        } else 100

        binding.tvLevelSaatIni.text = getString(R.string.level_format, level)
        binding.tvPoinSaatIni.text = getString(R.string.skor_format, poin)
        binding.tvPoinTarget.text = "Target: " + getString(R.string.skor_format, poinTarget)
        binding.progressLevel.progress = progress.coerceIn(0, 100)

        // Stats dari session
        binding.tvTotalBintang.text = sessionManager.getTotalBintang().toString()
        binding.tvTotalBadge.text = sessionManager.getTotalBadge().toString()
        binding.tvTotalPembelajaran.text = sessionManager.getTotalPembelajaran().toString()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnLeaderboard.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }
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