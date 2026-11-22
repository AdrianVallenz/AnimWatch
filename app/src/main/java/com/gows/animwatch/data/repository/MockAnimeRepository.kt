package com.gows.animwatch.data.repository

import com.gows.animwatch.data.model.Anime
import com.gows.animwatch.data.model.AnimeArtTheme
import com.gows.animwatch.data.model.AnimeStatus
import com.gows.animwatch.data.model.AnimeType
import com.gows.animwatch.data.model.CharacterRole
import com.gows.animwatch.data.model.CommentItem
import com.gows.animwatch.data.model.DownloadStatus
import com.gows.animwatch.data.model.DownloadTask
import com.gows.animwatch.data.model.Episode
import com.gows.animwatch.data.model.NotificationItem
import com.gows.animwatch.data.model.PlaybackSettings
import com.gows.animwatch.data.model.ReleaseDay
import com.gows.animwatch.data.model.StreamServer
import com.gows.animwatch.data.model.SubtitleCue
import com.gows.animwatch.data.model.SubtitleTrack
import com.gows.animwatch.data.model.UserProfile
import com.gows.animwatch.data.model.VideoQuality
import com.gows.animwatch.data.model.WatchProgress
import com.gows.animwatch.data.model.WatchlistCategory

class MockAnimeRepository : AnimeRepository {

    private val standardServers = listOf(
        StreamServer("sanka", "SankaStream", "SG-VIP", latencyMs = 24, isRecommended = true),
        StreamServer("kuro", "KuroCDN", "ID-JKT", latencyMs = 18, isRecommended = false),
        StreamServer("gdrive", "DriveMirror", "Global", latencyMs = 42, isRecommended = false),
        StreamServer("vidfast", "VidFast HLS", "SEA-Edge", latencyMs = 31, isRecommended = false)
    )

    private val standardQualities = listOf(
        VideoQuality("1080p", "1920×1080 FHD", sizeEstimateMb = 340, isUltraHd = true),
        VideoQuality("720p", "1280×720 HD", sizeEstimateMb = 195, isUltraHd = false),
        VideoQuality("480p", "854×480 SD", sizeEstimateMb = 110, isUltraHd = false),
        VideoQuality("360p", "640×360 Hemat", sizeEstimateMb = 68, isUltraHd = false)
    )

    private val standardSubtitles = listOf(
        SubtitleTrack("id", "Bahasa Indonesia", "ID"),
        SubtitleTrack("en", "English", "EN"),
        SubtitleTrack("off", "Nonaktif", "OFF")
    )

    private fun buildEpisodes(
        animeId: String,
        count: Int,
        durationMin: Int,
        titles: List<String>
    ): List<Episode> {
        return (1..count).map { epNum ->
            val epTitle = titles.getOrElse(epNum - 1) {
                "Bab $epNum: Gema Takdir di Ambang Fajar"
            }
            Episode(
                id = "${animeId}_ep_$epNum",
                animeId = animeId,
                number = epNum,
                title = epTitle,
                durationSeconds = durationMin * 60,
                releaseDate = if (epNum == count) "Hari Ini" else "${(count - epNum) * 7} hari lalu",
                synopsis = "Ketegangan meningkat saat rahasia kuno mulai terungkap. Pertarungan penentuan menentukan nasib seluruh aliansi.",
                introStartSec = 15,
                introEndSec = 85,
                isFiller = epNum == 7 && count > 10,
                servers = standardServers,
                qualities = standardQualities,
                subtitles = standardSubtitles
            )
        }
    }

