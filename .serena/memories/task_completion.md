# FunnyEnglish — Task Completion Checklist

When a coding task is finished, perform the following steps before declaring it done:

## 1. Build Verification
- **Android**: Run `.\gradlew :app:assembleDebug` and ensure `BUILD SUCCESSFUL`.
- **Python**: Run `python syntax_check.py` to verify imports and AST validity.
- **C++**: Re-compile `tictactoe.cpp` if modified.

## 2. Testing
- **Android**: Run unit tests for affected modules:
  ```powershell
  .\gradlew :feature:<name>:test
  .\gradlew test   # all modules
  ```
  If UI/instrumentation tests were modified, run `.\gradlew :app:connectedDebugAndroidTest` on an emulator/device.
- **Python**: Run `python test_bot.py` to verify command handlers.
- **C++**: Manual play-test (no automated test suite).

## 3. Lint / Quality Checks
- **Android**: Run `.\gradlew :app:lintDebug`.
- **Python**: No linter configured yet (optional: run `flake8` or `black` if added).
- **C++**: No linter configured.

## 4. Documentation Update
- If build steps, project structure, or runtime behavior changed, update:
  - `AGENTS.md` (agent-focused instructions)
  - `README.md` (human-facing docs)

## 5. Git
```powershell
git add .
git status
# Review staged changes
git commit -m "<clear description>"
# Do NOT push unless explicitly asked by the user
```

## 6. Docker (if bot changed)
```powershell
docker build -t funnyenglish-bot .
```
Ensure the image builds without errors.

## 7. Final Review
- No hardcoded secrets or tokens committed.
- No `local.properties` or `.env` changes committed (unless intentional and safe).
- Affected `AGENTS.md` / `README.md` are up to date.
