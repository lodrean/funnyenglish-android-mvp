@echo off
"C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Auxiliary\Build\vcvarsall.bat" x64
cd /d C:\Users\etaba\funnyenglish
cl.exe tictactoe.cpp /EHsc /W4 /Fe:tictactoe.exe
