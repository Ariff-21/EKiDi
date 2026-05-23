package com.example.ekidi.data.model

data class Soal(
    val pertanyaan: String,
    val emoji: String = "",
    val tipeJawaban: TipeJawaban,
    val pilihanJawaban: List<String> = emptyList(),
    val jawabanBenar: String,
    val penjelasan: String = ""
)

enum class TipeJawaban {
    BENAR_SALAH,      // Level 1
    PILIHAN_EMOJI,    // Level 2
    PILIHAN_TEKS      // Level 3
}

object SoalDatabase {

    // ═══════════════════════════════════════════════
    // TOPIK 1 — Pengenalan Perangkat Digital
    // ═══════════════════════════════════════════════
    val topik1Level1 = listOf(
        Soal("HP adalah perangkat digital yang bisa kita bawa ke mana-mana.",
            "📱", TipeJawaban.BENAR_SALAH, listOf("✅ Benar", "❌ Salah"),
            "✅ Benar", "HP memang perangkat digital yang mudah dibawa!"),
        Soal("Komputer adalah perangkat yang bisa membantu kita belajar dan bekerja.",
            "💻", TipeJawaban.BENAR_SALAH, listOf("✅ Benar", "❌ Salah"),
            "✅ Benar", "Betul! Komputer bisa untuk belajar, bermain, dan bekerja."),
        Soal("Tablet adalah buku biasa yang tidak bisa menyala.",
            "📱", TipeJawaban.BENAR_SALAH, listOf("✅ Benar", "❌ Salah"),
            "❌ Salah", "Tablet adalah perangkat digital yang bisa menyala dan terhubung internet!"),
        Soal("Printer bisa digunakan untuk mencetak gambar dan tulisan.",
            "🖨️", TipeJawaban.BENAR_SALAH, listOf("✅ Benar", "❌ Salah"),
            "✅ Benar", "Betul! Printer fungsinya untuk mencetak."),
        Soal("Mouse digunakan untuk mengetik huruf di komputer.",
            "🖱️", TipeJawaban.BENAR_SALAH, listOf("✅ Benar", "❌ Salah"),
            "❌ Salah", "Mouse untuk menggerakkan kursor, bukan mengetik. Yang untuk mengetik adalah keyboard!")
    )

    val topik1Level2 = listOf(
        Soal("Mana yang termasuk perangkat digital?",
            "", TipeJawaban.PILIHAN_EMOJI,
            listOf("💻 Komputer", "🪑 Kursi", "📚 Buku Biasa", "🌳 Pohon"),
            "💻 Komputer", "Komputer adalah perangkat digital!"),
        Soal("Perangkat mana yang bisa dibawa ke mana-mana?",
            "", TipeJawaban.PILIHAN_EMOJI,
            listOf("🖥️ Komputer Meja", "📱 Smartphone", "🖨️ Printer Besar", "📺 TV Besar"),
            "📱 Smartphone", "Smartphone mudah dibawa karena kecil dan ringan!"),
        Soal("Apa fungsi keyboard?",
            "⌨️", TipeJawaban.PILIHAN_EMOJI,
            listOf("⌨️ Mengetik", "🖱️ Menggerak Kursor", "🔊 Mengeluarkan Suara", "📸 Mengambil Foto"),
            "⌨️ Mengetik", "Keyboard digunakan untuk mengetik huruf dan angka!"),
        Soal("Perangkat mana yang digunakan untuk melihat gambar di komputer?",
            "", TipeJawaban.PILIHAN_EMOJI,
            listOf("⌨️ Keyboard", "🖱️ Mouse", "🖥️ Monitor", "🔌 Kabel"),
            "🖥️ Monitor", "Monitor adalah layar untuk melihat tampilan komputer!"),
        Soal("Apa yang dilakukan printer?",
            "🖨️", TipeJawaban.PILIHAN_EMOJI,
            listOf("🖨️ Mencetak Dokumen", "📷 Memotret", "🎵 Memutar Musik", "🌐 Browsing Internet"),
            "🖨️ Mencetak Dokumen", "Printer fungsinya untuk mencetak dokumen dan gambar!")
    )