    private val animeCatalog: List<Anime> by lazy {
        listOf(
            Anime(
                id = "kage-no-ou",
                slug = "kage-no-ou-monarch",
                title = "Kage no Ou: Kebangkitan Raja Bayangan",
                japaneseTitle = "影の王：覚醒の章",
                synopsis = "Di dunia di mana gerbang dimensi terbuka secara misterius, seorang pemburu peringkat terendah bernama Arata memperoleh kekuatan sistem terlarang yang memungkinkannya membangkitkan pasukan bayangan. Kini ia harus menembus labirin terdalam demi melindungi kota terakhir umat manusia.",
                rating = 9.4,
                ratingCountLabel = "142K",
                totalEpisodes = 24,
                releasedEpisodes = 18,
                durationMinutes = 24,
                year = 2026,
                season = "Fall 2026",
                studio = "Ufotable Studio",
                status = AnimeStatus.ONGOING,
                type = AnimeType.TV_SERIES,
                ageRating = "17+",
                genres = listOf("Action", "Fantasy", "Dark Fantasy", "Supernatural"),
                releaseDay = ReleaseDay.SABTU,
                releaseTimeWib = "22:00 WIB",
                viewsLabel = "4.8M",
                popularityRank = 1,
                isFeatured = true,
                isTrending = true,
                isNewRelease = true,
                artTheme = AnimeArtTheme.ABYSSAL_SHADOW,
                posterTag = "EKSKLUSIF",
                characters = listOf(
                    CharacterRole("Arata Kurosawa", "Protagonis", "Kaito Ishikawa", "A"),
                    CharacterRole("Shizuka Amamiya", "Wakil Komandan", "Saori Hayami", "S"),
                    CharacterRole("Igris Shadow", "Ksatria Bayangan", "Kenjiro Tsuda", "I"),
                    CharacterRole("Ketua Go", "Grandmaster Guild", "Akio Otsuka", "G")
                ),
                relatedIds = listOf("onigiri-blade", "chrono-requiem", "eclipse-alchemist"),
                episodes = buildEpisodes(
                    "kage-no-ou", 18, 24,
                    listOf(
                        "Pemburu Peringkat E",
                        "Kuil Ganda yang Terlupakan",
                        "Sistem Tersembunyi",
                        "Penjaga Gerbang Merah",
                        "Kontrak dengan Kegelapan",
                        "Pasukan Seratus Bayangan",
                        "Istirahat Sebelum Badai",
                        "Kaisar Darah dari Utara",
                        "Bangkitlah, Ksatria Hitam",
                        "Dendam Menara Emas",
                        "Sumpah di Bawah Bulan Sabit",
                        "Ujian Raja Bayangan",
                        "Retaknya Segel Abyss",
                        "Duel Dua Monarki",
                        "Gema Suara dari Masa Lalu",
                        "Benteng Langit Runtuh",
                        "Tahta Tanpa Mahkota",
                        "Puncak Takdir: Pedang Hitam"
                    )
                )
            ),
            Anime(
                id = "onigiri-blade",
                slug = "kijin-no-yaiba-crimson",
                title = "Kijin no Yaiba: Api Abadi di Puncak Salju",
                japaneseTitle = "鬼刃の伝説・雪炎編",
                synopsis = "Berlatar era Taisho yang penuh kabut dan iblis malam, pendekar muda Renji membawa pedang bermandikan api matahari untuk memburu dua belas jenderal malam bersama korps pemburu bayangan.",
                rating = 9.2,
                ratingCountLabel = "210K",
                totalEpisodes = 12,
                releasedEpisodes = 12,
                durationMinutes = 24,
                year = 2026,
                season = "Summer 2026",
                studio = "Kyoto Blade Works",
                status = AnimeStatus.COMPLETED,
                type = AnimeType.TV_SERIES,
                ageRating = "17+",
                genres = listOf("Action", "Historical", "Supernatural", "Drama"),
                releaseDay = ReleaseDay.MINGGU,
                releaseTimeWib = "21:30 WIB",
                viewsLabel = "6.2M",
                popularityRank = 2,
                isFeatured = true,
                isTrending = true,
                isNewRelease = false,
                artTheme = AnimeArtTheme.CRIMSON_BLADE,
                posterTag = "TOP RATED",
                characters = listOf(
                    CharacterRole("Renji Kamado", "Pendekar Api", "Natsuki Hanae", "R"),
                    CharacterRole("Yukina Hoshino", "Hashira Salju", "Rie Takahashi", "Y"),
                    CharacterRole("Kagura Akatsuki", "Iblis Bulan Atas", "Mamoru Miyano", "K")
                ),
                relatedIds = listOf("kage-no-ou", "sakura-wind-chronicles"),
                episodes = buildEpisodes(
                    "onigiri-blade", 12, 24,
                    listOf(
                        "Salju Merah di Lembah",
                        "Pernapasan Cahaya Pertama",
                        "Ujian Gunung Kabut",
                        "Lentera di Kuil Tua",
                        "Tarian Bilah Surya",
                        "Jeritan di Kereta Malam",
                        "Ikatan Saudara yang Teruji",
                        "Bayangan Jenderal Ketiga",
                        "Kobaran Api Tanpa Batas",
                        "Langit Terbelah Dua",
                        "Pengorbanan Terakhir",
                        "Fajar Setelah Malam Panjang"
                    )
                )
            ),
            Anime(
                id = "astral-frieren",
                slug = "hoshi-no-tabibito",
                title = "Hoshi no Tabibito: Perjalanan Melampaui Bintang",
                japaneseTitle = "星の旅人・悠久の記憶",
                synopsis = "Setelah mengalahkan Raja Iblis bersama rombongan pahlawan, penyihir elf abadi bernama Selena menyadari singkatnya usia manusia. Ia memulai perjalanan baru menyusuri benua utara demi memahami makna kenangan dan waktu.",
                rating = 9.5,
                ratingCountLabel = "185K",
                totalEpisodes = 28,
                releasedEpisodes = 28,
                durationMinutes = 24,
                year = 2025,
                season = "Spring 2025",
                studio = "Madhouse Astral",
                status = AnimeStatus.COMPLETED,
                type = AnimeType.TV_SERIES,
                ageRating = "13+",
                genres = listOf("Fantasy", "Adventure", "Drama", "Slice of Life"),
                releaseDay = ReleaseDay.JUMAT,
                releaseTimeWib = "23:00 WIB",
                viewsLabel = "5.4M",
                popularityRank = 3,
                isFeatured = true,
                isTrending = true,
                isNewRelease = false,
                artTheme = AnimeArtTheme.CELESTIAL_ASTRAL,
                posterTag = "MASTERPIECE",
                characters = listOf(
                    CharacterRole("Selena Aurelia", "Penyihir Elf", "Atsumi Tanezaki", "S"),
                    CharacterRole("Fernanda", "Murid Penyihir", "Kana Ichinose", "F"),
                    CharacterRole("Valken", "Ksatria Muda", "Chiaki Kobayashi", "V")
                ),
                relatedIds = listOf("chrono-requiem", "eclipse-alchemist"),
                episodes = buildEpisodes(
                    "astral-frieren", 14, 24,
                    listOf(
                        "Akhir Sebuah Petualangan",
                        "Bunga Biru dari Utara",
                        "Surat dari Sahabat Lama",
                        "Grimoire Bintang Jatuh",
                        "Ujian Penyihir Kelas Satu",
                        "Kota di Atas Awan",
                        "Kepingan Waktu yang Hilang"
                    )
                )
            ),
            Anime(
                id = "cyber-zero",
                slug = "zero-protocol-edge",
                title = "Zero Protocol: Neo-Tokyo 2099",
                japaneseTitle = "零式プロトコル 2099",
                synopsis = "Di kota metropolitan bertingkat yang dikuasai korporasi sibernetika, seorang peretas bayangan menemukan chip memori eksperimental yang menyimpan kesadaran AI pertama yang memiliki emosi manusia.",
                rating = 8.9,
                ratingCountLabel = "94K",
                totalEpisodes = 12,
                releasedEpisodes = 9,
                durationMinutes = 24,
                year = 2026,
                season = "Fall 2026",
                studio = "Trigger Cyber",
                status = AnimeStatus.ONGOING,
                type = AnimeType.TV_SERIES,
                ageRating = "17+",
                genres = listOf("Sci-Fi", "Action", "Mecha", "Thriller"),
                releaseDay = ReleaseDay.KAMIS,
                releaseTimeWib = "20:00 WIB",
                viewsLabel = "2.9M",
                popularityRank = 4,
                isFeatured = true,
                isTrending = true,
                isNewRelease = true,
                artTheme = AnimeArtTheme.CYBER_PROTOCOL,
                posterTag = "BARU EP 9",
                characters = listOf(
                    CharacterRole("Kaito Vance", "Netrunner", "Yuma Uchida", "K"),
                    CharacterRole("E.V.E-09", "AI Sintetis", "Inori Minase", "E")
                ),
                relatedIds = listOf("chrono-requiem", "kage-no-ou"),
                episodes = buildEpisodes(
                    "cyber-zero", 9, 24,
                    listOf(
                        "Sinyal dari Sektor 7",
                        "Protokol Tanpa Nama",
                        "Hujan Neon di Shibuya",
                        "Pencurian Memori Emas",
                        "Sistem Pertahanan Orbital",
                        "Identitas Palsu",
                        "Koneksi Terputus",
                        "Overclock",
                        "Gerbang Jaringan Pusat"
                    )
                )
            ),
            Anime(
                id = "eclipse-alchemist",
                slug = "imperial-alchemist-gold",
                title = "Sang Alkemis Kekaisaran Emas",
                japaneseTitle = "黄金帝国の錬金術師",
                synopsis = "Dua bersaudara alkemis muda melanggar hukum pertukaran setara dan memicu konspirasi besar di jantung Kekaisaran Amestris Emas. Kini mereka memburu Batu Bertuah demi mengembalikan tubuh mereka.",
                rating = 9.1,
                ratingCountLabel = "138K",
                totalEpisodes = 24,
                releasedEpisodes = 24,
                durationMinutes = 24,
                year = 2025,
                season = "Winter 2025",
                studio = "Bones Sovereign",
                status = AnimeStatus.COMPLETED,
                type = AnimeType.TV_SERIES,
                ageRating = "13+",
                genres = listOf("Action", "Adventure", "Fantasy", "Mystery"),
                releaseDay = ReleaseDay.RABU,
                releaseTimeWib = "19:30 WIB",
                viewsLabel = "4.1M",
                popularityRank = 5,
                isTrending = true,
                artTheme = AnimeArtTheme.IMPERIAL_ALCHEMY,
                posterTag = "KLASIK EMAS",
                characters = listOf(
                    CharacterRole("Edward Aurelius", "Alkemis Baja", "Romi Park", "E"),
                    CharacterRole("Alphonse", "Zirah Hidup", "Rie Kugimiya", "A")
                ),
                relatedIds = listOf("astral-frieren", "kage-no-ou"),
                episodes = buildEpisodes("eclipse-alchemist", 12, 24, emptyList())
            ),
            Anime(
                id = "sakura-wind-chronicles",
                slug = "sakura-kaze-monogatari",
                title = "Sakura Kaze: Janji di Stasiun Senja",
                japaneseTitle = "桜風・夕暮れの約束",
                synopsis = "Kisah hangat tentang Haruto, pianis muda yang kehilangan pendengaran emosional terhadap musiknya, hingga bertemu Aoi—seorang komposer misterius di sebuah kota pesisir yang dipenuhi bunga sakura.",
                rating = 8.8,
                ratingCountLabel = "76K",
                totalEpisodes = 12,
                releasedEpisodes = 8,
                durationMinutes = 24,
                year = 2026,
                season = "Fall 2026",
                studio = "CoMix Wave Arts",
                status = AnimeStatus.ONGOING,
                type = AnimeType.TV_SERIES,
                ageRating = "13+",
                genres = listOf("Romance", "Drama", "Slice of Life", "Music"),
                releaseDay = ReleaseDay.SENIN,
                releaseTimeWib = "18:30 WIB",
                viewsLabel = "1.9M",
                popularityRank = 6,
                isNewRelease = true,
                artTheme = AnimeArtTheme.SAKURA_TWILIGHT,
                posterTag = "ROMANCE",
                characters = listOf(
                    CharacterRole("Haruto Mizuki", "Pianis", "Koki Uchiyama", "H"),
                    CharacterRole("Aoi Tachibana", "Komposer", "Reina Ueda", "A")
                ),
                relatedIds = listOf("astral-frieren", "silent-sky-movie"),
                episodes = buildEpisodes(
                    "sakura-wind-chronicles", 8, 24,
                    listOf(
                        "Nada yang Terhenti",
                        "Kereta Terakhir Pukul Enam",
                        "Partitur di Bawah Sakura",
                        "Festival Musim Semi",
                        "Rahasia Ruang Musik Lama",
                        "Hujan di Peron Empat",
                        "Simfoni Pertama Kita",
                        "Janji Sebelum Musim Gugur"
                    )
                )
            ),
            Anime(
                id = "phantom-voyage",
                slug = "grand-corsair-sea",
                title = "Grand Corsair: Samudra Harta Karun",
                japaneseTitle = "大海賊・黄金航路",
                synopsis = "Mengikuti kru bajak laut Topi Emas melintasi lautan badai yang belum pernah dipetakan demi menemukan pulau legendaris dan mengungkap sejarah dunia yang disembunyikan Pemerintah Laut.",
                rating = 9.0,
                ratingCountLabel = "310K",
                totalEpisodes = 50,
                releasedEpisodes = 36,
                durationMinutes = 24,
                year = 2026,
                season = "Fall 2026",
                studio = "Toei Horizon",
                status = AnimeStatus.ONGOING,
                type = AnimeType.TV_SERIES,
                ageRating = "13+",
                genres = listOf("Adventure", "Action", "Comedy", "Fantasy"),
                releaseDay = ReleaseDay.MINGGU,
                releaseTimeWib = "09:30 WIB",
                viewsLabel = "8.1M",
                popularityRank = 7,
                isTrending = true,
                isNewRelease = true,
                artTheme = AnimeArtTheme.PHANTOM_VOYAGE,
                posterTag = "POPULER",
                characters = listOf(
                    CharacterRole("Luffy D. Aurel", "Kapten", "Mayumi Tanaka", "L"),
                    CharacterRole("Zoro Kuro", "Pendekar 3 Pedang", "Kazuya Nakai", "Z")
                ),
                relatedIds = listOf("onigiri-blade", "eclipse-alchemist"),
                episodes = buildEpisodes("phantom-voyage", 16, 24, emptyList())
            ),
            Anime(
                id = "chrono-requiem",
                slug = "chrono-requiem-gate",
                title = "Chrono Requiem: Gerbang Waktu ke-0",
                japaneseTitle = "クロノ・レクイエム",
                synopsis = "Sekelompok ilmuwan amatir di Akihabara secara tidak sengaja menciptakan mesin pengirim pesan ke masa lalu. Namun setiap perubahan garis waktu menuntut harga nyawa yang mengerikan.",
                rating = 9.1,
                ratingCountLabel = "112K",
                totalEpisodes = 12,
                releasedEpisodes = 10,
                durationMinutes = 24,
                year = 2026,
                season = "Fall 2026",
                studio = "White Fox Prime",
                status = AnimeStatus.ONGOING,
                type = AnimeType.TV_SERIES,
                ageRating = "17+",
                genres = listOf("Sci-Fi", "Thriller", "Mystery", "Drama"),
                releaseDay = ReleaseDay.SELASA,
                releaseTimeWib = "22:30 WIB",
                viewsLabel = "2.5M",
                popularityRank = 8,
                isNewRelease = true,
                artTheme = AnimeArtTheme.CHRONO_SANCTUM,
                posterTag = "MIND-BENDING",
                characters = listOf(
                    CharacterRole("Okabe Rintaro", "Ilmuwan Gila", "Mamoru Miyano", "O"),
                    CharacterRole("Kurisu Makise", "Peneliti Neuro", "Asami Imai", "K")
                ),
                relatedIds = listOf("cyber-zero", "astral-frieren"),
                episodes = buildEpisodes("chrono-requiem", 10, 24, emptyList())
            ),
            Anime(
                id = "isekai-sovereign",
                slug = "maou-gakuin-reborn",
                title = "Reinkarnasi Kaisar Iblis di Akademi Sihir",
                japaneseTitle = "魔王学院の黄金再臨",
                synopsis = "Setelah tidur selama dua ribu tahun, Raja Iblis terkuat bereinkarnasi sebagai siswa biasa di akademi sihir elit yang ternyata telah melupakan sejarah kekuasaannya yang sebenarnya.",
                rating = 8.4,
                ratingCountLabel = "89K",
                totalEpisodes = 12,
                releasedEpisodes = 11,
                durationMinutes = 24,
                year = 2026,
                season = "Fall 2026",
                studio = "Silver Link Gold",
                status = AnimeStatus.ONGOING,
                type = AnimeType.TV_SERIES,
                ageRating = "13+",
                genres = listOf("Isekai", "Fantasy", "Action", "Comedy"),
                releaseDay = ReleaseDay.RABU,
                releaseTimeWib = "21:00 WIB",
                viewsLabel = "3.4M",
                popularityRank = 9,
                isNewRelease = true,
                artTheme = AnimeArtTheme.IMPERIAL_ALCHEMY,
                posterTag = "ISEKAI HITS",
                characters = listOf(
                    CharacterRole("Anos Sovereign", "Kaisar Iblis", "Yuichiro Umehara", "A"),
                    CharacterRole("Misha Necron", "Penyihir Es", "Tomori Kusunoki", "M")
                ),
                relatedIds = listOf("kage-no-ou", "astral-frieren"),
                episodes = buildEpisodes("isekai-sovereign", 11, 24, emptyList())
            ),
            Anime(
                id = "silent-sky-movie",
                slug = "tenki-no-kane-movie",
                title = "Tenki no Kane: Lonceng di Balik Awan (Movie)",
                japaneseTitle = "天気の鐘・劇場版",
                synopsis = "Film animasi layar lebar tentang seorang gadis pengendali cuaca dan remaja pelarian di Tokyo yang menghadapi hujan tak berujung selama seratus hari.",
                rating = 9.3,
                ratingCountLabel = "164K",
                totalEpisodes = 1,
                releasedEpisodes = 1,
                durationMinutes = 114,
                year = 2025,
                season = "Movie 2025",
                studio = "CoMix Wave Arts",
                status = AnimeStatus.COMPLETED,
                type = AnimeType.MOVIE,
                ageRating = "13+",
                genres = listOf("Romance", "Fantasy", "Drama", "Slice of Life"),
                releaseDay = null,
                releaseTimeWib = null,
                viewsLabel = "5.0M",
                popularityRank = 10,
                isTrending = true,
                artTheme = AnimeArtTheme.SAKURA_TWILIGHT,
                posterTag = "MOVIE 4K",
                characters = listOf(
                    CharacterRole("Hodaka Morishima", "Pelajar", "Kotaro Daigo", "H"),
                    CharacterRole("Hina Amano", "Gadis Matahari", "Nana Mori", "H")
                ),
                relatedIds = listOf("sakura-wind-chronicles", "astral-frieren"),
                episodes = listOf(
                    Episode(
                        id = "silent-sky-movie_ep_1",
                        animeId = "silent-sky-movie",
                        number = 1,
                        title = "Full Movie — Lonceng di Balik Awan (Director's Cut)",
                        durationSeconds = 114 * 60,
                        releaseDate = "Tersedia",
                        synopsis = "Edisi penuh layar lebar dengan audio sinematik dan subtitle Bahasa Indonesia.",
                        introStartSec = 45,
                        introEndSec = 135,
                        servers = standardServers,
                        qualities = standardQualities,
                        subtitles = standardSubtitles
                    )
                )
            ),
            Anime(
                id = "eclipse-mecha-movie",
                slug = "valkyrie-orbital-movie",
                title = "Valkyrie Orbital: Serangan Balik Bulan (Movie)",
                japaneseTitle = "劇場版 軌道戦姫ヴァルキリー",
                synopsis = "Pertempuran kolosal di orbit rendah Bumi ketika armada mecha otonom mengancam menghancurkan koloni antariksa manusia.",
                rating = 8.7,
                ratingCountLabel = "61K",
                totalEpisodes = 1,
                releasedEpisodes = 1,
                durationMinutes = 108,
                year = 2026,
                season = "Movie 2026",
                studio = "Sunrise Imperial",
                status = AnimeStatus.COMPLETED,
                type = AnimeType.MOVIE,
                ageRating = "13+",
                genres = listOf("Mecha", "Sci-Fi", "Action"),
                releaseDay = null,
                releaseTimeWib = null,
                viewsLabel = "1.7M",
                popularityRank = 11,
                artTheme = AnimeArtTheme.CYBER_PROTOCOL,
                posterTag = "IMAX AUDIO",
                characters = listOf(
                    CharacterRole("Sora Takagi", "Pilot Unit-01", "Yoshitsugu Matsuoka", "S")
                ),
                relatedIds = listOf("cyber-zero", "kage-no-ou"),
                episodes = listOf(
                    Episode(
                        id = "eclipse-mecha-movie_ep_1",
                        animeId = "eclipse-mecha-movie",
                        number = 1,
                        title = "Full Movie — Serangan Balik Bulan",
                        durationSeconds = 108 * 60,
                        releaseDate = "Tersedia",
                        synopsis = "Pertempuran puncak antariksa dengan efek visual memukau.",
                        servers = standardServers,
                        qualities = standardQualities,
                        subtitles = standardSubtitles
                    )
                )
            ),
            Anime(
                id = "blue-court-ace",
                slug = "blue-court-striker",
                title = "Golden Striker: Penjaga Garis Gawang",
                japaneseTitle = "黄金のストライカー",
                synopsis = "Tiga ratus penyerang muda terbaik Jepang dikurung dalam fasilitas pelatihan ekstrem untuk melahirkan satu striker egois terhebat yang akan membawa negaranya ke panggung dunia.",
                rating = 8.8,
                ratingCountLabel = "128K",
                totalEpisodes = 24,
                releasedEpisodes = 15,
                durationMinutes = 24,
                year = 2026,
                season = "Fall 2026",
                studio = "Eight Bit Arena",
                status = AnimeStatus.ONGOING,
                type = AnimeType.TV_SERIES,
                ageRating = "13+",
                genres = listOf("Sports", "Drama", "Action"),
                releaseDay = ReleaseDay.SABTU,
                releaseTimeWib = "23:30 WIB",
                viewsLabel = "3.7M",
                popularityRank = 12,
                isNewRelease = true,
                artTheme = AnimeArtTheme.PHANTOM_VOYAGE,
                posterTag = "SPORTS",
                characters = listOf(
                    CharacterRole("Yoichi Kisaragi", "Striker #299", "Kazuki Ura", "Y"),
                    CharacterRole("Rin Saejima", "Playmaker", "Koki Uchiyama", "R")
                ),
                relatedIds = listOf("kage-no-ou", "onigiri-blade"),
                episodes = buildEpisodes("blue-court-ace", 15, 24, emptyList())
            ),
            Anime(
                id = "shrine-detective",
                slug = "kuil-misteri-kyoto",
                title = "Detektif Kuil Senja: Kasus Lentera Hitam",
                japaneseTitle = "黄昏神社の探偵録",
                synopsis = "Di sudut tersembunyi kota Kyoto, seorang mantan mahasiswa sastra dan roh rubah emas memecahkan misteri supranatural yang tidak bisa disentuh oleh kepolisian.",
                rating = 8.5,
                ratingCountLabel = "52K",
                totalEpisodes = 12,
                releasedEpisodes = 7,
                durationMinutes = 24,
                year = 2026,
                season = "Fall 2026",
                studio = "CloverWorks Noir",
                status = AnimeStatus.ONGOING,
                type = AnimeType.TV_SERIES,
                ageRating = "13+",
                genres = listOf("Mystery", "Supernatural", "Historical", "Thriller"),
                releaseDay = ReleaseDay.JUMAT,
                releaseTimeWib = "20:30 WIB",
                viewsLabel = "1.4M",
                popularityRank = 13,
                artTheme = AnimeArtTheme.CRIMSON_BLADE,
                posterTag = "MISTERI",
                characters = listOf(
                    CharacterRole("Renji Aoi", "Detektif", "Tomoaki Maeno", "R")
                ),
                relatedIds = listOf("chrono-requiem", "onigiri-blade"),
                episodes = buildEpisodes("shrine-detective", 7, 24, emptyList())
            ),
            Anime(
                id = "dungeon-bistro",
                slug = "bistro-penjelajah-dungeon",
                title = "Bistro Penjelajah Dungeon: Resep Naga Emas",
                japaneseTitle = "迷宮ビストロの黄金料理",
                synopsis = "Demi menyelamatkan rekannya di kedalaman dungeon, sekelompok petualang eksentrik memutuskan bertahan hidup dengan memasak monster-monster legendaris menjadi hidangan bintang lima.",
                rating = 8.7,
                ratingCountLabel = "91K",
                totalEpisodes = 24,
                releasedEpisodes = 24,
                durationMinutes = 24,
                year = 2024,
                season = "Spring 2024",
                studio = "Trigger Kitchen",
                status = AnimeStatus.COMPLETED,
                type = AnimeType.TV_SERIES,
                ageRating = "13+",
                genres = listOf("Comedy", "Adventure", "Fantasy", "Slice of Life"),
                releaseDay = ReleaseDay.KAMIS,
                releaseTimeWib = "21:30 WIB",
                viewsLabel = "3.1M",
                popularityRank = 14,
                artTheme = AnimeArtTheme.IMPERIAL_ALCHEMY,
                posterTag = "KOMEDI",
                characters = listOf(
                    CharacterRole("Laios", "Pemimpin Tim", "Kentaro Kumagai", "L"),
                    CharacterRole("Marcille", "Penyihir Elf", "Sayaka Senbongi", "M")
                ),
                relatedIds = listOf("astral-frieren", "phantom-voyage"),
                episodes = buildEpisodes("dungeon-bistro", 12, 24, emptyList())
            ),
            Anime(
                id = "hollow-academy",
                slug = "akademi-jujutsu-abyss",
                title = "Akademi Kutukan: Segel Jari Terakhir",
                japaneseTitle = "呪印学園・終焉の章",
                synopsis = "Setelah menelan relik terkutuk tingkat dewa demi menyelamatkan teman-temannya, siswa SMA bernama Yuji bergabung dengan sekolah rahasia pembasmi kutukan sebelum hari eksekusinya tiba.",
                rating = 9.0,
                ratingCountLabel = "198K",
                totalEpisodes = 24,
                releasedEpisodes = 16,
                durationMinutes = 24,
                year = 2026,
                season = "Fall 2026",
                studio = "MAPPA Obsidian",
                status = AnimeStatus.ONGOING,
                type = AnimeType.TV_SERIES,
                ageRating = "17+",
                genres = listOf("Action", "Dark Fantasy", "Supernatural"),
                releaseDay = ReleaseDay.KAMIS,
                releaseTimeWib = "23:00 WIB",
                viewsLabel = "5.8M",
                popularityRank = 15,
                isTrending = true,
                isNewRelease = true,
                artTheme = AnimeArtTheme.ABYSSAL_SHADOW,
                posterTag = "SHIBUYA ARC",
                characters = listOf(
                    CharacterRole("Yuji Itadori", "Wadah Kutukan", "Junya Enoki", "Y"),
                    CharacterRole("Satoru", "Penyihir Terkuat", "Yuichi Nakamura", "S")
                ),
                relatedIds = listOf("kage-no-ou", "onigiri-blade"),
                episodes = buildEpisodes("hollow-academy", 16, 24, emptyList())
            ),
            Anime(
                id = "valkyrie-ova",
                slug = "starlight-concerto-ova",
                title = "Starlight Concerto: Encore di Panggung Emas",
                japaneseTitle = "スターライト協奏曲 OVA",
                synopsis = "Episode spesial konser orkestra dan drama panggung yang mempertemukan kembali para rival akademi musik setelah lima tahun berpisah.",
                rating = 8.3,
                ratingCountLabel = "29K",
                totalEpisodes = 3,
                releasedEpisodes = 3,
                durationMinutes = 32,
                year = 2025,
                season = "OVA 2025",
                studio = "Kyoto Animation",
                status = AnimeStatus.COMPLETED,
                type = AnimeType.OVA,
                ageRating = "13+",
                genres = listOf("Music", "Drama", "Slice of Life"),
                releaseDay = null,
                releaseTimeWib = null,
                viewsLabel = "890K",
                popularityRank = 16,
                artTheme = AnimeArtTheme.CELESTIAL_ASTRAL,
                posterTag = "OVA SPESIAL",
                characters = listOf(
                    CharacterRole("Kumiko", "Pemain Euphonium", "Tomoyo Kurosawa", "K")
                ),
                relatedIds = listOf("sakura-wind-chronicles", "silent-sky-movie"),
                episodes = buildEpisodes("valkyrie-ova", 3, 32, emptyList())
            )
        )
    }

