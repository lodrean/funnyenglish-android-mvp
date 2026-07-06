# FunnyEnglish Project Overview

## Purpose
FunnyEnglish is a multi-platform English learning project consisting of:
1. **Android App** — Jetpack Compose application with offline AI chat (Gemma 2B via MediaPipe), vocabulary quizzes, dictionary, games (tic-tac-toe), and progress tracking.
2. **Python Telegram Bot** — Bot with games: Tic-Tac-Toe (PvP and vs bot), Chess (PvP), echo, and other commands.
3. **C++ Console Game** — Windows-only Tic-Tac-Toe with colored console UI and three AI difficulty levels.
4. **Docker** — Containerized deployment for the Python bot.

## Tech Stack

### Android
- Language: Kotlin 2.2.10
- UI: Jetpack Compose + Material 3 (Compose BOM 2024.06.00)
- DI: Koin 3.5.6
- Local DB: Room 2.7.1
- Network: Ktor Client 2.3.11 + kotlinx.serialization
- On-device AI: MediaPipe LLM Inference (Gemma 2B)
- Asset Delivery: Play Asset Delivery (PAD)
- Analytics/Crash: AppTracer (RuStore) 1.0.8
- Build: Android Gradle Plugin 9.1.1, Gradle 9.x
- Target JVM: 17
- Compile SDK: 34, Min SDK: 24, Target SDK: 34
- Architecture: Clean Architecture with feature modules
  - `:app` — entry point, navigation
  - `:core:{design-system,domain,data,presentation,navigation,common}`
  - `:feature:{home,dictionary,quiz,chat,games,profile}`
  - `:model_asset_pack` — Play Asset Delivery module

### Python
- python-telegram-bot >= 20.0
- Pillow >= 10.0.0
- openai >= 1.0.0
- Runtime: Python 3.8+ (Docker uses 3.13-slim)

### C++
- C++11
- Windows console APIs (colors, UTF-8)
- Build: MSVC (`cl.exe`) or MinGW (`g++`)

### Docker
- `Dockerfile` for Python bot
- `docker-compose.yml` with env vars `TELEGRAM_TOKEN` and `OPENAI_API_KEY`

## Project Structure (root)
```
funnyenglish/
├── android/                    # Android Gradle project
│   ├── app/
│   ├── core/
│   ├── feature/
│   ├── model_asset_pack/
│   └── gradle/libs.versions.toml
├── bot.py                      # Python Telegram bot
├── test_bot.py                 # Bot command tests
├── syntax_check.py             # Bot syntax & import validation
├── requirements.txt            # Python deps
├── Dockerfile / docker-compose.yml
├── tictactoe.cpp               # C++ console game
├── tictactoe.exe               # Compiled Windows binary
├── AGENTS.md                   # C++ game agent docs
├── README.md                   # Project docs
└── .haft/                      # Haft governance config
```

## Key Design Patterns
- **Android**: MVI (State + Action + Event), Clean Architecture, Repository pattern, Koin modules per layer
- **Python**: Async handlers with python-telegram-bot, in-memory game state (`GAMES` dict)
- **C++**: OOP class `TicTacToe` with board state and AI logic