    val topik1Level3 = listOf(
        Soal("Apa nama perangkat yang digunakan untuk menggerakkan kursor di layar komputer?",
            "🖱️", TipeJawaban.PILIHAN_TEKS,
            listOf("Keyboard", "Mouse", "Monitor", "Speaker"),
            "Mouse", "Mouse digunakan untuk menggerakkan kursor di layar!"),
        Soal("Perangkat digital mana yang paling tepat untuk anak belajar di sekolah?",
            "🎒", TipeJawaban.PILIHAN_TEKS,
            listOf("TV besar", "Tablet atau laptop", "Mesin cuci", "Kulkas"),
            "Tablet atau laptop", "Tablet dan laptop cocok untuk belajar karena mudah dibawa!"),
        Soal("Apa yang harus dilakukan setelah selesai menggunakan perangkat digital?",
            "💡", TipeJawaban.PILIHAN_TEKS,
            listOf("Dibanting", "Diletakkan sembarangan", "Dimatikan dan disimpan dengan baik", "Dibawa tidur"),
            "Dimatikan dan disimpan dengan baik", "Perangkat digital harus dijaga dengan baik agar tahan lama!"),
        Soal("Siapa yang boleh mengajari kita cara menggunakan perangkat digital dengan benar?",
            "👨‍👧", TipeJawaban.PILIHAN_TEKS,
            listOf("Orang asing di internet", "Siapa saja", "Ayah, Bunda, atau Guru", "Tidak perlu belajar"),
            "Ayah, Bunda, atau Guru", "Orang tua dan guru adalah orang terpercaya untuk mengajari kita!"),
        Soal("Berapa lama waktu yang baik untuk anak menggunakan perangkat digital setiap hari?",
            "⏰", TipeJawaban.PILIHAN_TEKS,
            listOf("Seharian penuh", "1-2 jam saja", "Tidak boleh sama sekali", "Semalam suntuk"),
            "1-2 jam saja", "Anak-anak sebaiknya menggunakan gadget maksimal 1-2 jam per hari!")
    )

    // ═══════════════════════════════════════════════
    // TOPIK 2 — Keamanan Internet
    // ═══════════════════════════════════════════════
    val topik2Level1 = listOf(
        Soal("Kita boleh memberitahu password kita kepada semua orang.",
            "🔐", TipeJawaban.BENAR_SALAH, listOf("✅ Benar", "❌ Salah"),
            "❌ Salah", "Password harus dijaga rahasia! Hanya boleh diberitahu ke Ayah/Bunda."),
        Soal("Kita harus minta izin Ayah atau Bunda sebelum menggunakan internet.",
            "👨‍👧", TipeJawaban.BENAR_SALAH, listOf("✅ Benar", "❌ Salah"),
            "✅ Benar", "Selalu minta izin orang tua sebelum menggunakan internet!"),
        Soal("Boleh bertemu dengan orang yang baru dikenal di internet tanpa izin orang tua.",
            "⚠️", TipeJawaban.BENAR_SALAH, listOf("✅ Benar", "❌ Salah"),
            "❌ Salah", "Tidak boleh! Selalu ceritakan ke orang tua jika ada orang asing yang mengajak bertemu."),
        Soal("Internet bisa digunakan untuk belajar hal-hal baru yang menyenangkan.",
            "🌐", TipeJawaban.BENAR_SALAH, listOf("✅ Benar", "❌ Salah"),
            "✅ Benar", "Betul! Internet banyak konten edukatif yang seru untuk belajar."),
        Soal("Jika menemukan konten yang menakutkan di internet, kita harus diam saja.",
            "😨", TipeJawaban.BENAR_SALAH, listOf("✅ Benar", "❌ Salah"),
            "❌ Salah", "Harus segera cerita ke Ayah atau Bunda!")
    )

