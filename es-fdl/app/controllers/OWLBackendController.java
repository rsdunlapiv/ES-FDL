package controllers;

import com.fasterxml.jackson.databind.JsonNode;

import backend.OWLBackend;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class OWLBackendController extends Controller {

	private static OWLBackend backend = new OWLBackend();
	
	public static Result getSearchTree() {
		return ok(Json.toJson(backend.getSearchTree()));
	}
	
	public static Result query() {
		JsonNode json = request().body().asJson();
		//Logger.info("RESULT: " + Json.toJson(backend.query(json)));
		return ok(Json.toJson(backend.query(json)));
	}
	
	
}
