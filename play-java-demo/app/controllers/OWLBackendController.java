package controllers;

import backend.OWLBackend;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class OWLBackendController extends Controller {

	private static OWLBackend backend = new OWLBackend();
	
	public static Result getSearchTree() {
		return ok(Json.toJson(backend.getSearchTree()));
	}
	
	
}
