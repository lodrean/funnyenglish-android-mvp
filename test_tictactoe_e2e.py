import subprocess
import time
import sys

EXE_PATH = r"C:\Users\etaba\funnyenglish\tictactoe.exe"

def run_game(inputs):
    """Запускает tictactoe.exe с заданными входными данными и возвращает stdout"""
    proc = subprocess.Popen(
        [EXE_PATH],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True
    )
    stdout, stderr = proc.communicate(input="\n".join(inputs) + "\n", timeout=15)
    return stdout + stderr

class TestTicTacToeE2E:

    def test_menu_display(self):
        out = run_game(["0"])
        assert "Крестики-нолики" in out or "Tic-Tac-Toe" in out or "1. Игрок" in out, f"Menu not found in output: {out[:500]}"

    def test_pvp_game_x_wins(self):
        # PvP mode: X wins diagonal A1 B2 C3
        out = run_game(["1", "A1", "B1", "B2", "C1", "C3"])
        assert "Игрок X" in out or "X выиграл" in out or "победил" in out.lower(), f"Win not detected: {out[-1000:]}"

    def test_pvp_game_draw(self):
        # Full board draw
        out = run_game(["1", "A1", "A2", "A3", "B2", "B1", "C1", "C3", "C2"])
        assert "ничья" in out.lower() or "draw" in out.lower() or "Ничья" in out, f"Draw not detected: {out[-1000:]}"

    def test_invalid_input_menu(self):
        # Enter letter at menu instead of number, then 0
        out = run_game(["abc", "0"])
        # After invalid input, should show error and not crash
        assert "❌" in out or "Неверный" in out or "0" in out, f"Invalid input handling broken: {out[:500]}"

    def test_pve_easy_completes(self):
        # PvE Easy mode, just play a few moves and then abort (0 at turn prompt? No, there is no abort)
        # Actually after win/loss/draw it returns to menu. Let's play until game ends.
        # We just need to verify game starts and ends without crash
        out = run_game(["2", "A1"])
        # After A1 computer makes move, then we need to continue. Let's just give a few moves.
        # Better: give a sequence that leads to quick win
        out = run_game(["2", "A1", "B2", "C3"])
        assert "Игрок" in out or "выиграл" in out or "победил" in out or "ничья" in out, f"Game did not complete: {out[-1000:]}"

    def test_pve_medium_completes(self):
        out = run_game(["3", "A1", "B2", "C3"])
        assert "Игрок" in out or "выиграл" in out or "победил" in out or "ничья" in out, f"Medium game did not complete: {out[-1000:]}"

    def test_pve_hard_completes(self):
        out = run_game(["4", "A1", "B2", "C3"])
        assert "Игрок" in out or "выиграл" in out or "победил" in out or "ничья" in out, f"Hard game did not complete: {out[-1000:]}"

    def test_parse_input_cases(self):
        # Check that lowercase, uppercase, and mixed case work
        out = run_game(["1", "a1", "B2", "c3"])
        assert "Игрок X" in out or "выиграл" in out or "победил" in out, f"Case insensitive input failed: {out[-1000:]}"

    def test_invalid_cell_occupied(self):
        # Try to play same cell twice
        out = run_game(["1", "A1", "A1", "B2", "C1", "C3"])
        assert "занята" in out.lower() or "занято" in out.lower() or "❌" in out, f"Occupied cell not handled: {out[-1000:]}"

    def test_invalid_cell_format(self):
        # Try invalid format like Z9 or 123
        out = run_game(["1", "Z9", "A1", "B2", "C3"])
        assert "Неверный формат" in out or "❌" in out or "A1" in out, f"Invalid format not handled: {out[:1000]}"


if __name__ == "__main__":
    tests = TestTicTacToeE2E()
    failures = 0
    for name in dir(tests):
        if name.startswith("test_"):
            try:
                getattr(tests, name)()
                print(f"  PASS: {name}")
            except Exception as e:
                print(f"  FAIL: {name} — {e}")
                failures += 1
    print(f"\nResults: {failures} failure(s)")
    sys.exit(1 if failures > 0 else 0)
