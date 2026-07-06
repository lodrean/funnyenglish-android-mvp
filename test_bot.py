import unittest
from unittest.mock import MagicMock, patch, AsyncMock
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from bot import (
    TicTacToe, TicTacToePvP, Chess,
    create_ttt_image, create_chess_image,
    get_ai_response, clear_ai_history,
    start, help_command
)


class TestTicTacToePvE(unittest.TestCase):

    def test_initial_board(self):
        game = TicTacToe()
        self.assertEqual(game.board, [" "] * 9)
        self.assertEqual(game.current, "X")

    def test_pos_from_str_digit(self):
        game = TicTacToe()
        self.assertEqual(game.pos_from_str("5"), 4)
        self.assertEqual(game.pos_from_str("1"), 0)
        self.assertEqual(game.pos_from_str("9"), 8)
        self.assertIsNone(game.pos_from_str("0"))
        self.assertIsNone(game.pos_from_str("10"))

    def test_pos_from_str_coords(self):
        game = TicTacToe()
        self.assertEqual(game.pos_from_str("A1"), 0)
        self.assertEqual(game.pos_from_str("b2"), 4)
        self.assertEqual(game.pos_from_str("C3"), 8)
        self.assertIsNone(game.pos_from_str("D1"))
        self.assertIsNone(game.pos_from_str("A4"))

    def test_make_move(self):
        game = TicTacToe()
        self.assertTrue(game.make_move(0, "X"))
        self.assertEqual(game.board[0], "X")
        self.assertEqual(game.current, "O")
        self.assertFalse(game.make_move(0, "O"))  # occupied
        self.assertFalse(game.make_move(-1, "X"))  # invalid

    def test_valid_moves(self):
        game = TicTacToe()
        self.assertEqual(game.valid_moves(), list(range(9)))
        game.make_move(0, "X")
        self.assertEqual(game.valid_moves(), list(range(1, 9)))

    def test_check_winner_row(self):
        game = TicTacToe()
        for i in [0, 1, 2]:
            game.make_move(i, "X")
        self.assertEqual(game.check_winner(), "X")

    def test_check_winner_col(self):
        game = TicTacToe()
        game.make_move(1, "O")
        game.make_move(4, "O")
        game.make_move(7, "O")
        self.assertEqual(game.check_winner(), "O")

    def test_check_winner_diag(self):
        game = TicTacToe()
        for i in [0, 4, 8]:
            game.make_move(i, "X")
        self.assertEqual(game.check_winner(), "X")

    def test_check_winner_anti_diag(self):
        game = TicTacToe()
        for i in [2, 4, 6]:
            game.make_move(i, "O")
        self.assertEqual(game.check_winner(), "O")

    def test_draw(self):
        game = TicTacToe()
        moves = [0, 1, 2, 3, 4, 5, 6, 7, 8]
        marks = ["X", "O", "X", "X", "O", "O", "O", "X", "X"]
        for idx, m in enumerate(moves):
            game.make_move(m, marks[idx])
        self.assertEqual(game.check_winner(), "draw")

    def test_bot_move_wins(self):
        game = TicTacToe()
        game.board = ["O", "O", " ", "X", "X", " ", " ", " ", " "]
        move = game.bot_move()
        self.assertEqual(move, 2)
        self.assertEqual(game.board[2], "O")

    def test_bot_move_blocks(self):
        game = TicTacToe()
        game.board = ["X", "X", " ", " ", " ", " ", " ", " ", " "]
        move = game.bot_move()
        self.assertEqual(move, 2)
        self.assertEqual(game.board[2], "O")

    def test_bot_move_center(self):
        game = TicTacToe()
        move = game.bot_move()
        self.assertEqual(move, 4)

    def test_bot_move_corner(self):
        game = TicTacToe()
        game.board[4] = "X"
        move = game.bot_move()
        self.assertIn(move, [0, 2, 6, 8])

    def test_render(self):
        game = TicTacToe()
        game.board = ["X", " ", "O", " ", "X", " ", " ", " ", "O"]
        text = game.render()
        self.assertIn("X", text)
        self.assertIn("O", text)
        self.assertIn("2", text)  # empty cell shows position


class TestTicTacToePvP(unittest.TestCase):

    def test_turn_alternation(self):
        game = TicTacToePvP("u1", "u2")
        self.assertEqual(game.current_player, "X")
        game.make_move(0)
        self.assertEqual(game.current_player, "O")
        game.make_move(1)
        self.assertEqual(game.current_player, "X")

    def test_pvp_winner(self):
        game = TicTacToePvP("u1", "u2")
        game.make_move(0)  # X
        game.make_move(3)  # O
        game.make_move(1)  # X
        game.make_move(4)  # O
        game.make_move(2)  # X wins top row
        self.assertEqual(game.check_winner(), "X")

    def test_pvp_draw(self):
        game = TicTacToePvP("u1", "u2")
        moves = [0, 1, 2, 4, 3, 5, 7, 6, 8]
        for m in moves:
            game.make_move(m)
        self.assertEqual(game.check_winner(), "draw")

    def test_get_status(self):
        game = TicTacToePvP("Alice", "Bob")
        status = game.get_status()
        self.assertIn("Alice", status)
        self.assertIn("X", status)


