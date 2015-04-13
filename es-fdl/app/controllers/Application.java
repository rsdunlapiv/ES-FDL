package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import models.Modellinginfrastructure;
import models.PropertiesAndValues;
import models.CheckedValuesDescription;

import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import views.html.index;
import views.html.submit;
import views.html.front;
import views.html.browse;
import views.html.search;
import views.html.connect;
import views.html.about;
import views.html.details;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Application extends Controller {

	/*
	 * used in various places to determine if we are in a dev environment or
	 * production
	 */
	public final static boolean isDebug = true;

	final static Form<Modellinginfrastructure> modellingInfraForm = Form
			.form(Modellinginfrastructure.class);

	public static Result index() {
		return ok(index.render(modellingInfraForm));
	}

	public static Result front() {
		return ok(front.render(""));
	}

	public static Result browse() {
		return ok(browse.render(""));
	}

	public static Result search() {
		return ok(search.render(""));
	}

	public static Result connect() {
		return ok(connect.render(""));
	}

	public static Result about() {
		return ok(about.render(""));
	}

	public static Result details(String name) {
		return ok(details.render(name));
	}

	public static Result submit() {
		if (modellingInfraForm.hasErrors()) {
			return badRequest(index.render(modellingInfraForm));
		} else {
			Modellinginfrastructure given = modellingInfraForm
					.bindFromRequest().get();
			GetAxiomsFromOntology axiomretriever = new GetAxiomsFromOntology(
					given.name);
			PropertiesAndValues propvalsAndValues = new PropertiesAndValues();
			propvalsAndValues.objpropandval = axiomretriever.getObjpropandVal();
			propvalsAndValues.datapropandval = axiomretriever
					.getDatapropandVal();
			return ok(submit.render(propvalsAndValues, given.name));
		}
	}

	public static Result getNames(String id) {
		ObjectNode result = Json.newObject();
		result.put("working", "working");
		return ok(result);
	}

	public static Result getProperties(String name) {
		GetAxiomsFromOntology axiomretriever = new GetAxiomsFromOntology(name);
		PropertiesAndValues propandvals = new PropertiesAndValues();
		propandvals.datapropandval = axiomretriever.getDatapropandVal();
		propandvals.objpropandval = axiomretriever.getObjpropandVal();
		return ok(Json.toJson(propandvals));
	}

	public static Result getDummyData() {
		HashMap<String, ArrayList<CheckedValuesDescription>> checked_value_desc = new HashMap<String, ArrayList<CheckedValuesDescription>>();
		JsonNode json = request().body().asJson();
		Iterator<String> itr = json.fieldNames();
		while (itr.hasNext()) {
			String temp = itr.next();
			ArrayList<CheckedValuesDescription> dummy = new ArrayList<CheckedValuesDescription>();
			JsonNode vals = json.get(temp);
			Iterator<JsonNode> values = vals.elements();
			while (values.hasNext()) {
				JsonNode value = values.next();
				CheckedValuesDescription val = new CheckedValuesDescription();
				val.IRI = value.get("iri").toString();
				val.label = val.IRI.split("#")[val.IRI.split("#").length - 1];// need
				// to
				// call
				// function
				// which
				// gives
				// label
				// value
				val.label = val.label.substring(0, val.label.length() - 1);
				val.description = "dummy description"; // need to call function
														// which gives
				// description value
				dummy.add(val);
			}
			checked_value_desc.put(temp, dummy);
		}
		return ok(Json.toJson(checked_value_desc));
	}
}