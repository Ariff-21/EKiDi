package com.example.ekidi.ui.literasi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ekidi.R
import com.example.ekidi.databinding.ActivityLiterasiBinding

class LiterasiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLiterasiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiterasiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupClickListeners()
        setupBottomNav()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.cardTopik1.setOnClickListener {
            bukaDetailTopik("Pengenalan Perangkat Digital", "💻", 1)
        }
        binding.cardTopik2.setOnClickListener {
            bukaDetailTopik("Keamanan Internet", "🌐", 2)
        }
        binding.cardTopik3.setOnClickListener {
            bukaDetailTopik("Etika Digital", "🤝", 3)
        }
        binding.cardTopik4.setOnClickListener {
            bukaDetailTopik("Dunia Online", "🌍", 4)
        }
    }

    private fun bukaDetailTopik(judul: String, ikon: String, topikId: Int) {
        val intent = Intent(this, DetailLiterasiActivity::class.java)
        intent.putExtra("JUDUL_TOPIK", judul)
        intent.putExtra("IKON_TOPIK", ikon)
        intent.putExtra("TOPIK_ID", topikId)
        startActivity(intent)
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_literasi
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { finish(); true }
                R.id.nav_literasi -> true
                else -> false
            }
        }
    }
}