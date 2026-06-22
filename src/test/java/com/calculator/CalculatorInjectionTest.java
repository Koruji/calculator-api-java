package test.java.com.calculator;

import com.sun.net.httpserver.HttpServer;
import main.java.core.calculator.Server;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class CalculatorInjectionTest {

    private static HttpServer server;
    private static HttpClient client;
    private static int port;

    @BeforeAll
    static void startServer() throws IOException {
        server = Server.createServer(0);
        server.start();
        port = server.getAddress().getPort();
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // ----- Injection dans le paramètre operation ----- //

    @ParameterizedTest(name = "operation=\"{0}\" -> 400")
    @ValueSource(strings = {
            "add;DROP TABLE users",
            "add OR 1=1",
            "<script>alert(1)</script>",
            "../../etc/passwd",
            "add&&rm -rf /",
            "__proto__",
            "constructor",
            "add`whoami`"
    })
    void shouldRejectInjectionInOperation(String maliciousOperation) throws Exception {
        String encoded = java.net.URLEncoder.encode(maliciousOperation, StandardCharsets.UTF_8);
        var res = get("/calculate?operation=" + encoded + "&a=5&b=3");
        assertEquals(400, res.statusCode());
        assertTrue(res.body().contains("\"error\""));
    }

    // ----- Injection dans les paramètres numériques ----- //

    @ParameterizedTest(name = "a=\"{0}\" -> 400")
    @ValueSource(strings = {
            "5;DROP TABLE users",
            "1 OR 1=1",
            "<img onerror=alert(1)>",
            "1e999999999",
            "NaN",
            "undefined",
            "null",
            "Infinity",
            "1+1",
            "Math.random()"
    })
    void shouldRejectInjectionInA(String maliciousValue) throws Exception {
        String encoded = java.net.URLEncoder.encode(maliciousValue, StandardCharsets.UTF_8);
        var res = get("/calculate?operation=add&a=" + encoded + "&b=3");
        assertEquals(400, res.statusCode());
        assertTrue(res.body().contains("\"error\""));
    }

    @ParameterizedTest(name = "b=\"{0}\" -> 400")
    @ValueSource(strings = {
            "0;DELETE FROM users",
            "0 UNION SELECT * FROM users",
            "<script>",
            "null",
            "undefined",
            "true"
    })
    void shouldRejectInjectionInB(String maliciousValue) throws Exception {
        String encoded = java.net.URLEncoder.encode(maliciousValue, StandardCharsets.UTF_8);
        var res = get("/calculate?operation=add&a=5&b=" + encoded);
        assertEquals(400, res.statusCode());
        assertTrue(res.body().contains("\"error\""));
    }

    // ----- Paramètres vides ----- //

    @Test
    void shouldRejectEmptyOperation() throws Exception {
        var res = get("/calculate?operation=&a=5&b=3");
        assertEquals(400, res.statusCode());
    }

    @Test
    void shouldRejectEmptyA() throws Exception {
        var res = get("/calculate?operation=add&a=&b=3");
        assertEquals(400, res.statusCode());
    }

    @Test
    void shouldRejectEmptyB() throws Exception {
        var res = get("/calculate?operation=add&a=5&b=");
        assertEquals(400, res.statusCode());
    }

    // ----- Payload très long (DoS basique) ----- //

    @Test
    void shouldRejectVeryLongOperation() throws Exception {
        String longString = "a".repeat(10000);
        String encoded = java.net.URLEncoder.encode(longString, StandardCharsets.UTF_8);
        var res = get("/calculate?operation=" + encoded + "&a=5&b=3");
        assertEquals(400, res.statusCode());
    }
}
