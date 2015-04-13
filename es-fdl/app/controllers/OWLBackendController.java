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
		// Logger.debug("request json: " + json);
		return ok(Json.toJson(backend.query(json)));
	}

	public static Result getModelingInfrastructureBriefDetails(String name) {
		return ok(Json.toJson(backend
				.getModellingInfrastructureBriefDetails(name)));
	}

	public static Result getModelingInfrastructureDetails(String name) {
		return ok(Json.toJson(backend.getModellingInfraData(name)));
	}
}