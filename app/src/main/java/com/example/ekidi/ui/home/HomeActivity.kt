package com.example.ekidi.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ekidi.R
import com.example.ekidi.databinding.ActivityHomeBinding
import com.example.ekidi.ui.game.GameActivity
import com.example.ekidi.ui.literasi.LiterasiActivity
import com.example.ekidi.ui.literasi.MateriActivity
import com.example.ekidi.ui.misi.MisiActivity
import com.example.ekidi.ui.pencapaian.PencapaianActivity
import com.example.ekidi.ui.profil.ProfilActivity
import com.example.ekidi.utils.DecisionTreeHelper
import com.example.ekidi.utils.FirebaseHelper
import com.example.ekidi.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var sessionManager: SessionManager
    private var rekomendasiSaatIni: DecisionTreeHelper.RekomendasiBelajar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        sessionManager = SessionManager(this)
        checkDailyReset()

        setupUI()
        setupClickListeners()
        setupBottomNav()
        listenProgressRealtime()
        loadRekomendasi()
    }

    private fun checkDailyReset() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.forLanguageTag("id-ID"))
        val today = sdf.format(Date())
        val lastReset = sessionManager.getLastResetDate()

        if (today != lastReset) {
            val uid = FirebaseHelper.getCurrentUid()
            if (uid != null) {
                lifecycleScope.launch {
                    FirebaseHelper.resetMisiHarian(uid, today)
                    sessionManager.setMisiStatus(SessionManager.MISI_HARIAN_1_STATUS, 0)
                    sessionManager.setMisiStatus(SessionManager.MISI_HARIAN_2_STATUS, 0)
                    sessionManager.setMisiStatus(SessionManager.MISI_HARIAN_3_STATUS, 0)
                    sessionManager.saveLastResetDate(today)
                }
            } else {
                sessionManager.setMisiStatus(SessionManager.MISI_HARIAN_1_STATUS, 0)
                sessionManager.setMisiStatus(SessionManager.MISI_HARIAN_2_STATUS, 0)
                sessionManager.setMisiStatus(SessionManager.MISI_HARIAN_3_STATUS, 0)
                sessionManager.saveLastResetDate(today)
            }
        }
    }

    private fun setupUI() {
        val nama = sessionManager.getUserName()
        val level = sessionManager.getUserLevel()
        val poin = sessionManager.getUserPoints()
        updateTampilan(poin, level, nama)
    }

    private fun updateTampilan(
        poin: Int,
        level: Int,
        nama: String = sessionManager.getUserName()
    ) {
        binding.tvGreeting.text = getString(R.string.greeting, nama)
        binding.tvLevel.text = getString(R.string.level_star_format, level)
        binding.tvLevelBadge.text = getString(R.string.level_format, level)

        val poinAwal = FirebaseHelper.poinAwalLevel(level)
        val poinTarget = FirebaseHelper.poinUntukLevelBerikutnya(level)
        val poinDiLevel = poin - poinAwal
        val totalPoinDiLevel = poinTarget - poinAwal
        val progress = if (totalPoinDiLevel > 0) {
            ((poinDiLevel.toFloat() / totalPoinDiLevel) * 100).toInt()
        } else 100

        binding.tvPoin.text = getString(R.string.poin_format, poin, poinTarget)
        binding.progressBelajar.progress = progress.coerceIn(0, 100)

        binding.tvLevelLabel.text = when {
            level <= 2 -> "Pemula"
            level <= 4 -> "Berkembang"
            level <= 6 -> "Mahir"
            level <= 8 -> "Ahli"
            else -> "Master"
        }
    }

    // ✅ Load rekomendasi dari Firebase (hasil Decision Tree)
    private fun loadRekomendasi() {
        val uid = FirebaseHelper.getCurrentUid() ?: return

        lifecycleScope.launch {
            val rekomendasi = FirebaseHelper.getRekomendasiTerakhir(uid)

            runOnUiThread {
                if (rekomendasi != null) {
                    rekomendasiSaatIni = rekomendasi
                    tampilkanRekomendasi(rekomendasi)
                } else {
                    // Belum ada data → tampilkan rekomendasi awal
                    val rekAwal = DecisionTreeHelper.rekomendasiAwal(listOf(1, 2, 3, 4))
                    rekomendasiSaatIni = rekAwal
                    tampilkanRekomendasi(rekAwal)
                }
            }
        }
    }

    private fun tampilkanRekomendasi(rek: DecisionTreeHelper.RekomendasiBelajar) {
        // Update card rekomendasi di beranda
        binding.tvRekomendasiJudul.text = rek.judulRekomendasi
        binding.tvRekomendasiDesc.text = rek.deskripsi
        binding.btnMulaiRekomendasi.text = "Mulai Sekarang ${rek.emoji}"
    }

    private fun listenProgressRealtime() {
        val uid = FirebaseHelper.getCurrentUid() ?: return
        FirebaseHelper.listenUserData(uid) { poin, level ->
            sessionManager.updatePoints(poin)
            sessionManager.updateLevel(level)
            runOnUiThread { updateTampilan(poin, level) }
        }
    }

    private fun setupClickListeners() {
        binding.cardLiterasi.setOnClickListener {
            startActivity(Intent(this, LiterasiActivity::class.java))
            finish()
        }
        binding.cardGame.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
            finish()
        }
        binding.cardMisi.setOnClickListener {
            startActivity(Intent(this, MisiActivity::class.java))
            finish()
        }
        binding.cardPencapaian.setOnClickListener {
            startActivity(Intent(this, PencapaianActivity::class.java))
            finish()
        }
        binding.ivSettings.setOnClickListener {
            startActivity(Intent(this, ProfilActivity::class.java))
            finish()
        }

        // ✅ Tombol rekomendasi → navigasi sesuai hasil Decision Tree
        binding.btnMulaiRekomendasi.setOnClickListener {
            navigasiRekomendasi()
        }
        // Bisa akses leaderboard dari stat badge di beranda
        binding.tvStatBadge.setOnClickListener {
            startActivity(Intent(this, com.example.ekidi.ui.pencapaian.LeaderboardActivity::class.java))
        }
    }

    // ✅ Navigasi sesuai aksi rekomendasi dari Decision Tree
    private fun navigasiRekomendasi() {
        val rek = rekomendasiSaatIni ?: run {
            startActivity(Intent(this, LiterasiActivity::class.java))
            finish()
            return
        }

        when (rek.aksi) {
            DecisionTreeHelper.AksiRekomendasi.NAIK_LEVEL,
            DecisionTreeHelper.AksiRekomendasi.ULANGI_LEVEL,
            DecisionTreeHelper.AksiRekomendasi.BACA_MATERI -> {
                // Buka halaman materi topik yang direkomendasikan
                val intent = Intent(this, MateriActivity::class.java)
                intent.putExtra("TOPIK_ID", rek.topikId)
                startActivity(intent)
                // Kita tidak finish() di sini karena MateriActivity adalah sub-page
            }
            DecisionTreeHelper.AksiRekomendasi.COBA_TOPIK_LAIN -> {
                startActivity(Intent(this, LiterasiActivity::class.java))
                finish()
            }
            DecisionTreeHelper.AksiRekomendasi.MAIN_GAME -> {
                startActivity(Intent(this, GameActivity::class.java))
                finish()
            }
            DecisionTreeHelper.AksiRekomendasi.TOPIK_SELESAI -> {
                startActivity(Intent(this, LiterasiActivity::class.java))
                finish()
            }
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