#!/usr/bin/env python3
import os
import logging
import random
import io
from typing import Dict, List, Optional

from telegram import Update
from telegram.ext import ApplicationBuilder, CommandHandler, MessageHandler, filters, ContextTypes

# Image generation imports
from PIL import Image, ImageDraw, ImageFont

# AI integration
import openai

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# --- AI Character Configuration ---
AI_CHARACTER_PROMPT = """Ты — игровой бот по имени "Арчи" с характером весёлого и немного саркастичного друга. 

Твой стиль общения:
- Обращайся к пользователю на "ты", дружелюбно и непринуждённо
- Используй юмор, иногда слегка поддразнивай игрока
- Говори эмодзи, но не переборщи
- Если игрок проигрывает — подбодри его, но с иронией
- Если выигрывает — поздравь, но намекни, что тебе просто повезло
- Отвечай кратко, 1-3 предложения
- Ты любишь игры и всегда готов сыграть ещё партию

Ты умеешь играть в:
- Крестики-нолики (очень хорошо играешь)
- Шахматы (средний уровень)

Контекст: ты находишься в Telegram-боте, общаешься через чат."""

# Store conversation history per user (optional, for context)
AI_CONVERSATIONS: Dict[int, List[dict]] = {}

def get_ai_response(user_id: int, user_message: str) -> str:
    """Get AI response with character"""
    openai_api_key = os.environ.get("OPENAI_API_KEY")
    if not openai_api_key:
        return "🤖 AI временно недоступен (нет API ключа). Но я всё ещё могу сыграть с тобой в игры!"
    
    try:
        client = openai.OpenAI(api_key=openai_api_key)
        
        # Initialize conversation history for new users
        if user_id not in AI_CONVERSATIONS:
            AI_CONVERSATIONS[user_id] = [
                {"role": "system", "content": AI_CHARACTER_PROMPT}
            ]
        
        # Add user message
        AI_CONVERSATIONS[user_id].append({"role": "user", "content": user_message})
        
        # Keep only last 10 messages to save tokens
        if len(AI_CONVERSATIONS[user_id]) > 11:
            AI_CONVERSATIONS[user_id] = [AI_CONVERSATIONS[user_id][0]] + AI_CONVERSATIONS[user_id][-10:]
        
        # Call OpenAI API
        response = client.chat.completions.create(
            model="gpt-3.5-turbo",  # или gpt-4 если есть доступ
            messages=AI_CONVERSATIONS[user_id],
            max_tokens=150,
            temperature=0.8  # чуть выше креативности
        )
        
        ai_message = response.choices[0].message.content
        
        # Add AI response to history
        AI_CONVERSATIONS[user_id].append({"role": "assistant", "content": ai_message})
        
        return ai_message
        
    except Exception as e:
        logger.error(f"AI error: {e}")
        return "Ой, что-то я запутался... 🤔 Давай лучше сыграем? Напиши /ttt или /chess!"

def clear_ai_history(user_id: int):
    """Clear conversation history for user"""
    if user_id in AI_CONVERSATIONS:
        del AI_CONVERSATIONS[user_id]

# --- Image Generation Utilities ---

