#include <iostream>
#include <vector>
#include <ctime>
#include <cstdlib>
#include <cstdio>

using namespace std;

// Цвета для консоли (Windows)
#ifdef _WIN32
    #include <windows.h>
    void setColor(int color) {
        SetConsoleTextAttribute(GetStdHandle(STD_OUTPUT_HANDLE), color);
    }
#else
    void setColor(int color) {}
#endif

// Константы цветов
const int COLOR_DEFAULT = 7;
const int COLOR_RED = 12;
const int COLOR_GREEN = 10;
const int COLOR_YELLOW = 14;
const int COLOR_CYAN = 11;

class TicTacToe {
private:
    vector<vector<char>> board;
    char currentPlayer;
    int moves;
    vector<vector<vector<char>>> history;
    vector<char> historyPlayers;
    vector<int> historyMoves;
    
public:
    TicTacToe() {
        resetBoard();
    }
    
    void resetBoard() {
        board = vector<vector<char>>(3, vector<char>(3, ' '));
        currentPlayer = 'X';
        moves = 0;
        history.clear();
        historyPlayers.clear();
        historyMoves.clear();
    }
    
    void saveState() {
        history.push_back(board);
        historyPlayers.push_back(currentPlayer);
        historyMoves.push_back(moves);
    }
    
    bool undo() {
        if (history.empty()) return false;
        board = history.back();
        currentPlayer = historyPlayers.back();
        moves = historyMoves.back();
        history.pop_back();
        historyPlayers.pop_back();
        historyMoves.pop_back();
        return true;
    }
    
    void displayBoard() {
        cout << "\n";
        setColor(COLOR_CYAN);
        cout << "     1   2   3\n";
        cout << "   +---+---+---+\n";
        
        for (int i = 0; i < 3; i++) {
            setColor(COLOR_CYAN);
            cout << " " << (char)('A' + i) << " |";
            
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == 'X') {
                    setColor(COLOR_RED);
                    cout << " X ";
                } else if (board[i][j] == 'O') {
                    setColor(COLOR_GREEN);
                    cout << " O ";
                } else {
                    setColor(COLOR_DEFAULT);
                    cout << "   ";
                }
                setColor(COLOR_CYAN);
                cout << "|";
            }
            cout << "\n   +---+---+---+\n";
        }
        setColor(COLOR_DEFAULT);
        cout << "\n";
    }
    
    bool makeMove(int row, int col) {
        if (row < 0 || row >= 3 || col < 0 || col >= 3) {
            return false;
        }
        if (board[row][col] != ' ') {
            return false;
        }
        
        saveState();
        board[row][col] = currentPlayer;
        moves++;
        return true;
    }
    
    bool checkWin() {
        // Проверка строк
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != ' ' && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return true;
            }
        }
        // Проверка столбцов
        for (int j = 0; j < 3; j++) {
            if (board[0][j] != ' ' && board[0][j] == board[1][j] && board[1][j] == board[2][j]) {
                return true;
            }
        }
        // Проверка диагоналей
        if (board[0][0] != ' ' && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return true;
        }
        if (board[0][2] != ' ' && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return true;
        }
        return false;
    }
    
    bool isDraw() {
        return moves >= 9;
    }
    
    void switchPlayer() {
        currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
    }
    
    char getCurrentPlayer() {
        return currentPlayer;
    }
    
    // Проверка, может ли игрок выиграть следующим ходом
    pair<int, int> findWinningMove(char player) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') {
                    board[i][j] = player;
                    bool win = checkWin();
                    board[i][j] = ' ';
                    if (win) {
                        return {i, j};
                    }
                }
            }
        }
        return {-1, -1};
    }
    
    // Ход компьютера (уровень сложности: 1-лёгкий, 2-средний, 3-сложный)
    void computerMove(int difficulty) {
        int row, col;
        
        if (difficulty >= 2) {
            // 1. Попытка выиграть
            auto winMove = findWinningMove('O');
            if (winMove.first != -1) {
                row = winMove.first;
                col = winMove.second;
                makeMove(row, col);
                return;
            }
        }
        
        if (difficulty >= 2) {
            // 2. Блокировать победу противника
            auto blockMove = findWinningMove('X');
            if (blockMove.first != -1) {
                row = blockMove.first;
                col = blockMove.second;
                makeMove(row, col);
                return;
            }
        }
        
        if (difficulty >= 3) {
            // 3. Занять центр
            if (board[1][1] == ' ') {
                makeMove(1, 1);
                return;
            }
            
            // 4. Занять углы
            vector<pair<int, int>> corners = {{0,0}, {0,2}, {2,0}, {2,2}};
            vector<pair<int, int>> availableCorners;
            for (auto& c : corners) {
                if (board[c.first][c.second] == ' ') {
                    availableCorners.push_back(c);
                }
            }
            if (!availableCorners.empty()) {
                int idx = rand() % availableCorners.size();
                makeMove(availableCorners[idx].first, availableCorners[idx].second);
                return;
            }
        }
        
        // Случайный ход
        vector<pair<int, int>> available;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') {
                    available.push_back({i, j});
                }
            }
        }
        
        if (!available.empty()) {
            int idx = rand() % available.size();
            makeMove(available[idx].first, available[idx].second);
        }
    }
    
    bool parseInput(const string& input, int& row, int& col) {
        if (input.length() != 2) return false;
        
        char r = toupper(input[0]);
        char c = input[1];
        
        if (r < 'A' || r > 'C') return false;
        if (c < '1' || c > '3') return false;
        
        row = r - 'A';
        col = c - '1';
        return true;
    }
};


