package com.example.ekidi.ui.game

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ekidi.R
import com.example.ekidi.databinding.ActivityGameBinding
import com.example.ekidi.utils.SoundManager

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        soundManager = SoundManager(this)

        binding.btnBack.setOnClickListener {
            soundManager.playClick()
            finish()
        }

        // ✅ Semua level sekarang buka RunnerGameActivity
        binding.btnMainMudah.setOnClickListener {
            soundManager.playClick()
            bukaGame("MUDAH")
        }
        binding.btnMainSedang.setOnClickListener {
            soundManager.playClick()
            bukaGame("SEDANG")
        }
        binding.btnMainSulit.setOnClickListener {
            soundManager.playClick()
            bukaGame("SULIT")
        }

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
                    val intent = Intent(this, com.example.ekidi.ui.home.HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    true
                }
                R.id.nav_literasi -> {
                    val intent = Intent(this, com.example.ekidi.ui.literasi.LiterasiActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_game -> true
                R.id.nav_misi -> {
                    val intent = Intent(this, com.example.ekidi.ui.misi.MisiActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profil -> {
                    val intent = Intent(this, com.example.ekidi.ui.profil.ProfilActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
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
