package com.example.ekidi.ui.literasi

import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
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

        // Tampilkan emoji & pertanyaan
        binding.tvEmoji.text = soal.emoji.ifEmpty {
            when (topikId) { 1 -> "💻"; 2 -> "🌐"; 3 -> "🤝"; else -> "🌍" }
        }
        binding.tvPertanyaan.text = soal.pertanyaan

        // Reset UI
        binding.tvFeedback.visibility = View.GONE
        binding.btnLanjut.visibility = View.GONE
        binding.layoutJawaban.removeAllViews()

        // ✅ Pilih tampilan jawaban berdasarkan tipe soal
        when (soal.tipeJawaban) {
            TipeJawaban.BENAR_SALAH -> {
                // Level 1: Pakai Drag & Drop
                binding.dropZone.visibility = View.VISIBLE
                binding.dropZone.setBackgroundResource(R.drawable.bg_drop_zone)
                binding.tvDropHint.text = "⬇ Seret jawaban ke kotak di atas"
                binding.layoutJawaban.orientation = LinearLayout.VERTICAL
                tampilkanJawabanDragDrop(soal)
                setupDropZone(soal)
            }
            TipeJawaban.PILIHAN_EMOJI -> {
                // Level 2: Drag & Drop
                binding.dropZone.visibility = View.VISIBLE
                binding.dropZone.setBackgroundResource(R.drawable.bg_drop_zone)
                binding.tvDropHint.text = "⬇ Seret jawaban ke kotak di atas"
                binding.layoutJawaban.orientation = LinearLayout.VERTICAL
                tampilkanJawabanDragDrop(soal)
                setupDropZone(soal)
            }
            TipeJawaban.PILIHAN_TEKS -> {
                // Level 3: Klik biasa
                binding.dropZone.visibility = View.GONE
                binding.layoutJawaban.orientation = LinearLayout.VERTICAL
                tampilkanJawabanKlik(soal)
            }
        }
    }

    // ─── Level 1 & 3: Tap/Klik biasa ────────────────────────────
    private fun tampilkanJawabanKlik(soal: com.example.ekidi.data.model.Soal) {
        val pilihan = soal.pilihanJawaban.shuffled()
        pilihan.forEach { jawaban ->
            val btn = TextView(this).apply {
                text = jawaban
                textSize = 14f
                setTextColor(Color.parseColor("#1F2937"))
                gravity = android.view.Gravity.CENTER
                
                val dp24 = (24 * resources.displayMetrics.density).toInt()
                setPadding(dp24, dp24, dp24, dp24)

                setBackgroundResource(R.drawable.bg_jawaban_chip)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 12, 0, 12) }

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

    // ─── Level 2: Drag & Drop ────────────────────────────────────
    private fun tampilkanJawabanDragDrop(soal: com.example.ekidi.data.model.Soal) {
        val pilihan = soal.pilihanJawaban.shuffled()
        pilihan.forEach { jawaban ->
            val chip = TextView(this).apply {
                text = jawaban
                textSize = 13f
                setTextColor(getColor(R.color.purple_primary))
                setBackgroundResource(R.drawable.bg_jawaban_chip)
                
                val dp32 = (32 * resources.displayMetrics.density).toInt()
                val dp20 = (20 * resources.displayMetrics.density).toInt()
                setPadding(dp32, dp20, dp32, dp20)

                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 12, 0, 12) }

                // ✅ Drag aktif saat sentuh pertama
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

    // ─── Setup Drop Zone untuk Level 2 ──────────────────────────
    private fun setupDropZone(soal: com.example.ekidi.data.model.Soal) {
        binding.dropZone.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    v.setBackgroundResource(R.drawable.bg_role_selected)
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    v.setBackgroundResource(R.drawable.bg_drop_zone)
                    true
                }
                DragEvent.ACTION_DROP -> {
                    val jawabanDipilih = event.clipData.getItemAt(0).text.toString()
                    val viewDrag = event.localState as? View
                    if (!sudahJawab) {
                        sudahJawab = true
                        viewDrag?.visibility = View.VISIBLE
                        periksaJawaban(
                            jawabanDipilih,
                            soal.jawabanBenar,
                            soal.penjelasan,
                            viewDrag as? TextView
                        )
                    }
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    val viewDrag = event.localState as? View
                    // Jika drag tidak sampai drop zone, tampilkan chip lagi
                    if (!event.result && !sudahJawab) {
                        viewDrag?.visibility = View.VISIBLE
                    }
                    true
                }
                else -> true
            }
        }
    }

    // ─── Periksa Jawaban ─────────────────────────────────────────
    private fun periksaJawaban(
        jawabanDipilih: String,
        jawabanBenar: String,
        penjelasan: String,
        viewDipilih: TextView?
    ) {
        val poinPerSoal = when (levelKuis) { 1 -> 10; 2 -> 15; else -> 20 }

        if (jawabanDipilih == jawabanBenar) {
            jumlahBenar++
            skor += poinPerSoal
            binding.tvSkorKuis.text = "⭐ $skor"
            viewDipilih?.setBackgroundResource(R.drawable.bg_jawaban_benar)
            viewDipilih?.setTextColor(Color.parseColor("#065F46"))

            // Update drop zone jika drag & drop
            if (soalList[soalIndex].tipeJawaban == TipeJawaban.PILIHAN_EMOJI) {
                binding.dropZone.setBackgroundResource(R.drawable.bg_drop_zone_benar)
                binding.tvDropHint.text = "✅ $jawabanDipilih"
            }

            binding.tvFeedback.text = "✅ Benar! $penjelasan"
            binding.tvFeedback.setTextColor(Color.parseColor("#10B981"))
        } else {
            viewDipilih?.setBackgroundResource(R.drawable.bg_jawaban_salah)
            viewDipilih?.setTextColor(Color.parseColor("#991B1B"))

            // Update drop zone jika drag & drop
            if (soalList[soalIndex].tipeJawaban == TipeJawaban.PILIHAN_EMOJI) {
                binding.dropZone.setBackgroundResource(R.drawable.bg_jawaban_salah)
                binding.tvDropHint.text = "❌ $jawabanDipilih"
            }

            binding.tvFeedback.text = "❌ Belum tepat!\n💡 $penjelasan"
            binding.tvFeedback.setTextColor(Color.parseColor("#EF4444"))

            // Highlight jawaban yang benar
            for (i in 0 until binding.layoutJawaban.childCount) {
                val child = binding.layoutJawaban.getChildAt(i) as? TextView
                if (child?.text == jawabanBenar) {
                    child.setBackgroundResource(R.drawable.bg_jawaban_benar)
                    child.setTextColor(Color.parseColor("#065F46"))
                }
            }
        }

        // Nonaktifkan semua chip
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

    // ─── Selesai ─────────────────────────────────────────────────
    private fun selesai() {
        val totalSoal = soalList.size
        val persentase = (jumlahBenar.toFloat() / totalSoal * 100).toInt()
        val lulus = persentase >= 70

        val pesan = when {
            persentase == 100 -> "Sempurna! Kamu bintang! 🌟"
            persentase >= 70 -> "Bagus sekali! Kamu lulus! 🎉"
            else -> "Semangat! Coba lagi ya! 💪"
        }

        binding.dropZone.visibility = View.GONE
        binding.tvEmoji.text = if (lulus) "🏆" else "💪"
        binding.tvPertanyaan.text =
            "Kuis Selesai!\n\nBenar: $jumlahBenar/$totalSoal ($persentase%)\nSkor: +$skor poin\n\n$pesan"
        binding.layoutJawaban.removeAllViews()
        binding.tvFeedback.visibility = View.GONE
        binding.progressKuis.progress = 100
        binding.tvNomorSoal.text = "Selesai!"

        val levelTerbukaSekarang = intent.getIntExtra("LEVEL_TERBUKA", 1)
        val levelTerbukaBaruValue = if (lulus && levelKuis >= levelTerbukaSekarang) {
            levelKuis + 1
        } else {
            levelTerbukaSekarang
        }

        // ✅ Simpan poin & progress ke Firebase
        val uid = FirebaseHelper.getCurrentUid()
        if (uid != null) {
            lifecycleScope.launch {
                if (skor > 0) {
                    FirebaseHelper.updatePoin(uid, skor)
                    val poinBaru = sessionManager.getUserPoints() + skor
                    val levelBaru = FirebaseHelper.hitungLevel(poinBaru)
                    sessionManager.updatePoints(poinBaru)
                    sessionManager.updateLevel(levelBaru)
                }
                if (lulus && levelTerbukaBaruValue > levelTerbukaSekarang) {
                    FirebaseHelper.simpanProgressKuis(uid, topikId, levelTerbukaBaruValue)
                    android.util.Log.d("EKIDI_DEBUG",
                        "SIMPAN PROGRESS: topik=$topikId, levelBaru=$levelTerbukaBaruValue")
                }
            }
        }

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