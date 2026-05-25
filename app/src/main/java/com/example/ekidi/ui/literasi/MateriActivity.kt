package com.example.ekidi.ui.literasi

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ekidi.databinding.ActivityMateriBinding
import com.example.ekidi.R
import com.example.ekidi.ui.home.HomeActivity
import com.example.ekidi.utils.FirebaseHelper
import kotlinx.coroutines.launch

class MateriActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMateriBinding
    private var topikId = 1
    private var levelTerbuka = 1

    private val dataMateri = mapOf(
        1 to listOf("💻", "Pengenalan Perangkat Digital",
            """Halo teman-teman! 👋

Tahukah kamu apa itu perangkat digital?

Perangkat digital adalah alat elektronik yang bisa menyimpan, memproses, dan mengirim informasi. Contohnya:

📱 Smartphone (HP)
Alat kecil yang bisa dibawa ke mana-mana. Bisa untuk telepon, foto, dan bermain!

💻 Laptop & Komputer
Alat untuk belajar, bekerja, dan bermain game. Komputer lebih besar, laptop bisa dibawa-bawa!

📱 Tablet
Seperti HP tapi lebih besar. Layarnya lebar sehingga nyaman untuk membaca dan belajar!

🖨️ Printer
Alat untuk mencetak tulisan dan gambar dari komputer!

⌨️ Keyboard & 🖱️ Mouse
Keyboard untuk mengetik, mouse untuk menggerakkan kursor di layar komputer.

Semua perangkat ini harus dijaga dengan baik ya! 💝""",
            """⭐ Ingat selalu:
- Gunakan perangkat digital dengan izin Ayah/Bunda
- Jaga perangkat agar tidak jatuh atau rusak
- Batasi waktu penggunaan maksimal 1-2 jam
- Selalu minta bantuan orang dewasa jika ada masalah"""),

        2 to listOf("🌐", "Keamanan Internet",
            """Halo teman-teman! 🌟

Internet adalah jaringan yang menghubungkan komputer di seluruh dunia. Kita bisa belajar, bermain, dan berkomunikasi lewat internet!

Tapi hati-hati, ada beberapa hal yang harus kita jaga:

🔐 Jaga Password
Password itu seperti kunci rumah. Jangan kasih ke siapapun kecuali Ayah/Bunda!

👨‍👧 Selalu Bersama Orang Tua
Saat berinternet, mintalah Ayah atau Bunda untuk mendampingi kamu ya!

🚫 Jangan Bagikan Data Pribadi
Nama lengkap, alamat rumah, dan nomor telepon adalah rahasia keluarga!

⚠️ Waspadai Orang Asing
Jika ada orang yang tidak dikenal mengajak chat atau bertemu, segera cerita ke orang tua!

✅ Pilih Konten yang Baik
Tontonlah video dan mainkan game yang sesuai dengan usia kamu!""",
            """⭐ Ingat selalu:
- Tidak ada yang boleh tahu password selain Ayah/Bunda
- Selalu minta izin sebelum menggunakan internet
- Langsung cerita ke orang tua jika ada yang aneh
- Gunakan internet untuk hal-hal positif"""),

        3 to listOf("🤝", "Etika Digital",
            """Halo teman-teman! 😊

Etika digital adalah cara kita bersikap baik dan sopan di dunia internet, sama seperti di dunia nyata!

😊 Berlaku Sopan
Kata-kata yang kita tulis di internet bisa menyakiti perasaan orang lain. Jadi selalu gunakan kata-kata yang baik!

🤜🤛 Jangan Bully
Cyberbullying adalah mengejek atau menghina orang lain di internet. Ini tidak boleh dilakukan!

✅ Sebar Informasi Benar
Sebelum membagikan berita atau informasi, pastikan dulu kebenarannya. Tanya orang tua!

📸 Minta Izin
Sebelum memposting foto teman atau keluarga, minta izin dulu ya!

🙏 Berani Minta Maaf
Jika tidak sengaja menyakiti perasaan orang di internet, beranikan diri untuk minta maaf!""",
            """⭐ Ingat selalu:
- Tulis komentar yang baik dan sopan
- Jangan sebarkan berita yang belum jelas kebenarannya
- Minta izin sebelum foto orang lain
- Bantu teman jika ada yang di-bully"""),

        4 to listOf("🌍", "Dunia Online",
            """Halo teman-teman! 🌍

Dunia online adalah dunia di internet yang terhubung dengan seluruh penjuru dunia. Di sini kita bisa belajar, bermain, dan berkomunikasi!

📚 Internet untuk Belajar
Banyak sekali video pembelajaran, buku digital, dan kuis seru di internet. Manfaatkan untuk belajar!

⏰ Atur Waktu
Batasi bermain gadget maksimal 1-2 jam per hari. Sisanya untuk bermain di luar, belajar, dan istirahat!

👁️ Jaga Kesehatan Mata
Istirahatkan mata setiap 20 menit dengan melihat benda yang jauh selama 20 detik!

🎮 Pilih Game yang Tepat
Mainkan game yang edukatif dan sesuai usia. Hindari game yang mengandung kekerasan!

👨‍👧 Selalu Bersama Orang Tua
Internet itu luas. Ayah dan Bunda ada untuk memastikan kamu aman dan mendapat konten yang baik!""",
            """⭐ Ingat selalu:
- Gunakan internet untuk hal yang positif dan mendidik
- Batasi waktu layar 1-2 jam per hari
- Istirahatkan mata secara berkala
- Selalu cerita ke orang tua tentang aktivitas online kamu""")
    )

    // ✅ Launcher — hanya update tampilan dari hasil kuis
    // TIDAK perlu simpan ke Firebase lagi karena sudah disimpan di KuisActivity
    private val kuisLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val levelBaru = result.data?.getIntExtra("LEVEL_TERBUKA_BARU", levelTerbuka)
                ?: levelTerbuka
            if (levelBaru > levelTerbuka) {
                // ✅ Update variabel lokal langsung tanpa load Firebase
                levelTerbuka = levelBaru
                setupStatusLevel()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMateriBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        topikId = intent.getIntExtra("TOPIK_ID", 1)
        binding.btnBack.setOnClickListener { finish() }

        // ✅ Load dari Firebase HANYA saat pertama buka (onCreate)
        // Tidak di onResume agar tidak override hasil kuis
        loadProgressDanSetup()
        setupBottomNav()
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_literasi
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_literasi -> { finish(); true }
                else -> false
            }
        }
    }

    private fun loadProgressDanSetup() {
        val uid = FirebaseHelper.getCurrentUid()
        if (uid != null) {
            lifecycleScope.launch {
                // Baca dari Firebase
                levelTerbuka = FirebaseHelper.getProgressKuis(uid, topikId)
                runOnUiThread {
                    setupUI()
                    setupClickListeners()
                }
            }
        } else {
            levelTerbuka = 1
            setupUI()
            setupClickListeners()
        }
    }

    private fun setupUI() {
        val materi = dataMateri[topikId] ?: dataMateri[1]!!
        binding.tvJudulMateri.text = "📖 ${materi[1]}"
        binding.tvIkonTopik.text = materi[0]
        binding.tvNamaTopik.text = materi[1]
        binding.tvKontenMateri.text = materi[2]
        binding.tvPoinPenting.text = materi[3]
        setupStatusLevel()
    }

    private fun setupStatusLevel() {
        // Level 1 selalu terbuka
        binding.tvStatusLevel1.text = "Mulai ▶"
        binding.tvStatusLevel1.setTextColor(getColor(com.example.ekidi.R.color.purple_primary))
        binding.cardLevel1.alpha = 1f

        // Level 2
        if (levelTerbuka >= 2) {
            binding.tvStatusLevel2.text = "Mulai ▶"
            binding.tvStatusLevel2.setBackgroundResource(com.example.ekidi.R.drawable.bg_button_primary)
            binding.tvStatusLevel2.setTextColor(getColor(android.R.color.white))
            binding.cardLevel2.alpha = 1f
        } else {
            binding.tvStatusLevel2.text = "🔒 Selesaikan Level 1 dulu"
            binding.tvStatusLevel2.setBackgroundResource(0)
            binding.tvStatusLevel2.setTextColor(getColor(com.example.ekidi.R.color.text_hint))
            binding.cardLevel2.alpha = 0.6f
        }

        // Level 3
        if (levelTerbuka >= 3) {
            binding.tvStatusLevel3.text = "Mulai ▶"
            binding.tvStatusLevel3.setBackgroundResource(com.example.ekidi.R.drawable.bg_button_primary)
            binding.tvStatusLevel3.setTextColor(getColor(android.R.color.white))
            binding.cardLevel3.alpha = 1f
        } else {
            binding.tvStatusLevel3.text = "🔒 Selesaikan Level 2 dulu"
            binding.tvStatusLevel3.setBackgroundResource(0)
            binding.tvStatusLevel3.setTextColor(getColor(com.example.ekidi.R.color.text_hint))
            binding.cardLevel3.alpha = 0.6f
        }
    }

    private fun setupClickListeners() {
        binding.cardLevel1.setOnClickListener { bukaKuis(1) }
        binding.cardLevel2.setOnClickListener { if (levelTerbuka >= 2) bukaKuis(2) }
        binding.cardLevel3.setOnClickListener { if (levelTerbuka >= 3) bukaKuis(3) }
    }

    private fun bukaKuis(level: Int) {
        val intent = Intent(this, KuisActivity::class.java)
        intent.putExtra("TOPIK_ID", topikId)
        intent.putExtra("LEVEL_KUIS", level)
        intent.putExtra("LEVEL_TERBUKA", levelTerbuka)
        kuisLauncher.launch(intent)
    }

    // ✅ onResume DIHAPUS TOTAL — tidak perlu reload Firebase di sini
}