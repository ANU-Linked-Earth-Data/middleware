package anuled.dynamicstore;
import java.util.Optional;

import spark.*;

public class API {
	
    public static void main(String[] args) {
        Spark.get("/obs/:id", "application/json", (Request req, Response res) -> {
        	res.type("application/json");
        	return "{\"observation-id\": \"" + req.params("id") + "\"}";
        });
        
        Spark.get("/obs/:id", "text/html", (Request req,Response res) -> {
        	res.type("text/html");
        	return "observationId = " + req.params("id");
        });
        
        Spark.get("/obs/query/:query", (Request req, Response res) -> {
        	res.type("application/json");
        	return "{}";
        });

    }
    
    

}