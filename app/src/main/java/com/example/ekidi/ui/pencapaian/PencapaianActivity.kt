package com.example.ekidi.ui.pencapaian

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.ekidi.R
import com.example.ekidi.databinding.ActivityPencapaianBinding
import com.example.ekidi.ui.home.HomeActivity
import com.example.ekidi.ui.game.GameActivity
import com.example.ekidi.ui.literasi.LiterasiActivity
import com.example.ekidi.ui.misi.MisiActivity
import com.example.ekidi.ui.profil.ProfilActivity
import com.example.ekidi.utils.SessionManager
import com.example.ekidi.utils.SoundManager

class PencapaianActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPencapaianBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPencapaianBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        sessionManager = SessionManager(this)
        soundManager = SoundManager(this)

        setupUI()
        setupClickListeners()
        setupBottomNav()
    }

    private fun setupUI() {
        val level = sessionManager.getUserLevel()
        val poin = sessionManager.getUserPoints()
        
        // Hitung progress bar dalam level saat ini
        val poinAwal = com.example.ekidi.utils.FirebaseHelper.poinAwalLevel(level)
        val poinTarget = com.example.ekidi.utils.FirebaseHelper.poinUntukLevelBerikutnya(level)
        val poinDiLevel = poin - poinAwal
        val totalPoinDiLevel = poinTarget - poinAwal
        val progress = if (totalPoinDiLevel > 0) {
            ((poinDiLevel.toFloat() / totalPoinDiLevel) * 100).toInt()
        } else 100

        binding.tvLevelSaatIni.text = getString(R.string.level_format, level)
        binding.tvPoinSaatIni.text = getString(R.string.skor_format, poin)
        binding.tvPoinTarget.text = "Target: " + getString(R.string.skor_format, poinTarget)
        binding.progressLevel.progress = progress.coerceIn(0, 100)

        // Stats dari session
        binding.tvTotalBintang.text = poin.toString()
        binding.tvTotalBadge.text = sessionManager.getTotalBadge().toString()
        binding.tvTotalPembelajaran.text = sessionManager.getTotalPembelajaran().toString()

        setupBadgeCollection()
    }

    private fun setupBadgeCollection() {
        val badgeData = listOf(
            BadgeInfo(SessionManager.KEY_BADGE_1, "Pemula", "🌟", "Daftar akun EKiDi"),
            BadgeInfo(SessionManager.KEY_BADGE_2, "Penjelajah", "🔍", "Selesaikan Level 3 topik materi"),
            BadgeInfo(SessionManager.KEY_BADGE_3, "Gamers", "🎮", "Main game 5 kali"),
            BadgeInfo(SessionManager.KEY_BADGE_4, "Misi Master", "⚡", "Klaim 10 hadiah misi"),
            BadgeInfo(SessionManager.KEY_BADGE_5, "Literasi Pro", "📖", "Selesaikan SEMUA topik materi"),
            BadgeInfo(SessionManager.KEY_BADGE_6, "Bintang EKiDi", "👑", "Capai Level 5"),
            BadgeInfo(SessionManager.KEY_BADGE_7, "Sniper", "🎯", "Skor 100% di Level 3"),
            BadgeInfo(SessionManager.KEY_BADGE_8, "Rajin", "🔥", "Capai streak 7 hari"),
            BadgeInfo(SessionManager.KEY_BADGE_9, "Master Run", "🏃", "Skor 500 di game Runner"),
            BadgeInfo(SessionManager.KEY_BADGE_10, "Kolektor", "✨", "Kumpulkan 2500 poin"),
            BadgeInfo(SessionManager.KEY_BADGE_11, "Jenius", "🧠", "3 kuis beruntun tanpa salah"),
            BadgeInfo(SessionManager.KEY_BADGE_12, "Legenda", "💎", "Capai Level 10")
        )

        val badgeViews = listOf(
            binding.badge1, binding.badge2, binding.badge3,
            binding.badge4, binding.badge5, binding.badge6,
            binding.badge7, binding.badge8, binding.badge9,
            binding.badge10, binding.badge11, binding.badge12
        )

        val badgeCards = listOf(
            binding.cardBadge1, binding.cardBadge2, binding.cardBadge3,
            binding.cardBadge4, binding.cardBadge5, binding.cardBadge6,
            binding.cardBadge7, binding.cardBadge8, binding.cardBadge9,
            binding.cardBadge10, binding.cardBadge11, binding.cardBadge12
        )

        badgeData.forEachIndexed { index, badge ->
            val isEarned = sessionManager.getBadgeStatus(badge.key)
            val view = badgeViews[index].root
            val card = badgeCards[index]

            val iconTv = view.findViewById<TextView>(R.id.tvBadgeIcon)
            val nameTv = view.findViewById<TextView>(R.id.tvBadgeName)
            val statusTv = view.findViewById<TextView>(R.id.tvBadgeStatus)
            val descTv = view.findViewById<TextView>(R.id.tvBadgeDesc)

            nameTv.text = badge.name
            descTv.text = badge.desc

            if (isEarned) {
                card.setCardBackgroundColor(getColor(R.color.badge_bg))
                iconTv.text = badge.icon
                statusTv.text = "Diraih!"
                statusTv.setTextColor(getColor(R.color.success))
            } else {
                card.setCardBackgroundColor(Color.parseColor("#F3F4F6"))
                iconTv.text = "🔒"
                statusTv.text = "Belum diraih"
                statusTv.setTextColor(getColor(R.color.text_hint))
            }
        }
    }

    private data class BadgeInfo(val key: String, val name: String, val icon: String, val desc: String)

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            soundManager.playClick()
            finish()
        }
        binding.btnLeaderboard.setOnClickListener {
            soundManager.playClick()
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_home
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    true
                }
                R.id.nav_literasi -> {
                    val intent = Intent(this, LiterasiActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_game -> {
                    val intent = Intent(this, GameActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_misi -> {
                    val intent = Intent(this, MisiActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profil -> {
                    val intent = Intent(this, ProfilActivity::class.java)
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
