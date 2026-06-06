package com.example.ekidi.utils

/**
 * DecisionTreeHelper — Implementasi Algoritma Decision Tree
 * sesuai proposal TA Muhammad Arif Rahmat (221111017)
 *
 * Input  : skor (%), jumlahKesalahan, waktuRataRata (detik/soal)
 * Output : RekomendasiBelajar (level, aktivitas, pesan, topik)
 *
 * Skema pohon keputusan:
 *
 *                    [Skor]
 *                 /    |    \
 *            Tinggi  Sedang  Rendah
 *           (≥80%)  (60-79%) (<60%)
 *             /         |        \
 *        [Kesalahan]  [Waktu]   → Ulangi
 *         /      \    /    \
 *      Sedikit  Banyak  Cepat  Lambat
 *      (≤1)    (≥2)
 *        |       |       |       |
 *    [Waktu]  Ulangi  Naikkan  Baca
 *    /    \   Level   Level   Materi
 *  Cepat Lambat
 *    |      |
 *  Naik  Tantangan
 *  Level  Lebih Sulit
 */
object DecisionTreeHelper {

    // ─── Data class output rekomendasi ───────────────────────────
    data class RekomendasiBelajar(
        val judulRekomendasi: String,       // Judul singkat untuk card beranda
        val deskripsi: String,              // Deskripsi detail
        val emoji: String,                  // Emoji representasi
        val aksi: AksiRekomendasi,          // Aksi yang harus dilakukan
        val topikId: Int,                   // Topik yang direkomendasikan (1-4)
        val levelRekomendasi: Int,          // Level yang direkomendasikan (1-3)
        val kategoriPerforma: KategoriPerforma, // Hasil klasifikasi performa
        val pesanMotivasi: String           // Pesan untuk anak
    )

    enum class AksiRekomendasi {
        NAIK_LEVEL,         // Lanjut ke level lebih tinggi
        ULANGI_LEVEL,       // Ulangi level yang sama
        BACA_MATERI,        // Baca ulang materi dulu
        COBA_TOPIK_LAIN,    // Coba topik literasi lain
        MAIN_GAME,          // Main game edukatif dulu
        TOPIK_SELESAI       // Semua level topik ini selesai
    }

    enum class KategoriPerforma {
        SANGAT_BAIK,    // Skor ≥80%, kesalahan ≤1, waktu cepat
        BAIK,           // Skor ≥80%, tapi ada kelemahan waktu/kesalahan
        CUKUP,          // Skor 60-79%
        PERLU_LATIHAN   // Skor <60%
    }

    // ─── Konstanta threshold ─────────────────────────────────────
    private const val SKOR_TINGGI = 80      // % → performa tinggi
    private const val SKOR_SEDANG = 60      // % → performa sedang
    private const val KESALAHAN_SEDIKIT = 1 // jumlah → kesalahan sedikit
    private const val WAKTU_CEPAT_L1 = 10f  // detik → cepat di level 1
    private const val WAKTU_CEPAT_L2 = 9f   // detik → cepat di level 2
    private const val WAKTU_CEPAT_L3 = 6f   // detik → cepat di level 3