def create_ttt_image(board: List[str], highlight_pos: Optional[int] = None) -> bytes:
    """Generate TicTacToe board as PNG image"""
    size = 300
    cell_size = size // 3
    img = Image.new('RGB', (size, size), color='#1a1a2e')
    draw = ImageDraw.Draw(img)
    
    # Draw grid lines
    line_color = '#16213e'
    line_width = 4
    for i in range(1, 3):
        # Vertical lines
        draw.line([(i * cell_size, 0), (i * cell_size, size)], fill=line_color, width=line_width)
        # Horizontal lines
        draw.line([(0, i * cell_size), (size, i * cell_size)], fill=line_color, width=line_width)
    
    # Draw X and O
    padding = 20
    for i, cell in enumerate(board):
        row = i // 3
        col = i % 3
        x = col * cell_size + cell_size // 2
        y = row * cell_size + cell_size // 2
        
        # Highlight cell if needed
        if highlight_pos == i:
            draw.rectangle(
                [(col * cell_size + 2, row * cell_size + 2), 
                 ((col + 1) * cell_size - 2, (row + 1) * cell_size - 2)],
                fill='#0f3460'
            )
        
        if cell == 'X':
            # Draw X in red
            draw.line([(x - cell_size//2 + padding, y - cell_size//2 + padding),
                      (x + cell_size//2 - padding, y + cell_size//2 - padding)],
                     fill='#e94560', width=6)
            draw.line([(x + cell_size//2 - padding, y - cell_size//2 + padding),
                      (x - cell_size//2 + padding, y + cell_size//2 - padding)],
                     fill='#e94560', width=6)
        elif cell == 'O':
            # Draw O in cyan
            draw.ellipse([(x - cell_size//2 + padding, y - cell_size//2 + padding),
                         (x + cell_size//2 - padding, y + cell_size//2 - padding)],
                        outline='#00d9ff', width=6)
    
    # Save to bytes
    buf = io.BytesIO()
    img.save(buf, format='PNG')
    buf.seek(0)
    return buf.getvalue()


def create_chess_image(chess_game: Optional['Chess'] = None) -> bytes:
    """Generate Chess board as PNG image with coordinates"""
    # Board size + margin for coordinates
    board_size = 400
    margin = 30  # Space for coordinates
    total_size = board_size + 2 * margin
    cell_size = board_size // 8
    
    # Colors
    bg_color = '#2c3e50'
    light_color = '#eeeed2'
    dark_color = '#769656'
    coord_color = '#ecf0f1'
    
    img = Image.new('RGB', (total_size, total_size), color=bg_color)
    draw = ImageDraw.Draw(img)
    
    # Try to use fonts
    try:
        piece_font = ImageFont.truetype("arial.ttf", cell_size // 2)
        coord_font = ImageFont.truetype("arial.ttf", 16)
    except:
        piece_font = ImageFont.load_default()
        coord_font = ImageFont.load_default()
    
    # Get board state (use provided game or default initial position)
    if chess_game:
        board = chess_game.board
    else:
        board = [
            ['r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'],
            ['p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'],
            [None] * 8,
            [None] * 8,
            [None] * 8,
            [None] * 8,
            ['P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'],
            ['R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'],
        ]
    
    # Draw board squares
    for row in range(8):
        for col in range(8):
            color = light_color if (row + col) % 2 == 0 else dark_color
            x1 = margin + col * cell_size
            y1 = margin + row * cell_size
            x2 = x1 + cell_size
            y2 = y1 + cell_size
            draw.rectangle([(x1, y1), (x2, y2)], fill=color)
    
    # Draw coordinates
    files = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h']
    ranks = ['8', '7', '6', '5', '4', '3', '2', '1']
    
    # File labels (a-h) - bottom
    for col in range(8):
        x = margin + col * cell_size + cell_size // 2
        y = total_size - margin + 8
        bbox = draw.textbbox((0, 0), files[col], font=coord_font)
        text_width = bbox[2] - bbox[0]
        draw.text((x - text_width // 2, y), files[col], fill=coord_color, font=coord_font)
    
    # File labels (a-h) - top
    for col in range(8):
        x = margin + col * cell_size + cell_size // 2
        y = 8
        bbox = draw.textbbox((0, 0), files[col], font=coord_font)
        text_width = bbox[2] - bbox[0]
        draw.text((x - text_width // 2, y), files[col], fill=coord_color, font=coord_font)
    
    # Rank labels (1-8) - left
    for row in range(8):
        x = 8
        y = margin + row * cell_size + cell_size // 2 - 8
        bbox = draw.textbbox((0, 0), ranks[row], font=coord_font)
        text_height = bbox[3] - bbox[1]
        draw.text((x, y), ranks[row], fill=coord_color, font=coord_font)
    
    # Rank labels (1-8) - right
    for row in range(8):
        x = total_size - margin + 8
        y = margin + row * cell_size + cell_size // 2 - 8
        bbox = draw.textbbox((0, 0), ranks[row], font=coord_font)
        text_width = bbox[2] - bbox[0]
        draw.text((x - text_width, y), ranks[row], fill=coord_color, font=coord_font)
    
    # Draw pieces with letter notation
    for row in range(8):
        for col in range(8):
            piece = board[row][col]
            if piece:
                x = margin + col * cell_size + cell_size // 2
                y = margin + row * cell_size + cell_size // 2
                
                # Use letter notation (R, N, B, Q, K, P)
                piece_char = piece.upper()
                
                # Center text
                bbox = draw.textbbox((0, 0), piece_char, font=piece_font)
                text_width = bbox[2] - bbox[0]
                text_height = bbox[3] - bbox[1]
                text_x = x - text_width // 2
                text_y = y - text_height // 2
                
                # Determine colors
                is_white = piece.isupper()
                if is_white:
                    main_color = '#ffffff'
                    outline_color = '#1a1a1a'
                else:
                    main_color = '#1a1a1a'
                    outline_color = '#ffffff'
                
                # Draw outline/shadow for better visibility
                outline_offset = 1
                for dx in [-outline_offset, 0, outline_offset]:
                    for dy in [-outline_offset, 0, outline_offset]:
                        if dx != 0 or dy != 0:
                            draw.text(
                                (text_x + dx, text_y + dy),
                                piece_char,
                                fill=outline_color,
                                font=piece_font
                            )
                
                # Draw main piece
                draw.text(
                    (text_x, text_y),
                    piece_char,
                    fill=main_color,
                    font=piece_font
                )
    
    buf = io.BytesIO()
    img.save(buf, format='PNG')
    buf.seek(0)
    return buf.getvalue()


async def start(update: Update, context: ContextTypes.DEFAULT_TYPE):
    user_name = update.effective_user.first_name or "друг"
    await update.message.reply_text(
        f"Привет, {user_name}! 👋\n\n"
        "Я Арчи — твой игровой компаньон с характером! 🎮\n\n"
        "Я умею:\n"
        "🗣️ *Болтать* — просто напиши мне что угодно\n"
        "❌⭕ *Крестики-нолики* — /ttt\n"
        "♟️ *Шахматы* — /chess\n\n"
        "Быстрый старт:\n"
        "• /ttt — играть со мной\n"
        "• /ttt_pvp — играть с другом\n"
        "• /help — все команды\n\n"
        "Давай поболтаем или сыграем? 😏"
    )

async def help_command(update: Update, context: ContextTypes.DEFAULT_TYPE):
    await update.message.reply_text(
        "**🎮 Доступные команды:**\n"
        "/start — приветствие\n"
        "/help — справка\n"
        "/ping — проверка\n"
        "/reset — сбросить память AI\n\n"
        "**Крестики-Нолики:**\n"
        "/ttt — игра с ботом\n"
        "/ttt_pvp — игра на двоих (в группе)\n\n"
        "**Шахматы:**\n"
        "/chess — начать партию на двоих\n\n"
        "💡 Просто напиши мне сообщение — я отвечу как настоящий собеседник!"
    )

async def ping(update: Update, context: ContextTypes.DEFAULT_TYPE):
    await update.message.reply_text("Pong!")

async def echo_command(update: Update, context: ContextTypes.DEFAULT_TYPE):
    text = " ".join(context.args) if context.args else "(пусто)"
    await update.message.reply_text(text)

async def echo_text(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Handle text messages with AI character"""
    if update.message and update.message.text:
        user_id = update.effective_user.id
        user_message = update.message.text
        
        # Show typing indicator
        await update.message.chat.send_action(action="typing")
        
        # Get AI response
        ai_response = get_ai_response(user_id, user_message)
        await update.message.reply_text(ai_response)


async def reset_ai(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Reset AI conversation history"""
    user_id = update.effective_user.id
    clear_ai_history(user_id)
    await update.message.reply_text("🧹 Память очищена! Я снова готов к новым приключениям! Чем займёмся?")


# --- TicTacToe game implementation (per-chat) ---
class TicTacToe:
    def __init__(self):
        # board: list of 9 cells, each ' ' or 'X' or 'O'
        self.board: List[str] = [" "] * 9
        self.current = "X"  # player always X, bot O

    def reset(self):
        self.board = [" "] * 9
        self.current = "X"

    def pos_from_str(self, s: str) -> Optional[int]:
        s = s.strip().upper()
        # allow 1-9
        if s.isdigit():
            n = int(s)
            if 1 <= n <= 9:
                return n - 1
        # allow A1..C3
        if len(s) == 2 and s[0] in "ABC" and s[1] in "123":
            row = ord(s[0]) - ord("A")
            col = int(s[1]) - 1
            return row * 3 + col
        return None

    def valid_moves(self) -> List[int]:
        return [i for i, v in enumerate(self.board) if v == " "]

    def make_move(self, idx: int, mark: str) -> bool:
        if 0 <= idx < 9 and self.board[idx] == " ":
            self.board[idx] = mark
            self.current = "O" if mark == "X" else "X"
            return True
        return False

    def check_winner(self) -> Optional[str]:
        wins = [
            (0, 1, 2), (3, 4, 5), (6, 7, 8),
            (0, 3, 6), (1, 4, 7), (2, 5, 8),
            (0, 4, 8), (2, 4, 6),
        ]
        for a, b, c in wins:
            if self.board[a] != " " and self.board[a] == self.board[b] == self.board[c]:
                return self.board[a]
        if all(cell != " " for cell in self.board):
            return "draw"
        return None

    def render(self) -> str:
        # Renders board with positions for empty cells (1-9)
        rows = []
        for r in range(3):
            cells = []
            for c in range(3):
                i = r * 3 + c
                v = self.board[i]
                cells.append(v if v != " " else str(i + 1))
            rows.append(" | ".join(cells))
        return "\n".join(rows)

    # Simple AI: win if possible, block if needed, else center, corner, side
    def bot_move(self) -> int:
        # try to win
        for i in self.valid_moves():
            self.board[i] = "O"
            if self.check_winner() == "O":
                return i
            self.board[i] = " "
        # try to block X
        for i in self.valid_moves():
            self.board[i] = "X"
            if self.check_winner() == "X":
                self.board[i] = "O"
                return i
            self.board[i] = " "
        # center
        if 4 in self.valid_moves():
            self.board[4] = "O"
            return 4
        # corners
        corners = [i for i in [0, 2, 6, 8] if i in self.valid_moves()]
        if corners:
            choice = random.choice(corners)
            self.board[choice] = "O"
            return choice
        # sides
        sides = [i for i in [1, 3, 5, 7] if i in self.valid_moves()]
        if sides:
            choice = random.choice(sides)
            self.board[choice] = "O"
            return choice
        return -1


# --- TicTacToe PvP (two players) ---
class TicTacToePvP:
    def __init__(self, player1: str, player2: str):
        self.board: List[str] = [" "] * 9
        self.current_player = "X"
        self.player_x = player1  # User ID or name
        self.player_o = player2  # User ID or name
        self.moves_count = 0

    def pos_from_str(self, s: str) -> Optional[int]:
        s = s.strip().upper()
        if s.isdigit():
            n = int(s)
            if 1 <= n <= 9:
                return n - 1
        if len(s) == 2 and s[0] in "ABC" and s[1] in "123":
            row = ord(s[0]) - ord("A")
            col = int(s[1]) - 1
            return row * 3 + col
        return None

    def make_move(self, idx: int) -> bool:
        if 0 <= idx < 9 and self.board[idx] == " ":
            self.board[idx] = self.current_player
            self.moves_count += 1
            self.current_player = "O" if self.current_player == "X" else "X"
            return True
        return False

    def check_winner(self) -> Optional[str]:
        wins = [
            (0, 1, 2), (3, 4, 5), (6, 7, 8),
            (0, 3, 6), (1, 4, 7), (2, 5, 8),
            (0, 4, 8), (2, 4, 6),
        ]
        for a, b, c in wins:
            if self.board[a] != " " and self.board[a] == self.board[b] == self.board[c]:
                return self.board[a]
        if self.moves_count == 9:
            return "draw"
        return None

    def render(self) -> str:
        rows = []
        for r in range(3):
            cells = []
            for c in range(3):
                i = r * 3 + c
                v = self.board[i]
                cells.append(v if v != " " else str(i + 1))
            rows.append(" | ".join(cells))
        return "\n".join(rows)

    def get_status(self) -> str:
        mark = self.current_player
        player = self.player_x if mark == "X" else self.player_o
        return f"Ход игрока {player} ({mark})\n" + self.render()


# --- Chess game implementation ---
class Chess:
    # Unicode symbols for pieces
    PIECES_UNICODE = {
        'r': '♜', 'n': '♞', 'b': '♝', 'q': '♛', 'k': '♚', 'p': '♟',
        'R': '♖', 'N': '♘', 'B': '♗', 'Q': '♕', 'K': '♔', 'P': '♙'
    }
    
    # Letter notation for pieces (standard chess notation)
    PIECES_LETTERS = {
        'r': 'r', 'n': 'n', 'b': 'b', 'q': 'q', 'k': 'k', 'p': 'p',
        'R': 'R', 'N': 'N', 'B': 'B', 'Q': 'Q', 'K': 'K', 'P': 'P'
    }

    def __init__(self, player1: str, player2: str):
        self.player_white = player1
        self.player_black = player2
        self.current = "white"
        self.moves_history: List[str] = []
        self.move_count = 0
        # Initialize board with starting position
        # Board is 8x8, row 0 is rank 8 (black's back rank), row 7 is rank 1 (white's back rank)
        self.board = [
            ['r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'],  # 8th rank
            ['p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'],  # 7th rank
            [None, None, None, None, None, None, None, None],  # 6th
            [None, None, None, None, None, None, None, None],  # 5th
            [None, None, None, None, None, None, None, None],  # 4th
            [None, None, None, None, None, None, None, None],  # 3rd
            ['P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'],  # 2nd rank
            ['R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'],  # 1st rank
        ]

    def parse_square(self, square: str) -> tuple:
        """Convert algebraic notation (e.g., 'e2') to (row, col)"""
        if len(square) != 2:
            return None
        file = square[0].lower()
        rank = square[1]
        if file < 'a' or file > 'h' or rank < '1' or rank > '8':
            return None
        col = ord(file) - ord('a')
        row = 8 - int(rank)  # Convert rank to row index (8->0, 1->7)
        return (row, col)

    def make_move(self, move: str) -> tuple:
        """Make a move in algebraic notation (e.g., 'e2e4' or 'e2 e4').
        Returns: (success: bool, error_message: str)"""
        # Parse move (handle both "e2e4" and "e2 e4" formats)
        move_clean = move.replace(' ', '').lower()
        if len(move_clean) < 4:
            return False, "Неверный формат хода. Используйте: e2 e4 или e2e4"
        
        from_sq = move_clean[:2]
        to_sq = move_clean[2:4]
        
        from_pos = self.parse_square(from_sq)
        to_pos = self.parse_square(to_sq)
        
        if from_pos is None or to_pos is None:
            return False, "Неверные координаты. Используйте формат a1-h8"
        
        from_row, from_col = from_pos
        to_row, to_col = to_pos
        
        piece = self.board[from_row][from_col]
        if piece is None:
            return False, f"На {from_sq} нет фигуры"
        
        # Check if it's the correct player's turn
        is_white_piece = piece.isupper()
        if (self.current == "white" and not is_white_piece) or \
           (self.current == "black" and is_white_piece):
            current_player = "Белые" if self.current == "white" else "Чёрные"
            piece_color = "белая" if is_white_piece else "чёрная"
            return False, f"Сейчас ход {current_player}, а фигура {piece_color}"
        
        # Check if destination has own piece (can't capture own piece)
        target_piece = self.board[to_row][to_col]
        if target_piece:
            target_is_white = target_piece.isupper()
            if is_white_piece == target_is_white:
                return False, "Нельзя срубить свою фигуру"
        
        # Simple validation: pawns move differently than other pieces
        piece_type = piece.upper()
        if piece_type == 'P':
            # Pawn moves
            direction = -1 if is_white_piece else 1  # White moves up (decreasing row), black down
            row_diff = to_row - from_row
            col_diff = abs(to_col - from_col)
            
            # Forward move (no capture)
            if col_diff == 0:
                if target_piece is not None:
                    return False, "Пешка не может ходить вперёд, если там занято"
                # One square forward
                if row_diff == direction:
                    pass  # Valid
                # Two squares from starting position
                elif row_diff == 2 * direction:
                    start_row = 6 if is_white_piece else 1  # 2nd rank for white, 7th for black
                    if from_row != start_row:
                        return False, "Пешка может ходить на 2 клетки только со стартовой позиции"
                    # Check intermediate square is empty
                    mid_row = from_row + direction
                    if self.board[mid_row][from_col] is not None:
                        return False, "Пешка не может перепрыгивать через фигуру"
                else:
                    return False, "Пешка ходит только на 1 клетку вперёд (или 2 со старта)"
            # Capture (diagonal)
            elif col_diff == 1 and row_diff == direction:
                if target_piece is None:
                    return False, "Пешка бьёт только по диагонали на занятое поле"
            else:
                return False, "Пешка ходит вперёд или бьёт по диагонали на 1 клетку"
        
        # Execute move
        self.board[to_row][to_col] = piece
        self.board[from_row][from_col] = None
        
        self.moves_history.append(f"{from_sq}{to_sq}")
        self.current = "black" if self.current == "white" else "white"
        self.move_count += 1
        return True, ""

    def render(self) -> str:
        """Render board as ASCII text"""
        lines = ["  a b c d e f g h"]
        for row_idx in range(8):
            rank = 8 - row_idx
            row_str = f"{rank} "
            for col_idx in range(8):
                piece = self.board[row_idx][col_idx]
                if piece:
                    row_str += self.PIECES_UNICODE.get(piece, '?') + " "
                else:
                    row_str += "· "
            row_str += str(rank)
            lines.append(row_str)
        lines.append("  a b c d e f g h")
        return "\n".join(lines)

    def get_status(self) -> str:
        player = self.player_white if self.current == "white" else self.player_black
        color_str = "Белые" if self.current == "white" else "Чёрные"
        return f"Ход: {player} ({color_str})\nХодов: {self.move_count}"


# store games per chat (type: ttt, ttt_pvp, chess)
GAMES: Dict[int, dict] = {}


async def ttt_start(update: Update, context: ContextTypes.DEFAULT_TYPE):
    chat_id = update.effective_chat.id
    # support subcommands: /ttt board, /ttt stop
    if context.args:
        sub = context.args[0].lower()
        if sub in ("stop", "end"):
            return await ttt_stop(update, context)
        if sub in ("board", "b"):
            return await ttt_board(update, context)

    game = TicTacToe()
    GAMES[chat_id] = {"type": "ttt", "game": game}
    
    # Generate and send image
    img_data = create_ttt_image(game.board)
    await update.message.reply_photo(
        photo=img_data,
        caption="Новая игра Крестики-Нолики (Вы — X).\nХоды: /move <позиция> — где позиция 1-9 или A1..C3.\nПример: /move 5 или /move B2"
    )


async def ttt_board(update: Update, context: ContextTypes.DEFAULT_TYPE):
    chat_id = update.effective_chat.id
    game_info = GAMES.get(chat_id)
    if not game_info or game_info.get("type") != "ttt":
        await update.message.reply_text("Игра не запущена. Используйте /ttt чтобы начать.")
        return
    game = game_info["game"]
    
    # Generate and send image
    img_data = create_ttt_image(game.board)
    await update.message.reply_photo(photo=img_data, caption="Текущая доска:")


async def ttt_stop(update: Update, context: ContextTypes.DEFAULT_TYPE):
    chat_id = update.effective_chat.id
    if chat_id in GAMES and GAMES[chat_id].get("type") == "ttt":
        del GAMES[chat_id]
        await update.message.reply_text("Игра остановлена.")
    else:
        await update.message.reply_text("Игра не запущена.")


async def ttt_move(update: Update, context: ContextTypes.DEFAULT_TYPE):
    chat_id = update.effective_chat.id
    game_info = GAMES.get(chat_id)
    if not game_info or game_info.get("type") != "ttt":
        await update.message.reply_text("Игра не запущена. Используйте /ttt чтобы начать.")
        return
    game = game_info["game"]
    if not context.args:
        await update.message.reply_text("Укажите позицию: /move <1-9|A1-C3>")
        return
    pos_raw = context.args[0]
    idx = game.pos_from_str(pos_raw)
    if idx is None:
        await update.message.reply_text("Неверная позиция. Используйте 1-9 или A1..C3.")
        return
    if not game.make_move(idx, "X"):
        await update.message.reply_text("Эта клетка занята. Выберите другую.")
        return

    winner = game.check_winner()
    if winner == "X":
        img_data = create_ttt_image(game.board)
        await update.message.reply_photo(photo=img_data, caption="🎉 Вы победили!")
        del GAMES[chat_id]
        return
    if winner == "draw":
        img_data = create_ttt_image(game.board)
        await update.message.reply_photo(photo=img_data, caption="Ничья!")
        del GAMES[chat_id]
        return

    # бот ходит
    bot_idx = game.bot_move()
    winner = game.check_winner()
    if winner == "O":
        img_data = create_ttt_image(game.board)
        await update.message.reply_photo(photo=img_data, caption=f"🤖 Бот ходит на {bot_idx+1} и побеждает!")
        del GAMES[chat_id]
        return
    if winner == "draw":
        img_data = create_ttt_image(game.board)
        await update.message.reply_photo(photo=img_data, caption="Ничья!")
        del GAMES[chat_id]
        return

    img_data = create_ttt_image(game.board)
    await update.message.reply_photo(photo=img_data, caption="Ход принят. Ваш ход!")


async def ttt_pvp_start(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Start two-player TicTacToe"""
    chat_id = update.effective_chat.id
    user_name = update.effective_user.first_name or f"Игрок {update.effective_user.id}"
    
    if chat_id in GAMES:
        await update.message.reply_text("В этом чате уже идёт игра! Завершите её командой /stop")
        return
    
    game = TicTacToePvP(user_name, "Ожидание игрока...")
    GAMES[chat_id] = {"type": "ttt_pvp", "game": game, "players": [update.effective_user.id]}
    
    img_data = create_ttt_image(game.board)
    await update.message.reply_photo(
        photo=img_data,
        caption=f"🎮 Крестики-Нолики на двоих (PvP)\n"
                f"Игрок 1 (X): {user_name}\n"
                f"Ожидание Игрока 2...\n\n"
                f"Другой игрок должен написать /join_ttt"
    )


async def ttt_pvp_join(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Join two-player TicTacToe"""
    chat_id = update.effective_chat.id
    
    if chat_id not in GAMES or GAMES[chat_id]["type"] != "ttt_pvp":
        await update.message.reply_text("Нет активной PvP игры. Начните с /ttt_pvp")
        return
    
    game_info = GAMES[chat_id]
    game = game_info["game"]
    
    if len(game_info["players"]) >= 2:
        await update.message.reply_text("Оба игрока уже в игре!")
        return
    
    player2_name = update.effective_user.first_name or f"Игрок {update.effective_user.id}"
    game.player_o = player2_name
    game_info["players"].append(update.effective_user.id)
    
    img_data = create_ttt_image(game.board)
    await update.message.reply_photo(
        photo=img_data,
        caption=f"✅ {player2_name} присоединился!\n\n"
                f"Игрок 1 (X): {game.player_x}\n"
                f"Игрок 2 (O): {player2_name}\n\n"
                f"Игрок 1 делает ход первым. Команда: /move_pvp <позиция>"
    )


async def ttt_pvp_move(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Make move in PvP TicTacToe"""
    chat_id = update.effective_chat.id
    
    if chat_id not in GAMES or GAMES[chat_id]["type"] != "ttt_pvp":
        await update.message.reply_text("Нет активной PvP игры.")
        return
    
    game_info = GAMES[chat_id]
    game = game_info["game"]
    
    if len(game_info["players"]) < 2:
        await update.message.reply_text("Ждём второго игрока!")
        return
    
    if not context.args:
        await update.message.reply_text("Укажите позицию: /move_pvp <1-9|A1-C3>")
        return
    
    idx = game.pos_from_str(context.args[0])
    if idx is None:
        await update.message.reply_text("Неверная позиция.")
        return
    
    if not game.make_move(idx):
        await update.message.reply_text("Клетка занята!")
        return
    
    winner = game.check_winner()
    if winner == "X":
        img_data = create_ttt_image(game.board)
        await update.message.reply_photo(photo=img_data, caption=f"🎉 {game.player_x} победил!")
        del GAMES[chat_id]
        return
    if winner == "O":
        img_data = create_ttt_image(game.board)
        await update.message.reply_photo(photo=img_data, caption=f"🎉 {game.player_o} победил!")
        del GAMES[chat_id]
        return
    if winner == "draw":
        img_data = create_ttt_image(game.board)
        await update.message.reply_photo(photo=img_data, caption="Ничья!")
        del GAMES[chat_id]
        return
    
    img_data = create_ttt_image(game.board)
    await update.message.reply_photo(photo=img_data, caption=game.get_status())


async def chess_start(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Start chess game"""
    try:
        chat_id = update.effective_chat.id
        user_name = update.effective_user.first_name or f"Игрок {update.effective_user.id}"
        logger.info(f"chess_start: chat_id={chat_id}, user={user_name}")
        
        if chat_id in GAMES:
            if update.message:
                await update.message.reply_text("В этом чате уже идёт игра!")
            return
        
        game = Chess(user_name, "Ожидание игрока...")
        GAMES[chat_id] = {"type": "chess", "game": game, "players": [update.effective_user.id]}
        logger.info(f"chess_start: game created for {chat_id}")
        
        # Generate and send chess board image
        if update.message:
            img_data = create_chess_image(game)
            await update.message.reply_photo(
                photo=img_data,
                caption=f"♟️ Шахматы на двоих\n"
                        f"Белые: {user_name}\n"
                        f"Чёрные: Ожидание...\n\n"
                        f"Другой игрок должен написать /join_chess"
            )
        logger.info(f"chess_start: message sent for {chat_id}")
    except Exception as e:
        logger.error(f"chess_start error: {e}", exc_info=True)


async def chess_join(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Join chess game"""
    try:
        chat_id = update.effective_chat.id
        logger.info(f"chess_join: chat_id={chat_id}")
        
        if chat_id not in GAMES or GAMES[chat_id]["type"] != "chess":
            if update.message:
                await update.message.reply_text("Нет активной шахматной партии. Начните с /chess")
            return
        
        game_info = GAMES[chat_id]
        game_info["players"].append(update.effective_user.id)
        game = game_info["game"]
        player2_name = update.effective_user.first_name or f"Игрок {update.effective_user.id}"
        game.player_black = player2_name
        
        if update.message:
            img_data = create_chess_image(game)
            await update.message.reply_photo(
                photo=img_data,
                caption=f"✅ Оба игрока в партии!\n\n"
                        f"Белые: {game.player_white}\n"
                        f"Чёрные: {player2_name}\n\n"
                        f"{game.get_status()}\n\n"
                        f"Ход: /move_chess <от> <к> (например: e2 e4)"
            )
        logger.info(f"chess_join: game joined for {chat_id}")
    except Exception as e:
        logger.error(f"chess_join error: {e}", exc_info=True)


async def chess_move(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Make move in chess"""
    try:
        chat_id = update.effective_chat.id
        logger.info(f"chess_move: chat_id={chat_id}, args={context.args}")
        
        if chat_id not in GAMES or GAMES[chat_id]["type"] != "chess":
            await update.message.reply_text("Нет активной шахматной партии.")
            return
        
        if not context.args or len(context.args) < 2:
            await update.message.reply_text("Формат: /move_chess <от> <к> (например: e2 e4)")
            return
        
        move = f"{context.args[0]}{context.args[1]}"
        game_info = GAMES[chat_id]
        game = game_info["game"]
        
        # Validate and make move
        success, error_msg = game.make_move(move)
        if success:
            if update.message:
                img_data = create_chess_image(game)
                await update.message.reply_photo(
                    photo=img_data,
                    caption=f"✓ Ход {move} принят.\n\n{game.get_status()}"
                )
        else:
            if update.message:
                await update.message.reply_text(f"❌ {error_msg}")
        logger.info(f"chess_move: move {move} processed for {chat_id}")
    except Exception as e:
        logger.error(f"chess_move error: {e}", exc_info=True)
        try:
            if update and update.message:
                await update.message.reply_text("Произошла ошибка при выполнении хода.")
        except:
            pass


async def stop_game(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Stop any game"""
    chat_id = update.effective_chat.id
    if chat_id in GAMES:
        del GAMES[chat_id]
        await update.message.reply_text("Игра остановлена.")
    else:
        await update.message.reply_text("Нет активной игры.")


def main() -> None:
    token = os.environ.get("TELEGRAM_TOKEN")
    if not token:
        print("Ошибка: переменная окружения TELEGRAM_TOKEN не установлена.")
        print("Установите токен и запустите снова.")
        return

    app = ApplicationBuilder().token(token).build()

    app.add_handler(CommandHandler("start", start))
    app.add_handler(CommandHandler("help", help_command))
    app.add_handler(CommandHandler("ping", ping))
    app.add_handler(CommandHandler("echo", echo_command))
    app.add_handler(CommandHandler("reset", reset_ai))
    app.add_handler(CommandHandler("ttt", ttt_start))
    app.add_handler(CommandHandler("move", ttt_move))
    app.add_handler(CommandHandler("board", ttt_board))
    app.add_handler(CommandHandler("ttt_pvp", ttt_pvp_start))
    app.add_handler(CommandHandler("join_ttt", ttt_pvp_join))
    app.add_handler(CommandHandler("move_pvp", ttt_pvp_move))
    app.add_handler(CommandHandler("chess", chess_start))
    app.add_handler(CommandHandler("join_chess", chess_join))
    app.add_handler(CommandHandler("move_chess", chess_move))
    app.add_handler(CommandHandler("stop", stop_game))
    app.add_handler(MessageHandler(filters.TEXT & ~filters.COMMAND, echo_text))

    logger.info("Запуск бота...")
    app.run_polling()


if __name__ == "__main__":
    main()
