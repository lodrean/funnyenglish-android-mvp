# FunnyEnglish — Code Style & Conventions

## Kotlin (Android)
- **Code style**: Official Kotlin code style (`kotlin.code.style=official` in `gradle.properties`)
- **Language**: Kotlin 2.2.10, JVM target 17
- **Naming**:
  - Packages: `com.funnyenglish.*`
  - Composables: `PascalCase`, descriptive (e.g. `QuizScreen`, `PriceInput`)
  - ViewModels: `*ViewModel` suffix
  - Screens: `*Screen` composable + `*ViewModel`
  - State classes: `*State` (data class)
  - Action classes: `*Action` (sealed class / data class)
  - Event classes: `*Event` (sealed class)
- **Architecture**: Clean Architecture with feature modules
  - `domain` — UseCases, Repository interfaces, Models
  - `data` — Repository implementations, DTOs, mappers, Room entities, Ktor APIs
  - `presentation` — ViewModels, State, Action, Event, UI models, UiText
  - `design-system` — Shared Compose components, theme, colors, typography
- **DI**: Koin. Modules per layer/feature. `koinViewModel()` in composables.
- **Async**: Kotlin Coroutines + Flow. `viewModelScope` for VM work. `StateFlow` for UI state.
- **Tests**: JUnit 5 (Jupiter), Turbine for Flow assertions, MockK for mocking, `UnconfinedTestDispatcher` for coroutine tests. Tests located in `src/test/java/...`.
- **Comments**: Minimal; prefer self-documenting names. Docstrings not heavily used.
- **Imports**: No wildcard imports.

## Python (Telegram Bot)
- **Style**: No strict formatter configured (black/flake8 absent). Keep consistent PEP 8-ish style.
- **Async**: All handlers are `async def` using `python-telegram-bot` v20+.
- **Naming**: `snake_case` for functions/variables, `PascalCase` for classes (`TicTacToe`, `Chess`).
- **State**: Global in-memory `GAMES` dict for active game sessions.
- **Tests**: `test_bot.py` uses mocked `Update`, `Chat`, `User`, `Message` objects.

## C++ (TicTacToe)
- **Standard**: C++11
- **Naming**: `camelCase` methods, `PascalCase` class (`TicTacToe`), member variables without explicit prefix.
- **Platform**: Windows-only (`windows.h` for console colors). UTF-8 output.
- **Build**: Single-file, no build system. Manual compilation with `cl.exe` or `g++`.

## General
- **Version control**: Git. Do NOT commit `.env`, tokens, or `local.properties`.
- **AGENTS.md**: Keep updated if build steps or project structure change.
