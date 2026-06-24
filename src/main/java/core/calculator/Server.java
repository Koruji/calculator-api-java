package core.calculator;

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
        int port = 3000;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        HttpServer server = createServer(port);
        server.start();
        System.out.println("Serveur démarré sur http://localhost:" + port);
    }
}
