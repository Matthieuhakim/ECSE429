import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

import static java.lang.Thread.sleep;

@TestMethodOrder(MethodOrderer.Random.class)
public class TodosTest {

    static final String baseURL = "http://localhost:4567";
    public static Process jar;

    @BeforeEach
    public void startServer() throws Exception {
        jar = Runtime.getRuntime().exec("java -jar runTodoManagerRestAPI-1.5.5.jar");
        sleep(500);
    }

    @AfterEach
    public void stopServer() throws InterruptedException{
        jar.destroy();
        sleep(500);
    }

    // Test GET /todos

    @Test
    public void testGetTodos() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos"))
                .GET()
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

            Assertions.assertEquals(200, response.statusCode());

            // Check if the array has 2 elements
            JsonArray todosList = jsonResponse.get("todos").getAsJsonArray();
            Assertions.assertEquals(2, todosList.size());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
