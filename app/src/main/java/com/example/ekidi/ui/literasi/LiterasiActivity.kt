package com.example.ekidi.ui.literasi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ekidi.R
import com.example.ekidi.databinding.ActivityLiterasiBinding
import com.example.ekidi.ui.home.HomeActivity
import com.example.ekidi.ui.game.GameActivity
import com.example.ekidi.ui.misi.MisiActivity
import com.example.ekidi.ui.profil.ProfilActivity
import com.example.ekidi.utils.SoundManager

class LiterasiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLiterasiBinding
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiterasiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        soundManager = SoundManager(this)
        setupClickListeners()
        setupBottomNav()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            soundManager.playClick()
            finish()
        }

        binding.cardTopik1.setOnClickListener {
            soundManager.playClick()
            bukaDetailTopik("Pengenalan Perangkat Digital", "💻", 1)
        }
        binding.cardTopik2.setOnClickListener {
            soundManager.playClick()
            bukaDetailTopik("Keamanan Internet", "🌐", 2)
        }
        binding.cardTopik3.setOnClickListener {
            soundManager.playClick()
            bukaDetailTopik("Etika Digital", "🤝", 3)
        }
        binding.cardTopik4.setOnClickListener {
            soundManager.playClick()
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
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    true
                }
                R.id.nav_literasi -> true
                R.id.nav_game -> {
                    startActivity(Intent(this, GameActivity::class.java))
                    true
                }
                R.id.nav_misi -> {
                    startActivity(Intent(this, MisiActivity::class.java))
                    true
                }
                R.id.nav_profil -> {
                    startActivity(Intent(this, ProfilActivity::class.java))
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
