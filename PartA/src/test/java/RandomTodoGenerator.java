import com.google.gson.JsonObject;
import com.github.javafaker.Faker;

public class RandomTodoGenerator {

    private static final Faker faker = new Faker();

    public static JsonObject generateTodo() {
        JsonObject todo = new JsonObject();
        todo.addProperty("title", faker.lorem().sentence());
        todo.addProperty("doneStatus", faker.bool().bool());
        todo.addProperty("description", faker.lorem().paragraph());
        return todo;
    }
}
