package test.java.com.calculator;

import com.sun.net.httpserver.HttpServer;
import core.calculator.Server;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class ApiTest {

    private static HttpServer server;
    private static HttpClient client;
    private static int port;

    // Démarre le serveur une seule fois avant tous les tests
    @BeforeAll
    static void startServer() throws IOException {
        server = Server.createServer(0); // port 0 = port libre aléatoire
        server.start();
        port = server.getAddress().getPort();
        client = HttpClient.newHttpClient();
    }

    // Arrête le serveur après tous les tests
    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    // Méthode utilitaire : envoie une requête GET et retourne la réponse
    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // Méthode utilitaire : envoie une requête avec une méthode custom (POST, PUT...)
    private HttpResponse<String> send(String method, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .method(method, HttpRequest.BodyPublishers.noBody())
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // ----- Headers ----- //

    @Test
    void shouldReturnJsonContentType() throws Exception {
        var res = get("/calculate?operation=add&a=1&b=2");
        assertEquals("application/json; charset=utf-8", res.headers().firstValue("content-type").orElse(""));
    }

    @Test
    void shouldReturnCorsHeader() throws Exception {
        var res = get("/calculate?operation=add&a=1&b=2");
        assertEquals("*", res.headers().firstValue("access-control-allow-origin").orElse(""));
    }

    // ----- OPTIONS (preflight CORS) ----- //

    @Test
    void shouldReturn204OnOptions() throws Exception {
        var res = send("OPTIONS", "/calculate");
        assertEquals(204, res.statusCode());
    }

    @Test
    void shouldReturnEmptyBodyOnOptions() throws Exception {
        var res = send("OPTIONS", "/calculate");
        assertTrue(res.body().isEmpty());
    }

    // ----- Cas nominaux ----- //

    @ParameterizedTest(name = "{0}: {1} op {2} = {3}")
    @CsvSource({
            "add,      5,    3,   8",
            "add,     -5,   -3,  -8",
            "subtract, 10,   4,   6",
            "subtract, 3,    10, -7",
            "multiply, 6,    7,  42",
            "multiply, -3,  -4,  12",
            "divide,   20,   5,   4",
            "divide,  -10,  -2,   5"
    })
    void shouldCalculate(String operation, double a, double b, double expected) throws Exception {
        var res = get("/calculate?operation=" + operation + "&a=" + a + "&b=" + b);
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("\"result\":" + (long) expected),
                "Body attendu contenant result:" + (long) expected + " — reçu : " + res.body());
    }

    @Test
    void shouldReturnCorrectJsonShape() throws Exception {
        var res = get("/calculate?operation=add&a=5&b=3");
        String body = res.body();
        assertTrue(body.contains("\"operation\":\"add\""));
        assertTrue(body.contains("\"a\":5"));
        assertTrue(body.contains("\"b\":3"));
        assertTrue(body.contains("\"result\":8"));
    }

    // ----- Méthodes non autorisées ----- //

    @ParameterizedTest(name = "{0} /calculate -> 405")
    @CsvSource({"POST", "PUT", "DELETE", "PATCH"})
    void shouldReturn405OnWrongMethod(String method) throws Exception {
        var res = send(method, "/calculate?operation=add&a=1&b=2");
        assertEquals(405, res.statusCode());
        assertTrue(res.body().contains("\"error\""));
    }

    @Test
    void shouldReturnAllowHeaderOn405() throws Exception {
        var res = send("POST", "/calculate");
        assertTrue(res.headers().firstValue("allow").isPresent());
    }

    // ----- Erreurs 400 ----- //

    @Test
    void shouldReturn400WhenMissingOperation() throws Exception {
        var res = get("/calculate?a=5&b=3");
        assertEquals(400, res.statusCode());
        assertTrue(res.body().contains("Paramètres attendus"));
    }

    @Test
    void shouldReturn400WhenMissingA() throws Exception {
        var res = get("/calculate?operation=add&b=3");
        assertEquals(400, res.statusCode());
    }

    @Test
    void shouldReturn400WhenMissingB() throws Exception {
        var res = get("/calculate?operation=add&a=5");
        assertEquals(400, res.statusCode());
    }

    @Test
    void shouldReturn400WhenAIsNotNumeric() throws Exception {
        var res = get("/calculate?operation=add&a=abc&b=3");
        assertEquals(400, res.statusCode());
        assertTrue(res.body().contains("nombres"));
    }

    @Test
    void shouldReturn400WhenBIsNotNumeric() throws Exception {
        var res = get("/calculate?operation=add&a=5&b=xyz");
        assertEquals(400, res.statusCode());
    }

    @Test
    void shouldReturn400WhenUnknownOperation() throws Exception {
        var res = get("/calculate?operation=modulo&a=5&b=3");
        assertEquals(400, res.statusCode());
        assertTrue(res.body().contains("Opération inconnue"));
    }

    @Test
    void shouldReturn400WhenDivisionByZero() throws Exception {
        var res = get("/calculate?operation=divide&a=10&b=0");
        assertEquals(400, res.statusCode());
        assertTrue(res.body().contains("zéro") || res.body().contains("zero"));
    }

    // ----- Erreurs 404 ----- //

    @ParameterizedTest(name = "GET {0} -> 404")
    @CsvSource({"/", "/unknown", "/calculate/"})
    void shouldReturn404OnUnknownRoute(String path) throws Exception {
        var res = get(path);
        assertEquals(404, res.statusCode());
        assertTrue(res.body().contains("Route introuvable"));
    }

    // ----- Cas limites ----- //

    @Test
    void shouldHandleVeryLargeNumbers() throws Exception {
        var res = get("/calculate?operation=add&a=1e308&b=1e308");
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("Infinity"));
    }

    @Test
    void shouldHandleDecimalResult() throws Exception {
        var res = get("/calculate?operation=divide&a=1&b=3");
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("\"result\""));
    }
}
