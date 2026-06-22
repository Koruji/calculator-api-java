# calculatrice-api-java

API REST de calculatrice en Java pur (sans framework), conversion du projet [calculator-api-js](https://github.com/Koruji/calculator-api-js).

## Stack

- Java 17+ (JDK natif uniquement : `com.sun.net.httpserver`)
- JUnit 5 pour les tests

## Lancer le serveur

Exécuter `Server.main()` depuis IntelliJ, puis :

```
http://localhost:3000/calculate?operation=add&a=5&b=3
```

## Endpoint

`GET /calculate`

| Paramètre   | Type   | Valeurs acceptées                        |
|-------------|--------|------------------------------------------|
| `operation` | string | `add`, `subtract`, `multiply`, `divide`  |
| `a`         | number | tout nombre décimal                      |
| `b`         | number | tout nombre décimal                      |

### Réponse succès (200)

```json
{ "operation": "add", "a": 5, "b": 3, "result": 8 }
```

### Réponse erreur (400 / 404 / 405)

```json
{ "error": "Division par zéro impossible." }
```

## Codes HTTP

| Code | Cas                                      |
|------|------------------------------------------|
| 200  | Calcul réussi                            |
| 204  | Requête OPTIONS (preflight CORS)         |
| 400  | Paramètre manquant, non numérique, opération inconnue, division par zéro |
| 404  | Route inconnue                           |
| 405  | Méthode autre que GET                    |

## Tests

| Fichier                    | Type          | Description                        |
|----------------------------|---------------|------------------------------------|
| `CalculatorTest`           | Unitaires     | Les 4 opérations, coercions, valeurs limites |
| `ApiTest`                  | Fonctionnels  | Requêtes HTTP end-to-end           |
| `CalculatorInjectionTest`  | Sécurité      | SQL injection, XSS, inputs invalides |

Lancer tous les tests depuis IntelliJ : clic droit sur `src/test` → **Run All Tests**.
