# FunnyEnglish Android — Technical Specification
## MVP Implementation Guide

---

## 1. Gradle Configuration

### `gradle/libs.versions.toml`
```toml
[versions]
agp = "8.5.0"
kotlin = "2.0.0"
coreKtx = "1.13.1"
lifecycle = "2.8.2"
activityCompose = "1.9.0"
composeBom = "2024.06.00"
navigation = "2.7.7"
room = "2.6.1"
koin = "3.5.6"
koinCompose = "3.5.6"
ktor = "2.3.11"
kotlinxSerialization = "1.6.3"
coil = "2.6.0"
firebaseBom = "33.1.0"
junit5 = "5.10.2"
turbine = "1.1.0"
mockk = "1.13.11"

[libraries]
# Core
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }

# Compose
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-navigation = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

# Koin DI
koin-android = { group = "io.insert-koin", name = "koin-android", version.ref = "koin" }
koin-compose = { group = "io.insert-koin", name = "koin-androidx-compose", version.ref = "koinCompose" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }

# Ktor
ktor-client-core = { group = "io.ktor", name = "ktor-client-core", version.ref = "ktor" }
ktor-client-android = { group = "io.ktor", name = "ktor-client-android", version.ref = "ktor" }
ktor-client-content-negotiation = { group = "io.ktor", name = "ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { group = "io.ktor", name = "ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-logging = { group = "io.ktor", name = "ktor-client-logging", version.ref = "ktor" }

# Serialization
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerialization" }

# Coil
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }

# Firebase
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-analytics = { group = "com.google.firebase", name = "firebase-analytics-ktx" }
firebase-crashlytics = { group = "com.google.firebase", name = "firebase-crashlytics-ktx" }
firebase-messaging = { group = "com.google.firebase", name = "firebase-messaging-ktx" }

# Testing
junit5 = { group = "org.junit.jupiter", name = "junit-jupiter", version.ref = "junit5" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version = "1.8.1" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

---

## 2. Core Module Setup

### `:core:data` — Network (Ktor)
```kotlin
// core/data/src/main/java/com/funnyenglish/data/network/HttpClientFactory.kt
package com.funnyenglish.core.data.network

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object HttpClientFactory {
    fun create(): HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(Logging) {
            level = LogLevel.ALL
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 10000
        }
    }
}
```

### `:core:data` — Dictionary API
```kotlin
// core/data/src/main/java/com/funnyenglish/data/remote/DictionaryApi.kt
package com.funnyenglish.core.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class DictionaryApi(private val client: HttpClient) {
    suspend fun getDefinition(word: String): List<WordDto> {
        return client.get("https://api.dictionaryapi.dev/api/v2/entries/en/$word").body()
    }
}

@kotlinx.serialization.Serializable
data class WordDto(
    val word: String,
    val phonetic: String? = null,
    val phonetics: List<PhoneticDto> = emptyList(),
    val meanings: List<MeaningDto> = emptyList()
)

@kotlinx.serialization.Serializable
data class PhoneticDto(
    val text: String? = null,
    val audio: String = ""
)

@kotlinx.serialization.Serializable
data class MeaningDto(
    val partOfSpeech: String,
    val definitions: List<DefinitionDto> = emptyList()
)

@kotlinx.serialization.Serializable
data class DefinitionDto(
    val definition: String,
    val example: String? = null
)
```

### `:core:data` — Room Database
```kotlin
// core/data/src/main/java/com/funnyenglish/data/local/AppDatabase.kt
package com.funnyenglish.core.data.local

import androidx.room.*

@Database(
    entities = [WordEntity::class, QuizQuestionEntity::class, AchievementEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun quizDao(): QuizDao
    abstract fun achievementDao(): AchievementDao
}

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey val word: String,
    val phonetic: String?,
    val jsonMeanings: String, // serialized JSON
    val isFavorite: Boolean = false,
    val searchedAt: Long = System.currentTimeMillis()
)

@Dao
interface WordDao {
    @Query("SELECT * FROM words WHERE word = :word LIMIT 1")
    suspend fun get(word: String): WordEntity?

    @Query("SELECT * FROM words WHERE isFavorite = 1 ORDER BY searchedAt DESC")
    suspend fun getFavorites(): List<WordEntity>