    override fun getAllAnime(): List<Anime> = animeCatalog

    override fun getAllGenres(): List<String> = listOf(
        "Action",
        "Adventure",
        "Fantasy",
        "Dark Fantasy",
        "Isekai",
        "Sci-Fi",
        "Romance",
        "Drama",
        "Mystery",
        "Thriller",
        "Slice of Life",
        "Supernatural",
        "Comedy",
        "Mecha",
        "Sports",
        "Music",
        "Historical"
    )

    override fun getInitialWatchlist(): Map<String, WatchlistCategory> = mapOf(
        "kage-no-ou" to WatchlistCategory.WATCHING,
        "cyber-zero" to WatchlistCategory.WATCHING,
        "astral-frieren" to WatchlistCategory.COMPLETED,
        "onigiri-blade" to WatchlistCategory.COMPLETED,
        "chrono-requiem" to WatchlistCategory.PLAN_TO_WATCH,
        "silent-sky-movie" to WatchlistCategory.PLAN_TO_WATCH
    )

    override fun getInitialWatchHistory(): List<WatchProgress> = listOf(
        WatchProgress(
            animeId = "kage-no-ou",
            episodeId = "kage-no-ou_ep_18",
            episodeNumber = 18,
            episodeTitle = "Puncak Takdir: Pedang Hitam",
            positionSeconds = 864,
            durationSeconds = 24 * 60,
            updatedAtLabel = "Baru saja"
        ),
        WatchProgress(
            animeId = "cyber-zero",
            episodeId = "cyber-zero_ep_9",
            episodeNumber = 9,
            episodeTitle = "Gerbang Jaringan Pusat",
            positionSeconds = 512,
            durationSeconds = 24 * 60,
            updatedAtLabel = "2 jam lalu"
        ),
        WatchProgress(
            animeId = "hollow-academy",
            episodeId = "hollow-academy_ep_14",
            episodeNumber = 14,
            episodeTitle = "Bab 14: Gema Takdir di Ambang Fajar",
            positionSeconds = 1120,
            durationSeconds = 24 * 60,
            updatedAtLabel = "Kemarin"
        )
    )

