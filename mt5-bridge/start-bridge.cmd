@echo off
rem Startet die MT5-Bridge. Beim ersten Start wird eine eigene Python-Umgebung (.venv) angelegt
rem und das Paket MetaTrader5 installiert.
cd /d "%~dp0"

if not exist ".venv\Scripts\python.exe" (
    echo Lege Python-Umgebung an ...
    python -m venv .venv || goto :error
    ".venv\Scripts\python.exe" -m pip install -r requirements.txt || goto :error
)

".venv\Scripts\python.exe" bridge.py
goto :eof

:error
echo.
echo Fehler beim Einrichten. Ist Python installiert und im PATH?
pause
