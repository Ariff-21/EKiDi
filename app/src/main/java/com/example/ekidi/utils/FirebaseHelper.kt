package com.example.ekidi.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseHelper {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private const val COL_USERS = "users"
    private const val COL_PROGRESS = "progress"
    private const val COL_HASIL_KUIS = "hasil_kuis"
    private const val COL_HASIL_GAME = "hasil_game"

    // ─── Auth: Register ──────────────────────────────────────────
    suspend fun register(
        email: String,
        password: String,
        nama: String,
        umur: Int? = null
    ): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user!!.uid
            val userData = hashMapOf(
                "uid" to uid,
                "nama" to nama,
                "email" to email,
                "umur" to (umur ?: 0),
                "level" to 1,
                "poin" to 0,
                "totalBintang" to 0,
                "totalBadge" to 1,
                "totalPembelajaran" to 0,
                "streak" to 0,
                "avatar" to "🐶",
                "createdAt" to System.currentTimeMillis(),
                SessionManager.KEY_BADGE_1 to true,
                SessionManager.KEY_BADGE_2 to false,
                SessionManager.KEY_BADGE_3 to false,
                SessionManager.KEY_BADGE_4 to false,
                SessionManager.KEY_BADGE_5 to false,
                SessionManager.KEY_BADGE_6 to false,
                SessionManager.KEY_BADGE_7 to false,
                SessionManager.KEY_BADGE_8 to false,
                SessionManager.KEY_BADGE_9 to false,
                SessionManager.KEY_BADGE_10 to false,
                SessionManager.KEY_BADGE_11 to false,
                SessionManager.KEY_BADGE_12 to false,
                // ✅ Data rekomendasi terakhir
                "rekomendasiTopikId" to 1,
                "rekomendasiLevel" to 1,
                "rekomendasiJudul" to "Mulai Belajar! 🎯",
                "rekomendasiDesc" to "Ayo mulai petualangan belajar literasi digitalmu!",
                "rekomendasiEmoji" to "🎯",
                // ✅ Status Misi Mingguan & Spesial
                SessionManager.MISI_MINGGUAN_STATUS to 0,
                SessionManager.MISI_MINGGUAN_PROGRESS to 0,
                SessionManager.MISI_SPESIAL_STATUS to 0,
                "lastWeeklyReset" to System.currentTimeMillis()
            )
            db.collection(COL_USERS).document(uid).set(userData).await()
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Auth: Login ─────────────────────────────────────────────
    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!.uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Ambil Data User ─────────────────────────────────────────
    suspend fun getUserData(uid: String): Result<Map<String, Any>> {
        return try {
            val doc = db.collection(COL_USERS).document(uid).get().await()
            if (doc.exists()) Result.success(doc.data!!)
            else Result.failure(Exception("Data user tidak ditemukan"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Update Poin & Level ─────────────────────────────────────
    suspend fun updatePoin(uid: String, tambahPoin: Int): Result<Unit> {
        return try {
            val doc = db.collection(COL_USERS).document(uid).get().await()
            val poinLama = (doc.getLong("poin") ?: 0).toInt()
            val poinBaru = poinLama + tambahPoin
            val levelBaru = hitungLevel(poinBaru)
            db.collection(COL_USERS).document(uid).update(
                mapOf("poin" to poinBaru, "level" to levelBaru)
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Simpan Progress Level Kuis ──────────────────────────────
    suspend fun simpanProgressKuis(
        uid: String,
        topikId: Int,
        levelTerbuka: Int
    ): Result<Unit> {
        return try {
            val progressData = hashMapOf(
                "topikId" to topikId,
                "levelTerbuka" to levelTerbuka,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection(COL_USERS)
                .document(uid)
                .collection(COL_PROGRESS)
                .document("topik_$topikId")
                .set(progressData)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Ambil Progress Level Kuis ───────────────────────────────
    suspend fun getProgressKuis(uid: String, topikId: Int): Int {
        return try {
            val doc = db.collection(COL_USERS)
                .document(uid)
                .collection(COL_PROGRESS)
                .document("topik_$topikId")
                .get()
                .await()
            if (doc.exists()) (doc.getLong("levelTerbuka") ?: 1).toInt()
            else 1
        } catch (e: Exception) {
            1
        }
    }

    // ─── ✅ Simpan Hasil Kuis + Jalankan Decision Tree ───────────
    suspend fun simpanHasilKuisAndRekomendasikan(
        uid: String,
        topikId: Int,
        levelKuis: Int,
        skorPersen: Int,
        jumlahBenar: Int,
        jumlahKesalahan: Int,
        totalWaktuDetik: Long,
        totalSoal: Int,
        levelTerbukaSekarang: Int
    ): Result<DecisionTreeHelper.RekomendasiBelajar> {
        return try {
            val waktuRataRata = if (totalSoal > 0)
                totalWaktuDetik.toFloat() / totalSoal
            else 0f

            // Simpan hasil kuis ke Firestore
            val hasilKuis = hashMapOf(
                "uid" to uid,
                "topikId" to topikId,
                "levelKuis" to levelKuis,
                "skorPersen" to skorPersen,
                "jumlahBenar" to jumlahBenar,
                "jumlahKesalahan" to jumlahKesalahan,
                "waktuRataRata" to waktuRataRata,
                "totalSoal" to totalSoal,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection(COL_USERS)
                .document(uid)
                .collection(COL_HASIL_KUIS)
                .add(hasilKuis)
                .await()

            // ✅ Jalankan Decision Tree
            val rekomendasi = DecisionTreeHelper.rekomendasikan(
                topikId = topikId,
                levelKuis = levelKuis,
                skorPersen = skorPersen,
                jumlahKesalahan = jumlahKesalahan,
                waktuRataRata = waktuRataRata,
                levelTerbukaSekarang = levelTerbukaSekarang
            )

            // ✅ Simpan rekomendasi ke dokumen user (untuk ditampilkan di Beranda)
            db.collection(COL_USERS).document(uid).update(
                mapOf(
                    "rekomendasiTopikId" to rekomendasi.topikId,
                    "rekomendasiLevel" to rekomendasi.levelRekomendasi,
                    "rekomendasiJudul" to rekomendasi.judulRekomendasi,
                    "rekomendasiDesc" to rekomendasi.deskripsi,
                    "rekomendasiEmoji" to rekomendasi.emoji,
                    "rekomendasiAksi" to rekomendasi.aksi.name,
                    "rekomendasiKategori" to rekomendasi.kategoriPerforma.name,
                    "rekomendasiPesan" to rekomendasi.pesanMotivasi,
                    "rekomendasiUpdatedAt" to System.currentTimeMillis()
                )
            ).await()

            Result.success(rekomendasi)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Ambil Rekomendasi Terakhir dari Firestore ───────────────
    suspend fun getRekomendasiTerakhir(uid: String): DecisionTreeHelper.RekomendasiBelajar? {
        return try {
            val doc = db.collection(COL_USERS).document(uid).get().await()
            if (doc.exists() && doc.contains("rekomendasiJudul")) {
                DecisionTreeHelper.RekomendasiBelajar(
                    judulRekomendasi = doc.getString("rekomendasiJudul") ?: "Mulai Belajar! 🎯",
                    deskripsi = doc.getString("rekomendasiDesc") ?: "Ayo mulai belajar!",
                    emoji = doc.getString("rekomendasiEmoji") ?: "🎯",
                    aksi = try {
                        DecisionTreeHelper.AksiRekomendasi.valueOf(
                            doc.getString("rekomendasiAksi") ?: "BACA_MATERI"
                        )
                    } catch (e: Exception) {
                        DecisionTreeHelper.AksiRekomendasi.BACA_MATERI
                    },
                    topikId = (doc.getLong("rekomendasiTopikId") ?: 1).toInt(),
                    levelRekomendasi = (doc.getLong("rekomendasiLevel") ?: 1).toInt(),
                    kategoriPerforma = try {
                        DecisionTreeHelper.KategoriPerforma.valueOf(
                            doc.getString("rekomendasiKategori") ?: "CUKUP"
                        )
                    } catch (e: Exception) {
                        DecisionTreeHelper.KategoriPerforma.CUKUP
                    },
                    pesanMotivasi = doc.getString("rekomendasiPesan") ?: "Semangat belajar! 🌟"
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // ─── Ambil Data User Realtime ────────────────────────────────
    fun listenUserData(uid: String, onUpdate: (poin: Int, level: Int) -> Unit) {
        db.collection(COL_USERS).document(uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val poin = (snapshot.getLong("poin") ?: 0).toInt()
                    val level = (snapshot.getLong("level") ?: 1).toInt()
                    onUpdate(poin, level)
                }
            }
    }

    // ─── Update Avatar ───────────────────────────────────────────
    suspend fun updateAvatar(uid: String, avatar: String): Result<Unit> {
        return try {
            db.collection(COL_USERS).document(uid).update("avatar", avatar).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Simpan Status Misi ───────────────────────────────────────
    suspend fun updateMisiStatus(uid: String, misiKey: String, status: Int): Result<Unit> {
        return try {
            db.collection(COL_USERS).document(uid).update(misiKey, status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBadgeStatus(uid: String, key: String, isEarned: Boolean, totalBadge: Int): Result<Unit> {
        return try {
            db.collection(COL_USERS).document(uid).update(
                key, isEarned,
                "totalBadge", totalBadge
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetMisiHarian(uid: String, todayDate: String): Result<Unit> {
        return try {
            db.collection(COL_USERS).document(uid).update(
                mapOf(
                    "misi_harian_1_status" to 0,
                    "misi_harian_2_status" to 0,
                    "misi_harian_3_status" to 0,
                    "last_reset_date" to todayDate
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Simpan Hasil Game ───────────────────────────────────────
    suspend fun simpanHasilGame(
        uid: String,
        level: String,
        skor: Int,
        totalSoal: Int
    ): Result<Unit> {
        return try {
            val hasilGame = hashMapOf(
                "uid" to uid,
                "level" to level,
                "skor" to skor,
                "totalSoal" to totalSoal,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection(COL_HASIL_GAME).add(hasilGame).await()
            updatePoin(uid, skor)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Logout ──────────────────────────────────────────────────
    fun logout() { auth.signOut() }

    fun isLoggedIn(): Boolean = auth.currentUser != null
    fun getCurrentUid(): String? = auth.currentUser?.uid

    // ─── Hitung Level dari Poin ──────────────────────────────────
    fun hitungLevel(poin: Int): Int = when {
        poin < 100 -> 1
        poin < 300 -> 2
        poin < 600 -> 3
        poin < 1000 -> 4
        poin < 1500 -> 5
        poin < 2100 -> 6
        poin < 2800 -> 7
        poin < 3600 -> 8
        poin < 4500 -> 9
        else -> 10
    }

    fun poinUntukLevelBerikutnya(level: Int): Int = when (level) {
        1 -> 100; 2 -> 300; 3 -> 600; 4 -> 1000
        5 -> 1500; 6 -> 2100; 7 -> 2800; 8 -> 3600
        9 -> 4500; else -> 4500
    }

    fun poinAwalLevel(level: Int): Int = when (level) {
        1 -> 0; 2 -> 100; 3 -> 300; 4 -> 600
        5 -> 1000; 6 -> 1500; 7 -> 2100; 8 -> 2800
        9 -> 3600; else -> 4500
    }
}