    override fun getInitialDownloads(): List<DownloadTask> = listOf(
        DownloadTask(
            episodeId = "onigiri-blade_ep_12",
            animeId = "onigiri-blade",
            animeTitle = "Kijin no Yaiba: Api Abadi di Puncak Salju",
            episodeNumber = 12,
            episodeTitle = "Fajar Setelah Malam Panjang",
            artTheme = AnimeArtTheme.CRIMSON_BLADE,
            qualityLabel = "1080p",
            totalMb = 340,
            progress = 1.0f,
            status = DownloadStatus.COMPLETED
        ),
        DownloadTask(
            episodeId = "astral-frieren_ep_1",
            animeId = "astral-frieren",
            animeTitle = "Hoshi no Tabibito: Perjalanan Melampaui Bintang",
            episodeNumber = 1,
            episodeTitle = "Akhir Sebuah Petualangan",
            artTheme = AnimeArtTheme.CELESTIAL_ASTRAL,
            qualityLabel = "720p",
            totalMb = 195,
            progress = 1.0f,
            status = DownloadStatus.COMPLETED
        )
    )

    override fun getInitialComments(): List<CommentItem> = listOf(
        CommentItem(
            id = "c1",
            animeId = "kage-no-ou",
            episodeNumber = 18,
            author = "Kurogane_ID",
            authorBadge = "PRO VIP",
            content = "Animasi pertarungan di menit ke-14 luar biasa! Transisi bayangannya benar-benar kelas film bioskop.",
            timeAgo = "12 menit lalu",
            likes = 148,
            isLiked = false,
            isSpoiler = false
        ),
        CommentItem(
            id = "c2",
            animeId = "kage-no-ou",
            episodeNumber = 18,
            author = "RizaAlchemist",
            authorBadge = "OTAKU",
            content = "Akhirnya Igris keluar lagi dengan armor barunya! Pas adegan Arata bilang 'Bangkitlah', merinding parah.",
            timeAgo = "38 menit lalu",
            likes = 92,
            isLiked = true,
            isSpoiler = true
        ),
        CommentItem(
            id = "c3",
            animeId = "onigiri-blade",
            episodeNumber = 12,
            author = "SenpaiNusantara",
            authorBadge = "PRO VIP",
            content = "Soundtrack penutup di episode terakhir bikin nangis. Penutup musim yang sempurna!",
            timeAgo = "3 jam lalu",
            likes = 215,
            isLiked = false,
            isSpoiler = false
        ),
        CommentItem(
            id = "c4",
            animeId = "cyber-zero",
            episodeNumber = 9,
            author = "CyberValkyrie",
            authorBadge = "WATCHER",
            content = "Plot twist soal chip E.V.E-09 nggak ketebak sama sekali. Mingdep pasti pecah banget!",
            timeAgo = "1 jam lalu",
            likes = 64,
            isLiked = false,
            isSpoiler = false
        )
    )

