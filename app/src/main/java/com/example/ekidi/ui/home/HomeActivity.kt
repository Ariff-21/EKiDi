package com.example.ekidi.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ekidi.R
import com.example.ekidi.databinding.ActivityHomeBinding
import com.example.ekidi.ui.game.GameActivity
import com.example.ekidi.ui.literasi.LiterasiActivity
import com.example.ekidi.ui.misi.MisiActivity
import com.example.ekidi.ui.pencapaian.PencapaianActivity
import com.example.ekidi.ui.profil.ProfilActivity
import com.example.ekidi.utils.FirebaseHelper
import com.example.ekidi.utils.SessionManager

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
        listenProgressRealtime()
    }

    override fun onResume() {
        super.onResume()
        // Navbar otomatis sesuai karena item dipilih
    }

    private fun setupUI() {
        val nama = sessionManager.getUserName()
        val level = sessionManager.getUserLevel()
        val poin = sessionManager.getUserPoints()

        updateTampilan(poin, level, nama)
    }

    private fun updateTampilan(poin: Int, level: Int, nama: String = sessionManager.getUserName()) {
        binding.tvGreeting.text = getString(R.string.greeting, nama)
        binding.tvLevel.text = "⭐ " + getString(R.string.level_format, level)
        binding.tvLevelBadge.text = getString(R.string.level_format, level)

        // Hitung progress bar dalam level saat ini
        val poinAwal = FirebaseHelper.poinAwalLevel(level)
        val poinTarget = FirebaseHelper.poinUntukLevelBerikutnya(level)
        val poinDiLevel = poin - poinAwal
        val totalPoinDiLevel = poinTarget - poinAwal
        val progress = if (totalPoinDiLevel > 0) {
            ((poinDiLevel.toFloat() / totalPoinDiLevel) * 100).toInt()
        } else 100

        binding.tvPoin.text = getString(R.string.poin_format, poin, poinTarget)
        binding.progressBelajar.progress = progress.coerceIn(0, 100)

        val levelLabel = when {
            level <= 2 -> "Pemula"
            level <= 4 -> "Berkembang"
            level <= 6 -> "Mahir"
            level <= 8 -> "Ahli"
            else -> "Master"
        }
        binding.tvLevelLabel.text = levelLabel
    }

    // ✅ Listener realtime — otomatis update saat poin bertambah
    private fun listenProgressRealtime() {
        val uid = FirebaseHelper.getCurrentUid() ?: return
        FirebaseHelper.listenUserData(uid) { poin, level ->
            // Update session lokal
            sessionManager.updatePoints(poin)
            sessionManager.updateLevel(level)
            // Update tampilan
            runOnUiThread {
                updateTampilan(poin, level)
            }
        }
    }

    private fun setupClickListeners() {
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
            startActivity(Intent(this, PencapaianActivity::class.java))
        }
        binding.btnMulaiRekomendasi.setOnClickListener {
            startActivity(Intent(this, LiterasiActivity::class.java))
        }
        binding.ivSettings.setOnClickListener {
            startActivity(Intent(this, ProfilActivity::class.java))
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_home
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
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