# FunnyEnglish — Suggested Commands

## Android (run from `android/` directory)

### Build
```powershell
# Debug APK
.\gradlew :app:assembleDebug

# Release APK (with ProGuard + resource shrinking)
.\gradlew :app:assembleRelease

# Build all modules
.\gradlew assembleDebug
```

### Testing
```powershell
# Run all unit tests across all modules
.\gradlew test

# Run tests for a specific feature module
.\gradlew :feature:quiz:test
.\gradlew :feature:profile:test
.\gradlew :feature:games:test

# Run connected (instrumentation) tests on a device/emulator
.\gradlew :app:connectedDebugAndroidTest
```

### Lint / Static Analysis
```powershell
# Android Lint
.\gradlew :app:lintDebug

# Note: detekt / ktlint are NOT currently configured in this project.
```

### Clean
```powershell
.\gradlew clean
```

## Python (run from project root)

### Environment Setup
```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install --upgrade pip
pip install -r requirements.txt
```

### Run Bot
```powershell
$env:TELEGRAM_TOKEN = "<TOKEN>"
python bot.py
```

### Testing & Validation
```powershell
# Run bot command tests
python test_bot.py

# Run syntax & import check
python syntax_check.py
```

### Docker
```powershell
# Build and run via docker-compose
$env:TELEGRAM_TOKEN = "<TOKEN>"
docker-compose up -d

# Or manual docker build
docker build -t funnyenglish-bot .
docker run -e TELEGRAM_TOKEN="<TOKEN>" funnyenglish-bot
```

## C++ (run from project root)

### MSVC
```cmd
cl.exe tictactoe.cpp /EHsc /Fe:tictactoe.exe
```

### MinGW / g++
```bash
g++ tictactoe.cpp -o tictactoe.exe
```

### Run
```cmd
tictactoe.exe
```

## Git (Windows)
```powershell
git status
git add .
git commit -m "message"
git push
```

## System Utilities (Windows)
```powershell
dir                              # list files
cd <path>                        # change directory
type <file>                      # show file contents
findstr "pattern" <file>         # search in file
where <command>                  # locate executable
```
