# FunnyEnglish — Android MVP PRD
## Подготовка к деплою в Google Play

---

## 1. Анализ текущего состояния

### Что есть сейчас (Telegram бот "Арчи")
| Компонент | Статус |
|-----------|--------|
| AI-спутник "Арчи" (GPT-3.5) | ✅ Работает |
| Крестики-нолики (vs бот, PvP) | ✅ Работает |
| Шахматы (PvP) | ✅ Работает |
| Генерация изображений доски | ✅ PIL |
| Docker-деплой | ✅ Готов |
| **English Learning** | ❌ Отсутствует |
| **Офлайн-режим** | ❌ Нет |
| **Прогресс пользователя** | ❌ Нет |
| **Геймификация** | ❌ Минимальная |

### Проблема
Название "FunnyEnglish" не соответствует функционалу — бот игровой, но не учит английский. Для деплоя в Google Play нужен чёткий Product-Market Fit.

---

## 2. Product Vision

> **FunnyEnglish** — мобильное приложение для изучения английского через игры, AI-диалоги и геймификацию. Персонаж "Арчи" — весёлый саркастичный напарник, который поддерживает мотивацию.

### УТП (Unique Value Proposition)
- 🎮 **Learn through Play** — квизы, флешкарты, мини-игры вместо скучных уроков
- 🤖 **AI-приятель Арчи** — объясняет слова, разговаривает, шутит, подбадривает
- 🏆 **Геймификация** — streaks, уровни, достижения, лидерборд
- 📴 **Офлайн-режим** — учись без интернета в метро

### Целевая аудитория
- Русскоязычные пользователи 16–35 лет
- Уровень: Beginner – Intermediate
- Хотят учить English не по учебнику, а "по-прикольному"

---

## 3. MVP Feature Scope

### Core Features (must have для деплоя)

#### 3.1 🗣️ AI-Чат с Арчи (`feature:chat`)
| Фича | Описание |
|------|----------|
| Свободный диалог | Пользователь пишет на английском/русском, Арчи отвечает |
| "Переведи" | Режим перевода с объяснением контекста |
| "Объясни слово" | Арчи разбирает слово: определение, примеры, синонимы |
| Голосовые сообщения | Speech-to-text + ответ текстом (MVP) |
| История чата | Сохранение последних 50 сообщений (локально) |

**API**: OpenAI GPT-4o-mini (дешевле, быстрее) с кастомным prompt

#### 3.2 📚 Словарь (`feature:dictionary`)
| Фича | Описание |
|------|----------|
| Поиск слова | Английское → определение, произношение, примеры |
| Оффлайн-кэш | Последние 100 поисковых слов хранятся в Room |
| Аудио произношение | Воспроизведение через TTS (Android `TextToSpeech`) |
| Сохранение в избранное | Личный список слов |
| "Слово дня" | Ежедневное push-уведомление с новым словом |

