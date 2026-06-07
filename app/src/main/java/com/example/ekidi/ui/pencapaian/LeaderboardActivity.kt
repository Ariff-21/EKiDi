package com.example.ekidi.ui.pencapaian

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.ekidi.R
import com.example.ekidi.databinding.ActivityLeaderboardBinding
import com.example.ekidi.ui.game.GameActivity
import com.example.ekidi.ui.home.HomeActivity
import com.example.ekidi.ui.literasi.LiterasiActivity
import com.example.ekidi.ui.misi.MisiActivity
import com.example.ekidi.ui.profil.ProfilActivity
import com.example.ekidi.utils.FirebaseHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Query

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeaderboardBinding

    data class UserLeaderboard(
        val uid: String,
        val nama: String,
        val poin: Int,
        val level: Int,
        val avatar: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.btnBack.setOnClickListener { finish() }
        setupBottomNav()
        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        binding.progressLoading.visibility = View.VISIBLE
        binding.layoutLeaderboard.removeAllViews()

        lifecycleScope.launch {
            try {
                val currentUid = FirebaseHelper.getCurrentUid() ?: ""

                // Ambil semua user diurutkan dari poin tertinggi
                val snapshot = FirebaseHelper.db
                    .collection("users")
                    .orderBy("poin", Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
                    .await()

                val userList = mutableListOf<UserLeaderboard>()
                snapshot.documents.forEach { doc ->
                    val uid = doc.id
                    val nama = doc.getString("nama") ?: "Pengguna"
                    val poin = (doc.getLong("poin") ?: 0).toInt()
                    val level = (doc.getLong("level") ?: 1).toInt()
                    val avatar = doc.getString("avatar") ?: "🐶"
                    userList.add(UserLeaderboard(uid, nama, poin, level, avatar))
                }

                // Cari posisi user saat ini
                val posisiKamu = userList.indexOfFirst { it.uid == currentUid } + 1

                runOnUiThread {
                    binding.progressLoading.visibility = View.GONE

                    // Update posisi kamu
                    binding.tvPosisiKamu.text = if (posisiKamu > 0) "#$posisiKamu" else "#-"

                    // Update podium Top 3
                    updatePodium(userList, currentUid)

                    // Tampilkan list semua peringkat mulai posisi 4
                    userList.forEachIndexed { index, user ->
                        val peringkat = index + 1
                        val isKamu = user.uid == currentUid
                        val itemView = buatItemLeaderboard(peringkat, user, isKamu)
                        binding.layoutLeaderboard.addView(itemView)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    binding.progressLoading.visibility = View.GONE
                    val tvError = TextView(this@LeaderboardActivity).apply {
                        text = "Gagal memuat data. Cek koneksi internet."
                        textSize = 13f
                        setTextColor(Color.parseColor("#EF4444"))
                        gravity = Gravity.CENTER
                        setPadding(0, 20, 0, 20)
                    }
                    binding.layoutLeaderboard.addView(tvError)
                }
            }
        }
    }

    private fun updatePodium(userList: List<UserLeaderboard>, currentUid: String) {
        // Posisi 1
        if (userList.isNotEmpty()) {
            val user1 = userList[0]
            binding.tvAvatar1.text = user1.avatar
            binding.tvNama1.text = if (user1.uid == currentUid) "Kamu! 🎉" else user1.nama
            binding.tvPoin1.text = "${user1.poin} poin"
        }

        // Posisi 2
        if (userList.size >= 2) {
            val user2 = userList[1]
            binding.tvAvatar2.text = user2.avatar
            binding.tvNama2.text = if (user2.uid == currentUid) "Kamu! 🎉" else user2.nama
            binding.tvPoin2.text = "${user2.poin} poin"
        }

        // Posisi 3
        if (userList.size >= 3) {
            val user3 = userList[2]
            binding.tvAvatar3.text = user3.avatar
            binding.tvNama3.text = if (user3.uid == currentUid) "Kamu! 🎉" else user3.nama
            binding.tvPoin3.text = "${user3.poin} poin"
        }
    }

    private fun buatItemLeaderboard(
        peringkat: Int,
        user: UserLeaderboard,
        isKamu: Boolean
    ): CardView {
        val card = CardView(this).apply {
            radius = 56f
            cardElevation = if (isKamu) 6f else 2f
            setCardBackgroundColor(
                if (isKamu) Color.parseColor("#EDE9FE")
                else Color.WHITE
            )
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 8) }
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(14, 14, 14, 14)
        }

        // ── Nomor Peringkat ──
        val tvPeringkat = TextView(this).apply {
            text = when (peringkat) {
                1 -> "🥇"
                2 -> "🥈"
                3 -> "🥉"
                else -> "#$peringkat"
            }
            textSize = if (peringkat <= 3) 20f else 14f
            setTextColor(
                when (peringkat) {
                    1 -> Color.parseColor("#F59E0B")
                    2 -> Color.parseColor("#9E9E9E")
                    3 -> Color.parseColor("#CD7F32")
                    else -> Color.parseColor("#6B7280")
                }
            )
            typeface = Typeface.DEFAULT_BOLD
            width = 60
            gravity = Gravity.CENTER
        }

        // ── Avatar ──
        val tvAvatar = TextView(this).apply {
            text = user.avatar
            textSize = 24f
            gravity = Gravity.CENTER
            width = 52
            height = 52
            setBackgroundResource(
                if (isKamu) R.drawable.bg_role_selected
                else R.drawable.circle_avatar_bg
            )
            layoutParams = LinearLayout.LayoutParams(52, 52).apply {
                setMargins(8, 0, 12, 0)
            }
        }

        // ── Nama & Info ──
        val layoutInfo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            )
        }

        val tvNama = TextView(this).apply {
            text = if (isKamu) "${user.nama} (Kamu)" else user.nama
            textSize = 13f
            setTextColor(
                if (isKamu) Color.parseColor("#7C3AED")
                else Color.parseColor("#1F2937")
            )
            typeface = if (isKamu) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }

        val tvLevelInfo = TextView(this).apply {
            text = "Level ${user.level} • ${user.poin} poin"
            textSize = 11f
            setTextColor(Color.parseColor("#6B7280"))
        }

        layoutInfo.addView(tvNama)
        layoutInfo.addView(tvLevelInfo)

        // ── Poin Badge ──
        val tvPoinBadge = TextView(this).apply {
            text = "⭐ ${user.poin}"
            textSize = 12f
            setTextColor(
                if (isKamu) Color.parseColor("#7C3AED")
                else Color.parseColor("#F59E0B")
            )
            typeface = Typeface.DEFAULT_BOLD
            setBackgroundResource(
                if (isKamu) R.drawable.bg_level_badge
                else R.drawable.bg_level_badge
            )
            setPadding(12, 4, 12, 4)
        }

        row.addView(tvPeringkat)
        row.addView(tvAvatar)
        row.addView(layoutInfo)
        row.addView(tvPoinBadge)
        card.addView(row)

        // Highlight border jika posisi kamu
        if (isKamu) {
            card.setCardBackgroundColor(Color.parseColor("#EDE9FE"))
        }

        return card
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_literasi -> {
                    startActivity(Intent(this, LiterasiActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_game -> {
                    startActivity(Intent(this, GameActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_misi -> {
                    startActivity(Intent(this, MisiActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profil -> {
                    startActivity(Intent(this, ProfilActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}