import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;


import static java.lang.Thread.sleep;

@TestMethodOrder(MethodOrderer.Random.class)
public class TodosTest {

    public static Process jar;
    private static MetricsRecorder metricsRecorder;

    static final String baseURL = "http://localhost:4567";

    static final int SUCCESS = 200;
    static final int CREATED = 201;
    static final int BAD_REQUEST = 400;
    static final int NOT_FOUND = 404;
    static final int METHOD_NOT_ALLOWED = 405;

    static final int sleepTime = 500;
    private static final int[] objectCounts = {1, 20, 50, 70, 100, 500};
    static final String csvFile = "todo_metrics.csv";

    private JsonObject randomTodo;



    @BeforeAll
    public static void setup() throws IOException {
        metricsRecorder = new MetricsRecorder(csvFile);
    }

    @AfterAll
    public static void tearDown() throws IOException {
        metricsRecorder.close();
    }

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

    // GET /todos?doneStatus=true
    @Test
    public void testGetTodosDoneStatusTrue() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos?doneStatus=true"))
                .GET()
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

            Assertions.assertEquals(SUCCESS, response.statusCode());

            // Check if the system returns its 1 element
            JsonArray todosList = jsonResponse.get("todos").getAsJsonArray();
            Assertions.assertEquals(0, todosList.size());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // GET /todos?doneStatus=false
    @Test
    public void testGetTodosDoneStatusFalse() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos?doneStatus=false"))
                .GET()
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

            Assertions.assertEquals(SUCCESS, response.statusCode());

            // Check if the system returns its 1 element
            JsonArray todosList = jsonResponse.get("todos").getAsJsonArray();
            Assertions.assertEquals(2, todosList.size());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // GET /todos?title=scan%20paperwork
    @Test
    public void testGetTodosTitle() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos?title=scan%20paperwork"))
                .GET()
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

            Assertions.assertEquals(SUCCESS, response.statusCode());

            // Check if the system returns its 1 element
            JsonArray todosList = jsonResponse.get("todos").getAsJsonArray();
            Assertions.assertEquals(1, todosList.size());

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

    // POST /todos Json
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

    // POST /todos XML
    @Test
    public void testPostTodosXml() {
        String newTodo = "<todo><title>New Todo</title><doneStatus>false</doneStatus><description>New Description</description></todo>";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos"))
                .header("Content-Type", "application/xml")
                .header("Accept", "application/xml")
                .POST(HttpRequest.BodyPublishers.ofString(newTodo))
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(CREATED, response.statusCode());

            // Parse the response body as XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(response.body().getBytes(StandardCharsets.UTF_8)));

            // Check content in response XML
            NodeList titleNode = doc.getElementsByTagName("title");
            NodeList doneStatusNode = doc.getElementsByTagName("doneStatus");
            NodeList descriptionNode = doc.getElementsByTagName("description");

            Assertions.assertEquals("New Todo", titleNode.item(0).getTextContent());
            Assertions.assertEquals("false", doneStatusNode.item(0).getTextContent());
            Assertions.assertEquals("New Description", descriptionNode.item(0).getTextContent());

        } catch (IOException | InterruptedException | org.xml.sax.SAXException | javax.xml.parsers.ParserConfigurationException e) {
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

    // POST /todos/:id valid id with no request body
    //WARN: Test will fail because it should return 400 but returns 200
    @Test
    public void testPostTodosIdNoBody() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos/1"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(BAD_REQUEST, response.statusCode());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // POST /todos/:id valid id with request body
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

    // PUT /todos/:id No request Body
    @Test
    public void testPutTodosIdNoBody() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos/1"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(BAD_REQUEST, response.statusCode());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // PUT /todos/:id with body
    @Test
    public void testPutTodosIdWithBody(){
        JsonObject newTodo = new JsonObject();
        newTodo.addProperty("title", "New Title");
        newTodo.addProperty("doneStatus", true);
        newTodo.addProperty("description", "New Description");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos/1"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(newTodo.toString()))
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

    // PUT /todos/:id invalid id
    @Test
    public void testPutTodosInvalidId() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos/3"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(NOT_FOUND, response.statusCode());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // DELETE /todos/:id
    @Test
    public void testDeleteTodosId() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos/1"))
                .DELETE()
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(SUCCESS, response.statusCode());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // DELETE /todos/:id invalid id
    @Test
    public void testDeleteTodosIdNotFound() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos/3"))
                .DELETE()
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
            Assertions.assertFalse(jar.isAlive(), "Process should be terminated after shutdown");
        }

        Assertions.assertFalse(jar.isAlive(), "Process should be terminated after shutdown");

    }


