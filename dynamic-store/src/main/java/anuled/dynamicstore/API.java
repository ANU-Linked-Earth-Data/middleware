package anuled.dynamicstore;

import spark.*;

public class API {

	public static void main(String[] args) {
		Spark.get("/obs/:id", "application/json",
				(Request req, Response res) -> {
					res.type("application/json");
					return "{\"observation-id\": \"" + req.params("id") + "\"}";
				});

		Spark.get("/obs/:id", "text/html", (Request req, Response res) -> {
			res.type("text/html");
			return "observationId = " + req.params("id");
		});

		// The :query and * syntax is not documented anywhere, but it seems that
		// a route like "/some/prefix/*" should send *everything* beginning wtih
		// /some/prefix/ to the handler (incl.
		// /some/prefix/and/suffix/with/trailing/slashes)
		Spark.get("/obs/query/:query", (Request req, Response res) -> {
			res.type("application/json");
			return "{}";
		});

	}

}