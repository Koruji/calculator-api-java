package main.java.core.calculator;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {

    // Crée et configure le serveur sur le port donné (0 = port aléatoire pour les tests)
    public static HttpServer createServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RequestHandler());
        return server;
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = createServer(3000);
        server.start();
        System.out.println("Serveur démarré sur http://localhost:3000");
    }
}