**API**: [dictionaryapi.dev](https://dictionaryapi.dev/) (бесплатно, без ключа)

#### 3.3 🎯 Квизы / Тренировки (`feature:quiz`)
| Фича | Описание |
|------|----------|
| "Угадай перевод" | 4 варианта ответа, карточка слова |
| "Составь предложение" | Drag-and-drop слова в правильном порядке |
| "Правильно / Неправильно" | Быстрая проверка утверждений |
| Категории слов | Food, Travel, Business, Slang, Idioms |
| Уровни сложности | A1, A2, B1, B2 |

**Данные**: Предзагруженные JSON-наборы (~2000 слов) + открытые word lists ([Oxford 3000](https://www.oxfordlearnersdictionaries.com/wordlist/))

#### 3.4 🎮 Игры (`feature:games`)
| Фича | Описание |
|------|----------|
| Крестики-нолики vs Арчи | Уже есть логика — портировать на Compose Canvas |
| "Виселица" (Hangman) | Угадай слово по буквам — English words only |
| "Word Scramble" | Переставь буквы в слове |
| Соревнование с Арчи | За правильные ответы в квизе — ход в крестики-нолики |

#### 3.5 📊 Прогресс (`feature:profile`)
| Фича | Описание |
|------|----------|
| Streak (дни подряд) | Как Duolingo — flame icon |
| XP / Уровни | Очки за квизы, чат, игры |
| Достижения | "Первые 100 слов", "7 дней streak", "Победи Арчи" |
| Статистика | Словарный запас, время обучения, слабые места |

#### 3.6 🏠 Главный экран (`feature:home`)
- Приветствие Арчи (персонализированное)
- Быстрый доступ: "Продолжить квиз", "Слово дня", "Поболтать с Арчи"
- Streak widget
- Daily goal progress

---

## 4. Архитектура (Clean Architecture + MVI)

### Стек технологий
| Слой | Технология |
|------|------------|
| UI | Jetpack Compose + Material 3 |
| State Management | MVI (StateFlow + Channel) |
| DI | Koin |
| Сеть | Ktor Client + KotlinX Serialization |
| Локальная БД | Room |
| Навигация | Compose Navigation (type-safe) |
| Images | Coil |
| Background | WorkManager (daily reminder) |
| Analytics | Firebase Analytics (free tier) |
| Crashlytics | Firebase Crashlytics |
| Push | Firebase Cloud Messaging |

### Модульная структура
```
:app                          # Сборка, навигация, тема
:core:domain                  # Общие модели, интерфейсы репозиториев
:core:data                    # Ktor, Room, SharedPreferences
:core:presentation            # UiText, ObserveAsEvents, BaseViewModel
:core:design-system           # Тема, типографика, общие компоненты
:feature:home                 # Главный экран
:feature:chat                 # AI-чат с Арчи
:feature:dictionary           # Словарь
:feature:quiz                 # Квизы и тренировки
:feature:games                # Игры (TTT, Hangman, Scramble)
:feature:profile              # Профиль, прогресс, достижения
```

### Правило зависимостей
```
:feature:*:presentation  →  :feature:*:domain  →  :core:domain
        ↓                        ↓                    ↓
:feature:*:data        →  :feature:*:domain  →  :core:data
```

### Data Layer
```kotlin
// core:data — Ktor API
dictionaryApi.getDefinition("hello") // → WordDto

// core:data — Room
wordDao.getFavorites()               // → List<WordEntity>
wordDao.insert(wordEntity)

// core:data — Mapper
fun WordDto.toDomain(): Word = Word(
    spelling = word,
    phonetic = phonetic,
    meanings = meanings.map { it.toDomain() }
)
```

### Presentation Layer (MVI)
```kotlin
// feature:chat
sealed interface ChatAction {
    data class SendMessage(val text: String) : ChatAction
    data object ClearHistory : ChatAction
}

sealed interface ChatEvent {
    data class ShowSnackbar(val message: String) : ChatEvent
}

data class ChatState(
    val messages: List<MessageUi> = emptyList(),
    val isLoading: Boolean = false,
    val inputText: String = ""
)

class ChatViewModel(
    private val getAiResponse: GetAiResponseUseCase,
    private val messageRepository: MessageRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ChatState())
    val state = _state.asStateFlow()
    
    private val _events = Channel<ChatEvent>()
    val events = _events.receiveAsFlow()
    
    fun onAction(action: ChatAction) { /* ... */ }
}
```

---

## 5. Открытые данные и API

### 5.1 Словарь
| Источник | URL | Лимиты |
|----------|-----|--------|
| Free Dictionary API | `https://api.dictionaryapi.dev/api/v2/entries/en/{word}` | Безлимитно, без ключа |
| FreeDictionaryAPI | `https://freedictionaryapi.com/api/v2/entries/en/{word}` | 8.5M+ слов |

### 5.2 Word Lists (для квизов)
| Источник | Описание |
|----------|----------|
| [Oxford 3000](https://www.oxfordlearnersdictionaries.com/wordlist/) | 3000 базовых слов с уровнями A1–B2 |
| [NGSL](https://www.newgeneralservicelist.org/) | 2800 самых частых слов |
| [AWL](https://www.victoria.acz/lals/resources/academicwordlist) | Академический лексикон |

### 5.3 AI
| Модель | Цена (approx) | Назначение |
|--------|---------------|------------|
| GPT-4o-mini | $0.15 / 1M tokens | Диалоги, объяснения |
| GPT-4o | $2.50 / 1M tokens | Сложные грамматические разборы |

### 5.4 TTS (Text-to-Speech)
| Вариант | Описание |
|---------|----------|
| Android `TextToSpeech` | Бесплатно, офлайн, английские голоса |
| Google Cloud TTS | Платно, более естественно |

---

## 6. Геймификация

### Система очков (XP)
| Действие | XP |
|----------|-----|
| Правильный ответ в квизе | +10 |
| Завершённый квиз (10 вопросов) | +50 |
| Добавлено слово в избранное | +5 |
| Прослушано произношение | +2 |
| Сообщение в чате с Арчи | +3 |
| Победа над Арчи в крестики-нолики | +30 |
| Слово дня открыто | +15 |

### Streak
- Считается день, если набрано ≥ 50 XP
- Визуал: огонь 🔥 с числом дней
- Push-уведомление: "Не потеряй streak! 5 минут до полуночи"

### Достижения
| Название | Условие | Иконка |
|----------|---------|--------|
| "Первые шаги" | 10 слов в избранном | 🌱 |
| "Словарный запас" | 100 слов в избранном | 📚 |
| "Непобедимый" | 10 побед над Арчи | 🏆 |
| "Марафонец" | 7 дней streak | 🔥 |
| "Чемпион" | 30 дней streak | 👑 |
| "Полиглот" | 1000 XP за неделю | 🌍 |

---

## 7. UI/UX Design (Material 3)

### Цветовая схема
- **Primary**: `#6C63FF` (фиолетовый — дружелюбный, креативный)
- **Secondary**: `#00BFA6` (бирюзовый — успех, прогресс)
- **Tertiary**: `#FF6584` (розовый — Арчи, акценты)
- **Background**: `#F8F9FE` (светло-лавандовый)
- **Surface**: `#FFFFFF`

### Типографика
- Display Large — "FunnyEnglish" логотип
- Headline Medium — заголовки экранов
- Title Large — карточки, кнопки
- Body Large — основной текст
- Label Medium — подписи, статус

### Ключевые экраны
1. **Splash** — Анимированный логотип + Арчи-маскот
2. **Onboarding** — 3 экрана: "Учись играя", "AI-напарник", "Офлайн-режим"
3. **Home** — Приветствие Арчи, quick actions, streak, daily word
4. **Chat** — Messenger-like UI с пузырями, аватар Арчи
5. **Dictionary** — Search bar, recent, favorites, word detail
6. **Quiz** — Карточка вопроса, 4 варианта, progress bar
7. **Games** — Grid игр, TTT canvas, Hangman
8. **Profile** — Уровень, XP bar, achievements grid, stats

### Navigation
```kotlin
sealed class Route {
    @Serializable data object Home
    @Serializable data object Chat
    @Serializable data object Dictionary
    @Serializable data class WordDetail(val word: String)
    @Serializable data object Quiz
    @Serializable data object Games
    @Serializable data object Profile
}
```

---

## 8. Offline-First стратегия

### Что работает без интернета
| Фича | Офлайн-данные |
|------|---------------|
| Квизы | Room: 2000 слов + вопросы |
| Словарь (поиск) | Room: кэш последних 100 слов |
| Игры | Локальная логика (TTT, Hangman, Scramble) |
| Прогресс | Room + DataStore |

### Что требует интернет
| Фича | Причина |
|------|---------|
| AI-чат | OpenAI API |
| Новые слова в словаре | Dictionary API |
| Синхронизация прогресса | Firebase (optional) |

### Стратегия кэширования
```kotlin
// Repository Pattern
class WordRepositoryImpl(
    private val remote: DictionaryApi,
    private val local: WordDao
) : WordRepository {
    override suspend fun getDefinition(word: String): Result<Word> {
        // 1. Проверяем локальный кэш
        local.get(word)?.let { return Result.Success(it.toDomain()) }
        
        // 2. Запрашиваем сеть
        return try {
            val dto = remote.define(word)
            val domain = dto.toDomain()
            local.insert(domain.toEntity())
            Result.Success(domain)
        } catch (e: Exception) {
            Result.Error(NetworkError)
        }
    }
}
```

---

## 9. Roadmap к деплою

### Phase 1: Foundation (Неделя 1–2)
- [ ] Создать проект с модульной структурой
- [ ] Настроить Gradle convention plugins
- [ ] Подключить Koin, Room, Ktor, Navigation
- [ ] Создать `:core:design-system` с темой и компонентами
- [ ] Реализовать `:feature:home` (скелетон)

### Phase 2: Core Features (Неделя 3–4)
- [ ] `:feature:dictionary` — поиск, кэш, избранное, TTS
- [ ] `:feature:quiz` — 3 типа вопросов, категории, уровни
- [ ] `:feature:chat` — интеграция OpenAI, история, Room
- [ ] `:feature:profile` — XP, streak, достижения, Room

### Phase 3: Games (Неделя 5)
- [ ] `:feature:games` — порт TTT на Compose Canvas
- [ ] Добавить Hangman, Word Scramble
- [ ] Интеграция игр с XP-системой

### Phase 4: Polish (Неделя 6)
- [ ] Onboarding flow
- [ ] Push-уведомления (FCM + WorkManager)
- [ ] Анимации (Lottie для достижений)
- [ ] Edge-to-edge, adaptive layouts
- [ ] Accessibility audit

### Phase 5: Pre-launch (Неделя 7)
- [ ] Unit tests (ViewModels, UseCases, Repositories)
- [ ] UI tests (Compose Test Rule)
- [ ] Firebase Crashlytics + Analytics
- [ ] ProGuard / R8 конфигурация
- [ ] Google Play Console: store listing, screenshots, AAB

---

## 10. Монетизация (пост-MVP)

| Модель | Описание |
|--------|----------|
| Freemium | Бесплатно: 10 квизов/день, базовый чат |
| Premium ($4.99/мес) | Безлимит квизов, расширенный AI, офлайн-словари, темы |
| Ads (опционально) | Rewarded video: +1 жизнь в квизе |

---

## 11. Риски и Mitigation

| Риск | Mitigation |
|------|------------|
| OpenAI API дорогой | Использовать GPT-4o-mini, кэшировать частые ответы |
| Dictionary API недоступен | Fallback на локальный кэш + предупреждение |
| Большой размер APK | ProGuard, dynamic delivery, asset compression |
| Низкая retention | Push-уведомления + streak-напоминания |
| Конкуренция (Duolingo) | Уникальность: AI-персонаж Арчи + игры |

---

## 12. Аналитика

### Метрики (Firebase)
| Метрика | Цель |
|---------|------|
| DAU/MAU | > 30% (месячная) |
| Retention D1 | > 40% |
| Retention D7 | > 15% |
| Avg. session length | > 5 мин |
| Quiz completion rate | > 70% |
| Chat messages / user | > 5 / день |

### A/B Tests
- Streak vs XP как primary motivator
- AI-ответы: короткие vs подробные
- Push frequency: 1 vs 2 vs 3 / день

---

*Документ составлен на основе:*
- *Анализа Telegram-бота FunnyEnglish*
- *Исследования gamified language learning apps (Duolingo, Memrise, Drops)*
- *Открытых API: dictionaryapi.dev, FreeDictionaryAPI*
- *Android Skills: Clean Architecture, Compose UI, MVI, Material 3*