class TestChess(unittest.TestCase):

    def test_initial_board(self):
        chess = Chess("white", "black")
        self.assertEqual(chess.board[0][0], 'r')
        self.assertEqual(chess.board[7][7], 'R')
        self.assertEqual(chess.current, "white")

    def test_parse_square(self):
        chess = Chess("w", "b")
        self.assertEqual(chess.parse_square("e2"), (6, 4))
        self.assertEqual(chess.parse_square("a8"), (0, 0))
        self.assertEqual(chess.parse_square("h1"), (7, 7))
        self.assertIsNone(chess.parse_square("i9"))
        self.assertIsNone(chess.parse_square("abc"))

    def test_make_move_invalid_format(self):
        chess = Chess("w", "b")
        ok, msg = chess.make_move("e2")
        self.assertFalse(ok)
        self.assertIn("Неверный формат", msg)

    def test_make_move_pawn(self):
        chess = Chess("w", "b")
        ok, msg = chess.make_move("e2e4")
        self.assertTrue(ok)
        self.assertIsNone(chess.board[6][4])  # e2 is now empty
        self.assertEqual(chess.board[4][4], 'P')  # e4 has white pawn
        self.assertEqual(chess.current, "black")

    def test_make_move_wrong_turn(self):
        chess = Chess("w", "b")
        ok, msg = chess.make_move("e7e5")
        self.assertFalse(ok)
        self.assertIn("Белые", msg)

    def test_make_move_own_piece_capture(self):
        chess = Chess("w", "b")
        ok, msg = chess.make_move("e2d2")
        self.assertFalse(ok)
        self.assertIn("свою фигуру", msg)


class TestImageGeneration(unittest.TestCase):

    def test_create_ttt_image(self):
        board = ["X"] + [" "] * 8
        img = create_ttt_image(board)
        self.assertIsInstance(img, bytes)
        self.assertGreater(len(img), 100)

    def test_create_ttt_image_highlight(self):
        board = [" "] * 9
        img = create_ttt_image(board, highlight_pos=4)
        self.assertIsInstance(img, bytes)
        self.assertGreater(len(img), 100)

    def test_create_chess_image(self):
        img = create_chess_image()
        self.assertIsInstance(img, bytes)
        self.assertGreater(len(img), 100)


class TestAI(unittest.TestCase):

    @patch.dict(os.environ, {"OPENAI_API_KEY": ""})
    def test_get_ai_response_no_key(self):
        result = get_ai_response(1, "hello")
        self.assertIn("недоступен", result)

    @patch.dict(os.environ, {"OPENAI_API_KEY": "fake_key"})
    @patch("bot.openai.OpenAI")
    def test_get_ai_response_with_key(self, mock_openai):
        mock_client = MagicMock()
        mock_response = MagicMock()
        mock_response.choices = [MagicMock()]
        mock_response.choices[0].message.content = "Привет!"
        mock_client.chat.completions.create.return_value = mock_response
        mock_openai.return_value = mock_client

        result = get_ai_response(1, "hello")
        self.assertEqual(result, "Привет!")
        # Verify history is maintained
        result2 = get_ai_response(1, "how are you?")
        mock_client.chat.completions.create.assert_called()

    def test_clear_ai_history(self):
        from bot import AI_CONVERSATIONS
        AI_CONVERSATIONS[999] = [{"role": "user", "content": "test"}]
        clear_ai_history(999)
        self.assertNotIn(999, AI_CONVERSATIONS)


class TestBotCommands(unittest.TestCase):

    def test_start_command(self):
        update = MagicMock()
        update.effective_user.first_name = "TestUser"
        update.message.reply_text = AsyncMock()
        import asyncio
        asyncio.run(start(update, MagicMock()))
        update.message.reply_text.assert_called_once()
        text = update.message.reply_text.call_args[0][0]
        self.assertIn("TestUser", text)
        self.assertIn("Арчи", text)

    def test_help_command(self):
        update = MagicMock()
        update.message.reply_text = AsyncMock()
        import asyncio
        asyncio.run(help_command(update, MagicMock()))
        update.message.reply_text.assert_called_once()
        text = update.message.reply_text.call_args[0][0]
        self.assertIn("команды", text.lower())


if __name__ == "__main__":
    unittest.main(verbosity=2)
