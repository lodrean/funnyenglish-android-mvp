# Telegram Bot с играми в Telegram

Это Telegram-бот на Python с встроенными играми: Крестики-нолики (PvP и против бота), шахматы и другие команды.

**Файлы проекта**
- [bot.py](bot.py) : главный скрипт бота с логикой всех игр
- [requirements.txt](requirements.txt) : зависимости Python
- [Dockerfile](Dockerfile) : сборка Docker образа
- [docker-compose.yml](docker-compose.yml) : запуск в Docker

**Требования**
- Python 3.8+ (для локального запуска)
- Docker (опционально)

**Получить токен**
1. Создайте бота через BotFather в Telegram и получите токен.

**Запуск локально (рекомендуется виртуальное окружение)**

PowerShell (одноразово для текущей сессии):
```powershell
$env:TELEGRAM_TOKEN = "<ВАШ_ТОКЕН>"
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install --upgrade pip
pip install -r requirements.txt
python bot.py
```

cmd.exe (одноразово для текущей сессии):
```cmd
set TELEGRAM_TOKEN=<ВАШ_ТОКЕН>
python -m venv .venv
.\.venv\Scripts\activate.bat
python -m pip install --upgrade pip
pip install -r requirements.txt
python bot.py
```

Если хотите установить переменную окружения глобально (PowerShell):
```powershell
setx TELEGRAM_TOKEN "<ВАШ_ТОКЕН>"
```
(после `setx` откройте новый терминал)

**Примечания**
- Для развёртывания на сервере можно использовать процессы/службы (Windows Service, NSSM) или контейнер.
- Токен храните в безопасности — не коммитьте его в репозиторий.

## Запуск в Docker

Если установлен Docker, можно запустить бота в контейнере:

**Способ 1: docker run (быстрый)**
```bash
docker build -t tictactoe-bot .
docker run -e TELEGRAM_TOKEN="8538430573:AAEwnoP95nZXUYE8sqUJrAvJU5oE0Pn3zc4" tictactoe-bot
```

**Способ 2: docker-compose (удобнее)**

Создайте файл `.env` в папке проекта:
```
TELEGRAM_TOKEN=8538430573:AAEwnoP95nZXUYE8sqUJrAvJU5oE0Pn3zc4
```

Затем:
```bash
docker-compose up -d
```

Для остановки:
```bash
docker-compose down
```

**Преимущества Docker:**
- Бот работает в изолированном контейнере (независимо от ОС).
- Легче развертить на VPS или облачных платформах.
- Не нужна локальная виртуальная среда.

Если хотите — могу добавить: webhook-версию, автозапуск как службу, или интеграцию с вашим `tictactoe.cpp` (запуск .exe из бота).

## Команды бота

### Основные команды
- `/start` — приветствие и справка
- `/help` — полная справка по командам
- `/ping` — проверка работы бота
- `/echo <текст>` — повторить текст (эхо)

### Крестики-нолики против бота
- `/ttt` — начать новую игру против бота
- `/board` — показать текущую доску
- `/move <позиция>` — сделать ход
- `/stop` — остановить игру

**Форматы позиций:**
- Цифры: `1`–`9` (стандартная раскладка)
- Буквы: `A1`, `A2`, ... `C3`

Пример:
```
/ttt          → Игра начата
/move 5       → X в центр
/move B1      → (ответ бота)
```

### Крестики-нолики на двоих (в группе)
- `/ttt_pvp` — первый игрок начинает игру
- `/join_ttt` — второй игрок присоединяется
- `/move_pvp <позиция>` — ход игрока

Пример (в групповом чате):
```
Игрок 1: /ttt_pvp
Игрок 2: /join_ttt
Игрок 1: /move_pvp 5
Игрок 2: /move_pvp 1
```

### Шахматы (PvP)
- `/chess` — первый игрок начинает партию
- `/join_chess` — второй игрок присоединяется
- `/move_chess <от> <к>` — ход (алгебраическая нотация)

Пример:
```
Игрок 1: /chess
Игрок 2: /join_chess
Игрок 1: /move_chess e2 e4
Игрок 2: /move_chess e7 e5
```

### Управление играми
- `/stop` — завершить текущую игруДоска отображается с номерами пустых клеток 1..9, которые соответствуют позициям слева направо и сверху вниз.

## Крестики-Нолики на двоих (PvP)

Версия для двух игроков в одном чате/группе. Команды:

- `/ttt_pvp` — начать новую игру (первый игрок)
- `/join_ttt` — присоединиться ко второму игроку
- `/move_pvp <позиция>` — сделать ход
- `/stop` — остановить игру

**Пример игры:**

```
Игрок 1: /ttt_pvp
Бот: "Игрок 1 (X): ..., Ожидание игрока..."
Игрок 2: /join_ttt
Бот: "✅ Игрок 2 присоединился!"
Игрок 1: /move_pvp 5
Игрок 2: /move_pvp 1
... и так далее
```

## Шахматы на двоих

Базовая поддержка шахмат на двоих. Команды:

- `/chess` — начать новую партию (первый игрок — белые)
- `/join_chess` — присоединиться (второй игрок — чёрные)
- `/move_chess <от> <к>` — сделать ход (алгебраическая нотация)
- `/stop` — остановить партию

**Пример:**

```
Игрок 1: /chess
Игрок 2: /join_chess
Игрок 1: /move_chess e2 e4
Игрок 2: /move_chess e7 e5
```

**Примечание:** Текущая реализация использует упрощённую нотацию. Для полной поддержки шахмат нужно интегрировать библиотеку `python-chess`.

---

## Android MVP

Мобильное приложение для изучения английского на Android (Jetpack Compose).

### Модули
- `:app` — точка входа, навигация
- `:core:{design-system,domain,data,presentation}` — общие слои
- `:feature:{home,dictionary,quiz,chat,games,profile}` — фичи

### Технологии
- Jetpack Compose + Material 3
- Koin DI
- Room + Ktor Client
- MediaPipe LLM Inference (Gemma 2B on-device)
- Play Asset Delivery (модель AI)
- AppTracer (crash reporting)

### Сборка
```bash
cd android
./gradlew :app:assembleDebug
```
