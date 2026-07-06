"""Test Telegram Bot inline keyboard generation."""
import sys
sys.path.insert(0, r"C:\Users\etaba\funnyenglish")

from bot import get_ttt_keyboard, REPLY_KEYBOARD

def test_ttt_keyboard():
    # Empty board
    board = [' '] * 9
    kb = get_ttt_keyboard(board)
    assert len(kb.inline_keyboard) == 3, f"Expected 3 rows, got {len(kb.inline_keyboard)}"
    for row in kb.inline_keyboard:
        assert len(row) == 3, f"Expected 3 cols per row, got {len(row)}"
    
    # Check callback_data for first row
    for i, btn in enumerate(kb.inline_keyboard[0]):
        assert btn.callback_data == f"ttt_move_{i}", f"Bad callback: {btn.callback_data}"
    
    print("✅ Empty board keyboard: 3x3, callback_data ttt_move_0..8")

def test_occupied_board():
    board = ['X', 'O', ' '] + [' '] * 6
    kb = get_ttt_keyboard(board)
    assert kb.inline_keyboard[0][0].text == "❌"
    assert kb.inline_keyboard[0][0].callback_data.startswith("ttt_nop_")
    assert kb.inline_keyboard[0][1].text == "⭕"
    assert kb.inline_keyboard[0][2].text == "3"
    print("✅ Occupied board: X=❌, O=⭕, empty=number")

def test_reply_keyboard():
    assert REPLY_KEYBOARD is not None
    assert len(REPLY_KEYBOARD.keyboard) == 2
    assert REPLY_KEYBOARD.resize_keyboard == True
    assert REPLY_KEYBOARD.one_time_keyboard == False
    print("✅ Reply keyboard: 2 rows, resize=True, one_time=False")

def test_chess_unicode():
    from bot import CHESS_UNICODE_PIECES
    assert CHESS_UNICODE_PIECES['K'] == '♔'
    assert CHESS_UNICODE_PIECES['Q'] == '♕'
    assert CHESS_UNICODE_PIECES['k'] == '♚'
    assert CHESS_UNICODE_PIECES['p'] == '♟'
    print("✅ Chess Unicode mapping correct")

if __name__ == "__main__":
    test_ttt_keyboard()
    test_occupied_board()
    test_reply_keyboard()
    test_chess_unicode()
    print("\n🎉 All inline keyboard tests passed!")
