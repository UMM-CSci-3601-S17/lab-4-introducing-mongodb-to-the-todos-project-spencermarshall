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
public class TodoController {

    private final MongoCollection<Document> todoCollection;

    public TodoController() throws IOException {
        // Set up our server address
        // (Default host: 'localhost', default port: 27017)
        // ServerAddress testAddress = new ServerAddress();

        // Try connecting to the server
        //MongoClient mongoClient = new MongoClient(testAddress, credentials);
        MongoClient mongoClient = new MongoClient(); // Defaults!

        // Try connecting to a database
        MongoDatabase db = mongoClient.getDatabase("test");

        todoCollection = db.getCollection("todos");
    }

    public MongoCollection<Document> listTodos(Map<String, String[]> queryParameter) {
        MongoCollection<Document> filteredTodos = todoCollection;

        //limited list of todos
        if (queryParameter.containsKey("limit")) {
            int listLimit = Integer.parseInt(queryParameter.get("limit")[0]);
            filteredTodos = limitedList(filteredTodos, listLimit);
        }

        //status todos
        if(queryParameter.containsKey("status")) {
            Boolean progress = false;
            String stateOfTodo = queryParameter.get("status")[0];
            if(stateOfTodo.equals("complete")){
                progress = true;
            }
            filteredTodos = filterStatusTodos(filteredTodos, progress);
        }

        //Todos with specified word in body
        if(queryParameter.containsKey("contains")){
            String specifiedWord = queryParameter.get("contains")[0];
            filteredTodos = containedInBody(filteredTodos, specifiedWord);
        }

        //Todos specified by owner
        if(queryParameter.containsKey("owner")){
            String whoseTodo = queryParameter.get("owner")[0];
            filteredTodos = findTheirTodos(filteredTodos, whoseTodo);
        }

        //Todos specified by category
        if(queryParameter.containsKey("category")){
            String whatCategory = queryParameter.get("category")[0];
            filteredTodos = filterByCategory(filteredTodos, whatCategory);
        }

        return filteredTodos;
    }

    // Returns a Single one
    public Todo getTodo(String id){
        return Arrays.stream(todos).filter(x -> x._id.equals(id)).findFirst().orElse(null);
    }

    //Return specified number of todos
    public Todo[] limitedList(Todo[] filteredTodos, int listLimit){
        filteredTodos = new Todo[listLimit];

        for(int i = 0; i < listLimit; i++) {
            filteredTodos[i] = todos[i];
        }

        return filteredTodos;
    }

    //returns completed or incomleted todos
    public Todo[] filterStatusTodos(Todo[] statusTodos, boolean todoStatus){
        return Arrays.stream(statusTodos).filter(x -> x.status == todoStatus).toArray(Todo[]::new);
    }

    //returns todos with specified word
    public Todo[] containedInBody(Todo[] todosWithWord, String specifiedWord){
        return Arrays.stream(todosWithWord).filter(x -> x.body.toLowerCase().contains(specifiedWord.toLowerCase())).toArray(Todo[]::new);
    }

    //returns todos of specified owner
    //Made it so it will automatically remove whitespaces when placed in URL
    public Todo[] findTheirTodos(Todo[] ownerTodos, String whoseTodo){
        return Arrays.stream(ownerTodos).filter(x -> x.owner.replace(" ","").equalsIgnoreCase(whoseTodo.replace(" ",""))).toArray(Todo[]::new);
    }

    //returns todos by category
    public Todo[] filterByCategory(Todo[] categoryTodos, String whatCategory){
        return Arrays.stream(categoryTodos).filter(x -> x.category.equalsIgnoreCase(whatCategory)).toArray(Todo[]::new);
    }


}