    override fun getInitialNotifications(): List<NotificationItem> = listOf(
        NotificationItem(
            id = "n1",
            title = "Episode Baru Rilis!",
            message = "Kage no Ou Episode 18 'Puncak Takdir: Pedang Hitam' sudah tersedia dalam kualitas 1080p Sub Indo.",
            timeAgo = "15 menit lalu",
            animeId = "kage-no-ou",
            isRead = false,
            tag = "RILIS BARU"
        ),
        NotificationItem(
            id = "n2",
            title = "Pengingat Jadwal Tayang",
            message = "Zero Protocol: Neo-Tokyo 2099 Episode 9 baru saja tayang malam ini pukul 20:00 WIB.",
            timeAgo = "4 jam lalu",
            animeId = "cyber-zero",
            isRead = false,
            tag = "JADWAL"
        ),
        NotificationItem(
            id = "n3",
            title = "Unduhan Selesai",
            message = "Kijin no Yaiba Episode 12 (1080p) siap ditonton secara offline di Koleksiku.",
            timeAgo = "Kemarin",
            animeId = "onigiri-blade",
            isRead = true,
            tag = "OFFLINE"
        )
    )

    override fun getInitialReminders(): Set<String> = setOf("kage-no-ou", "cyber-zero")

    override fun getInitialUserProfile(): UserProfile = UserProfile(
        displayName = "Adrian Vallenz",
        handle = "@adrianvallenz",
        bio = "Pecinta anime dark fantasy, sci-fi, dan musik orkestra. Kolektor watchlist 10/10.",
        avatarEmblem = "AV",
        memberTier = "PRO STREAM PASS",
        watchedHours = 348,
        completedAnimeCount = 42,
        favoriteGenre = "Dark Fantasy"
    )

