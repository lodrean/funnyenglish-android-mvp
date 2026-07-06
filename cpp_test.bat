@echo off
call "C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Auxiliary\Build\vcvarsall.bat" x64
cl.exe test_tictactoe.cpp /EHsc /Fe:test_tictactoe.exe /W4
if %ERRORLEVEL% == 0 (
  test_tictactoe.exe
) else (
  echo COMPILE_FAILED
)