    // Testing malformed request

    // Malformed JSON
    @Test
    public void testPostMalformedJson() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"New Todo\""))
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(BAD_REQUEST, response.statusCode());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Malformed XML
    @Test
    public void testPostMalformedXml() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos"))
                .header("Content-Type", "application/xml")
                .POST(HttpRequest.BodyPublishers.ofString("<title>New Todo</title>"))
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(BAD_REQUEST, response.statusCode());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }



    // Testing /todos APIs with different number of objects

    @Test
    public void testCreateMultipleTodos() {
        for (int numObjects : objectCounts) {
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < numObjects; i++) {
                randomTodo = RandomTodoGenerator.generateTodo();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(baseURL + "/todos"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(randomTodo.toString()))
                        .build();
                try {
                    HttpClient.newHttpClient()
                            .send(request, HttpResponse.BodyHandlers.ofString());
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            long endTime = System.currentTimeMillis();
            try {
                metricsRecorder.recordMetrics("POST", numObjects, endTime - startTime);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testDeleteMultipleTodos() {
        for (int numObjects : objectCounts) {
            // Create test objects first
            for (int i = 0; i < numObjects; i++) {
                randomTodo = RandomTodoGenerator.generateTodo();
                HttpRequest createRequest = HttpRequest.newBuilder()
                        .uri(URI.create(baseURL + "/todos"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(randomTodo.toString()))
                        .build();
                try {
                    HttpClient.newHttpClient()
                            .send(createRequest, HttpResponse.BodyHandlers.ofString());
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Measure time for deleting objects
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < numObjects; i++) {
                HttpRequest deleteRequest = HttpRequest.newBuilder()
                        .uri(URI.create(baseURL + "/todos/" + i)) // Assuming IDs are sequential
                        .DELETE()
                        .build();
                try {
                    HttpClient.newHttpClient()
                            .send(deleteRequest, HttpResponse.BodyHandlers.ofString());
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            long endTime = System.currentTimeMillis();

            try {
                metricsRecorder.recordMetrics("DELETE", numObjects, endTime - startTime);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testUpdateMultipleTodos() {
        for (int numObjects : objectCounts) {
            // Create test objects first
            for (int i = 0; i < numObjects; i++) {
                randomTodo = RandomTodoGenerator.generateTodo();
                HttpRequest createRequest = HttpRequest.newBuilder()
                        .uri(URI.create(baseURL + "/todos/" + i))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(randomTodo.toString()))
                        .build();
                try {
                    HttpClient.newHttpClient()
                            .send(createRequest, HttpResponse.BodyHandlers.ofString());
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Measure time for updating objects
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < numObjects; i++) {
                randomTodo = RandomTodoGenerator.generateTodo();
                HttpRequest updateRequest = HttpRequest.newBuilder()
                        .uri(URI.create(baseURL + "/todos/" + i)) // Assuming IDs are sequential
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(randomTodo.toString()))
                        .build();
                try {
                    HttpClient.newHttpClient()
                            .send(updateRequest, HttpResponse.BodyHandlers.ofString());
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            long endTime = System.currentTimeMillis();

            try {
                metricsRecorder.recordMetrics("PUT", numObjects, endTime - startTime);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
