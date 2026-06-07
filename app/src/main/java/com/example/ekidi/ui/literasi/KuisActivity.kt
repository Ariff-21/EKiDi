package com.example.ekidi.ui.literasi

import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ekidi.R
import com.example.ekidi.data.model.SoalDatabase
import com.example.ekidi.data.model.TipeJawaban
import com.example.ekidi.databinding.ActivityKuisBinding
import com.example.ekidi.utils.DecisionTreeHelper
import com.example.ekidi.utils.FirebaseHelper
import com.example.ekidi.utils.SessionManager
import com.example.ekidi.utils.SoundManager
import kotlinx.coroutines.launch
import java.util.Locale

class KuisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKuisBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var soundManager: SoundManager

    private var topikId = 1
    private var levelKuis = 1
    private var soalIndex = 0
    private var skor = 0
    private var jumlahBenar = 0
    private var jumlahKesalahan = 0
    private var sudahJawab = false

    // ✅ Tracking waktu per soal untuk Decision Tree
    private var waktuMulaiSoal = 0L
    private var totalWaktuDetik = 0L
    private val waktuPerSoal = mutableListOf<Long>()

    private val soalList by lazy {
        SoalDatabase.getSoal(topikId, levelKuis)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKuisBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        sessionManager = SessionManager(this)
        soundManager = SoundManager(this)
        topikId = intent.getIntExtra("TOPIK_ID", 1)
        levelKuis = intent.getIntExtra("LEVEL_KUIS", 1)

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

        // ✅ Mulai hitung waktu soal ini
        waktuMulaiSoal = SystemClock.elapsedRealtime()

        // Update progress
        val progress = ((soalIndex.toFloat() / soalList.size) * 100).toInt()
        binding.progressKuis.progress = progress
        binding.tvNomorSoal.text = "Soal ${soalIndex + 1} dari ${soalList.size}"
        binding.tvSkorKuis.text = "⭐ $skor"

        binding.tvEmoji.text = soal.emoji.ifEmpty {
            when (topikId) { 1 -> "💻"; 2 -> "🌐"; 3 -> "🤝"; else -> "🌍" }
        }
        binding.tvPertanyaan.text = soal.pertanyaan

        binding.tvFeedback.visibility = View.GONE
        binding.btnLanjut.visibility = View.GONE
        binding.layoutJawaban.removeAllViews()

        when (soal.tipeJawaban) {
            TipeJawaban.BENAR_SALAH -> {
                binding.dropZone.visibility = View.GONE
                binding.layoutJawaban.orientation = LinearLayout.VERTICAL
                tampilkanJawabanKlik(soal)
            }
            TipeJawaban.PILIHAN_EMOJI -> {
                binding.dropZone.visibility = View.VISIBLE
                binding.dropZone.setBackgroundResource(R.drawable.bg_drop_zone)
                binding.tvDropHint.text = "⬇ Seret jawaban ke sini"
                binding.layoutJawaban.orientation = LinearLayout.HORIZONTAL
                tampilkanJawabanDragDrop(soal)
                setupDropZone(soal)
            }
            TipeJawaban.PILIHAN_TEKS -> {
                binding.dropZone.visibility = View.GONE
                binding.layoutJawaban.orientation = LinearLayout.VERTICAL
                tampilkanJawabanKlik(soal)
            }
        }
    }

    private fun tampilkanJawabanKlik(soal: com.example.ekidi.data.model.Soal) {
        val pilihan = soal.pilihanJawaban.shuffled()
        pilihan.forEach { jawaban ->
            val btn = TextView(this).apply {
                text = jawaban
                textSize = 14f
                setTextColor(Color.parseColor("#1F2937"))
                gravity = android.view.Gravity.CENTER
                setPadding(24, 24, 24, 24)
                setBackgroundResource(R.drawable.bg_jawaban_chip)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 8, 0, 8) }

                setOnClickListener {
                    if (!sudahJawab) {
                        sudahJawab = true
                        catatWaktuJawab()
                        periksaJawaban(jawaban, soal.jawabanBenar, soal.penjelasan, this)
                    }
                }
            }
            binding.layoutJawaban.addView(btn)
        }
    }

    private fun tampilkanJawabanDragDrop(soal: com.example.ekidi.data.model.Soal) {
        val pilihan = soal.pilihanJawaban.shuffled()
        pilihan.forEach { jawaban ->
            val chip = TextView(this).apply {
                text = jawaban
                textSize = 13f
                setTextColor(getColor(R.color.purple_primary))
                setBackgroundResource(R.drawable.bg_jawaban_chip)
                setPadding(32, 20, 32, 20)
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(8, 8, 8, 8) }

                setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_DOWN && !sudahJawab) {
                        val chipView = v as TextView
                        val item = ClipData.Item(chipView.text)
                        val dragData = ClipData(
                            chipView.text,
                            arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                            item
                        )
                        val shadow = View.DragShadowBuilder(v)
                        v.startDragAndDrop(dragData, shadow, v, 0)
                        v.visibility = View.INVISIBLE
                        true
                    } else false
                }
            }
            binding.layoutJawaban.addView(chip)
        }
    }

    private fun setupDropZone(soal: com.example.ekidi.data.model.Soal) {
        binding.dropZone.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED ->
                    event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                DragEvent.ACTION_DRAG_ENTERED -> {
                    v.setBackgroundResource(R.drawable.bg_role_selected); true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    v.setBackgroundResource(R.drawable.bg_drop_zone); true
                }
                DragEvent.ACTION_DROP -> {
                    val jawabanDipilih = event.clipData.getItemAt(0).text.toString()
                    val viewDrag = event.localState as? View
                    if (!sudahJawab) {
                        sudahJawab = true
                        catatWaktuJawab()
                        viewDrag?.visibility = View.VISIBLE
                        periksaJawaban(
                            jawabanDipilih, soal.jawabanBenar,
                            soal.penjelasan, viewDrag as? TextView
                        )
                    }
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    val viewDrag = event.localState as? View
                    if (!event.result && !sudahJawab) viewDrag?.visibility = View.VISIBLE
                    true
                }
                else -> true
            }
        }
    }

    // ✅ Catat waktu pengerjaan soal ini
    private fun catatWaktuJawab() {
        val waktuSoalIni = (SystemClock.elapsedRealtime() - waktuMulaiSoal) / 1000
        waktuPerSoal.add(waktuSoalIni)
        totalWaktuDetik += waktuSoalIni
    }

    private fun periksaJawaban(
        jawabanDipilih: String,
        jawabanBenar: String,
        penjelasan: String,
        viewDipilih: TextView?
    ) {
        val poinPerSoal = when (levelKuis) { 1 -> 10; 2 -> 15; else -> 20 }

        if (jawabanDipilih == jawabanBenar) {
            soundManager.playCorrect()
            jumlahBenar++
            skor += poinPerSoal
            binding.tvSkorKuis.text = "⭐ $skor"
            viewDipilih?.setBackgroundResource(R.drawable.bg_jawaban_benar)
            viewDipilih?.setTextColor(Color.parseColor("#065F46"))

            if (soalList[soalIndex].tipeJawaban == TipeJawaban.PILIHAN_EMOJI) {
                binding.dropZone.setBackgroundResource(R.drawable.bg_drop_zone_benar)
                binding.tvDropHint.text = "✅ $jawabanDipilih"
            }

            binding.tvFeedback.text = "✅ Benar! $penjelasan"
            binding.tvFeedback.setTextColor(Color.parseColor("#10B981"))
        } else {
            soundManager.playWrong()
            // ✅ Catat kesalahan untuk Decision Tree
            jumlahKesalahan++

            viewDipilih?.setBackgroundResource(R.drawable.bg_jawaban_salah)
            viewDipilih?.setTextColor(Color.parseColor("#991B1B"))

            if (soalList[soalIndex].tipeJawaban == TipeJawaban.PILIHAN_EMOJI) {
                binding.dropZone.setBackgroundResource(R.drawable.bg_jawaban_salah)
                binding.tvDropHint.text = "❌ $jawabanDipilih"
            }

            binding.tvFeedback.text = "❌ Belum tepat!\n💡 $penjelasan"
            binding.tvFeedback.setTextColor(Color.parseColor("#EF4444"))

            for (i in 0 until binding.layoutJawaban.childCount) {
                val child = binding.layoutJawaban.getChildAt(i) as? TextView
                if (child?.text == jawabanBenar) {
                    child.setBackgroundResource(R.drawable.bg_jawaban_benar)
                    child.setTextColor(Color.parseColor("#065F46"))
                }
            }
        }

        for (i in 0 until binding.layoutJawaban.childCount) {
            binding.layoutJawaban.getChildAt(i).setOnTouchListener(null)
            binding.layoutJawaban.getChildAt(i).setOnClickListener(null)
        }

        binding.tvFeedback.visibility = View.VISIBLE
        binding.tvFeedback.alpha = 0f
        binding.tvFeedback.animate().alpha(1f).setDuration(300).start()
        binding.btnLanjut.visibility = View.VISIBLE
    }

    private fun soalBerikutnya() {
        soalIndex++
        tampilkanSoal()
    }

    private fun selesai() {
        val totalSoal = soalList.size
        val persentase = (jumlahBenar.toFloat() / totalSoal * 100).toInt()
        val lulus = persentase >= 70
        val waktuRataRata = if (totalSoal > 0) totalWaktuDetik.toFloat() / totalSoal else 0f

        val levelTerbukaSekarang = intent.getIntExtra("LEVEL_TERBUKA", 1)
        val levelTerbukaBaruValue = if (lulus && levelKuis >= levelTerbukaSekarang) {
            levelKuis + 1
        } else {
            levelTerbukaSekarang
        }

        binding.dropZone.visibility = View.GONE
        binding.tvEmoji.text = if (lulus) "🏆" else "💪"

        val pesan = when {
            persentase == 100 -> "Sempurna! 🌟"
            persentase >= 70 -> "Kamu Lulus! 🎉"
            else -> "Semangat Coba Lagi! 💪"
        }

        binding.tvPertanyaan.text =
            "Kuis Selesai!\n\nBenar: $jumlahBenar/$totalSoal ($persentase%)\n" +
                    "Kesalahan: $jumlahKesalahan\n" +
                    "Waktu rata-rata: ${String.format(Locale.getDefault(), "%.1f", waktuRataRata)} detik/soal\n" +
                    "Skor: +$skor poin\n\n$pesan"

        binding.layoutJawaban.removeAllViews()
        binding.tvFeedback.visibility = View.GONE
        binding.progressKuis.progress = 100
        binding.tvNomorSoal.text = "Selesai!"
        binding.btnLanjut.visibility = View.GONE

        // ✅ Update Misi Harian 3 (Selesaikan 1 Kuis)
        if (sessionManager.getMisiStatus(SessionManager.MISI_HARIAN_3_STATUS) == 0) {
            sessionManager.setMisiStatus(SessionManager.MISI_HARIAN_3_STATUS, 1)
            // ✅ Simpan ke Firebase
            val currentUid = FirebaseHelper.getCurrentUid()
            if (currentUid != null) {
                lifecycleScope.launch {
                    FirebaseHelper.updateMisiStatus(currentUid, SessionManager.MISI_HARIAN_3_STATUS, 1)
                }
            }
        }

        // ✅ Simpan hasil + jalankan Decision Tree
        val uid = FirebaseHelper.getCurrentUid()
        if (uid != null) {
            lifecycleScope.launch {
                // Update poin
                if (skor > 0) {
                    FirebaseHelper.updatePoin(uid, skor)
                    val poinBaru = sessionManager.getUserPoints() + skor
                    val levelBaru = FirebaseHelper.hitungLevel(poinBaru)
                    sessionManager.updatePoints(poinBaru)
                    sessionManager.updateLevel(levelBaru)
                }

                // Simpan progress level jika lulus
                if (lulus && levelTerbukaBaruValue > levelTerbukaSekarang) {
                    FirebaseHelper.simpanProgressKuis(uid, topikId, levelTerbukaBaruValue)
                    android.util.Log.d("EKIDI_DEBUG",
                        "SIMPAN PROGRESS: topik=$topikId, levelBaru=$levelTerbukaBaruValue")
                }

                // ✅ Simpan hasil kuis + jalankan Decision Tree + simpan rekomendasi
                val hasilRekomendasi = FirebaseHelper.simpanHasilKuisAndRekomendasikan(
                    uid = uid,
                    topikId = topikId,
                    levelKuis = levelKuis,
                    skorPersen = persentase,
                    jumlahBenar = jumlahBenar,
                    jumlahKesalahan = jumlahKesalahan,
                    totalWaktuDetik = totalWaktuDetik,
                    totalSoal = totalSoal,
                    levelTerbukaSekarang = levelTerbukaSekarang
                )

                // Tampilkan hasil rekomendasi di layar
                hasilRekomendasi.getOrNull()?.let { rek ->
                    runOnUiThread {
                        binding.tvFeedback.text =
                            "🧠 Rekomendasi EKiDi:\n${rek.pesanMotivasi}\n\n➡ ${rek.judulRekomendasi}"
                        binding.tvFeedback.setTextColor(
                            when (rek.kategoriPerforma) {
                                DecisionTreeHelper.KategoriPerforma.SANGAT_BAIK ->
                                    Color.parseColor("#10B981")
                                DecisionTreeHelper.KategoriPerforma.BAIK ->
                                    Color.parseColor("#7C3AED")
                                DecisionTreeHelper.KategoriPerforma.CUKUP ->
                                    Color.parseColor("#F59E0B")
                                DecisionTreeHelper.KategoriPerforma.PERLU_LATIHAN ->
                                    Color.parseColor("#EF4444")
                            }
                        )
                        binding.tvFeedback.visibility = View.VISIBLE

                        binding.btnLanjut.text = when {
                            !lulus -> "🔄 Coba Lagi"
                            levelKuis < 3 -> "🔓 Level ${levelKuis + 1} Terbuka!"
                            else -> "🏆 Topik Selesai!"
                        }
                        binding.btnLanjut.visibility = View.VISIBLE
                        binding.btnLanjut.setOnClickListener {
                            val resultIntent = Intent()
                            resultIntent.putExtra("LEVEL_TERBUKA_BARU", levelTerbukaBaruValue)
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        }
                    }
                } ?: run {
                    // Fallback jika Decision Tree gagal
                    runOnUiThread {
                        binding.btnLanjut.text = when {
                            !lulus -> "🔄 Coba Lagi"
                            levelKuis < 3 -> "🔓 Level ${levelKuis + 1} Terbuka!"
                            else -> "🏆 Topik Selesai!"
                        }
                        binding.btnLanjut.visibility = View.VISIBLE
                        binding.btnLanjut.setOnClickListener {
                            val resultIntent = Intent()
                            resultIntent.putExtra("LEVEL_TERBUKA_BARU", levelTerbukaBaruValue)
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        }
                    }
                }
            }
        } else {
            binding.btnLanjut.text = if (lulus) "Selesai 🏠" else "🔄 Coba Lagi"
            binding.btnLanjut.visibility = View.VISIBLE
            binding.btnLanjut.setOnClickListener {
                val resultIntent = Intent()
                resultIntent.putExtra("LEVEL_TERBUKA_BARU", levelTerbukaBaruValue)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::soundManager.isInitialized) {
            soundManager.release()
        }
    }
}
