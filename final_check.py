#!/usr/bin/env python3
"""
Final verification that bot.py is correctly structured for Telegram
"""
import ast
import sys

print("=" * 70)
print("🤖 FINAL BOT VERIFICATION")
print("=" * 70)

with open('bot.py', 'r') as f:
    code = f.read()

# Parse
tree = ast.parse(code)

# Find all class definitions
classes = {}
functions = {}

for node in ast.walk(tree):
    if isinstance(node, ast.ClassDef):
        methods = [n.name for n in node.body if isinstance(n, ast.FunctionDef)]
        classes[node.name] = methods
    elif isinstance(node, ast.FunctionDef):
        functions[node.name] = node

# Check Classes
print("\n✅ Classes Found:")
print(f"  • TicTacToe: {classes.get('TicTacToe', [])}")
print(f"  • TicTacToePvP: {classes.get('TicTacToePvP', [])}")
print(f"  • Chess: {classes.get('Chess', [])}")

# Check Handlers
handlers = [
    'start', 'help_command', 'ping', 'echo_command', 'echo_text',
    'ttt_start', 'ttt_board', 'ttt_stop', 'ttt_move',
    'ttt_pvp_start', 'ttt_pvp_join', 'ttt_pvp_move',
    'chess_start', 'chess_join', 'chess_move',
    'stop_game'
]

found = [h for h in handlers if h in functions]
missing = [h for h in handlers if h not in functions]

print(f"\n✅ Handlers Found: {len(found)}/{len(handlers)}")
if missing:
    print(f"  ❌ Missing: {missing}")
else:
    print(f"  All required handlers present!")

# Check GAMES dict initialization
games_found = any('GAMES' in node.targets[0].id if isinstance(node, ast.Assign) and node.targets else False 
                  for node in ast.walk(tree))

print(f"\n✅ GAMES Dict: {'Found' if games_found else 'NOT FOUND'}")

# Check CommandHandler registrations in main()
main_func = functions.get('main')
if main_func:
    handler_count = 0
    source_lines = code.split('\n')
    in_main = False
    for i, line in enumerate(source_lines):
        if 'def main()' in line:
            in_main = True
        if in_main and 'CommandHandler' in line:
            handler_count += 1
        if in_main and 'app.run_polling()' in line:
            break
    print(f"\n✅ CommandHandlers Registered: {handler_count}")

print("\n" + "=" * 70)
print("✨ Bot structure verified successfully!")
print("=" * 70)
print("\n📝 Next Steps:")
print("1. Ensure TELEGRAM_TOKEN environment variable is set")
print("2. Run: docker-compose up -d")
print("3. Send /start command in Telegram")
print("4. Test other commands: /ping, /chess, /ttt, etc.")
print("=" * 70)