    override fun getInitialPlaybackSettings(): PlaybackSettings = PlaybackSettings()

    override fun getSubtitleCues(animeTitle: String, episodeNumber: Int): List<SubtitleCue> {
        val shortName = animeTitle.substringBefore(":")
        return listOf(
            SubtitleCue(
                startSec = 0,
                endSec = 14,
                indoText = "Sebelumnya di $shortName...",
                englishText = "Previously on $shortName..."
            ),
            SubtitleCue(
                startSec = 15,
                endSec = 85,
                indoText = "♪ Lagu Pembuka — 'Gema Cahaya Emas di Atas Kegelapan' ♪",
                englishText = "♪ Opening Theme — 'Golden Echo Beyond the Dark' ♪"
            ),
            SubtitleCue(
                startSec = 86,
                endSec = 120,
                indoText = "\"Episode $episodeNumber dimulai. Jangan sampai lengah sedetik pun.\"",
                englishText = "\"Episode $episodeNumber begins. Don't let your guard down for a second.\""
            ),
            SubtitleCue(
                startSec = 121,
                endSec = 240,
                indoText = "\"Kekuatan sejati bukan berasal dari amarah, melainkan dari tekad yang tak tergoyahkan.\"",
                englishText = "\"True strength is not born from rage, but from an unbreakable resolve.\""
            ),
            SubtitleCue(
                startSec = 241,
                endSec = 480,
                indoText = "\"Lihat ke atas langit! Segel keemasan itu mulai retak!\"",
                englishText = "\"Look up at the sky! The golden seal is starting to fracture!\""
            ),
            SubtitleCue(
                startSec = 481,
                endSec = 900,
                indoText = "\"Jika ini adalah pertempuran terakhir kita, biarlah dunia mengingat nama kita malam ini.\"",
                englishText = "\"If this is our final battle, let the world remember our names tonight.\""
            ),
            SubtitleCue(
                startSec = 901,
                endSec = 1440,
                indoText = "\"Takdir telah memanggil. Saatnya melangkah melampaui batas!\"",
                englishText = "\"Destiny has called. It is time to step beyond our limits!\""
            )
        )
    }
}
