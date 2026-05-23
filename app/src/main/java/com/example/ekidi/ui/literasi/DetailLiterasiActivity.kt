package com.example.ekidi.ui.literasi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ekidi.R
import com.example.ekidi.databinding.ActivityDetailLiterasiBinding
import com.example.ekidi.utils.FirebaseHelper
import kotlinx.coroutines.launch

class DetailLiterasiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailLiterasiBinding
    private var topikId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailLiterasiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val judul = intent.getStringExtra("JUDUL_TOPIK") ?: "Materi"
        val ikon = intent.getStringExtra("IKON_TOPIK") ?: "📖"
        topikId = intent.getIntExtra("TOPIK_ID", 1)

        binding.tvJudulTopik.text = judul
        binding.tvNamaTopik.text = judul
        binding.tvIkonTopik.text = ikon

        binding.btnBack.setOnClickListener { finish() }

        // ✅ Load progress dari Firebase dulu baru buka MateriActivity
        binding.btnMulaiKuis.setOnClickListener {
            bukaMateriDenganProgress()
        }

        setupBottomNav()
    }

    private fun bukaMateriDenganProgress() {
        val uid = FirebaseHelper.getCurrentUid()

        if (uid != null) {
            // Tampilkan loading sementara
            binding.btnMulaiKuis.isEnabled = false
            binding.btnMulaiKuis.text = "Memuat..."

            lifecycleScope.launch {
                // ✅ Ambil progress dari Firebase
                val levelTerbuka = FirebaseHelper.getProgressKuis(uid, topikId)

                // Buka MateriActivity dengan level yang sudah tersimpan
                val intent = Intent(this@DetailLiterasiActivity, MateriActivity::class.java)
                intent.putExtra("TOPIK_ID", topikId)
                intent.putExtra("LEVEL_TERBUKA", levelTerbuka)
                startActivity(intent)

                // Reset tombol
                binding.btnMulaiKuis.isEnabled = true
                binding.btnMulaiKuis.text = "🎯 Mulai Kuis Topik Ini"
            }
        } else {
            // Tidak ada uid, buka dengan level 1
            val intent = Intent(this, MateriActivity::class.java)
            intent.putExtra("TOPIK_ID", topikId)
            intent.putExtra("LEVEL_TERBUKA", 1)
            startActivity(intent)
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_literasi
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { finish(); true }
                R.id.nav_literasi -> { finish(); true }
                else -> false
            }
        }
    }
}