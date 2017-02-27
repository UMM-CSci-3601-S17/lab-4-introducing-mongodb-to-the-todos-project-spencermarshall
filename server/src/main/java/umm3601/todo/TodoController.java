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
        AggregateIterable<Document> ownerDocs
                = TodoCollection.aggregate(
                Arrays.asList(
                        Aggregates.group("$owner",
                                Accumulators.push("status", "$status")),
                        Aggregates.sort(Sorts.ascending("status"))
                ));
        AggregateIterable<Document> categoryDocs
                = TodoCollection.aggregate(
                Arrays.asList(
                        Aggregates.group("$category",
                                Accumulators.push("status", "$status")),
                        Aggregates.sort(Sorts.ascending("status"))
                ));

        //Creating necessary ArrayLists

        //string versions of all owner docs
        ArrayList<String> ownerList = new ArrayList<String>();
        ArrayList<String> categoryList = new ArrayList<String>();

        //strings containing names of owners and categories
        ArrayList<String> owners = new ArrayList<String>();
        ArrayList<String> categories = new ArrayList<String>();

        //percentages of completed todos for each owner and category
        ArrayList<Float> ownerPercent = new ArrayList<Float>();
        ArrayList<Float> categoryPercent = new ArrayList<Float>();


        for (Document element : ownerDocs) {
            ownerList.add(element.values().toString());
        }
        for (Document element : categoryDocs) {
            categoryList.add(element.values().toString());
        }



        //adding all owners and categories to an arraylist
        for (String element : ownerList) {
            owners.add(groupingOfDoc(element));
        }
        for (String element : categoryList) {
            categories.add(groupingOfDoc(element));
        }

        //calculating percents and adding them to arraylist
        for (String element : ownerList) {
            ownerPercent.add(instancesOfString(element, "true") * 100 /(instancesOfString(element,"false") + instancesOfString(element,"true")));
        }
        for (String element : categoryList) {
            categoryPercent.add(instancesOfString(element, "true") * 100 / (instancesOfString(element,"false") + instancesOfString(element,"true")));
        }

        String allDocsString = "";

        for (String element : ownerList) {
            allDocsString += element;
        }

        String toReturn = "{\n";

        toReturn += "\tpercentTodosComplete: ";
        toReturn += instancesOfString(allDocsString, "true") * 100 /(instancesOfString(allDocsString,"false") + instancesOfString(allDocsString,"true")) + ",";
        toReturn += "\n";

        toReturn += "\tcategoriesPercentComplete: {\n";
        for (int i = 0; i < categoryList.size(); i++) {
            toReturn += "\t\t";
            toReturn += categories.get(i);
            toReturn += ": ";
            toReturn += categoryPercent.get(i) + ",";
            toReturn += "\n";
        }
        toReturn += "\t}\n";
        toReturn += "\townersPercentComplete: {\n";
        for (int i = 0; i < ownerList.size(); i++) {
            toReturn += "\t\t";
            toReturn += owners.get(i);
            toReturn += ": ";
            toReturn += ownerPercent.get(i) + ",";
            toReturn += "\n";
        }
        toReturn += "\t}\n";
        toReturn += "}";
//


//        String thingy = "";
//        int thingCount = 0;
//        for (int i = 0; i < docs.size(); i++) {
//            thingCount += instancesOfString(docs.get(i), "true");
//        }


        //System.err.println(JSON.serialize(documents));
        return toReturn; //JSON.serialize(documents);

    }


    public Float instancesOfString(String body, String term) {
        Float count = Float.parseFloat("0");
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

    public String groupingOfDoc(String doc) {
        int startingIndex = 0;
        int endingIndex = 0;

        while (!((doc.charAt(startingIndex) >= 97 && (doc.charAt(startingIndex) <= 122))
                || ((doc.charAt(startingIndex)) >= 65 && (doc.charAt(startingIndex) <= 90)))) {
            startingIndex++;
            endingIndex++;
        }
        while (((doc.charAt(endingIndex) >= 97 && (doc.charAt(endingIndex) <= 122))
                || ((doc.charAt(endingIndex)) >= 65 && (doc.charAt(endingIndex) <= 90)) || doc.charAt(endingIndex) == ' ')) {
            endingIndex++;
        }

        return doc.substring(startingIndex,endingIndex);
    }
    

}