    val topik2Level2 = listOf(
        Soal("Apa yang harus dilakukan jika ada orang asing mengirim pesan di internet?",
            "💬", TipeJawaban.PILIHAN_EMOJI,
            listOf("💬 Balas dan ajak bertemu", "🗣️ Cerita ke Ayah/Bunda", "🤫 Diam saja", "😄 Senang-senang"),
            "🗣️ Cerita ke Ayah/Bunda", "Selalu cerita ke orang tua jika ada orang asing yang menghubungi!"),
        Soal("Informasi apa yang TIDAK boleh dibagikan di internet?",
            "⚠️", TipeJawaban.PILIHAN_EMOJI,
            listOf("📍 Alamat Rumah", "🎨 Gambar Buatan Sendiri", "📖 Cerita Lucu", "🎵 Lagu Favorit"),
            "📍 Alamat Rumah", "Alamat rumah adalah informasi pribadi yang tidak boleh dibagikan!"),
        Soal("Kapan waktu yang tepat untuk menggunakan internet?",
            "⏰", TipeJawaban.PILIHAN_EMOJI,
            listOf("🌙 Tengah malam", "☀️ Saat ada izin orang tua", "🏫 Saat ujian sekolah", "🍽️ Saat makan"),
            "☀️ Saat ada izin orang tua", "Gunakan internet saat sudah ada izin dari orang tua!"),
        Soal("Apa yang harus dilakukan jika menemukan konten yang tidak baik?",
            "🚫", TipeJawaban.PILIHAN_EMOJI,
            listOf("👀 Terus ditonton", "📤 Disebarkan ke teman", "❌ Tutup dan lapor ke orang tua", "😂 Ditertawakan"),
            "❌ Tutup dan lapor ke orang tua", "Segera tutup dan lapor ke orang tua!"),
        Soal("Siapa yang harus tahu password akun kita?",
            "🔑", TipeJawaban.PILIHAN_EMOJI,
            listOf("👫 Semua teman", "👨‍👧 Hanya Ayah/Bunda", "🌍 Semua orang", "🤷 Tidak perlu ada yang tahu"),
            "👨‍👧 Hanya Ayah/Bunda", "Password hanya boleh diketahui oleh orang tua kita!")
    )

    val topik2Level3 = listOf(
        Soal("Mengapa kita tidak boleh memberikan alamat rumah kepada orang yang baru dikenal di internet?",
            "🏠", TipeJawaban.PILIHAN_TEKS,
            listOf("Karena tidak sopan", "Karena bisa berbahaya untuk keselamatan kita", "Karena internet lemot", "Tidak ada alasannya"),
            "Karena bisa berbahaya untuk keselamatan kita", "Orang asing bisa menyalahgunakan informasi tersebut!"),
        Soal("Apa yang dimaksud dengan 'jejak digital'?",
            "👣", TipeJawaban.PILIHAN_TEKS,
            listOf("Bekas kaki di pantai", "Semua aktivitas kita yang tersimpan di internet", "Nama pengguna kita", "Foto profil kita"),
            "Semua aktivitas kita yang tersimpan di internet", "Jejak digital adalah rekam jejak semua aktivitas kita di internet!"),
        Soal("Jika ada teman yang mengajak melihat konten dewasa di internet, apa yang harus dilakukan?",
            "🚫", TipeJawaban.PILIHAN_TEKS,
            listOf("Ikut melihat bersama", "Menolak dan lapor ke orang tua atau guru", "Pura-pura tidak tahu", "Menyebarkan ke teman lain"),
            "Menolak dan lapor ke orang tua atau guru", "Selalu tolak ajakan negatif dan cerita ke orang yang dipercaya!"),
        Soal("Berapa lama waktu layar yang direkomendasikan untuk anak usia 6-8 tahun?",
            "⏱️", TipeJawaban.PILIHAN_TEKS,
            listOf("Tidak ada batasan", "Maksimal 1-2 jam per hari", "5 jam per hari", "Semalaman"),
            "Maksimal 1-2 jam per hari", "WHO merekomendasikan maksimal 1-2 jam screen time untuk anak!"),
        Soal("Apa yang sebaiknya dilakukan sebelum mengunduh aplikasi baru?",
            "📲", TipeJawaban.PILIHAN_TEKS,
            listOf("Langsung unduh saja", "Minta izin dan bantuan orang tua", "Tanya teman", "Tidak perlu tanya siapapun"),
            "Minta izin dan bantuan orang tua", "Selalu minta bantuan orang tua sebelum mengunduh aplikasi!")
    )

