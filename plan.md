# Plan: UX/UI Improvements for FunnyEnglish

## Stage 1: Telegram Bot — Inline Keyboard & UX (3–4h)

### 1.1 Inline Keyboard for TicTacToe
- Replace `/move <pos>` text input with `InlineKeyboardMarkup` 3×3 grid
- Callback data: `ttt_move_0` … `ttt_move_8`
- Show New Game button after win/draw
- Update `ttt_start` and `ttt_move` to use `reply_markup`
- Add `CallbackQueryHandler` for inline buttons

### 1.2 Reply Keyboard (Persistent Menu)
- Add `ReplyKeyboardMarkup` with: 🎮 Games, 💬 Chat, 📚 Dictionary, ❓ Help
- Use `resize_keyboard=True`, `one_time_keyboard=False`

### 1.3 Bot Menu Commands (`set_my_commands`)
- `/start`, `/help`, `/ttt`, `/ttt_pvp`, `/chess`, `/reset`, `/stop`
- Register via `app.bot.set_my_commands` before polling

### 1.4 Chess Unicode Pieces in Images
- Replace letter notation (`R`, `N`, `B`) with Unicode chess symbols (`♖♘♗♕♔♙`/`♜♞♝♛♚♟`) in `create_chess_image`
- Use `unicodedata` or direct string literals; fallback to letters if font missing

### 1.5 Typing Indicator & Markdown
- Ensure `send_chat_action(ChatAction.TYPING)` is used before AI calls
- Use `parse_mode=ParseMode.MARKDOWN_V2` in `start`/`help` (already partially done)

## Stage 2: Android — Responsive & Data Binding (2–3h)

### 2.1 Adaptive TicTacToeBoard
- `Modifier.size(300.dp)` → `Modifier.fillMaxWidth(0.85f).aspectRatio(1f)`

### 2.2 Adaptive ChatBubble
- `Modifier.widthIn(max = 280.dp)` → `Modifier.fillMaxWidth(0.75f)` (or `LocalConfiguration` based)

### 2.3 ProfileScreen Data Binding
- Replace hardcoded "Ученик", "Уровень 3", "450 XP", "3 streak", "12 слов" with `state.*` fields
- Wire `ProfileViewModel` to provide real data (or mock data through state)

### 2.4 Achievements Locked State
- Add `isUnlocked` boolean to achievements list
- Gray out + lock icon for locked achievements

### 2.5 Haptic Feedback (optional, quick)
- Add `HapticFeedback` on quiz answer selection, TicTacToe move, chat send

## Stage 3: Admin Panel — Mobile & Polish (2–3h)

### 3.1 Mobile Responsive CSS
- `@media (max-width: 768px)` → collapsible sidebar (transform translateX)
- Hamburger button on mobile
- `content` margin-left → 0 on mobile, padding 16px

### 3.2 Table Overflow
- `.panel { overflow-x: auto; }`
- `.panel table { min-width: 600px; }` (or wider for games/chat)

### 3.3 Clickable Dashboard Cards
- Wrap cards in `<a>` tags or add `cursor: pointer` + hover effect
- Link to respective sections

### 3.4 Breadcrumbs
- Add breadcrumb nav under each page `<h1>`

### 3.5 Search Fields (client-side)
- Add `<input type="search">` above tables with `onkeyup` JS filter
- Lightweight, no backend changes needed

### 3.6 Login CSS Unification
- Move inline styles from `login.html` to `admin.css` or dedicated `login.css`

## Stage 4: C++ — Undo & Stats (if time permits, 2–3h)

### 4.1 Undo Stack
- `vector<vector<vector<char>>> boardHistory`
- `U` command in input → pop and restore

### 4.2 Stats File
- `stats.txt` with wins/losses/draws per difficulty
- Show on game over and in menu

### 4.3 ANSI Redraw (optional)
- Replace `system("cls")` with ANSI escape sequences or simple `\n` padding

## Execution Order

1. Telegram inline keyboard (highest impact)
2. Android responsive sizes + Profile binding
3. Admin responsive CSS + table overflow
4. C++ undo/stats (if time)
5. Cross-cutting: test all components

## Files to Modify

| File | Changes |
|------|---------|
| `bot.py` | Inline keyboard, reply keyboard, set_my_commands, chess unicode |
| `android/feature/games/GamesScreen.kt` | Adaptive board size |
| `android/feature/chat/ChatScreen.kt` | Adaptive bubble width |
| `android/feature/profile/ProfileScreen.kt` | Data binding, achievements state |
| `backend/src/main/resources/static/css/admin.css` | Mobile responsive, overflow, clickable cards |
| `backend/src/main/resources/templates/admin/login.html` | Extract CSS to admin.css |
| `backend/src/main/resources/templates/admin/dashboard.html` | Breadcrumbs, card links |
| `backend/src/main/resources/templates/admin/users.html` | Search input, breadcrumbs |
| `backend/src/main/resources/templates/admin/games.html` | Search input, breadcrumbs |
| `backend/src/main/resources/templates/admin/chat.html` | Search input, breadcrumbs |
| `tictactoe.cpp` | Undo, stats, ANSI redraw (optional) |
