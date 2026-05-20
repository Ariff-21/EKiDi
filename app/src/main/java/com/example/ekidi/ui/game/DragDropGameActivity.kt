package com.example.ekidi.ui.game

import android.content.ClipData
import android.content.ClipDescription
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.ekidi.R
import com.example.ekidi.databinding.ActivityDragDropGameBinding

class DragDropGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDragDropGameBinding
    private var level = "MUDAH"
    private var soalIndex = 0
    private var skor = 0
    private var jawabBenar = false

    // Data soal per level
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

        level = intent.getStringExtra("LEVEL") ?: "MUDAH"

        soalList = when (level) {
            "SEDANG" -> soalSedang
            "SULIT" -> soalSulit
            else -> soalMudah
        }

        val levelLabel = when (level) {
            "SEDANG" -> "🎮 Level Sedang"
            "SULIT" -> "🎮 Level Sulit"
            else -> "🎮 Level Mudah"
        }
        binding.tvLevelGame.text = levelLabel

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

        // Update progress
        val progressPersen = ((soalIndex.toFloat() / soalList.size) * 100).toInt()
        binding.progressSoal.progress = progressPersen
        binding.tvProgressSoal.text = "Soal ${soalIndex + 1} dari ${soalList.size}"
        binding.tvSkor.text = "⭐ $skor"

        // Tampilkan emoji perangkat
        binding.tvGambarPerangkat.text = soal.emoji

        // Reset drop zone
        binding.dropZone.setBackgroundResource(R.drawable.bg_drop_zone)
        binding.tvDropHint.text = "⬇ Seret jawaban ke sini"
        binding.tvDropHint.visibility = View.VISIBLE

        // Reset feedback
        binding.tvFeedback.visibility = View.GONE
        binding.btnLanjut.visibility = View.GONE

        // Acak urutan jawaban
        val jawabanAcak = soal.pilihanJawaban.shuffled()

        // Tampilkan pilihan jawaban
        binding.layoutJawaban.removeAllViews()
        jawabanAcak.forEach { jawaban ->
            val chip = TextView(this).apply {
                text = jawaban
                textSize = 13f
                setTextColor(getColor(R.color.purple_primary))
                setBackgroundResource(R.drawable.bg_jawaban_chip)
                setPadding(40, 0, 40, 0)
                gravity = android.view.Gravity.CENTER
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    120
                ).apply { setMargins(10, 10, 10, 10) }

                // Setup drag
                setOnLongClickListener { v ->
                    val clipText = (v as TextView).text.toString()
                    val item = ClipData.Item(clipText)
                    val dragData = ClipData(
                        clipText,
                        arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                        item
                    )
                    val shadow = View.DragShadowBuilder(v)
                    v.startDragAndDrop(dragData, shadow, v, 0)
                    true
                }

                // Juga bisa tap untuk jawab langsung (lebih mudah anak kecil)
                setOnClickListener { v ->
                    val jawabanDipilih = (v as TextView).text.toString()
                    periksaJawaban(jawabanDipilih, v)
                }
            }
            binding.layoutJawaban.addView(chip)
        }
    }

    private fun setupDropZone() {
        binding.dropZone.setOnDragListener { v, event ->
            when (event.action) {
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
                DragEvent.ACTION_DRAG_ENDED -> true
                else -> true
            }
        }
    }

    private fun periksaJawaban(jawabanDipilih: String, viewDipilih: View?) {
        if (jawabBenar) return // Sudah dijawab, abaikan

        val soal = soalList[soalIndex]
        jawabBenar = true

        if (jawabanDipilih == soal.jawabanBenar) {
            // ✅ BENAR
            skor += 10
            binding.tvSkor.text = "⭐ $skor"
            binding.dropZone.setBackgroundResource(R.drawable.bg_drop_zone_benar)
            binding.tvDropHint.text = jawabanDipilih
            binding.tvFeedback.text = "✅ Benar! Kamu hebat! 🎉"
            binding.tvFeedback.setTextColor(getColor(R.color.success))
            viewDipilih?.setBackgroundResource(R.drawable.bg_jawaban_benar)
        } else {
            // ❌ SALAH
            binding.tvDropHint.text = jawabanDipilih
            binding.tvFeedback.text = "❌ Belum tepat! Jawabannya: ${soal.jawabanBenar}"
            binding.tvFeedback.setTextColor(getColor(R.color.error))
            viewDipilih?.setBackgroundResource(R.drawable.bg_jawaban_salah)
        }

        binding.tvFeedback.visibility = View.VISIBLE
        binding.btnLanjut.visibility = View.VISIBLE

        // Animasi feedback
        binding.tvFeedback.alpha = 0f
        binding.tvFeedback.animate().alpha(1f).setDuration(300).start()
    }

    private fun soalBerikutnya() {
        soalIndex++
        tampilkanSoal()
    }

    private fun selesai() {
        // Tampilkan hasil akhir
        binding.tvGambarPerangkat.text = "🏆"
        binding.tvProgressSoal.text = "Selesai!"
        binding.progressSoal.progress = 100
        binding.tvSkor.text = "⭐ $skor"
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

        binding.btnLanjut.text = "🏠 Kembali ke Menu"
        binding.btnLanjut.visibility = View.VISIBLE
        binding.btnLanjut.setOnClickListener { finish() }
    }
}