    // ═══════════════════════════════════════════════
    // TOPIK 3 — Etika Digital
    // ═══════════════════════════════════════════════
    val topik3Level1 = listOf(
        Soal("Boleh menghina atau mengejek teman di internet.",
            "😢", TipeJawaban.BENAR_SALAH, listOf("✅ Benar", "❌ Salah"),
            "❌ Salah", "Tidak boleh! Kita harus tetap sopan di internet seperti di dunia nyata."),
        Soal("Kita harus minta izin sebelum memposting foto teman kita.",
            "📸", TipeJawaban.BENAR_SALAH, listOf("✅ Benar", "❌ Salah"),
            "✅ Benar", "Selalu minta izin sebelum memposting foto orang lain!"),
        Soal("Berbohong di internet tidak apa-apa karena tidak ada yang tahu.",
            "🤥", TipeJawaban.BENAR_SALAH, listOf("✅ Benar", "❌ Salah"),
            "❌ Salah", "Berbohong tetap tidak baik, di mana pun termasuk di internet!"),
        Soal("Kita harus berlaku sopan saat berkomentar di internet.",
            "💬", TipeJawaban.BENAR_SALAH, listOf("✅ Benar", "❌ Salah"),
            "✅ Benar", "Sopan santun berlaku di mana saja, termasuk di internet!"),
        Soal("Menyebarkan berita yang belum tentu benar kepada teman adalah hal yang baik.",
            "📢", TipeJawaban.BENAR_SALAH, listOf("✅ Benar", "❌ Salah"),
            "❌ Salah", "Kita harus cek dulu kebenarannya sebelum menyebarkan informasi!")
    )

    val topik3Level2 = listOf(
        Soal("Bagaimana cara berkomentar yang baik di internet?",
            "💬", TipeJawaban.PILIHAN_EMOJI,
            listOf("😡 Marah-marah", "😊 Sopan dan positif", "😈 Mengejek", "🤐 Tidak berkomentar sama sekali"),
            "😊 Sopan dan positif", "Berkomentar dengan sopan dan positif menciptakan suasana internet yang menyenangkan!"),
        Soal("Apa yang dimaksud dengan cyberbullying?",
            "⚠️", TipeJawaban.PILIHAN_EMOJI,
            listOf("🎮 Bermain game online", "😢 Mengejek/intimidasi di internet", "📧 Mengirim email", "📸 Mengambil foto"),
            "😢 Mengejek/intimidasi di internet", "Cyberbullying adalah perundungan yang terjadi di dunia digital!"),
        Soal("Jika melihat teman di-bully di internet, apa yang harus dilakukan?",
            "🤝", TipeJawaban.PILIHAN_EMOJI,
            listOf("👀 Diam saja", "😂 Ikut menertawakan", "🗣️ Lapor ke orang tua/guru", "📤 Sebarkan lebih lanjut"),
            "🗣️ Lapor ke orang tua/guru", "Bantu teman yang di-bully dengan melaporkan ke orang dewasa!"),
        Soal("Sebelum membagikan informasi di internet, kita harus...",
            "🤔", TipeJawaban.PILIHAN_EMOJI,
            listOf("⚡ Langsung dibagikan", "✅ Pastikan kebenarannya dulu", "😴 Tidak perlu dipikir", "🎲 Tebak-tebakan"),
            "✅ Pastikan kebenarannya dulu", "Selalu cek kebenaran informasi sebelum disebarkan!"),
        Soal("Apa yang harus dilakukan jika tidak sengaja menyakiti perasaan teman di internet?",
            "😔", TipeJawaban.PILIHAN_EMOJI,
            listOf("🏃 Kabur/hapus akun", "🙏 Minta maaf dengan tulus", "😒 Pura-pura tidak tahu", "😡 Menyalahkan orang lain"),
            "🙏 Minta maaf dengan tulus", "Berani minta maaf adalah tanda karakter yang baik!")
    )

