@echo off
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -cp "lib/*" -d bin @sources.txt
del sources.txt
echo Compile finished.
