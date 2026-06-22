@echo off
set JAVA_HOME=C:\Users\cotti\.jdks\openjdk-25
echo Compilation...
"%JAVA_HOME%\bin\javac" --enable-preview --release 25 -d out\production\calculatrice-api-java src\main\java\core\calculator\*.java
if %errorlevel% neq 0 (
    echo Erreur de compilation.
    pause
    exit /b 1
)
echo Demarrage du serveur sur http://localhost:3000
"%JAVA_HOME%\bin\java" --enable-preview -cp out\production\calculatrice-api-java main.java.core.calculator.Server