    val topik3Level3 = listOf(
        Soal("Mengapa penting untuk berlaku sopan di internet?",
            "🌐", TipeJawaban.PILIHAN_TEKS,
            listOf("Agar terlihat pintar", "Karena perasaan orang lain tetap bisa terluka meski di dunia maya", "Karena ada hadiah", "Tidak ada alasannya"),
            "Karena perasaan orang lain tetap bisa terluka meski di dunia maya", "Perasaan seseorang bisa terluka baik di dunia nyata maupun maya!"),
        Soal("Apa itu hoaks?",
            "📰", TipeJawaban.PILIHAN_TEKS,
            listOf("Berita yang benar dan terpercaya", "Informasi palsu atau tidak benar", "Nama aplikasi media sosial", "Jenis permainan online"),
            "Informasi palsu atau tidak benar", "Hoaks adalah berita bohong yang harus kita waspadai!"),
        Soal("Bagaimana cara mengetahui apakah sebuah informasi di internet itu benar?",
            "🔍", TipeJawaban.PILIHAN_TEKS,
            listOf("Langsung percaya saja", "Tanya orang tua atau cari dari sumber terpercaya", "Tanya teman", "Tidak perlu dicek"),
            "Tanya orang tua atau cari dari sumber terpercaya", "Selalu verifikasi informasi dari sumber terpercaya!"),
        Soal("Apa yang dimaksud dengan hak cipta dalam konten digital?",
            "©️", TipeJawaban.PILIHAN_TEKS,
            listOf("Bebas menyalin karya orang lain", "Hak pencipta atas karya yang dibuatnya", "Aturan bermain game", "Cara membuat akun"),
            "Hak pencipta atas karya yang dibuatnya", "Hak cipta melindungi karya seseorang dari penggunaan tanpa izin!"),
        Soal("Jika menerima berita mengejutkan di grup chat, apa yang sebaiknya dilakukan?",
            "💬", TipeJawaban.PILIHAN_TEKS,
            listOf("Langsung sebarkan ke semua orang", "Cek kebenarannya dulu sebelum menyebarkan", "Abaikan saja", "Hapus grupnya"),
            "Cek kebenarannya dulu sebelum menyebarkan", "Stop, think, check sebelum menyebarkan informasi!")
    )

    // ═══════════════════════════════════════════════
    // TOPIK 4 — Dunia Online
    // ═══════════════════════════════════════════════
    val topik4Level1 = listOf(
        Soal("Internet bisa digunakan untuk belajar hal baru yang menyenangkan.",
            "🌐", TipeJawaban.BENAR_SALAH, listOf("✅ Benar", "❌ Salah"),
            "✅ Benar", "Internet punya banyak konten edukatif yang seru!"),
        Soal("Semua informasi yang ada di internet pasti benar.",
            "⚠️", TipeJawaban.BENAR_SALAH, listOf("✅ Benar", "❌ Salah"),
            "❌ Salah", "Tidak semua informasi di internet benar. Kita harus berhati-hati!"),
        Soal("Media sosial adalah tempat untuk berbagi foto dan cerita dengan teman.",
            "📱", TipeJawaban.BENAR_SALAH, listOf("✅ Benar", "❌ Salah"),
            "✅ Benar", "Media sosial memang untuk berbagi dan berkomunikasi dengan orang lain!"),
        Soal("Anak-anak boleh menggunakan media sosial tanpa pengawasan orang tua.",
            "👨‍👧", TipeJawaban.BENAR_SALAH, listOf("✅ Benar", "❌ Salah"),
            "❌ Salah", "Anak-anak sebaiknya selalu didampingi orang tua saat berinternet!"),
        Soal("Bermain game online terlalu lama bisa mempengaruhi kesehatan mata.",
            "👁️", TipeJawaban.BENAR_SALAH, listOf("✅ Benar", "❌ Salah"),
            "✅ Benar", "Terlalu lama menatap layar bisa membuat mata lelah dan menurun kualitasnya!")
    )

