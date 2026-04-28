# Tic-Tac-Toe Project

## Project Overview

This is a simple console-based Tic-Tac-Toe (Крестики-Нолики) game written in C++. It is a single-file project with no external dependencies.

The game supports multiple modes:
- Player vs Player (Игрок vs Игрок)
- Player vs Computer with three difficulty levels:
  - Easy (Лёгкий) - random moves only
  - Medium (Средний) - can win and block opponent
  - Hard (Сложный) - strategic play with center/corner priority

## Technology Stack

- **Language**: C++ (C++11 or later recommended)
- **Platform**: Windows (uses Windows-specific console APIs)
- **Build System**: None (single file, manual compilation)

## Project Structure

```
funnyenglish/
├── tictactoe.cpp      # Main source file (single file containing all game logic)
├── tictactoe.exe      # Compiled Windows executable
├── tictactoe.obj      # Object file (MSVC)
└── AGENTS.md          # This file
```

## Build Instructions

### Using MSVC (Visual Studio Compiler)

```cmd
cl.exe tictactoe.cpp /EHsc /Fe:tictactoe.exe
```

### Using MinGW/g++

```bash
g++ tictactoe.cpp -o tictactoe.exe
```

### Build Flags

- `/EHsc` (MSVC) - Enable C++ exception handling
- No additional libraries required
- No special linker flags needed

## Code Organization

The source file is organized as follows:

1. **Includes and Color Setup** (lines 1-24):
   - Standard library headers (`<iostream>`, `<vector>`, `<ctime>`, `<cstdlib>`)
   - Windows-specific console color handling

2. **TicTacToe Class** (lines 25-214):
   - `board`: 3x3 game board represented as vector of vectors
   - `currentPlayer`: 'X' or 'O'
   - `moves`: move counter for draw detection
   - Core methods:
     - `resetBoard()`: Initialize/reset game state
     - `displayBoard()`: Render board with colors
     - `makeMove(row, col)`: Place mark on board
     - `checkWin()`: Check winning conditions
     - `isDraw()`: Check for draw
     - `computerMove(difficulty)`: AI logic
     - `parseInput(input, row, col)`: Parse player input (e.g., "A1", "B2")

3. **UI Functions** (lines 216-243):
   - `showTitle()`: ASCII art title
   - `showMenu()`: Main menu display

4. **Game Loop** (lines 245-317):
   - `playGame(mode)`: Main game flow

5. **Entry Point** (lines 319-353):
   - `main()`: Application initialization and menu loop

## Input Format

Players enter moves using a coordinate system:
- **Format**: Letter (A-C) followed by Number (1-3)
- **Examples**: `A1` (top-left), `B2` (center), `C3` (bottom-right)
- Case insensitive (a1, A1, A1 all valid)

```
     1   2   3
   +---+---+---+
 A |   |   |   |
   +---+---+---+
 B |   |   |   |
   +---+---+---+
 C |   |   |   |
   +---+---+---+
```

## AI Difficulty Levels

1. **Easy (1)**: Makes completely random moves
2. **Medium (2)**: 
   - Will take winning moves if available
   - Will block opponent's winning moves
   - Otherwise random
3. **Hard (3)**:
   - Winning move detection
   - Opponent blocking
   - Prioritizes center square
   - Prioritizes corners over edges
   - Otherwise random

## Runtime Behavior

- The game uses Windows console color codes for visual feedback:
  - Red (12): X marks
  - Green (10): O marks
  - Cyan (11): Board grid
  - Yellow (14): Title and highlights
  - Default (7): Normal text

- Requires UTF-8 console support (set via `chcp 65001`)
- Uses `system("cls")` to clear screen between turns
- Uses `system("pause")` to wait for user input

## Language

The user interface is in **Russian** (Русский язык). All menu text, prompts, and messages are displayed in Russian using UTF-8 encoding.

## Testing

This project has no automated test suite. Testing is manual:
1. Compile the program
2. Run and test each game mode
3. Verify win detection for all rows, columns, and diagonals
4. Verify draw detection
5. Test input validation (invalid coordinates, occupied cells)

## Known Limitations

- Windows-only (relies on Windows console API)
- No persistent storage (scores not saved)
- No network multiplayer
- Fixed 3x3 board size
- AI is not perfect (does not use minimax algorithm)

## Security Considerations

- Uses `system()` calls which can be a security concern in production code
- No user input sanitization beyond basic validation
- Console encoding change affects entire console session
