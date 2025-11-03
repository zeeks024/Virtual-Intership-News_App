# Bank Mandiri News App - Task 5
**Program:** Rakamin - Android Development
**Tujuan:** Mengikuti program rakamin ini untuk mendapatkan portofolio dan pengalaman yang berguna saat mencari pekerjaan setelah lulus nanti.

---

## TENTANG TASK 5

Aplikasi Android yang menampilkan list berita menggunakan API dari **newsapi.org**

**Fitur:**
- Mengambil data berita dari NewsAPI
- Menampilkan list berita dengan gambar, judul, deskripsi, dan tanggal
- Search berita berdasarkan keyword
- Detail berita dengan WebView
- Dark Mode / Light Mode
- Pull to refresh

---

## PENGAMBILAN API

### API yang Digunakan
**NewsAPI.org** - https://newsapi.org/v2/everything

### Konfigurasi
API Key disimpan di `local.properties`:
```properties
NEWS_API_KEY=your_api_key_here
```

### Request Parameters:
- `q`: Keyword pencarian (default: "Semua Berita")
- `apiKey`: API key untuk autentikasi
- `language`: Bahasa berita (id untuk Indonesia)
- `pageSize`: Jumlah artikel per halaman

### Response Example:
```json
{
  "articles": [
    {
      "title": "Momen Duel Pingpong Edi Kamtono Vs Bahasan...",
      "description": "KALBARONLINE.com - Momen menarik terjadi saat pembukaan...",
      "urlToImage": "https://kalbaronline.com/image.jpg",
      "publishedAt": "2025-11-01T14:30:00Z",
      "url": "https://kalbaronline.com/article"
    }
  ]
}
```

---

## SOURCE CODE

### 1. Data Models
```kotlin
data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<Article>
)

data class Article(
    val source: Source,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String
)

data class Source(
    val name: String
)
```

### 2. API Service (Retrofit)
```kotlin
interface NewsApiService {
    @GET("everything")
    suspend fun getNews(
        @Query("q") query: String = "Indonesia",
        @Query("apiKey") apiKey: String,
        @Query("language") language: String = "id",
        @Query("pageSize") pageSize: Int = 20
    ): NewsResponse
    
    companion object {
        private const val BASE_URL = "https://newsapi.org/v2/"
        
        fun create(): NewsApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NewsApiService::class.java)
        }
    }
}
```

### 3. Repository
```kotlin
class NewsRepository(private val apiService: NewsApiService) {
    suspend fun getNews(query: String, apiKey: String): Result<List<Article>> {
        return try {
            val response = apiService.getNews(query, apiKey)
            Result.success(response.articles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 4. ViewModel
```kotlin
class NewsViewModel(private val repository: NewsRepository) : ViewModel() {
    private val _newsState = MutableLiveData<UiState<List<Article>>>()
    val newsState: LiveData<UiState<List<Article>>> = _newsState

    fun fetchNews(query: String = "Semua Berita") {
        viewModelScope.launch {
            _newsState.value = UiState.Loading
            repository.getNews(query, BuildConfig.NEWS_API_KEY)
                .onSuccess { _newsState.value = UiState.Success(it) }
                .onFailure { _newsState.value = UiState.Error(it.message ?: "Unknown error") }
        }
    }
}

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

### 5. RecyclerView Adapter
```kotlin
class NewsAdapter(
    private val onItemClick: (Article) -> Unit
) : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {
    
    private var articles = listOf<Article>()
    
    fun submitList(newArticles: List<Article>) {
        articles = newArticles
        notifyDataSetChanged()
    }
    
    inner class ViewHolder(private val binding: ItemNewsBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(article: Article) {
            binding.apply {
                tvTitle.text = article.title
                tvDescription.text = article.description
                tvSource.text = article.source.name
                tvDate.text = formatDate(article.publishedAt)
                
                Glide.with(itemView)
                    .load(article.urlToImage)
                    .into(ivNews)
                
                root.setOnClickListener { onItemClick(article) }
            }
        }
    }
}
```

---

## TAMPILAN HASIL (SCREENSHOTS)

### 1. Home Screen - List Berita
<img src="screenshots/home_screen.png" width="300">

**Fitur:**
- Search bar di bagian atas untuk mencari berita
- Label "Semua Berita" menampilkan kategori berita
- List berita dengan card view yang menampilkan:
  - Gambar thumbnail berita
  - Judul berita lengkap
  - Source berita (contoh: Kalbaronline.com)
  - Deskripsi singkat
  - Tanggal & waktu publish
- Icon settings di kanan atas untuk mengatur tema

### 2. Detail Artikel
<img src="screenshots/detail_screen.png" width="300">

**Fitur:**
- WebView untuk menampilkan artikel lengkap
- Tombol back untuk kembali ke list
- Menampilkan artikel dari website aslinya

### 3. Theme Chooser (Dark Mode)
<img src="screenshots/theme_dialog.png" width="300">

