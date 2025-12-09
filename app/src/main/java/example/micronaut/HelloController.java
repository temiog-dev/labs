package example.micronaut;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;

public class HelloController implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        // Expected /hello/{name}
        String name = "";
        if (path.startsWith("/hello/")) {
            name = path.substring("/hello/".length());
        }
        
        String response = combineName(name);
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
    
    //Example of a function that can be tested through normal unit frameworks
    public String combineName( String name) {
        return "Hello "+name;
    }
}
