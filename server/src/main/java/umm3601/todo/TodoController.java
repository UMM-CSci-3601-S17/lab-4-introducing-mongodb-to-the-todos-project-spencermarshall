package umm3601.todo;

import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Sorts;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

public class TodoController {

    private final MongoCollection<Document> TodoCollection;

    public TodoController() throws IOException {
        // Set up our server address
        // (Default host: 'localhost', default port: 27017)
        // ServerAddress testAddress = new ServerAddress();

        // Try connecting to the server
        //MongoClient mongoClient = new MongoClient(testAddress, credentials);
        MongoClient mongoClient = new MongoClient(); // Defaults!

        // Try connecting to a database
        MongoDatabase db = mongoClient.getDatabase("test");

        TodoCollection = db.getCollection("todos");
    }

    // List Todos
    public String listTodos(Map<String, String[]> queryParams) {
        Document filterDoc = new Document();

        /*if (queryParams.containsKey("status")) {
            boolean targetStatus = queryParams.get("status")[0];
            filterDoc = filterDoc.append("age", targetAge);
        }*/

        FindIterable<Document> matchingTodos = TodoCollection.find(filterDoc);

        return JSON.serialize(matchingTodos);
    }

    // Get a single Todo
    public String getTodo(String id) {
        FindIterable<Document> jsonTodos
                = TodoCollection
                .find(eq("_id", new ObjectId(id)));

        Iterator<Document> iterator = jsonTodos.iterator();

        Document Todo = iterator.next();

        return Todo.toJson();
    }

    // Get the average age of all Todos by company
    public String getAverageAgeByCompany() {
        AggregateIterable<Document> documents
                = TodoCollection.aggregate(
                Arrays.asList(
                        Aggregates.group("$company",
                                Accumulators.avg("averageAge", "$age")),
                        Aggregates.sort(Sorts.ascending("_id"))
                ));
        System.err.println(JSON.serialize(documents));
        return JSON.serialize(documents);
    }

    public String summarizeTodos() {
        AggregateIterable<Document> documents
                = TodoCollection.aggregate(
                Arrays.asList(
                        Aggregates.group( "$category",
                                Accumulators.avg("status", "$status")),
                        Aggregates.sort(Sorts.ascending("category"))
                ));

        System.err.println(JSON.serialize(documents));
        return JSON.serialize(documents);


    }
    

}
