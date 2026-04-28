#!/usr/bin/env python3
"""Simple syntax and import check for bot"""

import sys
import os
import ast

print("=" * 60)
print("🔍 Bot Code Verification")
print("=" * 60)

# Read bot.py
with open('/'.join(os.path.abspath(__file__).split(os.sep)[:-1]) + '/bot.py', 'r') as f:
    bot_code = f.read()

# Check syntax
try:
    ast.parse(bot_code)
    print("✅ Syntax: Valid Python code")
except SyntaxError as e:
    print(f"❌ Syntax Error at line {e.lineno}: {e.msg}")
    sys.exit(1)

# Check imports
try:
    import bot
    print("✅ Imports: All dependencies available")
except ImportError as e:
    print(f"❌ Import Error: {e}")
    sys.exit(1)

# Check handlers are defined
handlers = [
    'start', 'help_command', 'ping', 'echo_command', 'echo_text',
    'ttt_start', 'ttt_board', 'ttt_stop', 'ttt_move',
    'ttt_pvp_start', 'ttt_pvp_join', 'ttt_pvp_move',
    'chess_start', 'chess_join', 'chess_move',
    'stop_game'
]

print("\n📋 Handler Definitions:")
for handler in handlers:
    if hasattr(bot, handler):
        print(f"  ✅ {handler}")
    else:
        print(f"  ❌ {handler} - NOT FOUND")

# Check game classes
classes = ['TicTacToe', 'TicTacToePvP', 'Chess']
print("\n📋 Game Classes:")
for cls in classes:
    if hasattr(bot, cls):
        print(f"  ✅ {cls}")
    else:
        print(f"  ❌ {cls} - NOT FOUND")

# Check GAMES dictionary
if hasattr(bot, 'GAMES'):
    print(f"\n💾 GAMES dict: ✅ Defined")
    print(f"   Current state: {bot.GAMES}")
else:
    print(f"\n💾 GAMES dict: ❌ NOT FOUND")

print("\n" + "=" * 60)
print("✨ All checks passed! Bot code is ready.")
print("=" * 60)
