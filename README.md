# FunnyEnglish — Full Stack Project

## Структура проекта

```
funnyenglish/
├── tictactoe.cpp          # C++ консольная игра (Windows)
├── tictactoe.exe          # Скомпилированный executable
├── bot.py                 # Python Telegram-бот (Игры + AI чат)
├── backend/               # Kotlin Spring Boot бэкенд + админ-панель
│   ├── Dockerfile
│   ├── build.gradle.kts
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/...   # REST API, JPA, Security, Thymeleaf
│   │   │   └── resources/   # application.yml, templates, static
│   └── build/libs/...       # Готовый JAR
├── android/               # Android-приложение (Jetpack Compose)
├── docker-compose.yml     # Docker orchestration (bot + backend)
├── requirements.txt       # Python зависимости
└── README.md              # Этот файл
```

---

## 1. C++ Tic-Tac-Toe (Windows)

Консольная игра «Крестики-нолики» с тремя уровнями AI, цветным выводом и меню на русском.

**Сборка (MSVC):**
```cmd
cl.exe tictactoe.cpp /EHsc /Fe:tictactoe.exe
```

**Запуск:**
```cmd
tictactoe.exe
```

---

## 2. Telegram Bot (Python)

Бот «Арчи» — игровой компаньон с AI-чатом (OpenAI GPT-3.5), крестики-нолики, шахматы.

**Локальный запуск:**
```powershell
$env:TELEGRAM_TOKEN = "<ВАШ_ТОКЕН>"
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python bot.py
```

**Docker:**
```bash
docker-compose up -d bot
```

---

## 3. Backend (Kotlin Spring Boot)

REST API + админ-панель для управления пользователями, играми и чатом.

**Стек:**
- Spring Boot 3.2 + Kotlin 1.9
- Spring Data JPA (H2 in-memory)
- Spring Security (Form-based auth)
- Thymeleaf (админ-панель)
- WebSocket (ready for real-time)

**API Endpoints:**
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/users` | Создать/обновить пользователя |
| GET | `/api/v1/users` | Список пользователей |
| GET | `/api/v1/users/{telegramId}` | Пользователь по Telegram ID |
| POST | `/api/v1/games` | Создать игровую сессию |
| GET | `/api/v1/games` | Все игры |
| GET | `/api/v1/games/active` | Активные игры |
| POST | `/api/v1/games/move` | Сделать ход |
| POST | `/api/v1/games/{id}/finish` | Завершить игру |
| POST | `/api/v1/chat/messages` | Отправить сообщение |
| GET | `/api/v1/chat/messages/recent` | Последние сообщения |

**Админ-панель:**
| URL | Описание | Логин / Пароль |
|-----|----------|----------------|
| `/admin/login` | Страница входа | |
| `/admin/dashboard` | Dashboard со статистикой | admin / admin123 |
| `/admin/users` | Список пользователей | |
| `/admin/games` | Игры (все + активные) | |
| `/admin/chat` | История сообщений | |
| `/h2-console` | H2 Database Console | (без пароля) |

**Сборка:**
```bash
cd backend
./gradlew bootJar
```

**Запуск:**
```bash
java -jar backend/build/libs/funnyenglish-backend-1.0.0.jar
```

**Docker:**
```bash
docker-compose up -d backend
```

Приложение стартует на `http://localhost:8080`.

---

## 4. Android (Jetpack Compose)

Мобильное приложение для изучения английского.

**Модули:**
- `:app` — точка входа, навигация
- `:core:{domain,data,presentation,design-system}` — общие слои
- `:feature:{home,dictionary,quiz,chat,games,profile}` — фичи

**Сборка:**
```bash
cd android
./gradlew :app:assembleDebug
```

---

## 5. Docker Compose (полный стек)

```bash
# Запуск всего стека
docker-compose up -d

# Только бэкенд + админка
docker-compose up -d backend

# Только бот
docker-compose up -d bot

# Остановка
docker-compose down
```

---

## Переменные окружения (.env)

```env
TELEGRAM_TOKEN=your_telegram_bot_token
OPENAI_API_KEY=your_openai_api_key
```

---

## Быстрый старт

1. **C++ игра:** `tictactoe.exe` (готов к запуску)
2. **Бэкенд + админка:** `cd backend && ./gradlew bootJar && java -jar build/libs/*.jar`
3. **Бот:** `pip install -r requirements.txt && python bot.py`
4. **Android:** `cd android && ./gradlew :app:assembleDebug`
