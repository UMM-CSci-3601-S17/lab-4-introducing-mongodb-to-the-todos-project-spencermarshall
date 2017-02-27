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
import java.util.ArrayList;
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
                        Aggregates.group("$owner",
                                Accumulators.push("status", "$status")),
                        Aggregates.sort(Sorts.ascending("status"))
                ));
        ArrayList<String> docs = new ArrayList<String>();


        for (Document element : documents) {
            docs.add(element.values().toString());
        }

        String temp = docs.get(0);
        String toReturn = "";
        String thingy = "";
        int thingCount = 0;
        for (int i = 0; i < docs.size(); i++) {
            thingCount += instancesOfString(docs.get(i), "true");
        }


        //System.err.println(JSON.serialize(documents));
        return thingy + thingCount + "DocsSize:" + docs.size(); //JSON.serialize(documents);

    }


    public int instancesOfString(String body, String term) {
        int count = 0;
        int termLength = term.length();
        int currentIndex = 0;
        int startingIndex = 0;

        while (body.charAt(startingIndex) != '[') {
            startingIndex++;
        }

        currentIndex = startingIndex;

        while (currentIndex < body.length()) {
            if (body.substring(currentIndex).indexOf(term) == 0) {
                count++;
                currentIndex += termLength; //CHANGE THIS
            }
            else {
                currentIndex++;
            }

        }

        return count;
    }
    

}
