#!/usr/bin/env python3
"""Test script for Telegram bot"""

import asyncio
import os
from telegram import Update, Chat, User, Message
from telegram.ext import ContextTypes
import sys

# Import the bot handlers
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from bot import (
    start, help_command, ping, echo_command, ttt_start, ttt_board, 
    ttt_stop, ttt_move, ttt_pvp_start, ttt_pvp_join, ttt_pvp_move,
    chess_start, chess_join, chess_move, stop_game, GAMES
)

# Clear any existing games
GAMES.clear()

async def test_command(handler, command_name, *args):
    """Test a command handler"""
    print(f"\n🧪 Testing /{command_name}...", end=" ")
    
    # Mock objects
    user = User(id=123456, first_name="Тест", is_bot=False)
    chat = Chat(id=789, type="private")
    
    # Create update with message
    message = Message(
        message_id=1,
        date=1,
        chat=chat,
        text=f"/{command_name}",
        from_user=user
    )
    update = Update(update_id=1, message=message)
    
    # Create context with args
    class MockContext:
        def __init__(self, cmd_args):
            self.args = list(cmd_args)
    
    context = MockContext(args)
    
    try:
        # Create mock reply_text method
        replies = []
        async def mock_reply(text, **kwargs):
            replies.append(text)
            return message
        
        message.reply_text = mock_reply
        
        # Run the handler
        await handler(update, context)
        
        if replies:
            print(f"✅ OK | Response: {replies[0][:50]}...")
        else:
            print("⚠️  No response")
        return True
    except Exception as e:
        print(f"❌ ERROR: {type(e).__name__}: {str(e)}")
        return False

async def main():
    """Run all tests"""
    print("=" * 60)
    print("🤖 Telegram Bot Command Tests")
    print("=" * 60)
    
    passed = 0
    failed = 0
    
    commands = [
        (start, "start"),
        (help_command, "help"),
        (ping, "ping"),
        (echo_command, "echo", "hello"),
        (ttt_start, "ttt"),
        (ttt_board, "board"),
        (ttt_pvp_start, "ttt_pvp"),
        (chess_start, "chess"),
        (stop_game, "stop"),
    ]
    
    for cmd_data in commands:
        handler = cmd_data[0]
        command_name = cmd_data[1]
        args = cmd_data[2:] if len(cmd_data) > 2 else ()
        
        success = await test_command(handler, command_name, *args)
        if success:
            passed += 1
        else:
            failed += 1
    
    print("\n" + "=" * 60)
    print(f"📊 Results: {passed} passed, {failed} failed")
    print("=" * 60)

if __name__ == "__main__":
    asyncio.run(main())
