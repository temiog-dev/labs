package example.micronaut;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelloControllerTest {

    private static HttpServer server;
    private static HttpClient client;
    private static final int PORT = 8081;

    @BeforeAll
    public static void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new RootController());
        server.createContext("/hello", new HelloController());
        server.createContext("/status", new ReadyController());
        server.setExecutor(null);
        server.start();
        
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    public static void tearDown() {
        server.stop(0);
    }

    @Test
    public void testHello() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/hello/sofus"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertEquals("Hello sofus", response.body());
    }

    @Test
    public void testCombineName() {
        String name = "Sonny";
        HelloController sut = new HelloController();
        assertEquals("Hello "+name, sut.combineName(name),"Name and greeting not properly combined");
    }

    @Test
    public void testIndexWithDifferentNames() throws IOException, InterruptedException {
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/hello/Alice"))
                .GET()
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals("Hello Alice", response1.body());

        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/hello/Bob"))
                .GET()
                .build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals("Hello Bob", response2.body());
    }

    @Test
    public void testCombineNameWithNull() {
        HelloController sut = new HelloController();
        String result = sut.combineName(null);
        assertEquals("Hello null", result);
    }

    @Test
    public void testCombineNameWithEmptyString() {
        HelloController sut = new HelloController();
        String result = sut.combineName("");
        assertEquals("Hello ", result);
    }
}