**Fitur:**
- Dialog untuk memilih tema aplikasi
- 3 pilihan: System, Light, Dark
- Tombol Cancel & OK

---

## CARA MENJALANKAN

### Prerequisite:
- Android Studio terbaru (Hedgehog atau lebih baru)
- Android SDK minimum API 24 (Android 7.0)
- Device Android atau Emulator
- Koneksi internet aktif
- **API Key dari NewsAPI.org** (daftar gratis di https://newsapi.org/)

### Langkah-langkah:

1. **Clone repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/Mandiri.git
   cd Mandiri
   ```

2. **Setup API Key**
   - Copy file `local.properties.example` menjadi `local.properties`
   - Daftar di https://newsapi.org/ untuk mendapatkan API key gratis
   - Edit `local.properties` dan isi dengan data Anda:
   ```properties
   sdk.dir=C\:\\Users\\YOUR_USERNAME\\AppData\\Local\\Android\\Sdk
   NEWS_API_KEY=your_api_key_here
   ```

3. **Sync Gradle**
   - Buka project di Android Studio
   - Klik "Sync Now" jika muncul notifikasi
   - Atau File → Sync Project with Gradle Files

4. **Build aplikasi**
   ```bash
   ./gradlew assembleDebug
   ```

5. **Install ke Device**
   - Hubungkan HP Android via USB
   - Enable USB Debugging di HP
   - Jalankan:
   ```bash
   ./gradlew installDebug
   ```

6. **Atau Run Langsung**
   - Klik tombol Run (▶) di Android Studio
   - Pilih device yang terhubung
   - Aplikasi akan otomatis terinstall dan berjalan

---

## FITUR APLIKASI

### ✅ Sudah Diimplementasikan:
- [x] Integrasi API NewsAPI.org
- [x] Menampilkan list berita dengan RecyclerView
- [x] Search berita
- [x] Detail artikel dengan WebView
- [x] Loading state dengan ProgressBar
- [x] Error handling
- [x] Dark Mode / Light Mode
- [x] Material Design UI
- [x] Image loading dengan Glide/Coil
- [x] Date formatting (31 Okt 2025 | 21.54)
- [x] Pull to refresh

### Komponen Utama:
- **MainActivity**: Menampilkan list berita
- **DetailActivity**: Menampilkan artikel lengkap di WebView
- **NewsAdapter**: Adapter untuk RecyclerView
- **NewsViewModel**: ViewModel dengan LiveData
- **NewsRepository**: Handling API calls
- **ThemeManager**: Mengatur Light/Dark mode

---

## TEKNOLOGI

- **Language:** Kotlin
- **Architecture:** MVVM (Model-View-ViewModel)
- **Networking:** Retrofit + OkHttp
- **Async:** Kotlin Coroutines
- **JSON Parsing:** Gson
- **Image Loading:** Coil
- **UI:** Jetpack Compose & Material Design 3
- **Lifecycle:** ViewModel, LiveData
- **WebView:** Untuk detail artikel
- **Navigation:** Navigation Compose

---

## STRUKTUR PROJECT

```
app/src/main/
├── java/com/example/mandiri/
│   ├── data/
│   │   ├── model/
│   │   │   ├── Article.kt
│   │   │   ├── NewsResponse.kt
│   │   │   └── Source.kt
│   │   ├── repository/
│   │   │   └── NewsRepository.kt
│   │   └── remote/
│   │       └── NewsApiService.kt
│   ├── ui/
│   │   ├── MainActivity.kt
│   │   ├── screen/
│   │   │   ├── NewsListScreen.kt
│   │   │   ├── DetailScreen.kt
│   │   │   └── SettingsScreen.kt
│   │   └── components/
│   │       └── NewsCard.kt
│   ├── viewmodel/
│   │   └── NewsViewModel.kt
│   └── util/
│       ├── ThemeManager.kt
│       └── DateFormatter.kt
└── res/
    └── values/
        ├── colors.xml
        ├── themes.xml
        └── strings.xml
```

---

## TROUBLESHOOTING

### Problem: API tidak mengembalikan data
**Solution:** 
- Periksa koneksi internet
- Pastikan API key valid di `local.properties`
- Cek quota API (free tier: 100 request/hari)
- Verifikasi BuildConfig.NEWS_API_KEY sudah terbaca dengan benar

### Problem: Build error
**Solution:**
- Sync Gradle files
- Clean & Rebuild project (`Build → Clean Project → Rebuild Project`)
- Invalidate Caches and Restart (`File → Invalidate Caches → Invalidate and Restart`)

### Problem: Gambar tidak muncul
**Solution:**
- Periksa permission INTERNET di AndroidManifest.xml
- Pastikan URL gambar valid
- Clear cache aplikasi

---

## LISENSI

Project ini dibuat untuk keperluan edukasi sebagai bagian dari Rakamin Bootcamp Task 5.


**© 2025 - Mandiri News App - Rakamin Task 5**
