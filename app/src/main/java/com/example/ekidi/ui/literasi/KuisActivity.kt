package com.example.ekidi.ui.literasi

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ekidi.R
import com.example.ekidi.data.model.SoalDatabase
import com.example.ekidi.data.model.TipeJawaban
import com.example.ekidi.databinding.ActivityKuisBinding
import com.example.ekidi.utils.FirebaseHelper
import com.example.ekidi.utils.SessionManager
import kotlinx.coroutines.launch

class KuisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKuisBinding
    private lateinit var sessionManager: SessionManager

    private var topikId = 1
    private var levelKuis = 1
    private var soalIndex = 0
    private var skor = 0
    private var jumlahBenar = 0
    private var sudahJawab = false

    private val soalList by lazy {
        SoalDatabase.getSoal(topikId, levelKuis)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKuisBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        sessionManager = SessionManager(this)
        topikId = intent.getIntExtra("TOPIK_ID", 1)
        levelKuis = intent.getIntExtra("LEVEL_KUIS", 1)

        val poinPerSoal = when (levelKuis) { 1 -> 10; 2 -> 15; else -> 20 }

        binding.tvJudulKuis.text = "🎯 Kuis Level $levelKuis"
        binding.btnBack.setOnClickListener { finish() }
        binding.btnLanjut.setOnClickListener { soalBerikutnya() }

        tampilkanSoal()
    }

    private fun tampilkanSoal() {
        if (soalIndex >= soalList.size) {
            selesai()
            return
        }

        sudahJawab = false
        val soal = soalList[soalIndex]

        // Update progress
        val progress = ((soalIndex.toFloat() / soalList.size) * 100).toInt()
        binding.progressKuis.progress = progress
        binding.tvNomorSoal.text = "Soal ${soalIndex + 1} dari ${soalList.size}"
        binding.tvSkorKuis.text = "⭐ $skor"

        // Tampilkan soal
        binding.tvEmoji.text = soal.emoji.ifEmpty {
            when (topikId) {
                1 -> "💻"; 2 -> "🌐"; 3 -> "🤝"; else -> "🌍"
            }
        }
        binding.tvPertanyaan.text = soal.pertanyaan

        // Reset
        binding.tvFeedback.visibility = View.GONE
        binding.btnLanjut.visibility = View.GONE
        binding.layoutJawaban.removeAllViews()

        // Tampilkan pilihan jawaban
        val pilihan = soal.pilihanJawaban.shuffled()
        pilihan.forEach { jawaban ->
            val btn = TextView(this).apply {
                text = jawaban
                textSize = 14f
                setTextColor(Color.parseColor("#1F2937"))
                gravity = Gravity.CENTER
                setPadding(24, 24, 24, 24)
                setBackgroundResource(R.drawable.bg_jawaban_chip)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 8, 0, 8) }

                setOnClickListener {
                    if (!sudahJawab) {
                        sudahJawab = true
                        periksaJawaban(jawaban, soal.jawabanBenar, soal.penjelasan, this)
                    }
                }
            }
            binding.layoutJawaban.addView(btn)
        }
    }

    private fun periksaJawaban(
        jawabanDipilih: String,
        jawabanBenar: String,
        penjelasan: String,
        viewDipilih: TextView
    ) {
        val poinPerSoal = when (levelKuis) { 1 -> 10; 2 -> 15; else -> 20 }

        if (jawabanDipilih == jawabanBenar) {
            jumlahBenar++
            skor += poinPerSoal
            binding.tvSkorKuis.text = "⭐ $skor"
            viewDipilih.setBackgroundResource(R.drawable.bg_jawaban_benar)
            viewDipilih.setTextColor(Color.parseColor("#065F46"))
            binding.tvFeedback.text = "✅ Benar! $penjelasan"
            binding.tvFeedback.setTextColor(Color.parseColor("#10B981"))
        } else {
            viewDipilih.setBackgroundResource(R.drawable.bg_jawaban_salah)
            viewDipilih.setTextColor(Color.parseColor("#991B1B"))
            binding.tvFeedback.text = "❌ Belum tepat!\n💡 $penjelasan"
            binding.tvFeedback.setTextColor(Color.parseColor("#EF4444"))

            // Highlight jawaban benar
            for (i in 0 until binding.layoutJawaban.childCount) {
                val child = binding.layoutJawaban.getChildAt(i) as? TextView
                if (child?.text == jawabanBenar) {
                    child.setBackgroundResource(R.drawable.bg_jawaban_benar)
                    child.setTextColor(Color.parseColor("#065F46"))
                }
            }
        }

        binding.tvFeedback.visibility = View.VISIBLE
        binding.btnLanjut.visibility = View.VISIBLE
        binding.tvFeedback.animate().alpha(0f).setDuration(0).start()
        binding.tvFeedback.animate().alpha(1f).setDuration(300).start()
    }

    private fun soalBerikutnya() {
        soalIndex++
        tampilkanSoal()
    }

    private fun selesai() {
        val totalSoal = soalList.size
        val persentase = (jumlahBenar.toFloat() / totalSoal * 100).toInt()
        val lulus = persentase >= 70

        val pesan = when {
            persentase == 100 -> "Sempurna! Kamu bintang! 🌟"
            persentase >= 70 -> "Bagus sekali! Kamu lulus! 🎉"
            else -> "Semangat! Coba lagi ya! 💪"
        }

        binding.tvEmoji.text = if (lulus) "🏆" else "💪"
        binding.tvPertanyaan.text =
            "Kuis Selesai!\n\nBenar: $jumlahBenar/$totalSoal ($persentase%)\nSkor: +$skor poin\n\n$pesan"
        binding.layoutJawaban.removeAllViews()
        binding.tvFeedback.visibility = View.GONE
        binding.progressKuis.progress = 100
        binding.tvNomorSoal.text = "Selesai!"

        val levelTerbukaSekarang = intent.getIntExtra("LEVEL_TERBUKA", 1)

        // ✅ Hitung level terbuka baru dengan benar
        // Max level adalah 3, nilai 4 berarti topik selesai semua
        val levelTerbukaBaruValue = if (lulus && levelKuis >= levelTerbukaSekarang) {
            levelKuis + 1  // buka level berikutnya (max nilai 4 = semua selesai)
        } else {
            levelTerbukaSekarang  // tidak berubah jika tidak lulus
        }

        // ✅ Simpan poin ke Firebase
        val uid = FirebaseHelper.getCurrentUid()
        if (uid != null && skor > 0) {
            lifecycleScope.launch {
                FirebaseHelper.updatePoin(uid, skor)
                val poinBaru = sessionManager.getUserPoints() + skor
                val levelBaru = FirebaseHelper.hitungLevel(poinBaru)
                sessionManager.updatePoints(poinBaru)
                sessionManager.updateLevel(levelBaru)
            }
        }

        binding.btnLanjut.text = when {
            !lulus -> "🔄 Coba Lagi"
            levelKuis < 3 -> "🔓 Level ${levelKuis + 1} Terbuka!"
            else -> "🏆 Topik Selesai!"
        }
        binding.btnLanjut.visibility = View.VISIBLE
        binding.btnLanjut.setOnClickListener {
            // ✅ Kirim hasil kembali ke MateriActivity
            val resultIntent = Intent()
            resultIntent.putExtra("LEVEL_TERBUKA_BARU", levelTerbukaBaruValue)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}