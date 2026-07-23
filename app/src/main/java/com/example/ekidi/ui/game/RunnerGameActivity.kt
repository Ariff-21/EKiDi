package com.example.ekidi.ui.game

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ekidi.R
import com.example.ekidi.databinding.ActivityRunnerGameBinding
import com.example.ekidi.utils.FirebaseHelper
import com.example.ekidi.utils.SessionManager
import com.example.ekidi.utils.SoundManager
import kotlinx.coroutines.launch

class RunnerGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRunnerGameBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var soundManager: SoundManager

    private var level = "MUDAH"
    private var nyawa = 3
    private var skor = 0
    private var rintanganIndex = 0
    private var countDownTimer: CountDownTimer? = null
    private var waktuJawab = 15

    // Data soal per level
    data class Rintangan(
        val emoji: String,
        val pertanyaan: String,
        val pilihan: List<String>,
        val jawaban: String,
        val poin: Int
    )

    private val rintanganMudah = listOf(
        Rintangan("💻", "Apakah komputer adalah perangkat digital?",
            listOf("✅ Ya, Benar", "❌ Tidak"), "✅ Ya, Benar", 10),
        Rintangan("📱", "Apakah HP bisa dibawa ke mana-mana?",
            listOf("✅ Ya, Benar", "❌ Tidak"), "✅ Ya, Benar", 10),
        Rintangan("🖨️", "Apakah printer digunakan untuk mengetik?",
            listOf("✅ Ya, Benar", "❌ Tidak, Salah"), "❌ Tidak, Salah", 10),
        Rintangan("⌨️", "Keyboard digunakan untuk mengetik. Benar?",
            listOf("✅ Ya, Benar", "❌ Tidak"), "✅ Ya, Benar", 10),
        Rintangan("🔒", "Bolehkah kita kasih password ke semua orang?",
            listOf("✅ Boleh", "❌ Tidak Boleh"), "❌ Tidak Boleh", 10)
    )

    private val rintanganSedang = listOf(
        Rintangan("💻", "Apa fungsi utama komputer?",
            listOf("Memasak", "Belajar & Bekerja", "Olahraga", "Tidur"),
            "Belajar & Bekerja", 15),
        Rintangan("📱", "Apa yang harus dilakukan sebelum pakai internet?",
            listOf("Langsung pakai", "Minta izin orang tua", "Sembunyikan dari ortu", "Tidak perlu izin"),
            "Minta izin orang tua", 15),
        Rintangan("🔐", "Siapa yang boleh tahu password kita?",
            listOf("Semua teman", "Orang asing", "Hanya Ayah/Bunda", "Siapa saja"),
            "Hanya Ayah/Bunda", 15),
        Rintangan("😢", "Apa itu cyberbullying?",
            listOf("Bermain game", "Mengejek di internet", "Belajar online", "Mengirim email"),
            "Mengejek di internet", 15),
        Rintangan("🌐", "Berapa lama waktu ideal pakai gadget per hari?",
            listOf("Seharian", "1-2 jam", "Semalam", "Tidak terbatas"),
            "1-2 jam", 15),
        Rintangan("📢", "Apa yang dilakukan jika ada berita aneh di internet?",
            listOf("Langsung sebar", "Cek dulu kebenarannya", "Abaikan saja", "Hapus internetnya"),
            "Cek dulu kebenarannya", 15),
        Rintangan("👁️", "Apa yang terjadi jika terlalu lama pakai gadget?",
            listOf("Mata jadi sehat", "Mata bisa lelah", "Jadi pintar", "Tidak ada efek"),
            "Mata bisa lelah", 15),
        Rintangan("🤝", "Bagaimana cara berkomentar yang baik di internet?",
            listOf("Marah-marah", "Mengejek", "Sopan dan positif", "Diam saja"),
            "Sopan dan positif", 15)
    )

    private val rintanganSulit = listOf(
        Rintangan("💻", "Apa yang dimaksud dengan jejak digital?",
            listOf("Bekas kaki di pantai", "Aktivitas kita di internet", "Nama akun kita", "Foto profil kita"),
            "Aktivitas kita di internet", 20),
        Rintangan("🔐", "Mengapa password harus dijaga rahasia?",
            listOf("Karena singkat", "Agar akun aman dari orang jahat", "Karena susah diingat", "Tidak ada alasan"),
            "Agar akun aman dari orang jahat", 20),
        Rintangan("🏠", "Mengapa alamat rumah tidak boleh dibagikan di internet?",
            listOf("Karena tidak sopan", "Bisa berbahaya untuk keselamatan", "Karena internet lemot", "Tidak ada alasan"),
            "Bisa berbahaya untuk keselamatan", 20),
        Rintangan("📰", "Apa itu hoaks?",
            listOf("Berita yang benar", "Informasi palsu", "Nama aplikasi", "Jenis game"),
            "Informasi palsu", 20),
        Rintangan("©️", "Apa yang dimaksud hak cipta?",
            listOf("Bebas salin karya orang", "Hak pencipta atas karyanya", "Aturan bermain game", "Cara buat akun"),
            "Hak pencipta atas karyanya", 20),
        Rintangan("⚖️", "Mengapa perlu batasi waktu gadget?",
            listOf("Gadget mahal", "Perlu waktu bermain & belajar juga", "Baterai cepat habis", "Tidak ada alasan"),
            "Perlu waktu bermain & belajar juga", 20),
        Rintangan("🤜", "Jika melihat teman di-bully di internet, apa yang dilakukan?",
            listOf("Diam saja", "Ikut menertawakan", "Lapor ke orang tua/guru", "Sebarkan lebih lanjut"),
            "Lapor ke orang tua/guru", 20),
        Rintangan("📲", "Sebelum unduh aplikasi baru, apa yang dilakukan?",
            listOf("Langsung unduh", "Minta izin orang tua", "Tanya teman", "Tidak perlu tanya"),
            "Minta izin orang tua", 20),
        Rintangan("🎖️", "Bagaimana jadi pengguna internet yang bertanggung jawab?",
            listOf("Gunakan sesuka hati", "Ikuti aturan & batasi waktu", "Sembunyikan dari ortu", "Main game saja"),
            "Ikuti aturan & batasi waktu", 20),
        Rintangan("👨‍👧", "Mengapa anak perlu didampingi orang tua saat internet?",
            listOf("Anak tidak pintar", "Agar aman dari konten berbahaya", "Internet susah dipakai", "Tidak perlu alasan"),
            "Agar aman dari konten berbahaya", 20),
        Rintangan("🌟", "Apa dampak positif internet yang digunakan dengan bijak?",
            listOf("Membuat malas", "Menambah wawasan & memudahkan belajar", "Membuat tidak punya teman", "Merusak kesehatan"),
            "Menambah wawasan & memudahkan belajar", 20),
        Rintangan("📺", "Apa yang dimaksud screen time?",
            listOf("Waktu tidur", "Waktu di depan layar digital", "Nama aplikasi", "Jenis game"),
            "Waktu di depan layar digital", 20)
    )

    private lateinit var rintanganList: List<Rintangan>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRunnerGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        sessionManager = SessionManager(this)
        soundManager = SoundManager(this)
        level = intent.getStringExtra("LEVEL") ?: "MUDAH"

        rintanganList = when (level) {
            "SEDANG" -> rintanganSedang
            "SULIT" -> rintanganSulit
            else -> rintanganMudah
        }

        waktuJawab = when (level) {
            "SEDANG" -> 12
            "SULIT" -> 8
            else -> 15
        }

        binding.tvLevelInfo.text = when (level) {
            "SEDANG" -> "Level Sedang"
            "SULIT" -> "Level Sulit"
            else -> "Level Mudah"
        }

        updateNyawa()
        updateSkor()
        updateProgressRintangan()

        binding.btnBack.setOnClickListener { finish() }

        binding.gameCanvas.onObstacleReached = {
            runOnUiThread {
                updateMisiGame()
                tampilkanRintangan()
            }
        }

        // Mulai game
        binding.gameCanvas.startGame()
        soundManager.startBackgroundMusic("game_bgm")

        // Spawn rintangan pertama
        binding.gameCanvas.postDelayed({
            binding.gameCanvas.spawnBatu()
        }, 1500)
    }

    private fun tampilkanRintangan() {
        if (rintanganIndex >= rintanganList.size) {
            menang()
            return
        }

        if (nyawa <= 0) {
            gameOver()
            return
        }

        val rintangan = rintanganList[rintanganIndex]

        // Tampilkan soal
        binding.tvEmojiRintangan.text = rintangan.emoji
        binding.tvPertanyaanGame.text = rintangan.pertanyaan

        // Buat pilihan jawaban
        binding.gridJawaban.removeAllViews()
        val pilihanAcak = rintangan.pilihan.shuffled()

        pilihanAcak.forEach { pilihan ->
            val btn = TextView(this).apply {
                text = pilihan
                textSize = 13f
                setTextColor(getColor(R.color.text_primary))
                gravity = android.view.Gravity.CENTER
                setPadding(16, 20, 16, 20)
                setBackgroundResource(R.drawable.bg_jawaban_chip)

                val params = android.widget.GridLayout.LayoutParams().apply {
                    width = 0
                    height = android.widget.GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = android.widget.GridLayout.spec(
                        android.widget.GridLayout.UNDEFINED, 1f
                    )
                    setMargins(6, 6, 6, 6)
                }
                layoutParams = params

                setOnClickListener {
                    periksaJawaban(pilihan, rintangan.jawaban, rintangan.poin)
                }
            }
            binding.gridJawaban.addView(btn)
        }

        // Tampilkan overlay soal
        binding.layoutSoal.visibility = View.VISIBLE
        binding.layoutSoal.alpha = 0f
        binding.layoutSoal.animate().alpha(1f).setDuration(300).start()

        // Mulai countdown timer
        startCountDown()
    }

    private fun updateMisiGame() {
        if (sessionManager.getMisiStatus(SessionManager.MISI_HARIAN_2_STATUS) == 0) {
            sessionManager.setMisiStatus(SessionManager.MISI_HARIAN_2_STATUS, 1)
            // ✅ Simpan ke Firebase
            val uid = FirebaseHelper.getCurrentUid()
            if (uid != null) {
                lifecycleScope.launch {
                    FirebaseHelper.updateMisiStatus(uid, SessionManager.MISI_HARIAN_2_STATUS, 1)
                }
            }
        }
        
        // ✅ Cek Badge 3 (Gamers) - Main 5 kali
        sessionManager.incrementGamePlayCount()
        if (sessionManager.getGamePlayCount() >= 5 && !sessionManager.getBadgeStatus(SessionManager.KEY_BADGE_3)) {
            sessionManager.setBadgeStatus(SessionManager.KEY_BADGE_3, true)
            val uid = FirebaseHelper.getCurrentUid()
            if (uid != null) {
                lifecycleScope.launch {
                    FirebaseHelper.updateBadgeStatus(uid, SessionManager.KEY_BADGE_3, true, sessionManager.getTotalBadge())
                }
            }
        }
    }

    private fun startCountDown() {
        countDownTimer?.cancel()
        binding.tvTimer.text = waktuJawab.toString()

        countDownTimer = object : CountDownTimer(waktuJawab * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val detik = (millisUntilFinished / 1000).toInt() + 1
                binding.tvTimer.text = detik.toString()

                // Warna timer berubah saat mepet
                binding.tvTimer.setBackgroundResource(
                    if (detik <= 3) R.drawable.bg_jawaban_salah
                    else R.drawable.bg_button_primary
                )
            }

            override fun onFinish() {
                // Waktu habis = salah
                binding.tvTimer.text = "0"
                periksaJawaban("", rintanganList[rintanganIndex].jawaban, 0)
            }
        }.start()
    }

    private fun periksaJawaban(jawaban: String, jawabanBenar: String, poin: Int) {
        countDownTimer?.cancel()

        // Nonaktifkan semua tombol
        for (i in 0 until binding.gridJawaban.childCount) {
            binding.gridJawaban.getChildAt(i).isEnabled = false
        }

        if (jawaban == jawabanBenar) {
            // ✅ BENAR
            soundManager.playCorrect()
            skor += poin
            updateSkor()
            binding.gameCanvas.doJump()
            binding.gameCanvas.addSuccessParticles()

            // Highlight tombol benar
            for (i in 0 until binding.gridJawaban.childCount) {
                val child = binding.gridJawaban.getChildAt(i) as? TextView
                if (child?.text == jawabanBenar) {
                    child.setBackgroundResource(R.drawable.bg_jawaban_benar)
                }
            }

            // Tutup soal dan lanjut setelah animasi lompat
            binding.layoutSoal.postDelayed({
                binding.layoutSoal.visibility = View.GONE
                // isStopped akan diatur ke false setelah jump selesai atau batu lewat
                // di sini kita hanya perlu spawn batu berikutnya setelah jeda
                rintanganIndex++
                updateProgressRintangan()

                if (rintanganIndex < rintanganList.size) {
                    binding.gameCanvas.postDelayed({
                        binding.gameCanvas.spawnBatu()
                    }, 1500)
                } else {
                    binding.gameCanvas.postDelayed({
                        menang()
                    }, 1000)
                }
            }, 1000)

        } else {
            // ❌ SALAH
            soundManager.playWrong()
            nyawa--
            updateNyawa()
            binding.gameCanvas.doHurt()

            // Highlight tombol salah & benar
            for (i in 0 until binding.gridJawaban.childCount) {
                val child = binding.gridJawaban.getChildAt(i) as? TextView
                when (child?.text) {
                    jawaban -> child.setBackgroundResource(R.drawable.bg_jawaban_salah)
                    jawabanBenar -> child.setBackgroundResource(R.drawable.bg_jawaban_benar)
                }
            }

            if (nyawa <= 0) {
                binding.layoutSoal.postDelayed({
                    binding.layoutSoal.visibility = View.GONE
                    gameOver()
                }, 1500)
            } else {
                // Tetap lanjut meski salah
                binding.layoutSoal.postDelayed({
                    binding.layoutSoal.visibility = View.GONE
                    rintanganIndex++
                    updateProgressRintangan()

                    if (rintanganIndex < rintanganList.size) {
                        binding.gameCanvas.postDelayed({
                            binding.gameCanvas.spawnBatu()
                        }, 2000)
                    } else {
                        binding.gameCanvas.postDelayed({
                            menang()
                        }, 1000)
                    }
                }, 1500)
            }
        }
    }

    private fun updateNyawa() {
        binding.tvNyawa.text = when (nyawa) {
            3 -> "❤️❤️❤️"
            2 -> "❤️❤️🖤"
            1 -> "❤️🖤🖤"
            else -> "🖤🖤🖤"
        }
    }

    private fun updateSkor() {
        binding.tvSkorGame.text = "⭐ $skor"
    }

    private fun updateProgressRintangan() {
        val total = rintanganList.size
        val progress = ((rintanganIndex.toFloat() / total) * 100).toInt()
        binding.progressRintangan.progress = progress
        binding.tvProgressRintangan.text = "Rintangan: $rintanganIndex/$total"
    }

    private fun gameOver() {
        countDownTimer?.cancel()
        binding.gameCanvas.stopGame()
        soundManager.stopBackgroundMusic()
        binding.layoutSoal.visibility = View.GONE

        binding.tvSkorGameOver.text = "Skor kamu: $skor poin"
        binding.layoutGameOver.visibility = View.VISIBLE
        binding.layoutGameOver.alpha = 0f
        binding.layoutGameOver.animate().alpha(1f).setDuration(500).start()

        binding.btnMainLagi.setOnClickListener {
            // Restart game
            val intent = Intent(this, RunnerGameActivity::class.java)
            intent.putExtra("LEVEL", level)
            startActivity(intent)
            finish()
        }

        binding.btnKeluar.setOnClickListener { finish() }
    }

    private fun menang() {
        countDownTimer?.cancel()
        binding.gameCanvas.stopGame()
        soundManager.stopBackgroundMusic()
        binding.layoutSoal.visibility = View.GONE

        // ✅ Cek Badge 9 (Master Run) - Skor 500
        if (skor >= 500 && !sessionManager.getBadgeStatus(SessionManager.KEY_BADGE_9)) {
            sessionManager.setBadgeStatus(SessionManager.KEY_BADGE_9, true)
            val uid = FirebaseHelper.getCurrentUid()
            if (uid != null) {
                lifecycleScope.launch {
                    FirebaseHelper.updateBadgeStatus(uid, SessionManager.KEY_BADGE_9, true, sessionManager.getTotalBadge())
                }
            }
        }

        // Hitung bintang
        val totalPoin = rintanganList.size * when (level) {
            "SEDANG" -> 15; "SULIT" -> 20; else -> 10
        }
        val persentase = (skor.toFloat() / totalPoin * 100).toInt()
        val bintang = when {
            persentase >= 90 -> "⭐⭐⭐"
            persentase >= 60 -> "⭐⭐"
            else -> "⭐"
        }

        binding.tvSkorMenang.text = "Skor kamu: $skor poin"
        binding.tvBintangMenang.text = bintang
        binding.layoutMenang.visibility = View.VISIBLE
        binding.layoutMenang.alpha = 0f
        binding.layoutMenang.animate().alpha(1f).setDuration(500).start()

        // Simpan hasil game ke Firebase
        val uid = FirebaseHelper.getCurrentUid()
        if (uid != null && skor > 0) {
            lifecycleScope.launch {
                FirebaseHelper.simpanHasilGame(uid, level, skor, rintanganList.size)

                val poinBaru = sessionManager.getUserPoints() + skor
                val levelBaru = FirebaseHelper.hitungLevel(poinBaru)
                sessionManager.updatePoints(poinBaru)
                sessionManager.updateLevel(levelBaru)
            }
        }

        binding.btnSelesai.setOnClickListener { finish() }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        binding.gameCanvas.stopGame()
        soundManager.release()
    }
}