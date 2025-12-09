package example.micronaut;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Application {

    public static void main(String[] args) throws IOException {
        int port = 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        server.createContext("/", new RootController());
        server.createContext("/hello", new HelloController());
        server.createContext("/status", new ReadyController());
        
        server.setExecutor(null); 
        server.start();
        System.out.println("Server started on port " + port);
    }
}
