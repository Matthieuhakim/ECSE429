import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.*;

import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


import static java.lang.Thread.sleep;

@TestMethodOrder(MethodOrderer.Random.class)
public class TodosTest {

    public static Process jar;

    static final String baseURL = "http://localhost:4567";

    static final int SUCCESS = 200;
    static final int CREATED = 201;
    static final int BAD_REQUEST = 400;
    static final int NOT_FOUND = 404;
    static final int METHOD_NOT_ALLOWED = 405;

    static final int sleepTime = 500;


    @BeforeEach
    public void startServer() throws Exception {
        jar = Runtime.getRuntime().exec("java -jar runTodoManagerRestAPI-1.5.5.jar");
        sleep(sleepTime);
    }

    @AfterEach
    public void stopServer() throws InterruptedException{

        if(jar.isAlive()){
            jar.destroy();
            sleep(sleepTime);
        }
    }

    // GET /todos
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

            Assertions.assertEquals(SUCCESS, response.statusCode());

            // Check if the system returns its 2 elements
            JsonArray todosList = jsonResponse.get("todos").getAsJsonArray();
            Assertions.assertEquals(2, todosList.size());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // PUT /todos
    @Test
    public void testPutTodos() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(METHOD_NOT_ALLOWED, response.statusCode());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // POST /todos
    @Test
    public void testPostTodosJson() {
        JsonObject newTodo = new JsonObject();
        newTodo.addProperty("title", "New Todo");
        newTodo.addProperty("doneStatus", false);
        newTodo.addProperty("description", "New Description");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(newTodo.toString()))
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

            Assertions.assertEquals(CREATED, response.statusCode());
            Assertions.assertEquals("New Todo", jsonResponse.get("title").getAsString());
            Assertions.assertEquals("New Description", jsonResponse.get("description").getAsString());
            Assertions.assertFalse(jsonResponse.get("doneStatus").getAsBoolean());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPostTodosNoTitle(){
        JsonObject newTodo = new JsonObject();
        newTodo.addProperty("doneStatus", false);
        newTodo.addProperty("description", "New Description");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(newTodo.toString()))
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(BAD_REQUEST, response.statusCode());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    // DELETE /todos
    @Test
    public void testDeleteTodos() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos"))
                .DELETE()
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(METHOD_NOT_ALLOWED, response.statusCode());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // OPTIONS /todos
    @Test
    public void testOptionsTodos() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos"))
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(SUCCESS, response.statusCode());
            Assertions.assertEquals("OPTIONS, GET, HEAD, POST", response.headers().firstValue("Allow").get());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // HEAD /todos
    @Test
    public void testHeadTodos() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos"))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(SUCCESS, response.statusCode());
            Assertions.assertEquals("", response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // PATCH /todos
    @Test
    public void testPatchTodos() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos"))
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(METHOD_NOT_ALLOWED, response.statusCode());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // GET /shutdown
    @Test
    public void testShutdown() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/shutdown"))
                .GET()
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(SUCCESS, response.statusCode());

            sleep(sleepTime);

            Assertions.assertFalse(jar.isAlive(), "Process should be terminated after shutdown");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
