package com.example.ekidi.ui.literasi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ekidi.R
import com.example.ekidi.databinding.ActivityDetailLiterasiBinding

class DetailLiterasiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailLiterasiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailLiterasiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val judul = intent.getStringExtra("JUDUL_TOPIK") ?: "Materi"
        val ikon = intent.getStringExtra("IKON_TOPIK") ?: "📖"

        binding.tvJudulTopik.text = judul
        binding.tvNamaTopik.text = judul
        binding.tvIkonTopik.text = ikon

        binding.btnBack.setOnClickListener { finish() }

        binding.btnMulaiKuis.setOnClickListener {
            // nanti dihubungkan ke GameActivity / KuisActivity
        }

        setupBottomNav()
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