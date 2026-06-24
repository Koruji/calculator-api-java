package core.calculator;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class RequestHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Étape 1 : CORS sur toutes les réponses
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");

        String method = exchange.getRequestMethod();

        // Étape 2 : OPTIONS → 204 (preflight CORS)
        if (method.equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        // Étape 3 : méthode non-GET → 405
        if (!method.equalsIgnoreCase("GET")) {
            exchange.getResponseHeaders().add("Allow", "GET, OPTIONS");
            sendJson(exchange, 405, "{\"error\":\"Méthode non autorisée. Utiliser GET.\"}");
            return;
        }

        // Étape 4 : route inconnue → 404
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();
        if (!path.equals("/calculate")) {
            sendJson(exchange, 404, "{\"error\":\"Route introuvable.\"}");
            return;
        }

        // Étape 5 : parse les query params
        Map<String, String> params = parseQuery(uri.getRawQuery());

        if (!params.containsKey("operation") || !params.containsKey("a") || !params.containsKey("b")) {
            sendJson(exchange, 400, "{\"error\":\"Paramètres attendus : operation, a, b\"}");
            return;
        }

        // Étape 6 : a et b doivent être numériques
        double a, b;
        try {
            a = Double.parseDouble(params.get("a"));
            b = Double.parseDouble(params.get("b"));
        } catch (NumberFormatException e) {
            sendJson(exchange, 400, "{\"error\":\"Les paramètres a et b doivent être des nombres.\"}");
            return;
        }

        // Étape 7 : opération inconnue → 400
        String operation = params.get("operation");
        if (!operation.equals("add") && !operation.equals("subtract")
                && !operation.equals("multiply") && !operation.equals("divide")) {
            sendJson(exchange, 400, "{\"error\":\"Opération inconnue. Utiliser : add, subtract, multiply, divide\"}");
            return;
        }

        // Étape 8 : calcul (division par zéro possible)
        double result;
        try {
            result = switch (operation) {
                case "add"      -> Calculator.add(a, b);
                case "subtract" -> Calculator.subtract(a, b);
                case "multiply" -> Calculator.multiply(a, b);
                default         -> Calculator.divide(a, b);
            };
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
            return;
        }

        // Succès → 200
        String json = String.format(
            "{\"operation\":\"%s\",\"a\":%s,\"b\":%s,\"result\":%s}",
            operation,
            formatNumber(a),
            formatNumber(b),
            formatNumber(result)
        );
        sendJson(exchange, 200, json);
    }

    private void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // Évite d'afficher "5.0" au lieu de "5" pour les entiers
    private String formatNumber(double n) {
        if (Double.isInfinite(n) || Double.isNaN(n)) {
            return String.valueOf(n);
        }
        if (n == Math.floor(n) && !Double.isInfinite(n)) {
            return String.valueOf((long) n);
        }
        return String.valueOf(n);
    }

    // Parse "operation=add&a=5&b=3" en Map
    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new LinkedHashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }
}
