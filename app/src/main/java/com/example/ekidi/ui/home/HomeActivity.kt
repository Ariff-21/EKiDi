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
import com.example.ekidi.utils.SoundManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var soundManager: SoundManager
    private var rekomendasiSaatIni: DecisionTreeHelper.RekomendasiBelajar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        sessionManager = SessionManager(this)
        soundManager = SoundManager(this)
        checkDailyReset()

        setupUI()
        setupClickListeners()
        setupBottomNav()
        listenProgressRealtime()
        loadRekomendasi()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::soundManager.isInitialized) {
            soundManager.release()
        }
    }

    private fun checkDailyReset() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.forLanguageTag("id-ID"))
        val today = sdf.format(Date())
        val lastReset = sessionManager.getLastResetDate()

        if (today != lastReset) {
            // ✅ Update Streak jika login hari berturut-turut
            // (Logika sederhana: jika selisih 1 hari, increment)
            // ... (implementasi streak logic jika diperlukan)
            // Untuk sementara kita cek Badge 8 jika streak >= 7
            if (sessionManager.getStreak() >= 7 && !sessionManager.getBadgeStatus(SessionManager.KEY_BADGE_8)) {
                sessionManager.setBadgeStatus(SessionManager.KEY_BADGE_8, true)
                val uid = FirebaseHelper.getCurrentUid()
                if (uid != null) {
                    lifecycleScope.launch {
                        FirebaseHelper.updateBadgeStatus(uid, SessionManager.KEY_BADGE_8, true, sessionManager.getTotalBadge())
                    }
                }
            }

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
        // ✅ Cek Badge 6 (Bintang EKiDi) - Capai Level 5
        if (level >= 5 && !sessionManager.getBadgeStatus(SessionManager.KEY_BADGE_6)) {
            sessionManager.setBadgeStatus(SessionManager.KEY_BADGE_6, true)
            val uid = FirebaseHelper.getCurrentUid()
            if (uid != null) {
                lifecycleScope.launch {
                    FirebaseHelper.updateBadgeStatus(uid, SessionManager.KEY_BADGE_6, true, sessionManager.getTotalBadge())
                }
            }
        }

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

        // ✅ Cek Badge 10 (Kolektor Bintang) - 2500 poin
        if (poin >= 2500 && !sessionManager.getBadgeStatus(SessionManager.KEY_BADGE_10)) {
            sessionManager.setBadgeStatus(SessionManager.KEY_BADGE_10, true)
            val uid = FirebaseHelper.getCurrentUid()
            if (uid != null) {
                lifecycleScope.launch {
                    FirebaseHelper.updateBadgeStatus(uid, SessionManager.KEY_BADGE_10, true, sessionManager.getTotalBadge())
                }
            }
        }

        // ✅ Cek Badge 12 (Legenda EKiDi) - Level 10
        if (level >= 10 && !sessionManager.getBadgeStatus(SessionManager.KEY_BADGE_12)) {
            sessionManager.setBadgeStatus(SessionManager.KEY_BADGE_12, true)
            val uid = FirebaseHelper.getCurrentUid()
            if (uid != null) {
                lifecycleScope.launch {
                    FirebaseHelper.updateBadgeStatus(uid, SessionManager.KEY_BADGE_12, true, sessionManager.getTotalBadge())
                }
            }
        }

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
            soundManager.playClick()
            startActivity(Intent(this, LiterasiActivity::class.java))
        }
        binding.cardGame.setOnClickListener {
            soundManager.playClick()
            startActivity(Intent(this, GameActivity::class.java))
        }
        binding.cardMisi.setOnClickListener {
            soundManager.playClick()
            startActivity(Intent(this, MisiActivity::class.java))
        }
        binding.cardPencapaian.setOnClickListener {
            soundManager.playClick()
            startActivity(Intent(this, PencapaianActivity::class.java))
        }
        binding.ivSettings.setOnClickListener {
            soundManager.playClick()
            startActivity(Intent(this, ProfilActivity::class.java))
        }

        // ✅ Tombol rekomendasi → navigasi sesuai hasil Decision Tree
        binding.btnMulaiRekomendasi.setOnClickListener {
            soundManager.playClick()
            navigasiRekomendasi()
        }
        // Bisa akses leaderboard dari stat badge di beranda
        binding.tvStatBadge.setOnClickListener {
            soundManager.playClick()
            startActivity(Intent(this, com.example.ekidi.ui.pencapaian.LeaderboardActivity::class.java))
        }
    }

    // ✅ Navigasi sesuai aksi rekomendasi dari Decision Tree
    private fun navigasiRekomendasi() {
        val rek = rekomendasiSaatIni ?: run {
            startActivity(Intent(this, LiterasiActivity::class.java))
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
            }
            DecisionTreeHelper.AksiRekomendasi.MAIN_GAME -> {
                startActivity(Intent(this, GameActivity::class.java))
            }
            DecisionTreeHelper.AksiRekomendasi.TOPIK_SELESAI -> {
                startActivity(Intent(this, LiterasiActivity::class.java))
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
                    startActivity(Intent(this, ProfilActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
