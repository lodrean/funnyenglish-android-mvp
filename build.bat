@echo off
call "C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Auxiliary\Build\vcvarsall.bat" x64
cl.exe tictactoe.cpp /EHsc /Fe:tictactoe.exe /W4
if %ERRORLEVEL% == 0 (
  echo COMPILE_SUCCESS
) else (
  echo COMPILE_FAILED
)