struct Stats {
    int wins[4];
    int losses[4];
    int draws[4];
    Stats() { for (int i = 0; i < 4; i++) wins[i] = losses[i] = draws[i] = 0; }
};

void loadStats(Stats& s) {
    FILE* f = fopen("stats.txt", "r");
    if (f) {
        for (int i = 0; i < 4; i++) if (fscanf(f, "%d", &s.wins[i]) != 1) s.wins[i] = 0;
        for (int i = 0; i < 4; i++) if (fscanf(f, "%d", &s.losses[i]) != 1) s.losses[i] = 0;
        for (int i = 0; i < 4; i++) if (fscanf(f, "%d", &s.draws[i]) != 1) s.draws[i] = 0;
        fclose(f);
    }
}

void saveStats(const Stats& s) {
    FILE* f = fopen("stats.txt", "w");
    if (f) {
        for (int i = 0; i < 4; i++) fprintf(f, "%d ", s.wins[i]);
        fprintf(f, "\n");
        for (int i = 0; i < 4; i++) fprintf(f, "%d ", s.losses[i]);
        fprintf(f, "\n");
        for (int i = 0; i < 4; i++) fprintf(f, "%d ", s.draws[i]);
        fprintf(f, "\n");
        fclose(f);
    }
}

void showStats(const Stats& s) {
    const char* names[] = {"PvP", "Лёгкий", "Средний", "Сложный"};
    cout << "\n=== 📊 Статистика ===\n";
    for (int i = 0; i < 4; i++) {
        int total = s.wins[i] + s.losses[i] + s.draws[i];
        if (total > 0) {
            cout << names[i] << ": Побед " << s.wins[i] 
                 << ", Поражений " << s.losses[i] 
                 << ", Ничьих " << s.draws[i] 
                 << " (Всего " << total << ")\n";
        }
    }
    cout << "====================\n\n";
}

void showTitle() {
    setColor(COLOR_YELLOW);
    cout << R"(
  _____  _         _____         _         _             
 |_   _|(_) _ __  |_   _| _   _ | |  ___  | |  ___   ___ 
   | |  | || '__|   | |  | | | || | / _ \ | | / _ \ / __|
   | |  | || |      | |  | |_| || || (_) || ||  __/| (__ 
   |_|  |_||_|      |_|   \__, ||_| \___/ |_| \___| \___|
                          |___/                          
)";
    setColor(COLOR_DEFAULT);
    cout << "\n";
}

void showMenu() {
    setColor(COLOR_CYAN);
    cout << "╔═══════════════════════════════════════╗\n";
    cout << "║         ГЛАВНОЕ МЕНЮ                  ║\n";
    cout << "╠═══════════════════════════════════════╣\n";
    cout << "║  1. Игрок vs Игрок                    ║\n";
    cout << "║  2. Игрок vs Компьютер (Лёгкий)       ║\n";
    cout << "║  3. Игрок vs Компьютер (Средний)      ║\n";
    cout << "║  4. Игрок vs Компьютер (Сложный)      ║\n";
    cout << "║  0. Выход                             ║\n";
    cout << "╚═══════════════════════════════════════╝\n";
    setColor(COLOR_DEFAULT);
    cout << "\nВыберите режим: ";
}

