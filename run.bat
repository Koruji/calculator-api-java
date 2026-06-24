@echo off
echo Compilation...
javac -d out\production\calculator-api-java src\main\java\core\calculator\*.java
if %errorlevel% neq 0 (
    echo Erreur de compilation.
    pause
    exit /b 1
)
echo Demarrage du serveur sur http://localhost:3000
java -cp out\production\calculator-api-java core.calculator.Server