    @Query("SELECT * FROM words ORDER BY searchedAt DESC LIMIT 100")
    suspend fun getRecent(): List<WordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: WordEntity)

    @Query("UPDATE words SET isFavorite = :isFavorite WHERE word = :word")
    suspend fun setFavorite(word: String, isFavorite: Boolean)
}
```

---

## 3. Feature: Dictionary (example)

### Domain
```kotlin
// feature/dictionary/domain/src/main/java/...
package com.funnyenglish.feature.dictionary.domain.model

data class Word(
    val spelling: String,
    val phonetic: String?,
    val audioUrl: String?,
    val meanings: List<Meaning>
)

data class Meaning(
    val partOfSpeech: String,
    val definitions: List<Definition>
)

data class Definition(
    val text: String,
    val example: String?
)

// Repository interface
interface WordRepository {
    suspend fun search(word: String): Result<Word>
    suspend fun getFavorites(): List<Word>
    suspend fun toggleFavorite(word: String, isFavorite: Boolean)
}
```

### Data
```kotlin
// feature/dictionary/data/src/main/java/...
class WordRepositoryImpl(
    private val api: DictionaryApi,
    private val dao: WordDao
) : WordRepository {
    override suspend fun search(word: String): Result<Word> {
        // 1. Check cache
        dao.get(word.lowercase())?.let { cached ->
            return Result.success(cached.toDomain())
        }

        // 2. Network call
        return try {
            val dto = api.getDefinition(word.lowercase()).firstOrNull()
                ?: return Result.failure(WordNotFoundException())
            
            val domain = dto.toDomain()
            dao.insert(domain.toEntity())
            Result.success(domain)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // ...
}
```

### Presentation (MVI)
```kotlin
// feature/dictionary/presentation/src/main/java/...
sealed interface DictionaryAction {
    data class Search(val query: String) : DictionaryAction
    data class ToggleFavorite(val word: String) : DictionaryAction
    data object ClearSearch : DictionaryAction
}

sealed interface DictionaryEvent {
    data class ShowError(val message: String) : DictionaryEvent
}

data class DictionaryState(
    val query: String = "",
    val isLoading: Boolean = false,
    val word: WordUi? = null,
    val recentWords: List<String> = emptyList(),
    val favorites: List<String> = emptyList(),
    val errorMessage: String? = null
)

class DictionaryViewModel(
    private val repository: WordRepository,
    private val tts: TextToSpeechHelper
) : ViewModel() {

    private val _state = MutableStateFlow(DictionaryState())
    val state = _state.asStateFlow()

    private val _events = Channel<DictionaryEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: DictionaryAction) {
        when (action) {
            is DictionaryAction.Search -> search(action.query)
            is DictionaryAction.ToggleFavorite -> toggleFavorite(action.word)
            DictionaryAction.ClearSearch -> _state.update { it.copy(query = "", word = null) }
        }
    }

    private fun search(query: String) {
        if (query.isBlank()) return
        
        _state.update { it.copy(query = query, isLoading = true, word = null) }
        
        viewModelScope.launch {
            repository.search(query)
                .onSuccess { word ->
                    _state.update { it.copy(
                        isLoading = false,
                        word = word.toUi()
                    )}
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false) }
                    _events.send(DictionaryEvent.ShowError(error.message ?: "Unknown error"))
                }
        }
    }
}
```

---

## 4. Feature: Quiz (with gamification)

```kotlin
// feature/quiz/presentation/QuizViewModel.kt
class QuizViewModel(
    private val quizRepository: QuizRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _state = MutableStateFlow(QuizState())
    val state = _state.asStateFlow()

    fun onAction(action: QuizAction) {
        when (action) {
            is QuizAction.SelectAnswer -> checkAnswer(action.index)
            QuizAction.NextQuestion -> nextQuestion()
            QuizAction.StartQuiz -> startQuiz()
        }
    }

    private fun checkAnswer(selectedIndex: Int) {
        val current = _state.value.currentQuestion ?: return
        val isCorrect = selectedIndex == current.correctIndex
        
        _state.update { state ->
            state.copy(
                selectedAnswer = selectedIndex,
                isCorrect = isCorrect,
                score = if (isCorrect) state.score + 10 else state.score
            )
        }

        if (isCorrect) {
            viewModelScope.launch {
                progressRepository.addXp(10)
            }
        }
    }

    private fun nextQuestion() {
        val currentIndex = _state.value.currentQuestionIndex
        val questions = _state.value.questions
        
        if (currentIndex >= questions.size - 1) {
            finishQuiz()
        } else {
            _state.update { state ->
                state.copy(
                    currentQuestionIndex = currentIndex + 1,
                    currentQuestion = questions[currentIndex + 1],
                    selectedAnswer = null,
                    isCorrect = null
                )
            }
        }
    }

    private fun finishQuiz() {
        val score = _state.value.score
        viewModelScope.launch {
            progressRepository.addXp(50) // Bonus for completion
            progressRepository.updateStreak()
            _events.send(QuizEvent.ShowResults(score, _state.value.questions.size))
        }
    }
}
```

---

## 5. AI Chat Integration

```kotlin
// feature/chat/data/remote/OpenAiApi.kt
class OpenAiApi(private val client: HttpClient) {
    suspend fun chat(messages: List<ChatMessageDto>): String {
        val response = client.post("https://api.openai.com/v1/chat/completions") {
            header("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
            setBody(ChatRequestDto(
                model = "gpt-4o-mini",
                messages = messages,
                max_tokens = 250,
                temperature = 0.8
            ))
        }.body<ChatResponseDto>()
        
        return response.choices.first().message.content
    }
}

// System prompt for Archie
val ARCHIE_SYSTEM_PROMPT = """
Ты — Арчи, весёлый AI-компаньон для изучения английского. 
Ты помогаешь пользователю учить английский через дружеские беседы.

Правила:
- Обращайся на "ты", дружелюбно, с лёгкой иронией
- Если пользователь пишет по-английски — исправляй ошибки мягко
- Если по-русски — переводи и объясняй
- Давай короткие ответы (2-4 предложения)
- Используй эмодзи умеренно
- Подбадривай, когда пользователь старается
- Можешь предлагать слова дня или фразы
""".trimIndent()
```

---

## 6. ProGuard / R8 Rules

```proguard
# Ktor
-keep class io.ktor.** { *; }
-keep class kotlinx.serialization.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Koin
-keep class org.koin.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
```

---

## 7. Firebase Configuration

### `build.gradle.kts` (app level)
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("com.google.gms.google-services") version "4.4.2"
    id("com.google.firebase.crashlytics") version "3.0.2"
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
}
```

### Push Notification — Daily Word
```kotlin
class DailyWordWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val word = repository.getDailyWord()
        showNotification(
            title = "📚 Слово дня: ${word.spelling}",
            body = "${word.definition} — нажми, чтобы узнать больше!"
        )
        return Result.success()
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<DailyWordWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(calculateDelayTo9am(), TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "daily_word",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
```

---

## 8. Testing Strategy

### Unit Tests (ViewModel)
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: QuizViewModel
    private val quizRepository = mockk<QuizRepository>()
    private val progressRepository = mockk<ProgressRepository>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel = QuizViewModel(quizRepository, progressRepository)
    }

    @Test
    fun `select correct answer increases score`() = runTest {
        // Given
        coEvery { quizRepository.getQuestions(any()) } returns sampleQuestions

        // When
        viewModel.onAction(QuizAction.StartQuiz)
        viewModel.onAction(QuizAction.SelectAnswer(2))

        // Then
        viewModel.state.test {
            assertEquals(10, awaitItem().score)
        }
    }
}
```

### UI Tests (Compose)
```kotlin
@HiltAndroidTest
class DictionaryScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun searchWord_showsResult() {
        composeTestRule.onNodeWithContentDescription("Search").performTextInput("hello")
        composeTestRule.onNodeWithText("Search").performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("hello").assertIsDisplayed()
        composeTestRule.onNodeWithText("/həˈləʊ/").assertIsDisplayed()
    }
}
```

---

## 9. Google Play Checklist

### Pre-release
- [ ] `versionCode` incremented
- [ ] `versionName` semantic (e.g., "1.0.0")
- [ ] `minSdk = 24`, `targetSdk = 34`
- [ ] ProGuard enabled (`isMinifiedEnabled = true`)
- [ ] `shrinkResources = true`
- [ ] Signing config configured
- [ ] `privacy_policy_url` provided
- [ ] Content rating questionnaire completed
- [ ] Store listing: title (30 chars), short desc (80), full desc (4000)
- [ ] Screenshots: phone (5), tablet (optional)
- [ ] Feature graphic (1024x500)
- [ ] App icon (512x512 PNG)

### Post-release
- [ ] Firebase Crashlytics monitoring
- [ ] Firebase Analytics events configured
- [ ] In-app update API (optional)
- [ ] Review prompt (after 3 days of usage)
