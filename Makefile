.PHONY: build run clean docker-build docker-run docker-stop

# Port par défaut
PORT ?= 3000

# Compilation
build:
	mkdir -p out/production/calculator-api-java
	javac -d out/production/calculator-api-java src/main/java/core/calculator/*.java

# Lancer le serveur
run: build
	java -cp out/production/calculator-api-java core.calculator.Server $(PORT)

# Nettoyer
clean:
	rm -rf out

# Docker : build
docker-build:
	docker build -t calculator-api .

# Docker : run
docker-run:
	docker run -p $(PORT):$(PORT) --rm calculator-api $(PORT)

# Docker : stop (arrête tous les conteneurs calculator-api)
docker-stop:
	docker stop $$(docker ps -q --filter ancestor=calculator-api) 2>/dev/null || true
