package steps;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TodoStepDefinitions {

    static final String baseURL = "http://localhost:4567";

    private HttpResponse<String> lastResponse;


    // Helper method to retrieve all todos
    private JsonArray retrieveAllTodos() throws IOException, InterruptedException {
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos"))
                .GET()
                .build();
        HttpResponse<String> getResponse = HttpClient.newHttpClient()
                .send(getRequest, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, getResponse.statusCode(), "Failed to retrieve todos");

        JsonObject jsonResponse = JsonParser.parseString(getResponse.body()).getAsJsonObject();
        return jsonResponse.get("todos").getAsJsonArray();
    }

    // Helper method to delete a todo by ID
    private void deleteTodoById(int id) throws IOException, InterruptedException {
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + "/todos/" + id))
                .DELETE()
                .build();
        HttpClient.newHttpClient().send(deleteRequest, HttpResponse.BodyHandlers.ofString());
    }

    // Helper method to delete all todos
    private void deleteAllTodos() throws IOException, InterruptedException {
        JsonArray todosList = retrieveAllTodos();
        for (JsonElement todoElement : todosList) {
            int id = todoElement.getAsJsonObject().get("id").getAsInt();
            deleteTodoById(id);
        }
    }

    // Helper method to add todos from a DataTable
    private void addTodosFromDataTable(io.cucumber.datatable.DataTable dataTable) throws IOException, InterruptedException {
        List<Map<String, String>> todosData = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : todosData) {
            JsonObject newTodo = new JsonObject();
            newTodo.addProperty("title", row.get("title"));
            newTodo.addProperty("doneStatus", Boolean.parseBoolean(row.get("doneStatus")));
            newTodo.addProperty("description", row.get("description"));

            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseURL + "/todos"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(newTodo.toString()))
                    .build();
            HttpResponse<String> postResponse = HttpClient.newHttpClient().send(postRequest, HttpResponse.BodyHandlers.ofString());
            Assertions.assertEquals(201, postResponse.statusCode(), "Todo creation failed");
        }
    }

    // Helper method to verify the number of todos in the system
    private void verifyTodosCount(int expectedCount) throws IOException, InterruptedException {
        JsonArray todosList = retrieveAllTodos();
        Assertions.assertEquals(expectedCount, todosList.size(), "Unexpected number of todos in the system");
    }

    // Private helper method to parse the XML response body
    private Document parseXmlResponse(String xmlResponse) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            Document doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xmlResponse)));
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            Assertions.fail("Failed to parse XML response body: " + e.getMessage());
            return null; // This line will not be reached due to the assertion failure
        }
    }

    // Private helper method to validate the XML element's value
    private void validateXmlElement(Document doc, String tag, String expectedValue) {
        NodeList nodes = doc.getElementsByTagName(tag);
        Assertions.assertTrue(nodes.getLength() > 0, "XML response is missing element: " + tag);

        String actualValue = nodes.item(0).getTextContent().trim();
        Assertions.assertEquals(expectedValue, actualValue, "Value for element '" + tag + "' does not match expected value.");
    }

    // Private helper method get the id of the todo with the given title
    private int getTodoIdByTitle(String title) throws IOException, InterruptedException {
        JsonArray todosList = retrieveAllTodos();
        for (JsonElement todoElement : todosList) {
            JsonObject todo = todoElement.getAsJsonObject();
            if (todo.get("title").getAsString().equals(title)) {
                return todo.get("id").getAsInt();
            }
        }
        return -1;
    }

    @Given("the thingifier application is running")
    public void the_thingifier_application_is_running() throws Exception{
        //Check that the application is running

        try{
            retrieveAllTodos();
        }catch (Exception e){
            Assertions.fail("The application is not running");
        }
    }

    @Given("no todo exists in the application except the following:")
    public void no_todo_exists_in_the_application_except_the_following(io.cucumber.datatable.DataTable dataTable) {

        try {
            deleteAllTodos();

            addTodosFromDataTable(dataTable);

            verifyTodosCount(dataTable.asMaps(String.class, String.class).size());

        } catch (IOException | InterruptedException e) {
            Assertions.fail("Error occurred while setting up todos: " + e.getMessage());
        }
    }

    @And("the response body should be a JSON object with the following key-value pairs:")
    public void the_response_body_should_be_a_json_object_with_the_following_key_value_pairs(io.cucumber.datatable.DataTable dataTable) {
        // Parse the response body as a JSON object
        JsonObject responseBody = JsonParser.parseString(lastResponse.body()).getAsJsonObject();

        // Parse DataTable into a list of key-value pairs
        List<Map<String, String>> keyValuePairs = dataTable.asMaps(String.class, String.class);

        // Verify each key-value pair in the response body
        for (Map<String, String> pair : keyValuePairs) {
            String key = pair.get("key");
            String expectedValue = pair.get("value");

            // Check if the JSON object has the expected key
            Assertions.assertTrue(responseBody.has(key), "Response JSON is missing key: " + key);

            // Check if the value matches the expected value
            String actualValue = responseBody.get(key).getAsString();

            // Convert actual boolean values to match string representation if needed
            if (expectedValue.equalsIgnoreCase("true") || expectedValue.equalsIgnoreCase("false")) {
                Assertions.assertEquals(Boolean.parseBoolean(expectedValue), responseBody.get(key).getAsBoolean(),
                        "Value for key '" + key + "' does not match expected boolean value.");
            } else {
                Assertions.assertEquals(expectedValue, actualValue, "Value for key '" + key + "' does not match expected value.");
            }
        }


    }

    @When("the user sends a POST request to the {string} endpoint with XML body with title {string} done status {string} and description {string}")
    public void the_user_sends_a_post_request_with_xml_body(String endpoint, String title, String doneStatus, String description) {
        // Construct XML body
        String xmlBody = String.format(
                "<todo>" +
                        "<title>%s</title>" +
                        "<doneStatus>%s</doneStatus>" +
                        "<description>%s</description>" +
                        "</todo>",
                title, doneStatus, description
        );

        // Build the POST request with XML body
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + endpoint))
                .header("Content-Type", "application/xml")
                .header("Accept", "application/xml")
                .POST(HttpRequest.BodyPublishers.ofString(xmlBody))
                .build();

        try {
            lastResponse = HttpClient.newHttpClient()
                    .send(postRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            Assertions.fail("POST request with XML body failed: " + e.getMessage());
        }
    }

    @And("the response body should be an XML object with the following elements:")
    public void the_response_body_should_be_an_xml_object_with_the_following_elements(io.cucumber.datatable.DataTable dataTable) {

        Document doc = parseXmlResponse(lastResponse.body());
        List<Map<String, String>> elements = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> element : elements) {
            String tag = element.get("element");
            String expectedValue = element.get("value");
            assert doc != null : "Document is null, parsing failed.";
            validateXmlElement(doc, tag, expectedValue);
        }
    }

    @When("the user sends a POST request to the {string} endpoint with JSON body with title {string} done status {string} and description {string}")
    public void the_user_sends_a_post_request_with_json_body(String endpoint, String title, String doneStatus, String description) {
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("title", title);
        jsonBody.addProperty("doneStatus", Boolean.parseBoolean(doneStatus));
        jsonBody.addProperty("description", description);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                .build();

        try {
            lastResponse = HttpClient.newHttpClient()
                    .send(postRequest, HttpResponse.BodyHandlers.ofString());

        } catch (IOException | InterruptedException e) {
            Assertions.fail("POST request failed: " + e.getMessage());
        }
    }

    @Then("the thingifier app should return a response with status code {string}")
    public void theThingifierAppShouldReturnAResponseWithStatusCode(String expectedStatusCode) {
        // Parse the expected status code as an integer
        int expectedCode = Integer.parseInt(expectedStatusCode);

        // Assert that the status code of the last response matches the expected code
        Assertions.assertEquals(expectedCode, lastResponse.statusCode(),
                "Unexpected status code returned by the application");

    }

    @When("the user sends a POST request to the {string} endpoint with the id of the todo with title {string} and JSON body with done status {string}")
    public void theUserSendsAPOSTRequestToTheEndpointWithTheIdOfTheTodoWithTitleAndJSONBodyWithDoneStatus(String endpoint, String title, String doneStatus) {
        try {
            int id = getTodoIdByTitle(title);
            if (id == -1) {
                Assertions.fail("Todo with title '" + title + "' not found");
            }

            JsonObject jsonBody = new JsonObject();
            if (doneStatus.equals("true") || doneStatus.equals("false")) {
                jsonBody.addProperty("doneStatus", Boolean.parseBoolean(doneStatus));
            }else{
                jsonBody.addProperty("doneStatus", doneStatus);
            }

            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseURL + "/todos/" + id))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                    .build();

            lastResponse = HttpClient.newHttpClient()
                    .send(postRequest, HttpResponse.BodyHandlers.ofString());

        } catch (IOException | InterruptedException e) {
            Assertions.fail("POST request failed: " + e.getMessage());
        }
    }

    @When("the user sends a POST request to the {string} endpoint with the id of the todo with title {string} and XML body with done status {string}")
    public void theUserSendsAPOSTRequestToTheEndpointWithTheIdOfTheTodoWithTitleAndXMLBodyWithDoneStatus(String endpoint, String title, String doneStatus) {
        try {
            // Retrieve the todo ID by its title
            int id = getTodoIdByTitle(title);
            if (id == -1) {
                Assertions.fail("Todo with title '" + title + "' not found");
            }

            // Construct XML body
            String xmlBody = "<todo>" +
                    "<doneStatus>" + doneStatus + "</doneStatus>" +
                    "</todo>";

            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseURL + "/todos/" + id))
                    .header("Content-Type", "application/xml")
                    .header("Accept", "application/xml")
                    .POST(HttpRequest.BodyPublishers.ofString(xmlBody))
                    .build();

            // Send the request and capture the last response
            lastResponse = HttpClient.newHttpClient()
                    .send(postRequest, HttpResponse.BodyHandlers.ofString());

        } catch (IOException | InterruptedException e) {
            Assertions.fail("POST request failed: " + e.getMessage());
        }
    }

    @When("the user sends a PUT request to the {string} endpoint with the id of the todo with title {string} and JSON body with title {string}")
    public void theUserSendsAPUTRequestToTheEndpointWithTheIdOfTheTodoWithTitleAndJSONBodyWithTitle(String endpoint, String title, String newTitle) {
        try {
            // Retrieve the todo ID by its title
            int id = getTodoIdByTitle(title);
            if (id == -1) {
                Assertions.fail("Todo with title '" + title + "' not found");
            }

            // Construct JSON body
            JsonObject jsonBody = new JsonObject();
            jsonBody.addProperty("title", newTitle);

            // Build the PUT request with JSON body
            HttpRequest putRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseURL + "/todos/" + id))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                    .build();

            // Send the request and capture the last response
            lastResponse = HttpClient.newHttpClient()
                    .send(putRequest, HttpResponse.BodyHandlers.ofString());

        } catch (IOException | InterruptedException e) {
            Assertions.fail("PUT request failed: " + e.getMessage());
        }
    }

    @When("the user sends a PUT request to the {string} endpoint with the id of the todo with title {string} and JSON body with description {string}")
    public void theUserSendsAPUTRequestToTheEndpointWithTheIdOfTheTodoWithTitleAndJSONBodyWithDescription(String endpoint, String title, String newDescription) {
        try {
            // Retrieve the todo ID by its title
            int id = getTodoIdByTitle(title);
            if (id == -1) {
                Assertions.fail("Todo with title '" + title + "' not found");
            }

            // Construct JSON body
            JsonObject jsonBody = new JsonObject();
            jsonBody.addProperty("title", title);
            jsonBody.addProperty("description", newDescription);

            // Build the PUT request with JSON body
            HttpRequest putRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseURL + "/todos/" + id))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                    .build();

            // Send the request and capture the last response
            lastResponse = HttpClient.newHttpClient()
                    .send(putRequest, HttpResponse.BodyHandlers.ofString());

        } catch (IOException | InterruptedException e) {
            Assertions.fail("PUT request failed: " + e.getMessage());
        }
    }

    @When("the user sends a DELETE request to the {string} endpoint with the id of the todo with title {string}")
    public void theUserSendsADELETERequestToTheEndpointWithTheIdOfTheTodoWithTitle(String endpoint, String title) {
        try {
            // Retrieve the todo ID by its title
            int id = getTodoIdByTitle(title);
            if (id == -1) {
                id = 0;
            }

            // Construct JSON body
            JsonObject jsonBody = new JsonObject();
            jsonBody.addProperty("title", title);

            // Build the DELETE request
            HttpRequest deleteRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseURL + "/todos/" + id))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .method("DELETE", HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                    .build();

            // Send the request and capture the last response
            lastResponse = HttpClient.newHttpClient()
                    .send(deleteRequest, HttpResponse.BodyHandlers.ofString());

        } catch (IOException | InterruptedException e) {
            Assertions.fail("DELETE request failed: " + e.getMessage());
        }
    }

    @And("the thingifier app should not contain the todo with title {string}")
    public void theThingifierAppShouldNotContainTheTodoWithTitle(String title) {
        try {
            int id = getTodoIdByTitle(title);
            Assertions.assertEquals(-1, id, "Todo with title '" + title + "' still exists in the system");

        } catch (IOException | InterruptedException e) {
            Assertions.fail("Failed to verify todo presence: " + e.getMessage());
        }
    }

    @When("the user sends a DELETE request to the {string} endpoint with the id of the todo with title {string} and JSON body with title")
    public void theUserSendsADELETERequestToTheEndpointWithTheIdOfTheTodoWithTitleAndJSONBodyWithTitle(String endpoint, String title) {
        try {
            // Retrieve the todo ID by its title
            int id = getTodoIdByTitle(title);
            if (id == -1) {
                id = 0;
            }

            // Build the DELETE request
            HttpRequest deleteRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseURL + "/todos/" + id))
                    .DELETE()
                    .build();

            // Send the request and capture the last response
            lastResponse = HttpClient.newHttpClient()
                    .send(deleteRequest, HttpResponse.BodyHandlers.ofString());

        } catch (IOException | InterruptedException e) {
            Assertions.fail("DELETE request failed: " + e.getMessage());
        }
    }

    @When("the user sends a GET request to the {string} endpoint")
    public void theUserSendsAGETRequestToTheEndpoint(String endpoint) {
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + endpoint))
                .GET()
                .build();

        try {
            lastResponse = HttpClient.newHttpClient()
                    .send(getRequest, HttpResponse.BodyHandlers.ofString());

        } catch (IOException | InterruptedException e) {
            Assertions.fail("GET request failed: " + e.getMessage());
        }
    }

    @And("the response body should be a list of JSON objects with the following key-value pairs:")
    public void theResponseBodyShouldBeAListOfJSONObjectsWithTheFollowingKeyValuePairs(io.cucumber.datatable.DataTable dataTable) {
        // Parse the expected data from the DataTable
        List<Map<String, String>> expectedTodos = dataTable.asMaps(String.class, String.class);

        // Parse the actual response body
        JsonArray actualTodosArray = JsonParser.parseString(lastResponse.body()).getAsJsonObject().get("todos").getAsJsonArray();

        // Check if the actual response contains the expected todos
        Assertions.assertEquals(expectedTodos.size(), actualTodosArray.size(), "The number of returned todos does not match the expected count.");

        // Create a Set for expected todos without the id
        Set<String> expectedTodoSet = new HashSet<>();

        // Populate the set with expected todos as strings without the id
        for (Map<String, String> expectedTodo : expectedTodos) {
            JsonObject expectedTodoJson = new JsonObject();
            expectedTodoJson.addProperty("title", expectedTodo.get("title"));
            expectedTodoJson.addProperty("doneStatus", Boolean.parseBoolean(expectedTodo.get("doneStatus")));
            expectedTodoJson.addProperty("description", expectedTodo.get("description"));

            expectedTodoSet.add(expectedTodoJson.toString());
        }

        // Validate each actual todo against the expected todos without considering id
        for (int i = 0; i < actualTodosArray.size(); i++) {
            JsonObject actualTodo = actualTodosArray.get(i).getAsJsonObject();

            // Create a temporary JsonObject for comparison without the id
            JsonObject actualTodoWithoutId = new JsonObject();
            actualTodoWithoutId.addProperty("title", actualTodo.get("title").getAsString());
            actualTodoWithoutId.addProperty("doneStatus", actualTodo.get("doneStatus").getAsBoolean());
            actualTodoWithoutId.addProperty("description", actualTodo.get("description").getAsString());

            Assertions.assertTrue(expectedTodoSet.contains(actualTodoWithoutId.toString()),
                    "Actual todo not found in expected todos: " + actualTodoWithoutId);
        }
    }
}