    val topik4Level2 = listOf(
        Soal("Apa manfaat internet untuk belajar?",
            "📚", TipeJawaban.PILIHAN_EMOJI,
            listOf("📚 Bisa akses banyak ilmu", "😴 Membuat mengantuk", "🏃 Membuat malas gerak", "😡 Membuat emosi"),
            "📚 Bisa akses banyak ilmu", "Internet memudahkan kita mengakses berbagai ilmu pengetahuan!"),
        Soal("Berapa batas waktu yang baik main gadget untuk anak setiap hari?",
            "⏰", TipeJawaban.PILIHAN_EMOJI,
            listOf("⏰ 1-2 jam", "🌙 Sampai tengah malam", "☀️ Seharian penuh", "⚡ Tidak terbatas"),
            "⏰ 1-2 jam", "Batas waktu 1-2 jam per hari baik untuk kesehatan mata dan tubuh!"),
        Soal("Apa yang sebaiknya dilakukan setelah lama main gadget?",
            "👁️", TipeJawaban.PILIHAN_EMOJI,
            listOf("📺 Nonton TV", "🏃 Istirahat dan bermain di luar", "📱 Ganti HP lain", "😴 Langsung tidur dengan HP"),
            "🏃 Istirahat dan bermain di luar", "Istirahat dan bermain di luar baik untuk mata dan tubuh!"),
        Soal("Konten apa yang baik untuk ditonton anak-anak di internet?",
            "🎬", TipeJawaban.PILIHAN_EMOJI,
            listOf("🎓 Video edukasi dan pembelajaran", "👻 Konten menakutkan", "🔞 Konten dewasa", "😈 Konten kekerasan"),
            "🎓 Video edukasi dan pembelajaran", "Pilih konten yang mendidik dan sesuai usia!"),
        Soal("Apa yang harus dilakukan jika mata sudah lelah karena gadget?",
            "😵", TipeJawaban.PILIHAN_EMOJI,
            listOf("👁️ Istirahatkan mata", "📱 Terus main gadget", "😎 Pakai kacamata hitam", "💊 Minum obat"),
            "👁️ Istirahatkan mata", "Istirahatkan mata dengan melihat benda jauh atau menutup mata sejenak!")
    )

    val topik4Level3 = listOf(
        Soal("Apa dampak positif penggunaan internet yang bijak bagi anak?",
            "🌟", TipeJawaban.PILIHAN_TEKS,
            listOf("Membuat malas belajar", "Menambah wawasan dan memudahkan belajar", "Membuat tidak punya teman", "Merusak kesehatan"),
            "Menambah wawasan dan memudahkan belajar", "Internet yang digunakan dengan bijak bisa sangat membantu belajar!"),
        Soal("Mengapa penting untuk tidak menghabiskan semua waktu dengan gadget?",
            "⚖️", TipeJawaban.PILIHAN_TEKS,
            listOf("Karena gadget mahal", "Karena perlu waktu untuk bermain, belajar, dan bersosialisasi", "Karena baterai cepat habis", "Tidak ada alasannya"),
            "Karena perlu waktu untuk bermain, belajar, dan bersosialisasi", "Keseimbangan antara dunia digital dan nyata sangat penting!"),
        Soal("Apa yang dimaksud dengan 'screen time'?",
            "📺", TipeJawaban.PILIHAN_TEKS,
            listOf("Waktu tidur", "Waktu yang dihabiskan di depan layar digital", "Nama aplikasi", "Jenis game"),
            "Waktu yang dihabiskan di depan layar digital", "Screen time adalah total waktu kita di depan layar gadget, TV, dll!"),
        Soal("Mengapa anak-anak perlu didampingi orang tua saat berinternet?",
            "👨‍👧", TipeJawaban.PILIHAN_TEKS,
            listOf("Karena anak tidak pintar", "Agar aman dari konten berbahaya dan belajar penggunaan yang baik", "Karena internet susah dipakai", "Tidak perlu alasan"),
            "Agar aman dari konten berbahaya dan belajar penggunaan yang baik", "Pendampingan orang tua memastikan anak berinternet dengan aman!"),
        Soal("Bagaimana cara menjadi pengguna internet yang bertanggung jawab?",
            "🎖️", TipeJawaban.PILIHAN_TEKS,
            listOf("Gunakan internet sesuka hati", "Ikuti aturan, batasi waktu, dan selalu jujur", "Sembunyikan aktivitas dari orang tua", "Hanya main game saja"),
            "Ikuti aturan, batasi waktu, dan selalu jujur", "Pengguna internet yang baik mengikuti aturan dan bersikap jujur!")
    )

    // ─── Fungsi ambil soal berdasarkan topik & level ────────────
    fun getSoal(topikId: Int, level: Int): List<Soal> {
        return when (topikId) {
            1 -> when (level) { 1 -> topik1Level1; 2 -> topik1Level2; else -> topik1Level3 }
            2 -> when (level) { 1 -> topik2Level1; 2 -> topik2Level2; else -> topik2Level3 }
            3 -> when (level) { 1 -> topik3Level1; 2 -> topik3Level2; else -> topik3Level3 }
            4 -> when (level) { 1 -> topik4Level1; 2 -> topik4Level2; else -> topik4Level3 }
            else -> topik1Level1
        }
    }
}