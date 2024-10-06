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


    // Testing /todos APIs

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

    // POST /todos no title
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


    // Testing /todos/:id APIs

    // GET /todos/:id existing id
    @Test
    public void testGetTodosId() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos/1"))
                .GET()
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            Assertions.assertEquals(SUCCESS, response.statusCode());

            JsonArray todosList = jsonResponse.get("todos").getAsJsonArray();
            Assertions.assertEquals(1, todosList.size());

            JsonObject todo = todosList.get(0).getAsJsonObject();

            Assertions.assertEquals("scan paperwork", todo.get("title").getAsString());
            Assertions.assertEquals("", todo.get("description").getAsString());
            Assertions.assertFalse(todo.get("doneStatus").getAsBoolean());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // GET /todos/:id non-existing id
    @Test
    public void testGetTodosIdNotFound() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos/3"))
                .GET()
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(NOT_FOUND, response.statusCode());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // TODO: Add test for POST PUT DELETE of /todos/:id

    // POST /todos/:id valid id no change
    @Test
    public void testPostTodosId() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos/1"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            JsonObject todo = JsonParser.parseString(response.body()).getAsJsonObject();

            Assertions.assertEquals(SUCCESS, response.statusCode());
            Assertions.assertEquals("scan paperwork", todo.get("title").getAsString());
            Assertions.assertEquals("", todo.get("description").getAsString());
            Assertions.assertFalse(todo.get("doneStatus").getAsBoolean());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // POST /todos/:id valid id with change
    @Test
    public void testPostTodosIdChange() {
        JsonObject newTodo = new JsonObject();
        newTodo.addProperty("title", "New Title");
        newTodo.addProperty("doneStatus", true);
        newTodo.addProperty("description", "New Description");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos/1"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(newTodo.toString()))
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

            Assertions.assertEquals(SUCCESS, response.statusCode());
            Assertions.assertEquals("New Title", jsonResponse.get("title").getAsString());
            Assertions.assertEquals("New Description", jsonResponse.get("description").getAsString());
            Assertions.assertTrue(jsonResponse.get("doneStatus").getAsBoolean());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // POST /todos/:id invalid id
    @Test
    public void testPostTodosInvalidId() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos/3"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(NOT_FOUND, response.statusCode());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // OPTIONS /todos/:id
    @Test
    public void testOptionsTodosId() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos/1"))
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(SUCCESS, response.statusCode());
            Assertions.assertEquals("OPTIONS, GET, HEAD, POST, PUT, DELETE", response.headers().firstValue("Allow").get());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // HEAD /todos/:id valid id
    @Test
    public void testHeadTodosId() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos/1"))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(SUCCESS, response.statusCode());
            Assertions.assertEquals("", response.body());
            Assertions.assertEquals("application/json", response.headers().firstValue("Content-Type").get());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // HEAD /todos/:id invalid id
    @Test
    public void testHeadTodosIdNotFound() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos/3"))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(NOT_FOUND, response.statusCode());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // PATCH /todos/:id
    @Test
    public void testPatchTodosId() {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos/1"))
                .header("Content-Type", "application/json")
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


    // Testing /shutdown API

    // GET /shutdown
    @Test
    public void testShutdown() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/shutdown"))
                .GET()
                .build();
        try {
            HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.discarding());
        }catch (IOException | InterruptedException e){
            System.out.println("Expected connection error due to shutdown");
        }

        Assertions.assertFalse(jar.isAlive(), "Process should be terminated after shutdown");

    }
}
