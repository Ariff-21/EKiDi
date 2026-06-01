package com.example.ekidi.ui.game

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ekidi.R
import com.example.ekidi.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.btnBack.setOnClickListener { finish() }

        // ✅ Semua level sekarang buka RunnerGameActivity
        binding.btnMainMudah.setOnClickListener { bukaGame("MUDAH") }
        binding.btnMainSedang.setOnClickListener { bukaGame("SEDANG") }
        binding.btnMainSulit.setOnClickListener { bukaGame("SULIT") }

        setupBottomNav()
    }

    private fun bukaGame(level: String) {
        // ✅ Ganti dari DragDropGameActivity ke RunnerGameActivity
        val intent = Intent(this, RunnerGameActivity::class.java)
        intent.putExtra("LEVEL", level)
        startActivity(intent)
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_game
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, com.example.ekidi.ui.home.HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_literasi -> {
                    startActivity(Intent(this, com.example.ekidi.ui.literasi.LiterasiActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_game -> true
                R.id.nav_misi -> {
                    startActivity(Intent(this, com.example.ekidi.ui.misi.MisiActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profil -> {
                    startActivity(Intent(this, com.example.ekidi.ui.profil.ProfilActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}