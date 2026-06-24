#!/bin/bash

echo "Compilation..."
javac -d out/production/calculator-api-java src/main/java/core/calculator/*.java

if [ $? -ne 0 ]; then
    echo "Erreur de compilation."
    exit 1
fi

PORT=${1:-3000}
echo "Démarrage du serveur sur http://localhost:$PORT"
java -cp out/production/calculator-api-java core.calculator.Server $PORT
