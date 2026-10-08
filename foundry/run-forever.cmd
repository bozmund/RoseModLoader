@echo off
rem Runs the foundry around the clock: a batch of tasks, a short pause, repeat. Stop with Ctrl+C.
rem Usage: foundry\run-forever.cmd [extra rose foundry run options, e.g. --model mony/qwen3.8-27b-q5]
setlocal
cd /d "%~dp0.."
:loop
call rose.cmd foundry run --max 20 --attempts 2 %*
echo [foundry] batch finished %date% %time%; next batch in 5 minutes
timeout /t 300 /nobreak >nul
goto loop
