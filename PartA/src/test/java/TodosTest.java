package test.java;

import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


@TestMethodOrder(MethodOrderer.Random.class)
public class TodosTest {

    static final String baseURL = "http://localhost:4567/todos";
    static HttpClient client = HttpClient.newHttpClient();
    static HttpRequest.Builder request = HttpRequest.newBuilder();

    @BeforeAll
    public static void setup() throws Throwable{
        try{
            HttpRequest init_ping_request = request.uri(new URI(baseURL)).build();
            client.send(init_ping_request, HttpResponse.BodyHandlers.ofString());
        }
        catch (Throwable  e){
            System.out.println(e);
            throw e;
        }
    }

    @Test
    public void testGetTodos() throws Throwable {
        try{
            HttpRequest get_request = request.uri(new URI(baseURL)).build();
            HttpResponse<String> response = client.send(get_request, HttpResponse.BodyHandlers.ofString());
            Assertions.assertEquals(200, response.statusCode());
        }
        catch (Throwable e){
            System.out.println(e);
            throw e;
        }
    }
}