char playGame(int mode) {
    TicTacToe game;
    string input;
    int row, col;
    bool vsComputer = (mode > 1);
    int difficulty = mode - 1; // 1=лёгкий, 2=средний, 3=сложный
    
    while (true) {
        game.displayBoard();
        
        setColor(COLOR_YELLOW);
        cout << "Ход игрока " << game.getCurrentPlayer();
        if (vsComputer && game.getCurrentPlayer() == 'O') {
            cout << " (Компьютер)";
        }
        cout << "  [U — отменить ход]\n";
        setColor(COLOR_DEFAULT);
        
        if (vsComputer && game.getCurrentPlayer() == 'O') {
            // Ход компьютера
            cout << "Компьютер думает...\n";
            system("timeout /t 1 >nul"); // Пауза 1 секунда
            game.saveState();
            game.computerMove(difficulty);
        } else {
            // Ход игрока
            cout << "Введите ход (например: A1, B2, C3) или U для отмены: ";
            cin >> input;
            
            if (!cin) {
                cin.clear();
                cin.ignore(10000, '\n');
                setColor(COLOR_RED);
                cout << "❌ Ошибка ввода. Попробуйте снова.\n";
                setColor(COLOR_DEFAULT);
                system("pause");
                continue;
            }
            
            if (input == "U" || input == "u") {
                if (game.undo()) {
                    setColor(COLOR_GREEN);
                    cout << "✅ Ход отменён.\n";
                    setColor(COLOR_DEFAULT);
                } else {
                    setColor(COLOR_RED);
                    cout << "❌ Нет ходов для отмены.\n";
                    setColor(COLOR_DEFAULT);
                }
                system("pause");
                continue;
            }
            
            if (!game.parseInput(input, row, col)) {
                setColor(COLOR_RED);
                cout << "❌ Некорректный ввод! Используйте формат: A1, B2, C3\n";
                setColor(COLOR_DEFAULT);
                system("pause");
                continue;
            }
            
            if (!game.makeMove(row, col)) {
                setColor(COLOR_RED);
                cout << "❌ Эта клетка уже занята или неверные координаты!\n";
                setColor(COLOR_DEFAULT);
                system("pause");
                continue;
            }
        }
        
        // Проверка победы
        if (game.checkWin()) {
            game.displayBoard();
            setColor(COLOR_GREEN);
            cout << "╔═══════════════════════════════════════╗\n";
            cout << "║     🎉 ИГРОК " << game.getCurrentPlayer() << " ПОБЕДИЛ! 🎉      ║\n";
            cout << "╚═══════════════════════════════════════╝\n";
            setColor(COLOR_DEFAULT);
            return game.getCurrentPlayer();
        }
        
        // Проверка ничьей
        if (game.isDraw()) {
            game.displayBoard();
            setColor(COLOR_YELLOW);
            cout << "╔═══════════════════════════════════════╗\n";
            cout << "║          🤝 НИЧЬЯ! 🤝                ║\n";
            cout << "╚═══════════════════════════════════════╝\n";
            setColor(COLOR_DEFAULT);
            return 'D';
        }
        
        game.switchPlayer();
    }
}

int main() {
    srand(time(nullptr));
    
    #ifdef _WIN32
        system("chcp 65001 >nul"); // UTF-8
    #endif
    
    Stats stats;
    loadStats(stats);
    
    int choice;
    
    while (true) {
        system("cls");
        showTitle();
        showMenu();
        showStats(stats);
        cin >> choice;
        
        if (!cin) {
            cin.clear();
            cin.ignore(10000, '\n');
            setColor(COLOR_RED);
            cout << "\n❌ Неверный выбор! Введите число от 0 до 4.\n";
            setColor(COLOR_DEFAULT);
            system("pause");
            continue;
        }
        
        if (choice == 0) {
            setColor(COLOR_CYAN);
            cout << "\nСпасибо за игру! До свидания! 👋\n";
            setColor(COLOR_DEFAULT);
            break;
        }
        
        if (choice < 1 || choice > 4) {
            setColor(COLOR_RED);
            cout << "\n❌ Неверный выбор!\n";
            setColor(COLOR_DEFAULT);
            system("pause");
            continue;
        }
        
        char result = playGame(choice);
        if (result == 'D') {
            stats.draws[choice]++;
        } else if (result == 'X') {
            stats.wins[choice]++;
        } else {
            stats.losses[choice]++;
        }
        saveStats(stats);
        
        cout << "\n";
        system("pause");
    }
    
    return 0;
}
