package com.example.ekidi.ui.game

import android.content.ClipData
import android.content.ClipDescription
import android.os.Bundle
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.ekidi.R
import com.example.ekidi.databinding.ActivityDragDropGameBinding
import com.example.ekidi.utils.FirebaseHelper
import com.example.ekidi.utils.SessionManager
import kotlinx.coroutines.launch

class DragDropGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDragDropGameBinding
    private lateinit var sessionManager: SessionManager
    private var level = "MUDAH"
    private var soalIndex = 0
    private var skor = 0
    private var jawabBenar = false

    data class Soal(
        val emoji: String,
        val jawabanBenar: String,
        val pilihanJawaban: List<String>
    )

    private val soalMudah = listOf(
        Soal("💻", "Komputer", listOf("Komputer", "Tablet", "Kamera")),
        Soal("📱", "Smartphone", listOf("Smartphone", "Laptop", "Radio")),
        Soal("🖨️", "Printer", listOf("Scanner", "Printer", "Monitor"))
    )

    private val soalSedang = listOf(
        Soal("💻", "Komputer", listOf("Komputer", "Tablet", "Kamera", "Radio")),
        Soal("📱", "Smartphone", listOf("Smartphone", "Laptop", "Radio", "TV")),
        Soal("🖨️", "Printer", listOf("Scanner", "Printer", "Monitor", "Keyboard")),
        Soal("⌨️", "Keyboard", listOf("Mouse", "Keyboard", "Speaker", "Webcam")),
        Soal("🖥️", "Monitor", listOf("Monitor", "Proyektor", "Tablet", "Printer"))
    )

    private val soalSulit = listOf(
        Soal("💻", "Komputer", listOf("Komputer", "Tablet", "Kamera", "Radio")),
        Soal("📱", "Smartphone", listOf("Smartphone", "Laptop", "Radio", "TV")),
        Soal("🖨️", "Printer", listOf("Scanner", "Printer", "Monitor", "Keyboard")),
        Soal("⌨️", "Keyboard", listOf("Mouse", "Keyboard", "Speaker", "Webcam")),
        Soal("🖥️", "Monitor", listOf("Monitor", "Proyektor", "Tablet", "Printer")),
        Soal("🖱️", "Mouse", listOf("Mouse", "Joystick", "Trackpad", "Stylus")),
        Soal("📷", "Kamera", listOf("Kamera", "Scanner", "Webcam", "Proyektor"))
    )

    private lateinit var soalList: List<Soal>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDragDropGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        sessionManager = SessionManager(this)
        level = intent.getStringExtra("LEVEL") ?: "MUDAH"

        soalList = when (level) {
            "SEDANG" -> soalSedang
            "SULIT" -> soalSulit
            else -> soalMudah
        }

        binding.tvLevelGame.text = when (level) {
            "SEDANG" -> "🎮 Level Sedang"
            "SULIT" -> "🎮 Level Sulit"
            else -> "🎮 Level Mudah"
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnLanjut.setOnClickListener { soalBerikutnya() }

        tampilkanSoal()
        setupDropZone()
    }

    private fun tampilkanSoal() {
        if (soalIndex >= soalList.size) {
            selesai()
            return
        }

        val soal = soalList[soalIndex]
        jawabBenar = false

        val progress = ((soalIndex.toFloat() / soalList.size) * 100).toInt()
        binding.progressSoal.progress = progress
        binding.tvProgressSoal.text = "Soal ${soalIndex + 1} dari ${soalList.size}"
        binding.tvSkor.text = "⭐ $skor"
        binding.tvGambarPerangkat.text = soal.emoji

        // Reset drop zone
        binding.dropZone.setBackgroundResource(R.drawable.bg_drop_zone)
        binding.tvDropHint.text = "⬇ Seret jawaban ke sini"
        binding.tvDropHint.visibility = View.VISIBLE

        binding.tvFeedback.visibility = View.GONE
        binding.btnLanjut.visibility = View.GONE

        // Generate chip jawaban
        binding.layoutJawaban.removeAllViews()
        val jawabanAcak = soal.pilihanJawaban.shuffled()

        jawabanAcak.forEach { jawaban ->
            val chip = TextView(this).apply {
                text = jawaban
                textSize = 14f
                setTextColor(getColor(R.color.purple_primary))
                setBackgroundResource(R.drawable.bg_jawaban_chip)
                setPadding(50, 24, 50, 24)
                gravity = android.view.Gravity.CENTER

                val params = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(10, 10, 10, 10)
                layoutParams = params

                // ✅ Drag aktif saat sentuh pertama kali (bukan long press)
                setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        val chipView = v as TextView
                        val item = ClipData.Item(chipView.text)
                        val dragData = ClipData(
                            chipView.text,
                            arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                            item
                        )
                        val shadow = View.DragShadowBuilder(v)
                        v.startDragAndDrop(dragData, shadow, v, 0)
                        v.visibility = View.INVISIBLE // sembunyikan chip asli saat di-drag
                        true
                    } else {
                        false
                    }
                }
            }
            binding.layoutJawaban.addView(chip)
        }
    }

    private fun setupDropZone() {
        binding.dropZone.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    // Terima semua drag
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
                    periksaJawaban(jawabanDipilih, viewDrag)
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    // ✅ Kalau drag tidak sampai ke drop zone, tampilkan chip lagi
                    val viewDrag = event.localState as? View
                    if (!event.result) {
                        // Drag dibatalkan / tidak kena drop zone
                        viewDrag?.visibility = View.VISIBLE
                    }
                    true
                }
                else -> true
            }
        }
    }

    private fun periksaJawaban(jawabanDipilih: String, viewDipilih: View?) {
        if (jawabBenar) return

        val soal = soalList[soalIndex]
        jawabBenar = true

        // Tampilkan chip yang di-drag kembali (dengan warna hasil)
        viewDipilih?.visibility = View.VISIBLE

        if (jawabanDipilih == soal.jawabanBenar) {
            skor += 10
            binding.tvSkor.text = "⭐ $skor"
            binding.dropZone.setBackgroundResource(R.drawable.bg_drop_zone_benar)
            binding.tvDropHint.text = "✅ $jawabanDipilih"
            binding.tvFeedback.text = "✅ Benar! Kamu hebat! 🎉"
            binding.tvFeedback.setTextColor(getColor(R.color.success))
            viewDipilih?.setBackgroundResource(R.drawable.bg_jawaban_benar)
        } else {
            binding.dropZone.setBackgroundResource(R.drawable.bg_jawaban_salah)
            binding.tvDropHint.text = "❌ $jawabanDipilih"
            binding.tvFeedback.text = "❌ Belum tepat!\nJawabannya: ${soal.jawabanBenar} 💡"
            binding.tvFeedback.setTextColor(getColor(R.color.error))
            viewDipilih?.setBackgroundResource(R.drawable.bg_jawaban_salah)

            // Highlight jawaban yang benar
            for (i in 0 until binding.layoutJawaban.childCount) {
                val child = binding.layoutJawaban.getChildAt(i) as? TextView
                if (child?.text == soal.jawabanBenar) {
                    child.setBackgroundResource(R.drawable.bg_jawaban_benar)
                }
            }
        }

        binding.tvFeedback.visibility = View.VISIBLE
        binding.tvFeedback.alpha = 0f
        binding.tvFeedback.animate().alpha(1f).setDuration(300).start()
        binding.btnLanjut.visibility = View.VISIBLE

        // Nonaktifkan semua chip setelah jawab
        for (i in 0 until binding.layoutJawaban.childCount) {
            binding.layoutJawaban.getChildAt(i).setOnTouchListener(null)
        }
    }

    private fun soalBerikutnya() {
        soalIndex++
        tampilkanSoal()
    }

    private fun selesai() {
        binding.tvGambarPerangkat.text = "🏆"
        binding.dropZone.visibility = View.GONE
        binding.layoutJawaban.removeAllViews()

        val pesanHasil = when {
            skor >= soalList.size * 10 -> "Sempurna! Kamu bintang! 🌟"
            skor >= soalList.size * 7 -> "Bagus sekali! Terus semangat! 💪"
            skor >= soalList.size * 5 -> "Cukup baik! Latihan lagi ya! 😊"
            else -> "Jangan menyerah! Coba lagi! 🔥"
        }

        binding.tvFeedback.text = "🎮 Game Selesai!\nSkor kamu: $skor poin\n$pesanHasil"
        binding.tvFeedback.visibility = View.VISIBLE
        binding.tvFeedback.setTextColor(getColor(R.color.purple_primary))
        binding.tvProgressSoal.text = "Selesai!"
        binding.progressSoal.progress = 100
        binding.tvSkor.text = "⭐ $skor"

        // Simpan poin ke Firebase
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

        binding.btnLanjut.text = "🏠 Kembali ke Menu"
        binding.btnLanjut.visibility = View.VISIBLE
        binding.btnLanjut.setOnClickListener { finish() }
    }
}