    /**
     * Fungsi utama Decision Tree
     * @param topikId       ID topik yang baru diselesaikan (1-4)
     * @param levelKuis     Level kuis yang baru diselesaikan (1-3)
     * @param skorPersen    Persentase jawaban benar (0-100)
     * @param jumlahKesalahan Jumlah jawaban salah
     * @param waktuRataRata Rata-rata waktu per soal dalam detik
     * @param levelTerbukaSekarang Level tertinggi yang sudah terbuka
     */
    fun rekomendasikan(
        topikId: Int,
        levelKuis: Int,
        skorPersen: Int,
        jumlahKesalahan: Int,
        waktuRataRata: Float,
        levelTerbukaSekarang: Int
    ): RekomendasiBelajar {

        val batasWaktuCepat = when (levelKuis) {
            1 -> WAKTU_CEPAT_L1
            2 -> WAKTU_CEPAT_L2
            else -> WAKTU_CEPAT_L3
        }

        val waktuCepat = waktuRataRata <= batasWaktuCepat
        val kesalahanSedikit = jumlahKesalahan <= KESALAHAN_SEDIKIT

        // ── NODE 1: Cek Skor ──────────────────────────────────────
        return when {

            // ── CABANG TINGGI (Skor ≥ 80%) ──────────────────────
            skorPersen >= SKOR_TINGGI -> {

                // ── NODE 2: Cek Kesalahan ──
                when {
                    kesalahanSedikit -> {
                        // ── NODE 3: Cek Waktu ──
                        when {
                            waktuCepat -> {
                                // ✅ SANGAT BAIK: Skor tinggi + sedikit salah + cepat
                                // → Naik ke level berikutnya
                                if (levelKuis < 3 && levelTerbukaSekarang <= levelKuis) {
                                    RekomendasiBelajar(
                                        judulRekomendasi = "Lanjut Level ${levelKuis + 1}! 🚀",
                                        deskripsi = "Performa kamu sangat bagus! Coba tantangan level ${levelKuis + 1} sekarang.",
                                        emoji = "🚀",
                                        aksi = AksiRekomendasi.NAIK_LEVEL,
                                        topikId = topikId,
                                        levelRekomendasi = levelKuis + 1,
                                        kategoriPerforma = KategoriPerforma.SANGAT_BAIK,
                                        pesanMotivasi = "Wah, kamu hebat sekali! Yuk naik ke level berikutnya! 🌟"
                                    )
                                } else if (levelKuis == 3) {
                                    // Topik selesai semua → coba topik lain
                                    val topikBerikutnya = if (topikId < 4) topikId + 1 else 1
                                    RekomendasiBelajar(
                                        judulRekomendasi = "Topik Selesai! Coba Topik Baru 🏆",
                                        deskripsi = "Kamu sudah menguasai semua level topik ini! Ayo pelajari topik baru.",
                                        emoji = "🏆",
                                        aksi = AksiRekomendasi.COBA_TOPIK_LAIN,
                                        topikId = topikBerikutnya,
                                        levelRekomendasi = 1,
                                        kategoriPerforma = KategoriPerforma.SANGAT_BAIK,
                                        pesanMotivasi = "Luar biasa! Kamu sudah master di topik ini! 🎓"
                                    )
                                } else {
                                    // Level sudah terbuka sebelumnya → main game
                                    RekomendasiBelajar(
                                        judulRekomendasi = "Main Game Yuk! 🎮",
                                        deskripsi = "Kamu sangat pintar! Coba buktikan kemampuanmu di Game Edukatif.",
                                        emoji = "🎮",
                                        aksi = AksiRekomendasi.MAIN_GAME,
                                        topikId = topikId,
                                        levelRekomendasi = levelKuis,
                                        kategoriPerforma = KategoriPerforma.SANGAT_BAIK,
                                        pesanMotivasi = "Kamu sudah jago! Tunjukkan kehebatanmu di game! 🕹️"
                                    )
                                }
                            }
                            else -> {
                                // ✅ BAIK: Skor tinggi + sedikit salah + lambat
                                // → Naik level tapi perlu perhatikan waktu
                                if (levelKuis < 3) {
                                    RekomendasiBelajar(
                                        judulRekomendasi = "Hampir Sempurna! Level ${levelKuis + 1} ⭐",
                                        deskripsi = "Jawabanmu benar, tapi coba lebih cepat lagi ya! Lanjut ke level ${levelKuis + 1}.",
                                        emoji = "⭐",
                                        aksi = AksiRekomendasi.NAIK_LEVEL,
                                        topikId = topikId,
                                        levelRekomendasi = levelKuis + 1,
                                        kategoriPerforma = KategoriPerforma.BAIK,
                                        pesanMotivasi = "Bagus! Jawaban kamu benar. Coba lebih cepat di level berikutnya! ⚡"
                                    )
                                } else {
                                    RekomendasiBelajar(
                                        judulRekomendasi = "Coba Topik Lain! 📚",
                                        deskripsi = "Kamu sudah selesai semua level! Coba topik literasi lainnya.",
                                        emoji = "📚",
                                        aksi = AksiRekomendasi.COBA_TOPIK_LAIN,
                                        topikId = if (topikId < 4) topikId + 1 else 1,
                                        levelRekomendasi = 1,
                                        kategoriPerforma = KategoriPerforma.BAIK,
                                        pesanMotivasi = "Topik ini sudah selesai! Ayo pelajari yang lain! 🌈"
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        // ⚠️ CUKUP: Skor tinggi tapi banyak kesalahan
                        // → Ulangi level yang sama untuk pemahaman lebih baik
                        RekomendasiBelajar(
                            judulRekomendasi = "Ulangi Level $levelKuis Sekali Lagi 🔄",
                            deskripsi = "Skor kamu bagus, tapi masih ada beberapa yang salah. Coba sekali lagi ya!",
                            emoji = "🔄",
                            aksi = AksiRekomendasi.ULANGI_LEVEL,
                            topikId = topikId,
                            levelRekomendasi = levelKuis,
                            kategoriPerforma = KategoriPerforma.CUKUP,
                            pesanMotivasi = "Hampir benar semua! Coba lagi biar makin sempurna! 💪"
                        )
                    }
                }
            }

            // ── CABANG SEDANG (Skor 60-79%) ─────────────────────
            skorPersen >= SKOR_SEDANG -> {

                // ── NODE 2: Cek Waktu ──
                when {
                    waktuCepat -> {
                        // ⚠️ Sedang + Cepat → Latihan lagi level sama
                        // Cepat tapi banyak salah = tergesa-gesa
                        RekomendasiBelajar(
                            judulRekomendasi = "Pelan-Pelan Tapi Benar ya! 🐢",
                            deskripsi = "Kamu terlalu tergesa-gesa! Coba lebih teliti saat menjawab soal.",
                            emoji = "🐢",
                            aksi = AksiRekomendasi.ULANGI_LEVEL,
                            topikId = topikId,
                            levelRekomendasi = levelKuis,
                            kategoriPerforma = KategoriPerforma.CUKUP,
                            pesanMotivasi = "Jangan terburu-buru ya! Baca soalnya dengan teliti dulu! 👀"
                        )
                    }
                    else -> {
                        // ⚠️ Sedang + Lambat → Baca materi dulu
                        RekomendasiBelajar(
                            judulRekomendasi = "Baca Materi Dulu ya! 📖",
                            deskripsi = "Yuk baca materi lagi sebelum mengerjakan kuis. Pasti bisa lebih baik!",
                            emoji = "📖",
                            aksi = AksiRekomendasi.BACA_MATERI,
                            topikId = topikId,
                            levelRekomendasi = levelKuis,
                            kategoriPerforma = KategoriPerforma.CUKUP,
                            pesanMotivasi = "Ayo baca materinya dulu bersama Ayah/Bunda, pasti lebih mudah! 📚"
                        )
                    }
                }
            }

            // ── CABANG RENDAH (Skor < 60%) ──────────────────────
            else -> {
                // ❌ PERLU LATIHAN: Skor rendah
                // → Baca ulang materi dari awal
                RekomendasiBelajar(
                    judulRekomendasi = "Yuk Belajar Lagi! 💡",
                    deskripsi = "Jangan menyerah! Baca materi bersama Ayah/Bunda dulu, lalu coba lagi ya!",
                    emoji = "💡",
                    aksi = AksiRekomendasi.BACA_MATERI,
                    topikId = topikId,
                    levelRekomendasi = levelKuis,
                    kategoriPerforma = KategoriPerforma.PERLU_LATIHAN,
                    pesanMotivasi = "Semangat! Setiap anak belajar dengan caranya sendiri. Kamu pasti bisa! 🌟"
                )
            }
        }
    }

    /**
     * Fungsi untuk rekomendasi awal (belum ada data kuis)
     * Dipanggil saat user baru login / belum pernah mengerjakan kuis
     */
    fun rekomendasiAwal(topikYangBelumDikerjakan: List<Int>): RekomendasiBelajar {
        val topikPertama = topikYangBelumDikerjakan.firstOrNull() ?: 1
        val namaTopik = getNamaTopik(topikPertama)
        return RekomendasiBelajar(
            judulRekomendasi = "Mulai Belajar $namaTopik! 🎯",
            deskripsi = "Ayo mulai petualangan belajar literasi digitalmu! Baca materi dan kerjakan kuis.",
            emoji = "🎯",
            aksi = AksiRekomendasi.BACA_MATERI,
            topikId = topikPertama,
            levelRekomendasi = 1,
            kategoriPerforma = KategoriPerforma.CUKUP,
            pesanMotivasi = "Halo! Ayo mulai belajar bersama EKiDi! 👋"
        )
    }

    /**
     * Fungsi helper: nama topik berdasarkan ID
     */
    fun getNamaTopik(topikId: Int): String = when (topikId) {
        1 -> "Pengenalan Perangkat Digital"
        2 -> "Keamanan Internet"
        3 -> "Etika Digital"
        4 -> "Dunia Online"
        else -> "Literasi Digital"
    }

    /**
     * Fungsi helper: emoji topik berdasarkan ID
     */
    fun getEmojiTopik(topikId: Int): String = when (topikId) {
        1 -> "💻"; 2 -> "🌐"; 3 -> "🤝"; else -> "🌍"
    }
}