package com.example.ekidi.ui.literasi

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ekidi.R
import com.example.ekidi.databinding.ActivityDetailLiterasiBinding
import com.example.ekidi.ui.home.HomeActivity
import com.example.ekidi.ui.game.GameActivity
import com.example.ekidi.ui.misi.MisiActivity
import com.example.ekidi.ui.profil.ProfilActivity
import com.example.ekidi.utils.FirebaseHelper
import com.example.ekidi.utils.SoundManager
import kotlinx.coroutines.launch

class DetailLiterasiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailLiterasiBinding
    private lateinit var soundManager: SoundManager
    private var topikId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailLiterasiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        soundManager = SoundManager(this)

        val judul = intent.getStringExtra("JUDUL_TOPIK") ?: "Materi"
        val ikon = intent.getStringExtra("IKON_TOPIK") ?: "📖"
        topikId = intent.getIntExtra("TOPIK_ID", 1)

        binding.tvJudulTopik.text = judul
        binding.tvNamaTopik.text = judul
        binding.tvIkonTopik.text = ikon

        binding.btnBack.setOnClickListener {
            soundManager.playClick()
            finish()
        }

        // ✅ Klik kartu materi juga buka MateriActivity
        val bukaMateriListener = View.OnClickListener {
            soundManager.playClick()
            bukaMateriDenganProgress()
        }
        binding.cardMateri1.setOnClickListener(bukaMateriListener)
        binding.cardMateri2.setOnClickListener(bukaMateriListener)
        binding.cardMateri3.setOnClickListener(bukaMateriListener)

        // ✅ Load progress dari Firebase dulu baru buka MateriActivity
        binding.btnMulaiKuis.setOnClickListener {
            soundManager.playClick()
            bukaMateriDenganProgress()
        }

        setupBottomNav()
    }

    private fun bukaMateriDenganProgress() {
        val uid = FirebaseHelper.getCurrentUid()

        // Tampilkan loading sementara
        binding.btnMulaiKuis.isEnabled = false
        val originalText = binding.btnMulaiKuis.text
        binding.btnMulaiKuis.text = "Memuat..."

        if (uid != null) {
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
                binding.btnMulaiKuis.text = originalText
            }
        } else {
            // Tidak ada uid, buka dengan level 1
            val intent = Intent(this, MateriActivity::class.java)
            intent.putExtra("TOPIK_ID", topikId)
            intent.putExtra("LEVEL_TERBUKA", 1)
            startActivity(intent)
            
            binding.btnMulaiKuis.isEnabled = true
            binding.btnMulaiKuis.text = originalText
        }
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
                R.id.nav_literasi -> {
                    startActivity(Intent(this, LiterasiActivity::class.java))
                    true
                }
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
