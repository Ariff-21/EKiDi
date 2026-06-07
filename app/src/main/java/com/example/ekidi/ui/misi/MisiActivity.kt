package com.example.ekidi.ui.misi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ekidi.R
import com.example.ekidi.databinding.ActivityMisiBinding
import com.example.ekidi.ui.home.HomeActivity
import com.example.ekidi.ui.game.GameActivity
import com.example.ekidi.ui.literasi.LiterasiActivity
import com.example.ekidi.ui.profil.ProfilActivity
import com.example.ekidi.utils.FirebaseHelper
import com.example.ekidi.utils.SessionManager
import com.example.ekidi.utils.SoundManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MisiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMisiBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMisiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        sessionManager = SessionManager(this)
        soundManager = SoundManager(this)

        checkDailyReset()
        setupUI()
        setupClickListeners()
        setupBottomNav()
    }

    private fun checkDailyReset() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.forLanguageTag("id-ID"))
        val today = sdf.format(Date())
        val lastReset = sessionManager.getLastResetDate()

        if (today != lastReset) {
            val uid = FirebaseHelper.getCurrentUid()
            if (uid != null) {
                lifecycleScope.launch {
                    // Reset di Cloud
                    FirebaseHelper.resetMisiHarian(uid, today)
                    
                    // Reset di Lokal
                    sessionManager.setMisiStatus(SessionManager.MISI_HARIAN_1_STATUS, 0)
                    sessionManager.setMisiStatus(SessionManager.MISI_HARIAN_2_STATUS, 0)
                    sessionManager.setMisiStatus(SessionManager.MISI_HARIAN_3_STATUS, 0)
                    sessionManager.saveLastResetDate(today)
                    
                    runOnUiThread { updateMisiViews() }
                }
            } else {
                // User belum login (guest?), reset lokal saja
                sessionManager.setMisiStatus(SessionManager.MISI_HARIAN_1_STATUS, 0)
                sessionManager.setMisiStatus(SessionManager.MISI_HARIAN_2_STATUS, 0)
                sessionManager.setMisiStatus(SessionManager.MISI_HARIAN_3_STATUS, 0)
                sessionManager.saveLastResetDate(today)
                updateMisiViews()
            }
        }
    }

    private fun setupUI() {
        // Tampilkan tanggal hari ini
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.forLanguageTag("id-ID"))
        val tanggalSekarang = sdf.format(Date())
        binding.tvMisiHariIni.text = "📅 $tanggalSekarang"

        val streak = sessionManager.getStreak()
        binding.tvStreak.text = "🔥 $streak Hari"
        binding.tvStreakAngka.text = streak.toString()
        binding.tvStreakDesc.text = if (streak == 0)
            "Mulai petualanganmu hari ini!"
        else
            "Hebat! Pertahankan semangatmu!"

        updateMisiViews()
    }

    private fun updateMisiViews() {
        // Misi Harian 1
        val statusH1 = sessionManager.getMisiStatus(SessionManager.MISI_HARIAN_1_STATUS)
        updateSingleMisiUI(statusH1, binding.tvStatusMisiHarian1, binding.btnClaimHarian1)

        // Misi Harian 2
        val statusH2 = sessionManager.getMisiStatus(SessionManager.MISI_HARIAN_2_STATUS)
        updateSingleMisiUI(statusH2, binding.tvStatusMisiHarian2, binding.btnClaimHarian2)

        // Misi Harian 3
        val statusH3 = sessionManager.getMisiStatus(SessionManager.MISI_HARIAN_3_STATUS)
        updateSingleMisiUI(statusH3, binding.tvStatusMisiHarian3, binding.btnClaimHarian3)

        // Misi Mingguan
        val progressMingguan = sessionManager.getMisiMingguanProgress()
        binding.progressMisiMingguan.progress = progressMingguan
        binding.tvProgressMisiMingguan.text = "$progressMingguan/5 misi selesai"
        
        val statusM = sessionManager.getMisiStatus(SessionManager.MISI_MINGGUAN_STATUS)
        if (statusM == 1) {
            binding.btnClaimMingguan.visibility = View.VISIBLE
        } else if (statusM == 2) {
            binding.btnClaimMingguan.visibility = View.GONE
            // Optional: show "Selesai" label
        }

        // Misi Spesial
        val statusS = sessionManager.getMisiStatus(SessionManager.MISI_SPESIAL_STATUS)
        if (statusS == 1) {
            binding.btnClaimSpesial.visibility = View.VISIBLE
            binding.tvIconSpesial.visibility = View.GONE
        } else if (statusS == 2) {
            binding.btnClaimSpesial.visibility = View.GONE
            binding.tvIconSpesial.text = "✅"
            binding.tvIconSpesial.visibility = View.VISIBLE
        }
    }

    private fun updateSingleMisiUI(status: Int, statusTv: View, claimBtn: View) {
        when (status) {
            0 -> { // Belum
                statusTv.visibility = View.VISIBLE
                (statusTv as? android.widget.TextView)?.text = "Mulai"
                claimBtn.visibility = View.GONE
            }
            1 -> { // Selesai, belum klaim
                statusTv.visibility = View.GONE
                claimBtn.visibility = View.VISIBLE
            }
            2 -> { // Sudah klaim
                statusTv.visibility = View.VISIBLE
                if (statusTv is android.widget.TextView) {
                    statusTv.text = "Selesai ✅"
                    statusTv.setBackgroundResource(R.drawable.bg_level_badge)
                    statusTv.setTextColor(getColor(R.color.sky_blue))
                }
                claimBtn.visibility = View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.cardMisiHarian1.setOnClickListener {
            if (sessionManager.getMisiStatus(SessionManager.MISI_HARIAN_1_STATUS) == 0) {
                startActivity(Intent(this, LiterasiActivity::class.java))
            }
        }

        binding.cardMisiHarian2.setOnClickListener {
            if (sessionManager.getMisiStatus(SessionManager.MISI_HARIAN_2_STATUS) == 0) {
                startActivity(Intent(this, GameActivity::class.java))
            }
        }

        binding.cardMisiHarian3.setOnClickListener {
            if (sessionManager.getMisiStatus(SessionManager.MISI_HARIAN_3_STATUS) == 0) {
                startActivity(Intent(this, LiterasiActivity::class.java))
            }
        }

        binding.btnClaimHarian1.setOnClickListener { claimReward(SessionManager.MISI_HARIAN_1_STATUS, 20) }
        binding.btnClaimHarian2.setOnClickListener { claimReward(SessionManager.MISI_HARIAN_2_STATUS, 15) }
        binding.btnClaimHarian3.setOnClickListener { claimReward(SessionManager.MISI_HARIAN_3_STATUS, 25) }
        binding.btnClaimMingguan.setOnClickListener { claimReward(SessionManager.MISI_MINGGUAN_STATUS, 100) }
        binding.btnClaimSpesial.setOnClickListener { claimReward(SessionManager.MISI_SPESIAL_STATUS, 200) }
    }

    private fun claimReward(misiKey: String, points: Int) {
        soundManager.playCorrect()
        sessionManager.setMisiStatus(misiKey, 2)
        
        val uid = FirebaseHelper.getCurrentUid()
        if (uid != null) {
            lifecycleScope.launch {
                // ✅ Simpan status klaim ke Firebase agar permanen
                FirebaseHelper.updateMisiStatus(uid, misiKey, 2)

                FirebaseHelper.updatePoin(uid, points)
                val newPoints = sessionManager.getUserPoints() + points
                sessionManager.updatePoints(newPoints)
                sessionManager.updateLevel(FirebaseHelper.hitungLevel(newPoints))
                
                runOnUiThread {
                    Toast.makeText(this@MisiActivity, "Selamat! Kamu dapat $points poin! ⭐", Toast.LENGTH_SHORT).show()
                    updateMisiViews()
                }
            }
        }
        
        // Update mingguan progress if harian claimed
        if ((misiKey == SessionManager.MISI_HARIAN_1_STATUS) || 
            (misiKey == SessionManager.MISI_HARIAN_2_STATUS) ||
            (misiKey == SessionManager.MISI_HARIAN_3_STATUS)) {
            val currentProgress = sessionManager.getMisiMingguanProgress()
            if (currentProgress < 5) {
                sessionManager.updateMisiMingguanProgress(1)
                if (sessionManager.getMisiMingguanProgress() >= 5) {
                    sessionManager.setMisiStatus(SessionManager.MISI_MINGGUAN_STATUS, 1)
                }
            }
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

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
